package de.dennisthegamer.killstats.tracker;

import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Map;

/**
 * Predefined Emerald value table per mob type.
 * Values represent estimated drop worth based on typical loot tables.
 */
public class DropValueTable {

    private static final Map<EntityType<?>, double[]> MOB_VALUES = new HashMap<>();

    static {
        // Format: { baseValue, lootingBonusPerLevel }

        // Hostile
        MOB_VALUES.put(EntityType.ZOMBIE, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityType.SKELETON, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityType.CREEPER, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityType.SPIDER, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityType.WITCH, new double[]{1.5, 0.3});
        MOB_VALUES.put(EntityType.BLAZE, new double[]{1.0, 0.5});
        MOB_VALUES.put(EntityType.ENDERMAN, new double[]{0.2, 0.1});
        MOB_VALUES.put(EntityType.PHANTOM, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityType.DROWNED, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityType.HUSK, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityType.STRAY, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityType.CAVE_SPIDER, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityType.SILVERFISH, new double[]{0.0, 0.0});
        MOB_VALUES.put(EntityType.SLIME, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityType.MAGMA_CUBE, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityType.GUARDIAN, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityType.VINDICATOR, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityType.PILLAGER, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityType.RAVAGER, new double[]{1.0, 0.3});
        MOB_VALUES.put(EntityType.EVOKER, new double[]{2.0, 0.5});
        MOB_VALUES.put(EntityType.VEX, new double[]{0.0, 0.0});
        MOB_VALUES.put(EntityType.ZOMBIE_VILLAGER, new double[]{0.1, 0.05});

        // Passive
        MOB_VALUES.put(EntityType.COW, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityType.SHEEP, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityType.PIG, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityType.CHICKEN, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityType.RABBIT, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityType.SQUID, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityType.GLOW_SQUID, new double[]{0.2, 0.1});
        MOB_VALUES.put(EntityType.MOOSHROOM, new double[]{0.3, 0.1});

        // Nether
        MOB_VALUES.put(EntityType.PIGLIN, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityType.HOGLIN, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityType.GHAST, new double[]{0.8, 0.3});
        MOB_VALUES.put(EntityType.WITHER_SKELETON, new double[]{2.0, 1.0});
        MOB_VALUES.put(EntityType.PIGLIN_BRUTE, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityType.ZOMBIFIED_PIGLIN, new double[]{0.2, 0.1});

        // End
        MOB_VALUES.put(EntityType.SHULKER, new double[]{1.5, 0.5});
        MOB_VALUES.put(EntityType.ENDERMITE, new double[]{0.0, 0.0});

        // Bosses
        MOB_VALUES.put(EntityType.WITHER, new double[]{20.0, 0.0});
        MOB_VALUES.put(EntityType.ELDER_GUARDIAN, new double[]{5.0, 0.5});
        MOB_VALUES.put(EntityType.ENDER_DRAGON, new double[]{50.0, 0.0});
    }

    /**
     * Get the estimated Emerald value for a kill.
     *
     * @param entityType  the type of entity killed
     * @param lootingLevel the looting enchantment level (0-3)
     * @return estimated value in Emeralds
     */
    public static double getDropValue(EntityType<?> entityType, int lootingLevel) {
        double[] values = MOB_VALUES.get(entityType);
        if (values == null) {
            return 0.05; // minimal default for unknown mobs
        }

        double base = values[0];
        double lootingBonus = values[1] * lootingLevel;

        // Looting III cap: up to 30% increase on base
        double maxBonus = base * 0.3;
        if (lootingLevel == 3 && lootingBonus > maxBonus && maxBonus > 0) {
            lootingBonus = Math.max(lootingBonus, maxBonus);
        }

        return base + lootingBonus;
    }

    /**
     * Check if an entity type is a boss mob.
     */
    public static boolean isBoss(EntityType<?> entityType) {
        return entityType == EntityType.WITHER
                || entityType == EntityType.ENDER_DRAGON
                || entityType == EntityType.ELDER_GUARDIAN;
    }

}
