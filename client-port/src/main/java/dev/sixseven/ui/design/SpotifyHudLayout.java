package dev.sixseven.ui.design;

/** Geometry only: draw with the client's existing canvas and texture renderer. */
public final class SpotifyHudLayout {
    public static final float WIDTH = 238f;
    public static final float HEIGHT = 63f;
    public static final float PADDING = 8f;
    public static final float ART_SIZE = 46f;
    public static final float GAP = 9f;

    private SpotifyHudLayout() {}

    public record Parts(HudLayout.Size size, HudLayout.Rect art, HudLayout.Rect title,
                        HudLayout.Rect artist, HudLayout.Rect progress, HudLayout.Rect glyph) {}

    public static Parts measure(boolean showArt, boolean showProgress) {
        float textX = PADDING + (showArt ? ART_SIZE + GAP : 0f);
        float textWidth = WIDTH - PADDING - textX;
        return new Parts(
                new HudLayout.Size(WIDTH, HEIGHT),
                showArt ? new HudLayout.Rect(PADDING, PADDING, ART_SIZE, ART_SIZE) : null,
                new HudLayout.Rect(textX, 23f, textWidth, 12f),
                new HudLayout.Rect(textX, 37f, textWidth, 10f),
                showProgress ? new HudLayout.Rect(textX, 52f, textWidth, 2f) : null,
                new HudLayout.Rect(WIDTH - PADDING - 13f, PADDING, 13f, 13f));
    }
}