package dev.watermalon.module.impl;

import dev.watermalon.gui.Theme;
import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import dev.watermalon.module.setting.BoolSetting;
import dev.watermalon.module.setting.ModeSetting;
import dev.watermalon.module.setting.NumberSetting;
import dev.watermalon.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class CustomCrosshair extends Module {
    public final ModeSetting style = add(new ModeSetting("Style", "Crosshair shape", "Plus", "Plus", "Dot", "Cross", "Circle"));
    public final NumberSetting size = add(new NumberSetting("Size", "Size in pixels", 5, 2, 12, 1));
    public final NumberSetting gap = add(new NumberSetting("Gap", "Space in the middle", 1, 0, 6, 1));
    public final BoolSetting outline = add(new BoolSetting("Outline", "Dark outline for contrast", true));

    public CustomCrosshair() {
        super("CustomCrosshair", "Draws a custom crosshair in your accent color.", Category.RENDER);
        setEnabled(true);
    }

    /** Returns true when the custom crosshair replaced the vanilla one this frame. */
    public boolean render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        if (!enabled() || !mc.options.getCameraType().isFirstPerson()) return false;
        int cx = g.guiWidth() / 2;
        int cy = g.guiHeight() / 2;
        int s = size.getInt();
        int gp = gap.getInt();
        int color = Theme.accent();
        int shade = 0xAA000000;
        switch (style.get()) {
            case "Dot" -> {
                if (outline.get()) Render.circle(g, cx, cy, Math.max(2, s / 2) + 1, shade);
                Render.circle(g, cx, cy, Math.max(1, s / 2), color);
            }
            case "Circle" -> {
                if (outline.get()) Render.ring(g, cx, cy, s + 2, 3, shade);
                Render.ring(g, cx, cy, s + 1, 1, color);
                Render.rect(g, cx - 1, cy - 1, 2, 2, color);
            }
            case "Cross" -> {
                for (int i = gp; i <= s + gp; i++) {
                    plot(g, cx + i, cy + i, color, shade);
                    plot(g, cx - i - 1, cy + i, color, shade);
                    plot(g, cx + i, cy - i - 1, color, shade);
                    plot(g, cx - i - 1, cy - i - 1, color, shade);
                }
            }
            default -> {
                if (outline.get()) {
                    Render.rect(g, cx - 2, cy - s - gp - 1, 4, s + 1, shade);
                    Render.rect(g, cx - 2, cy + gp, 4, s + 1, shade);
                    Render.rect(g, cx - s - gp - 1, cy - 2, s + 1, 4, shade);
                    Render.rect(g, cx + gp, cy - 2, s + 1, 4, shade);
                }
                Render.rect(g, cx - 1, cy - s - gp, 2, s, color);
                Render.rect(g, cx - 1, cy + gp, 2, s, color);
                Render.rect(g, cx - s - gp, cy - 1, s, 2, color);
                Render.rect(g, cx + gp, cy - 1, s, 2, color);
            }
        }
        return true;
    }

    private void plot(GuiGraphics g, int x, int y, int color, int shade) {
        if (outline.get()) Render.rect(g, x - 1, y - 1, 3, 3, shade);
        Render.rect(g, x, y, 1, 1, color);
    }
}
