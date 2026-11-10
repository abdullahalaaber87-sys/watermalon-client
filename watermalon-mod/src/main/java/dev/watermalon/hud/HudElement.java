package dev.watermalon.hud;

import net.minecraft.client.gui.GuiGraphics;

/** A HUD widget placed by normalized position (fx, fy) within the free screen space. */
public abstract class HudElement {
    private final String id;
    private final String label;
    private final float defaultX, defaultY;
    public float fx, fy;
    public float scale = 1f;
    public boolean shown = true;

    protected HudElement(String id, String label, float fx, float fy) {
        this.id = id;
        this.label = label;
        this.defaultX = fx;
        this.defaultY = fy;
        this.fx = fx;
        this.fy = fy;
    }

    public String id() { return id; }
    public String label() { return label; }

    public void reset() {
        fx = defaultX;
        fy = defaultY;
        scale = 1f;
        shown = true;
    }

    /** Whether the owning module is on. Hidden elements still appear in the editor. */
    public abstract boolean available();
    public abstract int width();
    public abstract int height();
    /** Draws at local (0, 0); scaling and placement are applied by the manager. */
    public abstract void render(GuiGraphics g, boolean editing);

    public int screenX(int screenWidth) {
        return Math.round(fx * Math.max(0, screenWidth - width() * scale));
    }

    public int screenY(int screenHeight) {
        return Math.round(fy * Math.max(0, screenHeight - height() * scale));
    }

    public boolean contains(double mx, double my, int screenWidth, int screenHeight) {
        int x = screenX(screenWidth), y = screenY(screenHeight);
        return mx >= x && my >= y && mx <= x + width() * scale && my <= y + height() * scale;
    }
}
