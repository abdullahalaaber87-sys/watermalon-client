# Watermalon

A Fabric client mod for **Minecraft 1.21.11**: a clean click GUI, a draggable HUD, and quality-of-life modules.

## Get the jar with GitHub (no setup on your PC)

1. Create a new repository on github.com. Public or private both work.
2. Upload the contents of this folder (`build.gradle`, `src/`, `.github/`, and the rest) to the repository root.
   - On the repository page, click **Add file > Upload files** and drag everything in.
   - `.github` is a hidden folder. If your file browser hides it, show hidden files first, or create `.github/workflows/build.yml` on GitHub and paste in the contents of that file.
3. Open the **Actions** tab. The "Build Watermalon" workflow starts automatically. If it doesn't, select it and click **Run workflow**.
4. When it shows a green check (about 3–5 minutes), open the run and download **watermalon-jar** under **Artifacts**. Unzip it to get `watermalon-1.0.0.jar`.

If you pushed the whole preview project instead (with this folder inside `watermalon-mod/`), the root workflow `.github/workflows/build-mod.yml` builds it the same way.

## Install

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.11.
2. Put `watermalon-1.0.0.jar` and [Fabric API](https://modrinth.com/mod/fabric-api) for 1.21.11 in your `.minecraft/mods` folder.
3. Launch the Fabric profile and press **Right Shift** in game.

## Controls

| Action | How |
| --- | --- |
| Open or close the menu | Right Shift (rebind under Controls > Watermalon) |
| Move the menu | Drag the header |
| Resize the menu | Drag the bottom-right corner |
| Menu size | `-` / `+` in the header, or Theme > GUI size |
| Search modules | Click the search box, or just start typing |
| Module settings and keybind | Click a module row |
| Edit the HUD | HUD Editor tab: drag elements, scroll to scale, arrows to nudge |

Settings save to `.minecraft/config/watermalon.json`.

## What's included

- **Click GUI:** categories, search, toggles, sliders, modes, text fields, keybinds. Plus a Theme tab for accent color, GUI size, opacity, corner radius, background blur, and Inter or the Minecraft font.
- **HUD:** watermark, info bar (FPS, ping, XYZ, facing), arraylist, keystrokes with CPS, and a Spotify card. Every element can be moved, scaled, and hidden.
- **Modules:** FullBright, CustomCrosshair, Zoom (hold C), AutoWalk (G), WeatherNotifier, CoordSnapper, HUD, and SpotifyHUD.

This is an original codebase. It doesn't contain code from the PrivateClient jar, and it doesn't include combat automation, fake payment messages, or gambling tools.

## Spotify (optional)

1. Go to <https://developer.spotify.com/dashboard> and create an app.
2. Add this exact redirect URI: `http://127.0.0.1:43821/callback`
3. Copy the app's **Client ID**.
4. In game: Right Shift > SpotifyHUD > click the Client ID box > Ctrl+V > **Connect**, then approve in your browser.

Watermalon uses Authorization Code with PKCE, so it never needs a client secret. Only the refresh token is stored in the config file. Play, pause, and skip buttons require Spotify Premium.

## Build locally (optional)

With Java 21 installed: `./gradlew build` (Windows: `gradlew.bat build`). The jar appears in `build/libs/`.
