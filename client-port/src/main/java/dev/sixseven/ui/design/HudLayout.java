package dev.sixseven.ui.design;

/** Normalized HUD placement that remains stable across resolutions and scales. */
public final class HudLayout {
    private HudLayout() {}

    public record Size(float width, float height) {
        public Size {
            if (!Float.isFinite(width) || !Float.isFinite(height) || width < 0f || height < 0f) {
                throw new IllegalArgumentException("Dimensions must be finite and non-negative");
            }
        }
    }

    public record Rect(float x, float y, float width, float height) {}

    public record Position(float fx, float fy, float scale, boolean shown) {
        public Position {
            fx = clamp01(fx);
            fy = clamp01(fy);
            if (!Float.isFinite(scale) || scale <= 0f) {
                throw new IllegalArgumentException("Scale must be finite and positive");
            }
        }
    }

    public static Rect place(Position position, Size viewport, Size naturalSize) {
        float width = naturalSize.width() * position.scale();
        float height = naturalSize.height() * position.scale();
        float availableX = Math.max(0f, viewport.width() - width);
        float availableY = Math.max(0f, viewport.height() - height);
        return new Rect(position.fx() * availableX, position.fy() * availableY, width, height);
    }

    public static Position drag(Position position, Size viewport, Size naturalSize,
                                float pointerX, float pointerY, float grabX, float grabY) {
        Rect measured = place(position, viewport, naturalSize);
        float availableX = Math.max(0f, viewport.width() - measured.width());
        float availableY = Math.max(0f, viewport.height() - measured.height());
        float fx = availableX == 0f ? 0f : (pointerX - grabX) / availableX;
        float fy = availableY == 0f ? 0f : (pointerY - grabY) / availableY;
        return new Position(fx, fy, position.scale(), position.shown());
    }

    private static float clamp01(float value) {
        return Float.isFinite(value) ? Math.max(0f, Math.min(1f, value)) : 0f;
    }
}