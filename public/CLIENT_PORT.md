# PrivateClient redesign: implementation and porting guide

This guide is based on static inspection of the supplied `PrivateClient-1.21.11.jar` (PrivateClient 2.0.0, Fabric, Java 21). The accompanying web application is a design prototype, not a patched mod. Its module set and Spotify tracks are examples; it does not connect to Minecraft or Spotify. The JAR was not executed and the original Gradle source project was not supplied.

The repository also contains tested, renderer-independent Java utilities in `client-port/`, including a genuine Inter TTF. [Download the Java utilities and font](/client-port.zip). They are integration building blocks, not a replacement mod JAR.

## What is already in the client

- `com.water.gui.ClickGuiScreen` owns category layout, searching, settings, mouse input, keybinds and animation in a single large screen. Keep its existing setting types and module behavior when replacing the presentation.
- `com.water.module.modules.client.WaterPlus` owns the interface accent, background, blur, roundness and related settings. Its default accent is `#A855F7`, and its tick logic currently forces the Bliss Bloom font. Although a file is named `assets/water/font/inter.ttf`, its font metadata identifies it as Xuong Bold, not Inter. Replace it with a genuine Inter TTF from the official Inter distribution and include the font license.
- `dev.sixseven.theme.ThemeManager` exposes `interfaceTheme()`, which resolves colors through `WaterPlus`; HUD components already use it. Avoid creating another independent color source.
- `dev.sixseven.hud.HudManager` measures and positions `HudComponent`s using normalized `fx`, `fy`, scale and visibility, and serializes these values. `WatermarkHud` and `ArrayListHud` already use this system.
- `com.water.module.modules.client.SpotifyHud` is separately registered in `ModuleManager`, with its own normalized `fx`/`fy`, scale, visibility and rendering. It polls playback on a worker every four seconds and optionally fetches the queue every twelve seconds. Artwork loading is asynchronous and is applied on the client thread. `com.water.spotify.SpotifyAuth` already implements authorization code with PKCE.
- There is also a `dev.sixseven.spotify` Windows media-session bridge in the archive. Establish which source is active before consolidating it; do not start a second player service merely to draw the new HUD.

Names in a release JAR can be intermediary or obfuscated. Apply these changes to the original mapped source project, not by copying the decompiler output into a new build.

## Shared design tokens

Use one immutable palette or a `UiTheme` object referenced by both the click GUI and HUD. Colors below are ARGB:

```java
public final class UiTokens {
    public static final int BACKGROUND = 0xFF0B0B0E;
    public static final int SURFACE = 0xFF111516;
    public static final int ELEVATED = 0xFF171C1D;
    public static final int BORDER = 0x24E5FFF6;
    public static final int TEXT = 0xFFEDF2F0;
    public static final int MUTED = 0xFF85928D;
    public static final int ACCENT_CYAN = 0xFF70EADB;
    public static final int ACCENT_ACID = 0xFFC9F36B;
    public static final float WINDOW_RADIUS = 12f;
    public static final float CONTROL_RADIUS = 8f;
    public static final float HUD_RADIUS = 8f;
    private UiTokens() {}
}
```

Set the `WaterPlus` defaults to the cyan accent and `#0B0B0E` background, and have `ThemeManager.interfaceTheme()` resolve those settings. Preserve custom colors and existing config values; migrate the old default only when there is no saved user choice. Avoid a second hard-coded accent in `ClickGuiScreen`.

Replace the mislabeled `assets/water/font/inter.ttf` with the genuine Inter TTF in `client-port/assets/`, make that face the default in `WaterFontRenderer`, and remove the unconditional font reset from `WaterPlus.onTick()`. The prototype uses Inter Variable WOFF2 for the browser; the porting bundle includes a TTF and OFL license. Verify font weight behavior in the target Java renderer. Persist a user's font selection instead. Use the same font and matching measurement function for drawing, ellipsis and hit testing. Suggested sizes in GUI coordinates: section label 9, metadata 10, module title 13, page title 30; use consistent scaling for the actual Minecraft GUI scale.

## Click GUI structure

1. Keep `ClickGuiScreen` as the lifecycle and input owner. Extract a `ClientLayout` that computes the window, sidebar, content and preview rectangles once per frame. Convert mouse coordinates into that same scaled coordinate system before hit testing.
2. Add `CategorySidebar`, `ModuleListView`, `ModuleRow`, and `SettingEditorFactory`. Each consumes the existing `ModuleManager` and `Setting` objects; the preview's regrouping is only a presentation choice, not a replacement for the client's actual registry.
3. Make module rows the interactive unit: 40 px icon, 13 px title, one muted description, favorite action, category and 36 x 20 px toggle. Use 69 px row height, subtle separators, 8 px control radii and a 2 px cyan selection rule. The switch calls the existing module toggle, never a separate UI-only boolean.
4. Search by name, description and category, then apply enabled/favorite filters. Clip the list with scissoring, clamp scrolling after filtering, and derive both rendering and hit boxes from the same measured row layout. Keep existing expanded setting editors, bind capture, drag batching, and config save behavior.
5. Use a 160-220 ms ease-out for window entry and row expansion, a 220 ms switch transition, and an optional stagger for visible rows. Provide an animation-off option and do not animate the layout on every render tick.
6. Keep tooltip text near its hovered control, clamp it within the viewport and use the shared text, surface and border tokens. Retain keyboard navigation, Escape and the configured GUI key.

