package com.killstats.config;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.SerialEntry;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.Identifier;

public class ModConfig {

    public static final ConfigClassHandler<ModConfig> HANDLER =
            ConfigClassHandler.createBuilder(ModConfig.class)
                    .id(Identifier.fromNamespaceAndPath("killstats", "config"))
                    .serializer(config -> GsonConfigSerializerBuilder.create(config)
                            .setPath(FabricLoader.getInstance().getConfigDir()
                                    .resolve("killstats.json5"))
                            .setJson5(true)
                            .build())
                    .build();

    // General
    @SerialEntry public boolean enabled = true;

    // HUD Settings
    @SerialEntry public HudPosition hudPosition = HudPosition.BOTTOM_RIGHT;
    @SerialEntry public boolean hudVisibleAlways = false;
    @SerialEntry public float hudOpacity = 0.6f;
    @SerialEntry public float hudScale = 1.0f;

    // Session Settings
    @SerialEntry public boolean showDropValue = true;
    @SerialEntry public boolean showSessionSummary = true;
    @SerialEntry public boolean pauseTimerInMenu = true;
    @SerialEntry public boolean persistSessions = false;

    // Milestone Settings
    @SerialEntry public boolean playMilestoneSound = true;
    @SerialEntry public int milestoneInterval = 100;

    public static ModConfig get() {
        return HANDLER.instance();
    }

    public enum HudPosition {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }
}
