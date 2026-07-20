package de.dennisthegamer.killstats.event;

import de.dennisthegamer.killstats.KillStatsClient;
import de.dennisthegamer.killstats.config.ModConfig;
import de.dennisthegamer.killstats.render.HudEffects;
import de.dennisthegamer.killstats.tracker.DropValueTable;
import de.dennisthegamer.killstats.tracker.KillSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.throwableitemprojectile.AbstractThrownPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;

/**
 * Handles entity death events, determines if the local player caused the kill,
 * and records it in the session.
 */
public class KillEventHandler {

    private static Holder<Enchantment> cachedLooting;

    private static void ensureEnchantmentCache() {
        if (cachedLooting == null && Minecraft.getInstance().level != null) {
            var registry = Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
            cachedLooting = registry.getOrThrow(Enchantments.LOOTING);
        }
    }

    public static void invalidateCache() {
        cachedLooting = null;
    }

    /**
     * Called when a LivingEntity dies on the server thread.
     * Extracts kill info, then schedules processing on the client thread.
     */
    public static void onEntityDeath(LivingEntity entity, DamageSource damageSource) {
        ModConfig config = ModConfig.get();
        if (!config.enabled) return;

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        // Don't count player deaths (compare by UUID since threads differ)
        if (entity.getUUID().equals(player.getUUID())) return;

        // Determine if the player caused this kill
        if (!isPlayerKill(player, damageSource)) return;

        // Extract data on server thread (entity may be gone later)
        EntityType<?> entityType = entity.getType();
        String displayName = entity.getName().getString();
        int entityId = entity.getId();

        // Schedule the rest on the client thread
        client.execute(() -> {
            LocalPlayer clientPlayer = client.player;
            if (clientPlayer == null) return;

            // Get looting level from held weapon
            ensureEnchantmentCache();
            ItemStack mainHand = clientPlayer.getMainHandItem();
            int lootingLevel = 0;
            if (cachedLooting != null) {
                lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(cachedLooting, mainHand);
            }

            // Calculate drop value
            double dropValue = DropValueTable.getDropValue(entityType, lootingLevel);

            // Record the kill
            KillSession session = KillSession.getInstance();
            session.recordKill(entityType, displayName, lootingLevel, dropValue, entityId);

            // Boss kill effects
            if (DropValueTable.isBoss(entityType)) {
                HudEffects.triggerFlash();
                if (config.playMilestoneSound) {
                    clientPlayer.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.5f);
                }
                clientPlayer.sendSystemMessage(
                        Component.translatable("killstats.boss_kill", displayName)
                                .withStyle(style -> style.withColor(0xFFD700).withBold(true))
                );
            }

            // Milestone check
            checkMilestones(session, entityType, displayName);

            KillStatsClient.LOGGER.debug("Killed {} (total: {})", displayName, session.getTotalKills());
        });
    }

    /**
     * Check if the local player caused the kill.
     * Covers: direct melee, projectiles (arrow, trident), splash potions.
     * Excludes: TNT, lava, fall damage, environmental damage.
     * Uses UUID comparison because die() runs on the server thread where entities
     * are ServerPlayer instances, not the LocalPlayer reference.
     */
    private static boolean isPlayerKill(LocalPlayer player, DamageSource damageSource) {
        Entity directEntity = damageSource.getDirectEntity();
        Entity causingEntity = damageSource.getEntity();
        java.util.UUID playerUUID = player.getUUID();

        // Direct melee kill: the player is the direct attacker
        if (directEntity != null && directEntity.getUUID().equals(playerUUID)) {
            return true;
        }

        // Projectile kill: arrow, trident, etc. where the player is the owner
        // (ThrownTrident extends AbstractArrow, so this covers both)
        if (directEntity instanceof AbstractArrow arrow) {
            Entity owner = arrow.getOwner();
            return owner != null && owner.getUUID().equals(playerUUID);
        }

        // Splash potion kill: player is the thrower
        if (directEntity instanceof AbstractThrownPotion potion) {
            Entity owner = potion.getOwner();
            return owner != null && owner.getUUID().equals(playerUUID);
        }

        // Causing entity is the player (covers some edge cases)
        return causingEntity != null && causingEntity.getUUID().equals(playerUUID) && directEntity != null;
    }

    private static void checkMilestones(KillSession session, EntityType<?> entityType, String displayName) {
        ModConfig config = ModConfig.get();
        int interval = config.milestoneInterval;
        if (interval <= 0) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // Total kills milestone
        int totalKills = session.getTotalKills();
        if (totalKills > 0 && totalKills % interval == 0) {
            client.player.sendSystemMessage(
                    Component.translatable("killstats.milestone.total", totalKills)
                            .withStyle(style -> style.withColor(0xFFD700))
            );
            if (config.playMilestoneSound) {
                client.player.playSound(SoundEvents.NOTE_BLOCK_PLING.value(), 1.0f, 1.0f);
            }
            HudEffects.triggerFlash();
        }

        // Per-mob milestone (every 50 of same type)
        int mobKills = session.getKillCount(entityType);
        if (mobKills > 0 && mobKills % 50 == 0) {
            client.player.sendSystemMessage(
                    Component.translatable("killstats.milestone", mobKills, displayName)
                            .withStyle(style -> style.withColor(0xFFD700))
            );
        }
    }
}
