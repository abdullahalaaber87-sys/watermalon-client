package dev.watermalon.util;

import dev.watermalon.gui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;

/** Small immediate-mode drawing helpers on top of GuiGraphics. */
public final class Render {
    private Render() {}

    public static final Identifier ICON = Identifier.fromNamespaceAndPath("watermalon", "icon.png");
    private static final FontDescription INTER = new FontDescription.Resource(Identifier.fromNamespaceAndPath("watermalon", "inter"));

    public static Font font() {
        return Minecraft.getInstance().font;
    }

    public static Component text(String value) {
        if (!Theme.interFont) return Component.literal(value);
        return Component.literal(value).withStyle(style -> style.withFont(INTER));
    }

    public static int width(String value) {
        return font().width(text(value));
    }

    public static void draw(GuiGraphics g, String value, int x, int y, int color) {
        g.drawString(font(), text(value), x, y, color, false);
    }

    public static void drawTwoTone(GuiGraphics g, String first, String second, int x, int y, int firstColor, int secondColor) {
        Component line = text(first).copy().withStyle(style -> style.withColor(firstColor & 0xFFFFFF))
                .append(text(second).copy().withStyle(style -> style.withColor(secondColor & 0xFFFFFF)));
        g.drawString(font(), line, x, y, 0xFFFFFFFF, false);
    }

    public static int twoToneWidth(String first, String second) {
        return font().width(text(first).copy().append(text(second)));
    }

    public static void drawShadow(GuiGraphics g, String value, int x, int y, int color) {
        g.drawString(font(), text(value), x, y, color, true);
    }

    public static void drawRight(GuiGraphics g, String value, int right, int y, int color) {
        draw(g, value, right - width(value), y, color);
    }

    public static void drawCentered(GuiGraphics g, String value, int centerX, int y, int color) {
        draw(g, value, centerX - width(value) / 2, y, color);
    }

    /** Draws text at a fractional scale, anchored at its top-left. */
    public static void drawScaled(GuiGraphics g, String value, float x, float y, float scale, int color) {
        g.pose().pushMatrix();
        g.pose().translate(x, y);
        g.pose().scale(scale, scale);
        draw(g, value, 0, 0, color);
        g.pose().popMatrix();
    }

    public static String trim(String value, int maxWidth) {
        if (width(value) <= maxWidth) return value;
        String ellipsis = "...";
        int end = value.length();
        while (end > 0 && width(value.substring(0, end) + ellipsis) > maxWidth) end--;
        return value.substring(0, end) + ellipsis;
    }

    public static void rect(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (w <= 0 || h <= 0 || (color >>> 24) == 0) return;
        g.fill(x, y, x + w, y + h, color);
    }

    private static int inset(int r, int row) {
        double dy = r - row - 0.5;
        return (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
    }

    private static int spanInset(int h, int r, int row) {
        if (row < r) return inset(r, row);
        if (row >= h - r) return inset(r, h - 1 - row);
        return 0;
    }

    public static void round(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            rect(g, x, y, w, h, color);
            return;
        }
        rect(g, x, y + r, w, h - 2 * r, color);
        for (int i = 0; i < r; i++) {
            int in = inset(r, i);
            rect(g, x + in, y + i, w - 2 * in, 1, color);
            rect(g, x + in, y + h - 1 - i, w - 2 * in, 1, color);
        }
    }

    /** One pixel rounded outline. */
    public static void outline(GuiGraphics g, int x, int y, int w, int h, int r, int color) {
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        int ir = Math.max(0, r - 1);
        for (int row = 0; row < h; row++) {
            int out = spanInset(h, r, row);
            boolean inner = row >= 1 && row < h - 1;
            if (!inner) {
                rect(g, x + out, y + row, w - 2 * out, 1, color);
                continue;
            }
            int in = 1 + spanInset(h - 2, ir, row - 1);
            rect(g, x + out, y + row, in - out, 1, color);
            rect(g, x + w - in, y + row, in - out, 1, color);
        }
    }

    public static void circle(GuiGraphics g, int cx, int cy, int r, int color) {
        round(g, cx - r, cy - r, r * 2, r * 2, r, color);
    }

    public static void ring(GuiGraphics g, int cx, int cy, int r, int thickness, int color) {
        int ir = Math.max(0, r - thickness);
        for (int dy = -r; dy < r; dy++) {
            double fy = dy + 0.5;
            int outer = (int) Math.round(Math.sqrt(Math.max(0, r * r - fy * fy)));
            int inner = Math.abs(fy) < ir ? (int) Math.round(Math.sqrt(ir * ir - fy * fy)) : 0;
            if (inner == 0) {
                rect(g, cx - outer, cy + dy, outer * 2, 1, color);
            } else {
                rect(g, cx - outer, cy + dy, outer - inner, 1, color);
                rect(g, cx + inner, cy + dy, outer - inner, 1, color);
            }
        }
    }

    public static void shadow(GuiGraphics g, int x, int y, int w, int h, int r) {
        for (int i = 6; i >= 1; i--) {
            round(g, x - i, y - i + 2, w + i * 2, h + i * 2, r + i, Colors.alpha(0, 0.045f));
        }
    }

    public static void icon(GuiGraphics g, int x, int y, int size) {
        g.blit(RenderPipelines.GUI_TEXTURED, ICON, x, y, 0f, 0f, size, size, 64, 64, 64, 64);
    }

    public static void toggle(GuiGraphics g, int x, int y, boolean on, float anim) {
        int track = Colors.mix(Theme.TRACK_OFF, Theme.accent(), anim);
        round(g, x, y, 18, 10, 5, track);
        int knob = Colors.mix(Theme.KNOB_OFF, 0xFF1A0D11, anim);
        circle(g, x + 5 + Math.round(8 * anim), y + 5, 3, knob);
    }
}