## Spotify HUD

Create a `SpotifyHudComponent` under the existing HUD component system and make it consume an immutable playback snapshot. The existing Spotify module/service should remain responsible for authorization, polling and commands. Register the new component once; remove the old standalone draw call only after it is wired in, to avoid drawing twice. Migrate the old Spotify `fx`/`fy` and scale into the component's saved entry if that entry does not already exist.

Suggested default geometry: 238 x 63 px at scale 1, 8 px outer padding, 46 x 46 px cover with a 5 px radius, a 9 px gap, 10 px title, 8 px artist, 2 px progress track and 8 px outer radius. Use one dark translucent fill and a restrained 1 px border; blur is optional. Truncate long text based on the actual available width. Keep controls available through an interactive screen, and preserve the existing `hideWhenIdle` and restricted-device behavior.

The progress bar should be interpolated from a sampled playback snapshot, not driven by HTTP calls in the render loop. Use a monotonic timestamp for a new snapshot; do not mix it with the existing wall-clock `sampledAt` field without converting the sampling code too:

```java
static float progress(long positionMs, long durationMs, boolean playing,
                      long sampleNanos, long nowNanos) {
    if (durationMs <= 0) return 0f;
    long elapsed = playing ? Math.max(0L, (nowNanos - sampleNanos) / 1_000_000L) : 0L;
    return Math.clamp((float) (positionMs + elapsed) / durationMs, 0f, 1f);
}
```

Keep network and image decoding off the render thread. Retain the artwork HTTPS host validation and size cap, reject stale artwork after a track change, create/release the GPU texture on the client thread, and release it on disable. Treat HTTP 204 as idle, refresh once on 401, and back off on 429 using `Retry-After`. Do not ship a Spotify client secret; use the existing PKCE flow and request only the scopes needed by enabled features. Playback controls need `user-modify-playback-state`; playback read requires the appropriate read scope. The preview's play and seek controls are simulated.

## HUD placement and polish

For every HUD component, use the measured *scaled* size in the available-space calculation. The existing `HudManager.layout()` follows this normalized model:

```java
float x = fx * Math.max(0f, screenWidth - scaledWidth);
float y = fy * Math.max(0f, screenHeight - scaledHeight);
float newFx = Math.clamp((mouseX - grabOffsetX) / Math.max(1f, screenWidth - scaledWidth), 0f, 1f);
float newFy = Math.clamp((mouseY - grabOffsetY) / Math.max(1f, screenHeight - scaledHeight), 0f, 1f);
```

Save `fx`, `fy`, `scale`, and `shown` on drag release or via a debounced config write. Use a small padded watermark with the same Inter font, a low-contrast surface, and no heavy glow. In `ArrayListHud`, keep its existing width sorting and slide animation, but use the same surface, 8 px horizontal padding, white primary text and a thin accent edge instead of bright gradients. Ensure all items remain in bounds after resolution or GUI-scale changes.

For Fabric HUD registration, verify the exact mappings and Fabric API version used by the source project. On current 1.21.x Fabric APIs, `HudElementRegistry` is the supported registration mechanism; do not add a second callback if the existing client already renders the component in its HUD pipeline.

## Verification before releasing a JAR

- Build using the original Fabric Loom project and its matching Minecraft 1.21.11 mappings; test with the existing saved config as well as a fresh profile.
- Test GUI scales and small windows, scroll clipping, search, setting changes, keybind capture, screen closing, drag bounds and config reload.
- Test Spotify connected, idle, paused, restricted, disconnected, token expiry, rate limiting, missing artwork and track changes; confirm render-thread frame time remains stable.
- Verify only one Spotify HUD is drawn, old positions migrate correctly, and GPU textures and worker threads are released on disable/shutdown.
- Check contrast, focus visibility, tooltips, reduced-motion settings and every HUD component against bright and dark game scenes.

References: [Fabric HUD API](https://maven.fabricmc.net/docs/fabric-api-0.128.2+1.21.6/net/fabricmc/fabric/api/client/rendering/v1/hud/HudElement.html), [Spotify scopes](https://developer.spotify.com/documentation/web-api/concepts/scopes), and [Spotify player API](https://developer.spotify.com/documentation/web-api/reference/get-information-about-the-users-current-playback).