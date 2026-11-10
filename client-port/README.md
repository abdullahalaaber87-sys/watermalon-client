# PrivateClient design utilities

These dependency-free Java 21 classes are a starting point for porting the preview into the original mapped Fabric source project. They are not a mod, do not contain the original client code, and do not register a renderer or contact Spotify.

- `UiTheme` provides the shared ARGB palette and radii.
- `HudLayout` resolves normalized positions using the scaled widget size and clamps drag coordinates.
- `SpotifyHudLayout` provides the compact widget geometry.
- `PlaybackSnapshot` interpolates the displayed progress using a monotonic clock.
- `UiMotion` provides a frame-rate-independent approach and a reduced-motion path.
- `assets/inter.ttf` is genuine Inter Variable, distributed under the included SIL Open Font License. Check the font-loading and weight behavior of the target renderer before shipping.

Integrate these utilities with the existing `WaterPlus`, `ThemeManager`, `HudManager`, `SpotifyHud`, and `SpotifyApi` code as described in `public/CLIENT_PORT.md`. Preserve existing module settings and migrate saved Spotify positions before removing the old render path.

To check the standalone logic with a Java 21 JDK:

```sh
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out dev.sixseven.ui.design.DesignLogicTest
```