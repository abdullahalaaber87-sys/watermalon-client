package dev.watermalon.gui;

import dev.watermalon.util.Colors;

/** Shared look for the click GUI and HUD. Colors are ARGB. */
public final class Theme {
    private Theme() {}

    public record Accent(String name, int color) {}

    public static final Accent[] ACCENTS = {
            new Accent("Melon", 0xFFFF6B8A),
            new Accent("Cyber", 0xFF70EADB),
            new Accent("Acid", 0xFFC9F36B),
            new Accent("Ice", 0xFFB7D8E8),
    };

    public static final int TEXT = 0xFFEDF2F0;
    public static final int MUTED = 0xFF8A9792;
    public static final int DIM = 0xFF5E6B66;
    public static final int LINE = 0x14FFFFFF;
    public static final int BORDER = 0x26E5FFF6;
    public static final int KNOB_OFF = 0xFF93A19A;
    public static final int TRACK_OFF = 0xFF2A3230;

    public static int accentIndex = 0;
    public static int opacity = 96;
    public static int radius = 6;
    public static boolean blur = true;
    public static boolean interFont = true;

    public static int accent() {
        return ACCENTS[Math.floorMod(accentIndex, ACCENTS.length)].color();
    }

    public static int accent(float alpha) {
        return Colors.alpha(accent(), alpha);
    }

    public static int window() {
        return Colors.alpha(0x0B0B0E, opacity / 100f);
    }

    public static int panel() {
        return Colors.alpha(0x101415, Math.min(1f, opacity / 100f));
    }

    public static int raised() {
        return 0xFF171C1D;
    }

    public static int hudCard() {
        return 0xB8090C0D;
    }

    public static void reset() {
        accentIndex = 0;
        opacity = 96;
        radius = 6;
        blur = true;
        interFont = true;
    }
}
