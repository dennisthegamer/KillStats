package de.dennisthegamer.killstats.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.EnumControllerBuilder;
import dev.isxander.yacl3.api.controller.FloatSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.IntegerSliderControllerBuilder;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModConfigScreen {

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.create(ModConfig.HANDLER, (defaults, config, builder) ->
                builder
                        .title(Component.translatable("config.killstats.title"))

                        // Tab 1: HUD Settings
                        .category(ConfigCategory.createBuilder()
                                .name(Component.translatable("config.killstats.category.hud"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.killstats.enabled"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.enabled.tooltip")))
                                        .binding(defaults.enabled, () -> config.enabled, v -> config.enabled = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<ModConfig.HudPosition>createBuilder()
                                        .name(Component.translatable("config.killstats.hud_position"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.hud_position.tooltip")))
                                        .binding(defaults.hudPosition, () -> config.hudPosition, v -> config.hudPosition = v)
                                        .controller(opt -> EnumControllerBuilder.create(opt)
                                                .enumClass(ModConfig.HudPosition.class))
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.killstats.hud_visible_always"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.hud_visible_always.tooltip")))
                                        .binding(defaults.hudVisibleAlways, () -> config.hudVisibleAlways, v -> config.hudVisibleAlways = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.killstats.hud_opacity"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.hud_opacity.tooltip")))
                                        .binding(defaults.hudOpacity, () -> config.hudOpacity, v -> config.hudOpacity = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.0f, 1.0f).step(0.05f))
                                        .build())
                                .option(Option.<Float>createBuilder()
                                        .name(Component.translatable("config.killstats.hud_scale"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.hud_scale.tooltip")))
                                        .binding(defaults.hudScale, () -> config.hudScale, v -> config.hudScale = v)
                                        .controller(opt -> FloatSliderControllerBuilder.create(opt)
                                                .range(0.5f, 2.0f).step(0.1f))
                                        .build())
                                .build())

                        // Tab 2: Session Settings
                        .category(ConfigCategory.createBuilder()
                                .name(Component.translatable("config.killstats.category.session"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.killstats.show_drop_value"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.show_drop_value.tooltip")))
                                        .binding(defaults.showDropValue, () -> config.showDropValue, v -> config.showDropValue = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.killstats.show_session_summary"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.show_session_summary.tooltip")))
                                        .binding(defaults.showSessionSummary, () -> config.showSessionSummary, v -> config.showSessionSummary = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.killstats.pause_timer_in_menu"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.pause_timer_in_menu.tooltip")))
                                        .binding(defaults.pauseTimerInMenu, () -> config.pauseTimerInMenu, v -> config.pauseTimerInMenu = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.killstats.persist_sessions"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.persist_sessions.tooltip")))
                                        .binding(defaults.persistSessions, () -> config.persistSessions, v -> config.persistSessions = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .build())

                        // Tab 3: Milestones
                        .category(ConfigCategory.createBuilder()
                                .name(Component.translatable("config.killstats.category.milestones"))
                                .option(Option.<Boolean>createBuilder()
                                        .name(Component.translatable("config.killstats.play_milestone_sound"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.play_milestone_sound.tooltip")))
                                        .binding(defaults.playMilestoneSound, () -> config.playMilestoneSound, v -> config.playMilestoneSound = v)
                                        .controller(TickBoxControllerBuilder::create)
                                        .build())
                                .option(Option.<Integer>createBuilder()
                                        .name(Component.translatable("config.killstats.milestone_interval"))
                                        .description(_ -> OptionDescription.of(
                                                Component.translatable("config.killstats.milestone_interval.tooltip")))
                                        .binding(defaults.milestoneInterval, () -> config.milestoneInterval, v -> config.milestoneInterval = v)
                                        .controller(opt -> IntegerSliderControllerBuilder.create(opt)
                                                .range(0, 1000).step(10))
                                        .build())
                                .build())
        ).generateScreen(parent);
    }
}
