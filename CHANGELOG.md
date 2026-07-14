# Changelog

All notable changes to KillStats will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2026-07-14

### Changed
- Unified the version number across all supported Minecraft versions so every build carries one release version
- Standardized release jar naming to `killstats-fabric-<version>+mc<range>` (e.g. `killstats-fabric-1.1.0+mc26.1-26.1.2.jar`)
- Corrected author and contact metadata (Modrinth and GitHub links)

## [1.0.0] - 2026-04-14

### Added
- Initial release
- Per-mob kill tracking with player attribution
- Kill detection for melee, projectile, and splash potion kills
- Estimated drop value calculation in Emeralds
- Looting enchantment bonus support (I, II, III)
- Drop value table covering 40+ mob types including bosses
- Full HUD display with per-mob breakdown and drop icons
- Compact HUD mode (single-line kill count)
- Configurable HUD position (top-left, top-right, bottom-left, bottom-right)
- Adjustable HUD opacity (0-100%) and scale (0.5x-2.0x)
- Weapon-based HUD visibility (show only when holding weapons)
- Always-visible HUD option
- Session timer with start/pause/resume via keybind
- Auto-pause timer when opening menus or inventory
- Session summary in chat on world exit or manual reset
- Optional persistent session storage across world exits
- Kill milestone notifications every X total kills (configurable)
- Per-mob milestone notifications every 50 kills
- Boss kill gold flash effect and sound alert
- Milestone sound effects (configurable)
- Full YACL config screen with tabbed layout:
  1. **HUD Settings**: Position, visibility, opacity, scale
  2. **Session Settings**: Summary, timer behavior, persistence
  3. **Milestones**: Sound, notification frequency
- ModMenu integration
- Dedicated "KillStats" keybind category in controls
- Keybind to start/pause timer (`K`)
- Keybind to reset session (`L`)
- Keybind to toggle compact mode (`J`)
- Localization support (English & German)
- Client-side only - no server installation required
- Compatibility with Minecraft 26.1+
- Fabric Loader 0.18.4+ support
- Fabric API integration
