package dev.sixseven.ui.design;

public final class UiMotion {
    private UiMotion() {}

    public static float easeOutCubic(float progress) {
        float t = Math.max(0f, Math.min(1f, progress));
        return 1f - (1f - t) * (1f - t) * (1f - t);
    }

    public static float approach(float current, float target, float deltaSeconds, boolean reducedMotion) {
        if (reducedMotion) return target;
        float dt = Math.max(0f, Math.min(.1f, deltaSeconds));
        return current + (target - current) * (1f - (float) Math.exp(-12f * dt));
    }
}