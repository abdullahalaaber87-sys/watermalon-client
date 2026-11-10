package dev.watermalon.hud;

import dev.watermalon.Watermalon;
import dev.watermalon.gui.Theme;
import dev.watermalon.util.Render;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public final class KeystrokesElement extends HudElement {
    private static final int K = 16, GAP = 2;

    public KeystrokesElement() { super("keystrokes", "Keystrokes", 0.99f, 0.97f); }

    @Override public boolean available() { return Watermalon.modules().hud.enabled(); }
    @Override public int width() { return K * 3 + GAP * 2; }
    @Override public int height() { return K * 3 + GAP * 3 + 8; }

    @Override
    public void render(GuiGraphics g, boolean editing) {
        Minecraft mc = Minecraft.getInstance();
        int full = width();
        key(g, "W", K + GAP, 0, K, K, mc.options.keyUp);
        key(g, "A", 0, K + GAP, K, K, mc.options.keyLeft);
        key(g, "S", K + GAP, K + GAP, K, K, mc.options.keyDown);
        key(g, "D", (K + GAP) * 2, K + GAP, K, K, mc.options.keyRight);
        int half = (full - GAP) / 2;
        int my = (K + GAP) * 2;
        mouse(g, "LMB", 0, my, half, mc.options.keyAttack.isDown(), Watermalon.hud().leftCps());
        mouse(g, "RMB", half + GAP, my, full - half - GAP, mc.options.keyUse.isDown(), Watermalon.hud().rightCps());
        boolean jump = mc.options.keyJump.isDown();
        int sy = my + K + GAP;
        box(g, 0, sy, full, 8, jump);
        Render.rect(g, full / 2 - 8, sy + 4, 16, 1, jump ? 0xFF1A0D11 : 0xAAE8EFEB);
    }

    private void key(GuiGraphics g, String label, int x, int y, int w, int h, KeyMapping mapping) {
        boolean down = mapping.isDown();
        box(g, x, y, w, h, down);
        Render.drawCentered(g, label, x + w / 2, y + (h - 8) / 2, down ? 0xFF1A0D11 : 0xFFE8EFEB);
    }

    private void mouse(GuiGraphics g, String label, int x, int y, int w, boolean down, int cps) {
        box(g, x, y, w, K, down);
        int color = down ? 0xFF1A0D11 : 0xFFE8EFEB;
        Render.drawScaled(g, label, x + w / 2f - Render.width(label) * 0.35f, y + 2, 0.7f, color);
        String text = cps + " CPS";
        Render.drawScaled(g, text, x + w / 2f - Render.width(text) * 0.3f, y + 9, 0.6f, color);
    }

    private void box(GuiGraphics g, int x, int y, int w, int h, boolean down) {
        Render.round(g, x, y, w, h, 3, down ? Theme.accent() : Theme.hudCard());
        if (!down) Render.outline(g, x, y, w, h, 3, 0x18FFFFFF);
    }
}
