<p align="center"><img src="docs/logo.png" width="160" alt="Hotbar Profiles logo"></p>

# Hotbar Profiles

A client-side Fabric mod for **Minecraft 26.3** that lets you save and switch between multiple hotbar layouts (Building, Mining, Combat...).

## Features
- Save your current hotbar as a named profile (up to 8)
- Load a profile: items are pulled from your inventory into the saved slots
- Works in multiplayer (uses normal inventory swap clicks)
- Cycle profiles with a keybind, no menu needed
- Profiles are stored in `config/hotbarprofiles.json`

## Keys (rebindable in Controls)
| Key | Action |
|-----|--------|
| H   | Open the profile menu |
| ]   | Next profile |
| [   | Previous profile |

## Notes
- Items are matched by type only (enchantments and names are not compared).
- Empty slots in a profile are left alone.
- If an item is missing from your inventory, that slot is skipped.

## Install
1. Install Fabric Loader for Minecraft 26.3
2. Put **Fabric API (26.3)** and `hotbarprofiles-1.0.0.jar` in your `mods` folder

## Build
Needs **JDK 25**.
```bash
gradle wrapper --gradle-version 9.8.2   # once, if you have Gradle installed
./gradlew build
```
The jar is created in `build/libs/` (use the one without `-sources`).

You can also let GitHub build it: every push runs the workflow in `.github/workflows/build.yml`
and uploads the jar as a downloadable artifact (Actions tab).

## License
MIT
