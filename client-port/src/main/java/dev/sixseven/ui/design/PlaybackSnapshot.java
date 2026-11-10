package dev.sixseven.ui.design;

import java.util.Objects;

/** Immutable snapshot; sampledAtNanos must use the same monotonic clock as nowNanos. */
public record PlaybackSnapshot(String title, String artist, long positionMs, long durationMs,
                               boolean playing, long sampledAtNanos) {
    public PlaybackSnapshot {
        title = Objects.requireNonNullElse(title, "");
        artist = Objects.requireNonNullElse(artist, "");
        durationMs = Math.max(0L, durationMs);
        positionMs = Math.max(0L, positionMs);
        if (durationMs > 0L) positionMs = Math.min(positionMs, durationMs);
    }

    public long positionAt(long nowNanos) {
        if (durationMs <= 0L) return 0L;
        long elapsedMs = playing && nowNanos > sampledAtNanos
                ? (nowNanos - sampledAtNanos) / 1_000_000L : 0L;
        // Saturate before adding so a stale snapshot cannot overflow the long.
        return elapsedMs >= durationMs - positionMs ? durationMs : positionMs + elapsedMs;
    }

    public float progressAt(long nowNanos) {
        return durationMs <= 0L ? 0f : (float) positionAt(nowNanos) / durationMs;
    }
}