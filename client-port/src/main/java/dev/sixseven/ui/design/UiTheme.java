package dev.sixseven.ui.design;

/** Renderer-independent defaults for the redesigned interface. Colors are ARGB. */
public record UiTheme(
        int background,
        int surface,
        int elevated,
        int border,
        int text,
        int muted,
        int accent,
        float windowRadius,
        float controlRadius,
        float hudRadius) {

    public static UiTheme cyberCyan() {
        return new UiTheme(0xFF0B0B0E, 0xFF111516, 0xFF171C1D, 0x24E5FFF6,
                0xFFEDF2F0, 0xFF85928D, 0xFF70EADB, 12f, 8f, 8f);
    }

    public UiTheme withAccent(int argb) {
        return new UiTheme(background, surface, elevated, border, text, muted,
                argb, windowRadius, controlRadius, hudRadius);
    }
}