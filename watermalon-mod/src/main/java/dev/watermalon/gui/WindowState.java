package dev.watermalon.gui;

/** Click GUI window placement in screen units; size is unscaled window units. */
public final class WindowState {
    private WindowState() {}

    public static float x = -1, y = -1;
    public static int w = 420, h = 260;
    public static float scale = 1f;

    public static void reset() {
        x = -1;
        y = -1;
        w = 420;
        h = 260;
        scale = 1f;
    }
}
