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

    public static void onEntityDeath(LivingEntity entity, DamageSource damageSource) {
        ModConfig config = ModConfig.get();
        if (!config.enabled) return;

        Minecraft client = Minecraft.getInstance();
        LocalPlayer player = client.player;
        if (player == null) return;

        if (entity.getUUID().equals(player.getUUID())) return;

        if (!isPlayerKill(player, damageSource)) return;

        EntityType<?> entityType = entity.getType();
        String displayName = entity.getName().getString();
        java.util.UUID entityUuid = entity.getUUID();

        client.execute(() -> {
            LocalPlayer clientPlayer = client.player;
            if (clientPlayer == null) return;

            ensureEnchantmentCache();
            ItemStack mainHand = clientPlayer.getMainHandItem();
            int lootingLevel = 0;
            if (cachedLooting != null) {
                lootingLevel = EnchantmentHelper.getItemEnchantmentLevel(cachedLooting, mainHand);
            }

            double dropValue = DropValueTable.getDropValue(entityType, lootingLevel);

            KillSession session = KillSession.getInstance();
            session.recordKill(entityType, displayName, lootingLevel, dropValue, entityUuid);

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

            checkMilestones(session, entityType, displayName);

            KillStatsClient.LOGGER.debug("Killed {} (total: {})", displayName, session.getTotalKills());
        });
    }

    private static boolean isPlayerKill(LocalPlayer player, DamageSource damageSource) {
        Entity directEntity = damageSource.getDirectEntity();
        Entity causingEntity = damageSource.getEntity();
        java.util.UUID playerUUID = player.getUUID();

        if (directEntity != null && directEntity.getUUID().equals(playerUUID)) {
            return true;
        }

        if (directEntity instanceof AbstractArrow arrow) {
            Entity owner = arrow.getOwner();
            return owner != null && owner.getUUID().equals(playerUUID);
        }

        if (directEntity instanceof AbstractThrownPotion potion) {
            Entity owner = potion.getOwner();
            return owner != null && owner.getUUID().equals(playerUUID);
        }

        return causingEntity != null && causingEntity.getUUID().equals(playerUUID) && directEntity != null;
    }

    private static void checkMilestones(KillSession session, EntityType<?> entityType, String displayName) {
        ModConfig config = ModConfig.get();
        int interval = config.milestoneInterval;
        if (interval <= 0) return;

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

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

        int mobKills = session.getKillCount(entityType);
        if (mobKills > 0 && mobKills % 50 == 0) {
            client.player.sendSystemMessage(
                    Component.translatable("killstats.milestone", mobKills, displayName)
                            .withStyle(style -> style.withColor(0xFFD700))
            );
        }
    }
}
