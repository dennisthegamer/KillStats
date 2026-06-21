package de.dennisthegamer.killstats.render;

import de.dennisthegamer.killstats.config.ModConfig;
import de.dennisthegamer.killstats.tracker.KillSession;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class KillStatsHud {

    private static boolean compactMode = false;
    private static final Map<EntityType<?>, LivingEntity> entityCache = new HashMap<>();

    public static void toggleCompactMode() {
        compactMode = !compactMode;
    }

    @SuppressWarnings("unused")
    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui.hud.isHidden()) return;

        ModConfig config = ModConfig.get();
        if (!config.enabled) return;

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

        int bgColor = ((int) (config.hudOpacity * 255) << 24);
        graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        if (HudEffects.isFlashing()) {
            int flashColor = (((int) (HudEffects.getFlashAlpha() * 100)) << 24) | 0xFFD700;
            graphics.fill(x, y, x + hudWidth, y + hudHeight, flashColor);
        }

        graphics.text(font, text, x + padding, y + padding, 0xFFFFFFFF, true);
    }

    private static void renderFull(GuiGraphicsExtractor graphics, Minecraft client, float scale) {
        Font font = client.font;
        KillSession session = KillSession.getInstance();
        ModConfig config = ModConfig.get();

        int padding = HudLayout.getPadding();
        int lineHeight = Math.max(font.lineHeight, 16) + 2;

        Map<String, KillSession.MobKillData> killData = session.getKillData();

        int mobLines = 0;
        for (KillSession.MobKillData data : killData.values()) {
            if (data.totalKills > 0) mobLines++;
        }

        int contentLines = 1 + mobLines;
        contentLines++;

        int hudWidth = 220;
        int hudHeight = padding * 2 + contentLines * lineHeight + (mobLines > 0 ? 4 : 0);

        int scaledScreenWidth = (int) (client.getWindow().getGuiScaledWidth() / scale);
        int scaledScreenHeight = (int) (client.getWindow().getGuiScaledHeight() / scale);
        int x = HudLayout.getX(scaledScreenWidth, hudWidth);
        int y = HudLayout.getY(scaledScreenHeight, hudHeight);

        int bgColor = ((int) (config.hudOpacity * 255) << 24);
        graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);

        if (HudEffects.isFlashing()) {
            int flashAlpha = (int) (HudEffects.getFlashAlpha() * 100);
            graphics.fill(x - 2, y - 2, x + hudWidth + 2, y + hudHeight + 2, (flashAlpha << 24) | 0xFFD700);
            graphics.fill(x, y, x + hudWidth, y + hudHeight, bgColor);
        }

        int currentY = y + padding;

        if (HudEffects.isShowingResetMessage()) {
            String resetText = I18n.get("killstats.hud.reset");
            int resetX = x + (hudWidth - font.width(resetText)) / 2;
            int resetY = y + (hudHeight - font.lineHeight) / 2;
            graphics.text(font, resetText, resetX, resetY, 0xFFFFFF00, true);
            return;
        }

        String title = I18n.get("killstats.hud.title");
        int titleX = x + (hudWidth - font.width(title)) / 2;
        graphics.text(font, title, titleX, currentY, 0xFFFFFFFF, true);
        currentY += lineHeight;

        int rightEdge = x + hudWidth - padding;
        for (KillSession.MobKillData data : killData.values()) {
            if (data.totalKills <= 0) continue;

            renderMobHead(graphics, data.entityType, client, x + padding, currentY - 4);

            int textOffsetX = 16 + 4;
            graphics.text(font, data.displayName, x + padding + textOffsetX, currentY, 0xFFFFFFFF, true);

            String countText = String.valueOf(data.totalKills);
            int countWidth = font.width(countText);
            graphics.text(font, countText, rightEdge - countWidth, currentY, 0xFFFFFFFF, true);

            currentY += lineHeight;
        }

        if (mobLines > 0) {
            graphics.fill(x + padding, currentY, x + hudWidth - padding, currentY + 1, 0x80FFFFFF);
            currentY += 4;
        }

        String timeText = I18n.get("killstats.hud.session_time", session.getFormattedDuration());
        graphics.text(font, timeText, x + padding, currentY, 0xFFAAAAAA, true);
    }

    private static LivingEntity getOrCreateEntity(EntityType<?> type, Minecraft client) {
        if (client.level == null) return null;
        LivingEntity cached = entityCache.get(type);
        if (cached != null) return cached;

        Entity entity = type.create(client.level, EntitySpawnReason.LOAD);
        if (entity instanceof LivingEntity living) {
            living.setId(Integer.MAX_VALUE - entityCache.size());
            entityCache.put(type, living);
            return living;
        }
        return null;
    }

    private static void renderMobHead(GuiGraphicsExtractor graphics, EntityType<?> entityType, Minecraft client, int x, int y) {
        LivingEntity entity = getOrCreateEntity(entityType, client);
        if (entity != null) {
            entity.setYRot(0);
            entity.yRotO = 0;
            entity.setXRot(0);
            entity.xRotO = 0;
            entity.yHeadRot = 0;
            entity.yHeadRotO = 0;
            entity.yBodyRot = 0;
            entity.yBodyRotO = 0;

            float entityHeight = entity.getBbHeight();
            int scale = (int) (8 / entityHeight);
            if (scale < 2) scale = 2;
            if (scale > 8) scale = 8;

            float yOffset = entityHeight * 0.35f;
            InventoryScreen.extractEntityInInventoryFollowsMouse(
                    graphics,
                    x, y, x + 16, y + 16,
                    scale, yOffset,
                    0, 0,
                    entity
            );
        } else {
            graphics.item(new ItemStack(Items.IRON_SWORD), x, y);
        }
    }

    public static void clearEntityCache() {
        entityCache.clear();
    }
}
