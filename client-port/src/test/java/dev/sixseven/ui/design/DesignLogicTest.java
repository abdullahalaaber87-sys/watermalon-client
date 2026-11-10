package dev.sixseven.ui.design;

public final class DesignLogicTest {
    private static void close(float actual, float expected) {
        if (Math.abs(actual - expected) > .001f) {
            throw new AssertionError("Expected " + expected + ", got " + actual);
        }
    }

    public static void main(String[] args) {
        var viewport = new HudLayout.Size(1920f, 1080f);
        var size = SpotifyHudLayout.measure(true, true).size();
        var pos = new HudLayout.Position(.5f, 1f, 1f, true);
        var rect = HudLayout.place(pos, viewport, size);
        close(rect.x(), 841f);
        close(rect.y(), 1017f);
        var dragged = HudLayout.drag(pos, viewport, size, 4000f, -10f, 20f, 20f);
        close(dragged.fx(), 1f);
        close(dragged.fy(), 0f);
        var tiny = HudLayout.drag(pos, new HudLayout.Size(100f, 30f), size, 20f, 20f, 0f, 0f);
        close(tiny.fx(), 0f);
        close(tiny.fy(), 0f);

        var playback = new PlaybackSnapshot("Night Drive", "Sora", 84_000, 224_000, true, 1_000_000_000L);
        close(playback.progressAt(3_000_000_000L), 86f / 224f);
        var paused = new PlaybackSnapshot("", "", 84_000, 224_000, false, 1_000_000_000L);
        close(paused.progressAt(3_000_000_000L), 84f / 224f);
        close(new PlaybackSnapshot("", "", 5, 0, true, 0).progressAt(1_000_000L), 0f);
        close(new PlaybackSnapshot("", "", 223_000, 224_000, true, 0).progressAt(9_000_000_000L), 1f);
        if (SpotifyHudLayout.measure(false, false).art() != null) throw new AssertionError("Art must be hidden");
        if (SpotifyHudLayout.measure(false, false).progress() != null) throw new AssertionError("Progress must be hidden");
        close(UiMotion.easeOutCubic(1f), 1f);
        close(UiMotion.approach(0f, 1f, .016f, true), 1f);
        if (UiTheme.cyberCyan().accent() != 0xFF70EADB) throw new AssertionError("Unexpected accent");
        System.out.println("Design logic checks passed");
    }
}