package de.dennisthegamer.killstats.tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.dennisthegamer.killstats.KillStatsClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Stores all session data: kill counts per mob type, looting level kills, drop values.
 * Purely in-memory, not persistent.
 */
public class KillSession {

    private static final KillSession INSTANCE = new KillSession();

    // EntityType key string -> MobKillData
    private final Map<String, MobKillData> killData = new LinkedHashMap<>();
    private long sessionStartTime;
    private long accumulatedMillis = 0;
    private boolean paused = true;
    private boolean started = false;
    private double totalDropValue = 0.0;
    private int totalKills = 0;

    // Combat tracking: entity ID -> first hit time (for average combat duration)
    private final Map<Integer, Long> combatStartTimes = new LinkedHashMap<>();

    private KillSession() {
        reset();
    }

    public static KillSession getInstance() {
        return INSTANCE;
    }

    public void reset() {
        killData.clear();
        combatStartTimes.clear();
        sessionStartTime = System.currentTimeMillis();
        accumulatedMillis = 0;
        paused = true;
        started = false;
        totalDropValue = 0.0;
        totalKills = 0;
    }

    public void start() {
        if (!started) {
            sessionStartTime = System.currentTimeMillis();
            accumulatedMillis = 0;
            paused = false;
            started = true;
        }
    }

    public void pause() {
        if (started && !paused) {
            accumulatedMillis += System.currentTimeMillis() - sessionStartTime;
            paused = true;
        }
    }

