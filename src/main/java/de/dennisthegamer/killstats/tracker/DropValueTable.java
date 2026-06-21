package de.dennisthegamer.killstats.tracker;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;

import java.util.HashMap;
import java.util.Map;

public class DropValueTable {

    private static final Map<EntityType<?>, double[]> MOB_VALUES = new HashMap<>();

    static {
        // Format: { baseValue, lootingBonusPerLevel }

        // Hostile
        MOB_VALUES.put(EntityTypes.ZOMBIE, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityTypes.SKELETON, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityTypes.CREEPER, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityTypes.SPIDER, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityTypes.WITCH, new double[]{1.5, 0.3});
        MOB_VALUES.put(EntityTypes.BLAZE, new double[]{1.0, 0.5});
        MOB_VALUES.put(EntityTypes.ENDERMAN, new double[]{0.2, 0.1});
        MOB_VALUES.put(EntityTypes.PHANTOM, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityTypes.DROWNED, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityTypes.HUSK, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityTypes.STRAY, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityTypes.CAVE_SPIDER, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityTypes.SILVERFISH, new double[]{0.0, 0.0});
        MOB_VALUES.put(EntityTypes.SLIME, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityTypes.MAGMA_CUBE, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityTypes.GUARDIAN, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityTypes.VINDICATOR, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityTypes.PILLAGER, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityTypes.RAVAGER, new double[]{1.0, 0.3});
        MOB_VALUES.put(EntityTypes.EVOKER, new double[]{2.0, 0.5});
        MOB_VALUES.put(EntityTypes.VEX, new double[]{0.0, 0.0});
        MOB_VALUES.put(EntityTypes.ZOMBIE_VILLAGER, new double[]{0.1, 0.05});

        // Passive
        MOB_VALUES.put(EntityTypes.COW, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityTypes.SHEEP, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityTypes.PIG, new double[]{0.2, 0.05});
        MOB_VALUES.put(EntityTypes.CHICKEN, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityTypes.RABBIT, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityTypes.SQUID, new double[]{0.1, 0.05});
        MOB_VALUES.put(EntityTypes.GLOW_SQUID, new double[]{0.2, 0.1});
        MOB_VALUES.put(EntityTypes.MOOSHROOM, new double[]{0.3, 0.1});

        // Nether
        MOB_VALUES.put(EntityTypes.PIGLIN, new double[]{0.3, 0.1});
        MOB_VALUES.put(EntityTypes.HOGLIN, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityTypes.GHAST, new double[]{0.8, 0.3});
        MOB_VALUES.put(EntityTypes.WITHER_SKELETON, new double[]{2.0, 1.0});
        MOB_VALUES.put(EntityTypes.PIGLIN_BRUTE, new double[]{0.5, 0.2});
        MOB_VALUES.put(EntityTypes.ZOMBIFIED_PIGLIN, new double[]{0.2, 0.1});

        // End
        MOB_VALUES.put(EntityTypes.SHULKER, new double[]{1.5, 0.5});
        MOB_VALUES.put(EntityTypes.ENDERMITE, new double[]{0.0, 0.0});

        // Bosses
        MOB_VALUES.put(EntityTypes.WITHER, new double[]{20.0, 0.0});
        MOB_VALUES.put(EntityTypes.ELDER_GUARDIAN, new double[]{5.0, 0.5});
        MOB_VALUES.put(EntityTypes.ENDER_DRAGON, new double[]{50.0, 0.0});
    }

    public static double getDropValue(EntityType<?> entityType, int lootingLevel) {
        double[] values = MOB_VALUES.get(entityType);
        if (values == null) {
            return 0.05;
        }

        double base = values[0];
        double lootingBonus = values[1] * lootingLevel;

        double maxBonus = base * 0.3;
        if (lootingLevel == 3 && lootingBonus > maxBonus && maxBonus > 0) {
            lootingBonus = Math.max(lootingBonus, maxBonus);
        }

        return base + lootingBonus;
    }

    public static boolean isBoss(EntityType<?> entityType) {
        return entityType == EntityTypes.WITHER
                || entityType == EntityTypes.ENDER_DRAGON
                || entityType == EntityTypes.ELDER_GUARDIAN;
    }

}
