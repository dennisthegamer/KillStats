package com.killstats.render;

import com.killstats.config.ModConfig;

public class HudLayout {

    private static final int PADDING = 6;
    private static final int MARGIN = 10;

    public static int getX(int screenWidth, int hudWidth) {
        ModConfig.HudPosition pos = ModConfig.get().hudPosition;
        return switch (pos) {
            case TOP_LEFT, BOTTOM_LEFT -> MARGIN;
            case TOP_RIGHT, BOTTOM_RIGHT -> screenWidth - hudWidth - MARGIN;
        };
    }

    public static int getY(int screenHeight, int hudHeight) {
        ModConfig.HudPosition pos = ModConfig.get().hudPosition;
        return switch (pos) {
            case TOP_LEFT, TOP_RIGHT -> MARGIN;
            case BOTTOM_LEFT, BOTTOM_RIGHT -> screenHeight - hudHeight - MARGIN;
        };
    }

    public static int getPadding() {
        return PADDING;
    }
}
