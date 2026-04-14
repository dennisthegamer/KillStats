package com.killstats;

import com.killstats.config.ModConfig;
import com.killstats.event.KillEventHandler;
import com.killstats.render.HudEffects;
import com.killstats.render.KillStatsHud;
import com.killstats.tracker.KillSession;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class KillStatsClient implements ClientModInitializer {

    public static final String MOD_ID = "killstats";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private boolean wasInWorld = false;
    private boolean wasPaused = false;
    private boolean hintShown = false;
    private boolean sessionRestored = false;

    private static final KeyMapping.Category CATEGORY =
            new KeyMapping.Category(Identifier.fromNamespaceAndPath("killstats", "killstats"));

    private static KeyMapping compactKey;
    private static KeyMapping resetKey;
    private static KeyMapping toggleTimerKey;

    @Override
    public void onInitializeClient() {
        LOGGER.info("KillStats loaded!");

        // Load config
        ModConfig.HANDLER.load();

        // Register keybinds
        compactKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.killstats.compact",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));

        resetKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.killstats.reset",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_L,
                CATEGORY
        ));

        toggleTimerKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.killstats.toggle_timer",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_K,
                CATEGORY
        ));

        // Register HUD renderer
        HudElementRegistry.attachElementAfter(
                VanillaHudElements.BOSS_BAR,
                Identifier.fromNamespaceAndPath("killstats", "hud"),
                KillStatsHud::render
        );

        // Register tick handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Keybind handling
            handleKeybinds(client);

            // HUD effects tick (flash, reset message)
            HudEffects.tick();

            // Session lifecycle: detect world join/leave
            boolean inWorld = client.level != null && client.player != null;

            if (inWorld && !wasInWorld) {
                // Just joined a world - reset session (timer stays paused)
                KillSession session = KillSession.getInstance();
                session.reset();
                hintShown = false;
                wasPaused = false;

                // Try to restore saved session
                sessionRestored = ModConfig.get().persistSessions && session.loadFromDisk();
                LOGGER.info("World joined -- session ready (paused)");
            } else if (inWorld && !hintShown) {
                // Show hint with keybind to start/continue timer
                String keyName = toggleTimerKey.getTranslatedKeyMessage().getString();
                if (sessionRestored) {
                    client.player.sendSystemMessage(
                            Component.translatable("killstats.session.restored", keyName)
                                    .withStyle(style -> style.withColor(0x55FF55))
                    );
                } else {
                    client.player.sendSystemMessage(
                            Component.translatable("killstats.hint.start", keyName)
                                    .withStyle(style -> style.withColor(0x55FF55))
                    );
                }
                hintShown = true;
            } else if (!inWorld && wasInWorld) {
                // Just left a world - pause timer, save/send summary
                KillSession session = KillSession.getInstance();
                session.pause();
                if (session.getTotalKills() > 0) {
                    if (client.player != null) {
                        sendSessionSummary(client);
                    }
                    if (ModConfig.get().persistSessions) {
                        session.saveToDisk();
                    }
                }
                session.reset();
                KillEventHandler.invalidateCache();
                LOGGER.info("Session ended");
            }

            // Pause/resume timer when a screen is open (Escape menu, inventory, etc.)
            if (inWorld && ModConfig.get().pauseTimerInMenu) {
                KillSession session = KillSession.getInstance();
                boolean screenOpen = client.screen != null;
                if (screenOpen && !wasPaused && session.isRunning()) {
                    session.pause();
                    wasPaused = true;
                } else if (!screenOpen && wasPaused) {
                    session.resume();
                    wasPaused = false;
                }
            }

            wasInWorld = inWorld;
        });
    }

    private void handleKeybinds(Minecraft client) {
        if (client.player == null) return;

        while (compactKey.consumeClick()) {
            KillStatsHud.toggleCompactMode();
        }

        while (resetKey.consumeClick()) {
            KillSession session = KillSession.getInstance();
            if (session.getTotalKills() > 0) {
                sendSessionSummary(client);
            }
            session.reset();
            session.deleteSavedSession();
            HudEffects.triggerResetMessage();
        }

        while (toggleTimerKey.consumeClick()) {
            KillSession session = KillSession.getInstance();
            if (!session.isStarted()) {
                // First press: start the timer
                session.start();
                client.player.sendSystemMessage(
                        Component.translatable("killstats.timer.started")
                                .withStyle(style -> style.withColor(0x55FF55))
                );
            } else if (session.isRunning()) {
                // Running -> pause
                session.pause();
                wasPaused = false; // reset menu-pause tracking
                client.player.sendSystemMessage(
                        Component.translatable("killstats.timer.paused")
                                .withStyle(style -> style.withColor(0xFFAA00))
                );
            } else {
                // Paused -> resume
                session.resume();
                client.player.sendSystemMessage(
                        Component.translatable("killstats.timer.resumed")
                                .withStyle(style -> style.withColor(0x55FF55))
                );
            }
        }
    }

    private void sendSessionSummary(Minecraft client) {
        if (client.player == null) return;

        ModConfig config = ModConfig.get();
        if (!config.showSessionSummary) return;

        KillSession session = KillSession.getInstance();

        // Header
        client.player.sendSystemMessage(Component.literal(""));
        client.player.sendSystemMessage(
                Component.translatable("killstats.session.summary_header")
                        .withStyle(style -> style.withColor(0xFFD700).withBold(true))
        );

        // Duration
        client.player.sendSystemMessage(
                Component.translatable("killstats.session.duration", session.getFormattedDuration())
                        .withStyle(style -> style.withColor(0xFFFFFF))
        );

        // Total kills
        client.player.sendSystemMessage(
                Component.translatable("killstats.session.kills_total", session.getTotalKills())
                        .withStyle(style -> style.withColor(0xFFFFFF))
        );

        // Per-mob breakdown
        Map<String, KillSession.MobKillData> killData = session.getKillData();
        StringBuilder breakdown = new StringBuilder("  ");
        boolean first = true;
        for (KillSession.MobKillData data : killData.values()) {
            if (data.totalKills <= 0) continue;
            if (!first) breakdown.append(" | ");
            breakdown.append(data.displayName).append(": ").append(data.totalKills);
            first = false;
        }
        if (!first) {
            client.player.sendSystemMessage(
                    Component.literal(breakdown.toString())
                            .withStyle(style -> style.withColor(0xAAAAAA))
            );
        }

    }
}
