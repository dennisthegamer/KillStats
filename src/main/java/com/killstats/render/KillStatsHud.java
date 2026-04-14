package com.killstats.render;

import com.killstats.config.ModConfig;
import com.killstats.tracker.KillSession;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Map;

public class KillStatsHud {

    private static boolean compactMode = false;

    public static void toggleCompactMode() {
        compactMode = !compactMode;
    }

    @SuppressWarnings("unused")
    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.options.hideGui) return;

        ModConfig config = ModConfig.get();
        if (!config.enabled) return;

        // Check visibility: only show when holding a weapon (unless always visible)
        if (!config.hudVisibleAlways && !isHoldingWeapon(client)) return;

        float scale = config.hudScale;
        if (scale != 1.0f) {
            graphics.pose().pushMatrix();
            graphics.pose().scale(scale, scale);
        }

        if (compactMode) {
            renderCompact(graphics, client, scale);
        } else {
            renderFull(graphics, client, scale);
        }

        if (scale != 1.0f) {
            graphics.pose().popMatrix();
        }
    }

    private static boolean isHoldingWeapon(Minecraft client) {
        if (client.player == null) return false;
        ItemStack mainHand = client.player.getMainHandItem();
        ItemStack offHand = client.player.getOffhandItem();
        return isWeapon(mainHand) || isWeapon(offHand);
    }

    private static boolean isWeapon(ItemStack stack) {
        if (stack.isEmpty()) return false;
        return stack.is(holder -> holder.is(ItemTags.SWORDS))
                || stack.is(holder -> holder.is(ItemTags.AXES))
                || stack.getItem() == Items.BOW
                || stack.getItem() == Items.CROSSBOW
                || stack.getItem() == Items.TRIDENT;
    }

    private static void renderCompact(GuiGraphicsExtractor graphics, Minecraft client, float scale) {
        Font font = client.font;
        KillSession session = KillSession.getInstance();
        ModConfig config = ModConfig.get();

        String text = I18n.get("killstats.hud.compact", session.getTotalKills());
        int textWidth = font.width(text);
        int padding = HudLayout.getPadding();
        int hudWidth = textWidth + padding * 2;
        int hudHeight = font.lineHeight + padding * 2;

        int scaledScreenWidth = (int) (client.getWindow().getGuiScaledWidth() / scale);
        int scaledScreenHeight = (int) (client.getWindow().getGuiScaledHeight() / scale);
        int x = HudLayout.getX(scaledScreenWidth, hudWidth);
        int y = HudLayout.getY(scaledScreenHeight, hudHeight);

        // Background
        int bgColor = ((int) (config.hudOpacity * 255) << 24);
        graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        // Flash effect
        if (HudEffects.isFlashing()) {
            int flashColor = (((int) (HudEffects.getFlashAlpha() * 100)) << 24) | 0xFFD700;
            graphics.fill(x, y, x + hudWidth, y + hudHeight, flashColor);
        }

        // Text
        graphics.text(font, text, x + padding, y + padding, 0xFFFFFFFF, true);
    }

    private static void renderFull(GuiGraphicsExtractor graphics, Minecraft client, float scale) {
        Font font = client.font;
        KillSession session = KillSession.getInstance();
        ModConfig config = ModConfig.get();

        int padding = HudLayout.getPadding();
        int lineHeight = Math.max(font.lineHeight, 16) + 2;

        Map<String, KillSession.MobKillData> killData = session.getKillData();

        // Count active mob lines
        int mobLines = 0;
        for (KillSession.MobKillData data : killData.values()) {
            if (data.totalKills > 0) mobLines++;
        }

        // Title + mob lines + separator + session time
        int contentLines = 1 + mobLines; // title + mobs
        contentLines++; // session time line

        int hudWidth = 220;
        int hudHeight = padding * 2 + contentLines * lineHeight + (mobLines > 0 ? 4 : 0); // 4px separator

        int scaledScreenWidth = (int) (client.getWindow().getGuiScaledWidth() / scale);
        int scaledScreenHeight = (int) (client.getWindow().getGuiScaledHeight() / scale);
        int x = HudLayout.getX(scaledScreenWidth, hudWidth);
        int y = HudLayout.getY(scaledScreenHeight, hudHeight);

        // Background
        int bgColor = ((int) (config.hudOpacity * 255) << 24);
        graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        // Flash effect (gold border)
        if (HudEffects.isFlashing()) {
            int flashAlpha = (int) (HudEffects.getFlashAlpha() * 100);
            graphics.fill(x - 2, y - 2, x + hudWidth + 2, y + hudHeight + 2, (flashAlpha << 24) | 0xFFD700);
            graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);
        }

        int currentY = y + padding;

        // Reset message overlay
        if (HudEffects.isShowingResetMessage()) {
            String resetText = I18n.get("killstats.hud.reset");
            int resetX = x + (hudWidth - font.width(resetText)) / 2;
            int resetY = y + (hudHeight - font.lineHeight) / 2;
            graphics.text(font, resetText, resetX, resetY, 0xFFFFFF00, true);
            return;
        }

        // Title: "Kill Stats" in bold white (centered)
        String title = I18n.get("killstats.hud.title");
        int titleX = x + (hudWidth - font.width(title)) / 2;
        graphics.text(font, title, titleX, currentY, 0xFFFFFFFF, true);
        currentY += lineHeight;

        // Per-mob lines
        int rightEdge = x + hudWidth - padding;
        for (KillSession.MobKillData data : killData.values()) {
            if (data.totalKills <= 0) continue;

            // Draw mob drop icon
            graphics.item(getDropIcon(data.entityType), x + padding, currentY - 4);

            // Mob name (left-aligned after icon)
            int textOffsetX = 16 + 4; // icon width + gap
            graphics.text(font, data.displayName, x + padding + textOffsetX, currentY, 0xFFFFFFFF, true);

            // Kill count (right-aligned)
            String countText = String.valueOf(data.totalKills);
            int countWidth = font.width(countText);
            graphics.text(font, countText, rightEdge - countWidth, currentY, 0xFFFFFFFF, true);

            currentY += lineHeight;
        }

        // Separator line
        if (mobLines > 0) {
            graphics.fill(x + padding, currentY, x + hudWidth - padding, currentY + 1, 0x80FFFFFF);
            currentY += 4;
        }

        // Session time in gray
        String timeText = I18n.get("killstats.hud.session_time", session.getFormattedDuration());
        graphics.text(font, timeText, x + padding, currentY, 0xFFAAAAAA, true);
    }

    /**
     * Get an item icon representing the mob's primary drop.
     */
    private static ItemStack getDropIcon(EntityType<?> entityType) {
        if (entityType == EntityType.ZOMBIE) return new ItemStack(Items.ROTTEN_FLESH);
        if (entityType == EntityType.SKELETON) return new ItemStack(Items.BONE);
        if (entityType == EntityType.CREEPER) return new ItemStack(Items.GUNPOWDER);
        if (entityType == EntityType.SPIDER) return new ItemStack(Items.STRING);
        if (entityType == EntityType.WITCH) return new ItemStack(Items.GLASS_BOTTLE);
        if (entityType == EntityType.BLAZE) return new ItemStack(Items.BLAZE_ROD);
        if (entityType == EntityType.ENDERMAN) return new ItemStack(Items.ENDER_PEARL);
        if (entityType == EntityType.PHANTOM) return new ItemStack(Items.PHANTOM_MEMBRANE);
        if (entityType == EntityType.COW) return new ItemStack(Items.LEATHER);
        if (entityType == EntityType.SHEEP) return new ItemStack(Items.WHITE_WOOL);
        if (entityType == EntityType.PIG) return new ItemStack(Items.PORKCHOP);
        if (entityType == EntityType.CHICKEN) return new ItemStack(Items.FEATHER);
        if (entityType == EntityType.GHAST) return new ItemStack(Items.GHAST_TEAR);
        if (entityType == EntityType.WITHER_SKELETON) return new ItemStack(Items.WITHER_SKELETON_SKULL);
        if (entityType == EntityType.HOGLIN) return new ItemStack(Items.PORKCHOP);
        if (entityType == EntityType.PIGLIN) return new ItemStack(Items.GOLD_INGOT);
        if (entityType == EntityType.SHULKER) return new ItemStack(Items.SHULKER_SHELL);
        if (entityType == EntityType.GUARDIAN) return new ItemStack(Items.PRISMARINE_SHARD);
        if (entityType == EntityType.ELDER_GUARDIAN) return new ItemStack(Items.SPONGE);
        if (entityType == EntityType.WITHER) return new ItemStack(Items.NETHER_STAR);
        if (entityType == EntityType.ENDER_DRAGON) return new ItemStack(Items.DRAGON_HEAD);
        if (entityType == EntityType.EVOKER) return new ItemStack(Items.TOTEM_OF_UNDYING);
        if (entityType == EntityType.SLIME) return new ItemStack(Items.SLIME_BALL);
        if (entityType == EntityType.MAGMA_CUBE) return new ItemStack(Items.MAGMA_CREAM);
        if (entityType == EntityType.DROWNED) return new ItemStack(Items.ROTTEN_FLESH);
        if (entityType == EntityType.RABBIT) return new ItemStack(Items.RABBIT_HIDE);
        if (entityType == EntityType.SQUID) return new ItemStack(Items.INK_SAC);
        if (entityType == EntityType.GLOW_SQUID) return new ItemStack(Items.GLOW_INK_SAC);
        if (entityType == EntityType.VINDICATOR) return new ItemStack(Items.EMERALD);
        if (entityType == EntityType.PILLAGER) return new ItemStack(Items.CROSSBOW);
        // Default: iron sword icon
        return new ItemStack(Items.IRON_SWORD);
    }
}