    public void resume() {
        if (started && paused) {
            sessionStartTime = System.currentTimeMillis();
            paused = false;
        }
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isRunning() {
        return started && !paused;
    }

    /**
     * Record a kill.
     *
     * @param entityType   the entity type killed
     * @param displayName  human-readable name
     * @param lootingLevel looting enchantment level (0-3)
     * @param dropValue    estimated drop value in emeralds
     * @param entityId     the runtime entity ID for combat duration tracking
     */
    public void recordKill(EntityType<?> entityType, String displayName, int lootingLevel, double dropValue, int entityId) {
        String key = EntityType.getKey(entityType).toString();
        MobKillData data = killData.computeIfAbsent(key, _ -> new MobKillData(entityType, displayName));

        data.totalKills++;
        totalKills++;

        // Track looting kills
        if (lootingLevel >= 1 && lootingLevel <= 3) {
            data.lootingKills[lootingLevel - 1]++;
        }

        // Calculate combat duration
        Long combatStart = combatStartTimes.remove(entityId);
        if (combatStart != null) {
            double durationSeconds = (System.currentTimeMillis() - combatStart) / 1000.0;
            data.totalCombatDuration += durationSeconds;
            data.combatEncounters++;
        }

        // Add drop value
        data.totalDropValue += dropValue;
        totalDropValue += dropValue;
    }

    /**
     * Record the first hit on an entity (for combat duration tracking).
     */
    public void recordFirstHit(int entityId) {
        combatStartTimes.putIfAbsent(entityId, System.currentTimeMillis());
    }

    public int getTotalKills() {
        return totalKills;
    }

    /**
     * Get all mob kill data entries (only mobs with kills > 0).
     */
    public Map<String, MobKillData> getKillData() {
        return killData;
    }

    /**
     * Get kill count for a specific entity type.
     */
    public int getKillCount(EntityType<?> entityType) {
        String key = EntityType.getKey(entityType).toString();
        MobKillData data = killData.get(key);
        return data != null ? data.totalKills : 0;
    }

    public long getSessionDurationMillis() {
        if (paused) {
            return accumulatedMillis;
        }
        return accumulatedMillis + (System.currentTimeMillis() - sessionStartTime);
    }

    public String getFormattedDuration() {
        long totalSeconds = getSessionDurationMillis() / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // ── Persistence ──────────────────────────────────────────────

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static File getSaveFile() {
        return FabricLoader.getInstance().getConfigDir().resolve("killstats_session.json").toFile();
    }

    public void saveToDisk() {
        try {
            Map<String, Object> root = new HashMap<>();

            // Save per-mob kill data
            Map<String, Map<String, Object>> mobData = new LinkedHashMap<>();
            for (Map.Entry<String, MobKillData> entry : killData.entrySet()) {
                MobKillData data = entry.getValue();
                if (data.totalKills <= 0) continue;
                Map<String, Object> mob = new HashMap<>();
                mob.put("displayName", data.displayName);
                mob.put("totalKills", data.totalKills);
                mob.put("lootingKills", new int[]{data.lootingKills[0], data.lootingKills[1], data.lootingKills[2]});
                mob.put("totalCombatDuration", data.totalCombatDuration);
                mob.put("combatEncounters", data.combatEncounters);
                mob.put("totalDropValue", data.totalDropValue);
                mobData.put(entry.getKey(), mob);
            }
            root.put("killData", mobData);
            root.put("durationMillis", getSessionDurationMillis());
            root.put("totalDropValue", totalDropValue);
            root.put("totalKills", totalKills);

            File file = getSaveFile();
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists() && !parentDir.mkdirs()) {
                KillStatsClient.LOGGER.warn("Failed to create config directory: {}", parentDir);
            }
            try (Writer writer = new FileWriter(file)) {
                GSON.toJson(root, writer);
            }
            KillStatsClient.LOGGER.info("Session saved to disk");
        } catch (IOException e) {
            KillStatsClient.LOGGER.error("Failed to save session", e);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean loadFromDisk() {
        File file = getSaveFile();
        if (!file.exists()) return false;

        try (Reader reader = new FileReader(file)) {
            Type type = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> root = GSON.fromJson(reader, type);
            if (root == null) return false;

            // Restore duration (session starts paused)
            Number savedDuration = (Number) root.get("durationMillis");
            if (savedDuration != null) {
                accumulatedMillis = savedDuration.longValue();
            }
            paused = true;
            started = true;

            // Restore per-mob data
            Map<String, Map<String, Object>> mobData = (Map<String, Map<String, Object>>) root.get("killData");
            if (mobData != null) {
                for (Map.Entry<String, Map<String, Object>> entry : mobData.entrySet()) {
                    String entityKey = entry.getKey();
                    Map<String, Object> mob = entry.getValue();

                    // Resolve EntityType from key
                    EntityType<?> entityType;
                    try {
                        entityType = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entityKey));
                    } catch (Exception e) {
                        continue; // skip unknown entity types
                    }

                    String displayName = (String) mob.getOrDefault("displayName", entityKey);
                    MobKillData data = new MobKillData(entityType, displayName);
                    data.totalKills = ((Number) mob.getOrDefault("totalKills", 0)).intValue();

                    // Restore looting kills
                    Object lootingObj = mob.get("lootingKills");
                    if (lootingObj instanceof java.util.List<?> list) {
                        for (int i = 0; i < Math.min(3, list.size()); i++) {
                            data.lootingKills[i] = ((Number) list.get(i)).intValue();
                        }
                    }

                    data.totalCombatDuration = ((Number) mob.getOrDefault("totalCombatDuration", 0.0)).doubleValue();
                    data.combatEncounters = ((Number) mob.getOrDefault("combatEncounters", 0)).intValue();
                    data.totalDropValue = ((Number) mob.getOrDefault("totalDropValue", 0.0)).doubleValue();

                    killData.put(entityKey, data);
                }
            }

            // Restore totals
            Number savedTotalKills = (Number) root.get("totalKills");
            if (savedTotalKills != null) {
                totalKills = savedTotalKills.intValue();
            }
            Number savedTotalDrop = (Number) root.get("totalDropValue");
            if (savedTotalDrop != null) {
                totalDropValue = savedTotalDrop.doubleValue();
            }

            KillStatsClient.LOGGER.info("Session restored from disk ({} kills)", totalKills);
            return totalKills > 0;
        } catch (Exception e) {
            KillStatsClient.LOGGER.error("Failed to load session", e);
            return false;
        }
    }

    public void deleteSavedSession() {
        File file = getSaveFile();
        if (file.exists() && !file.delete()) {
            KillStatsClient.LOGGER.warn("Failed to delete session file: {}", file);
        }
    }

    /**
     * Data class for per-mob-type kill statistics.
     */
    public static class MobKillData {
        public final EntityType<?> entityType;
        public final String displayName;
        public int totalKills = 0;
        public int[] lootingKills = new int[3]; // index 0=Looting I, 1=II, 2=III
        public double totalCombatDuration = 0.0;
        public int combatEncounters = 0;
        public double totalDropValue = 0.0;

        public MobKillData(EntityType<?> entityType, String displayName) {
            this.entityType = entityType;
            this.displayName = displayName;
        }
    }
}
