# KillStats

A Minecraft Fabric mod that tracks your entity kills during gameplay sessions, estimates drop values in Emeralds, and displays live statistics via a configurable in-game HUD overlay.

## Features

- Per-mob kill tracking with live HUD display
- Estimated drop values in Emeralds (with Looting support)
- Full and compact HUD display modes
- Configurable HUD position, opacity, and scale
- Session timer with start/pause/resume controls
- Auto-pause timer when opening menus
- Milestone notifications for total and per-mob kills
- Boss kill flash effects and sound alerts
- Session summary in chat on world exit or reset
- Optional persistent sessions across world exits
- Client-side only - no server required

## Compatibility

- **Minecraft**: 26.1+
- **Fabric Loader**: 0.18.4+
- **Fabric API**: Required
- **Java**: 25+
- **ModMenu**: Optional (for config screen)
- **YACL**: Optional (for config screen)

## Download

Download the latest release from [Modrinth](https://modrinth.com/mod/killstats) or [GitHub Releases](https://github.com/DennisTheGamer/killstats/releases).

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/)
2. Download [Fabric API](https://modrinth.com/mod/fabric-api)
3. Download KillStats (this mod)
4. Place both JAR files in your `mods` folder
5. Launch Minecraft

## Configuration

Open the config screen via ModMenu. Settings are organized in three tabs:

- **HUD Settings** - Position, always visible, opacity, scale
- **Session Settings** - Summary, timer pause in menu, persistent sessions
- **Milestones** - Sound alerts, milestone interval

Keybinds are listed under **Controls > KillStats**:
- `K` - Start/Pause/Resume timer
- `L` - Reset session
- `J` - Toggle compact mode

Config file is saved at `config/killstats.json5`.

## Building from Source

```bash
git clone https://github.com/DennisTheGamer/killstats.git
cd killstats
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Credits

- **Author**: DennisTheGamer
- **Built with**: Fabric, Fabric API, YACL

## Support

- Report bugs on [GitHub Issues](https://github.com/DennisTheGamer/killstats/issues)
- Visit the [Modrinth page](https://modrinth.com/mod/killstats) for more information
