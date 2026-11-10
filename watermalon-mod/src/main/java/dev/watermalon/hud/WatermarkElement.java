package dev.watermalon.hud;

import dev.watermalon.Watermalon;
import dev.watermalon.gui.Theme;
import dev.watermalon.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class WatermarkElement extends HudElement {
    public WatermarkElement() { super("watermark", "Watermark", 0.01f, 0.015f); }

    @Override public boolean available() { return Watermalon.modules().hud.enabled(); }

    private String fps() { return Minecraft.getInstance().getFps() + " fps"; }

    @Override public int width() { return 22 + Render.twoToneWidth("Water", "malon") + 8 + Render.width(fps()) + 8; }
    @Override public int height() { return 18; }

    @Override
    public void render(GuiGraphics g, boolean editing) {
        int w = width();
        HudManager.card(g, w, height());
        Render.icon(g, 4, 3, 12);
        int x = 20;
        Render.drawTwoTone(g, "Water", "malon", x, 5, Theme.TEXT, Theme.MUTED);
        x += Render.twoToneWidth("Water", "malon") + 6;
        String fps = fps();
        Render.round(g, x, 3, Render.width(fps) + 6, 12, 2, Theme.accent(0.16f));
        Render.draw(g, fps, x + 3, 5, Theme.accent());
    }
}
