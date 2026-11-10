package dev.watermalon.gui;

import dev.watermalon.Watermalon;
import dev.watermalon.hud.HudElement;
import dev.watermalon.util.Render;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** Full-screen HUD layout editor: drag elements, scroll to scale, arrows to nudge. */
public final class HudEditorScreen extends Screen {
    private final Screen parent;
    private HudElement selected, hovered, dragging;
    private double grabX, grabY;
    private boolean guideX, guideY;

    private record Hit(int x, int y, int w, int h, Runnable action) {
        boolean contains(double px, double py) { return px >= x && py >= y && px < x + w && py < y + h; }
    }

    private final List<Hit> hits = new ArrayList<>();

    public HudEditorScreen(Screen parent) {
        super(Component.literal("Watermalon HUD editor"));
        this.parent = parent;
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x30000000);
        for (int x = 0; x < width; x += 20) Render.rect(g, x, 0, 1, height, 0x0CFFFFFF);
        for (int y = 0; y < height; y += 20) Render.rect(g, 0, y, width, 1, 0x0CFFFFFF);
    }

    private HudElement elementAt(double mx, double my) {
        List<HudElement> elements = Watermalon.hud().elements();
        for (int i = elements.size() - 1; i >= 0; i--) {
            HudElement element = elements.get(i);
            if (element.shown && element.contains(mx, my, width, height)) return element;
        }
        return null;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        hits.clear();
        hovered = dragging != null ? dragging : elementAt(mouseX, mouseY);
        if (guideX) Render.rect(g, width / 2, 0, 1, height, Theme.accent());
        if (guideY) Render.rect(g, 0, height / 2, width, 1, Theme.accent());
        Watermalon.hud().render(g, true, selected, hovered);
        drawToolbar(g, mouseX, mouseY);
    }

    // Toolbar is drawn in its own local space and scaled down to fit narrow screens.
    private final List<Hit> toolbarHits = new ArrayList<>();
    private float barX, barY, barScale = 1f;

    private void drawToolbar(GuiGraphics g, int mouseX, int mouseY) {
        toolbarHits.clear();
        List<HudElement> elements = Watermalon.hud().elements();
        int gap = 4;
        int titleW = 16 + Render.width("HUD layout") + 10;
        int chipsW = 0;
        for (HudElement element : elements) chipsW += Render.width(element.label()) + 18 + gap;
        int scaleW = selected == null ? 0 : 14 + Render.width("100%") + 14 + 8;
        int resetW = Render.width("Reset") + 14, doneW = Render.width("Done") + 16;
        int total = 8 + titleW + chipsW + 6 + scaleW + resetW + gap + doneW + 8;
        int h = 24;
        barScale = Math.min(1f, (width - 12f) / total);
        barX = (width - total * barScale) / 2f;
        barY = Math.max(6, Math.round(height * 0.26f));
        double lmx = (mouseX - barX) / barScale, lmy = (mouseY - barY) / barScale;

        g.pose().pushMatrix();
        g.pose().translate(barX, barY);
        g.pose().scale(barScale, barScale);
        Render.shadow(g, 0, 0, total, h, 6);
        Render.round(g, 0, 0, total, h, 6, 0xEE0B0B0E);
        Render.outline(g, 0, 0, total, h, 6, Theme.accent(0.35f));
        int cx = 8;
        Render.icon(g, cx, 6, 12);
        Render.draw(g, "HUD layout", cx + 16, 8, Theme.TEXT);
        cx += titleW;

        for (HudElement element : elements) {
            int w = Render.width(element.label()) + 18;
            boolean hover = lmx >= cx && lmy >= 5 && lmx < cx + w && lmy < 19;
            Render.round(g, cx, 5, w, 14, 3, element.shown ? Theme.accent(0.14f) : hover ? 0x10FFFFFF : 0x06FFFFFF);
            Render.outline(g, cx, 5, w, 14, 3, element.shown ? Theme.accent(0.55f) : 0x22FFFFFF);
            Render.round(g, cx + 4, 9, 6, 6, 1, element.shown ? Theme.accent() : 0xFF4A5551);
            Render.draw(g, element.label(), cx + 13, 8, element.shown ? Theme.TEXT : Theme.MUTED);
            toolbarHits.add(new Hit(cx, 5, w, 14, () -> element.shown = !element.shown));
            cx += w + gap;
        }
        cx += 6;

        if (selected != null) {
            HudElement target = selected;
            Render.drawCentered(g, "-", cx + 7, 8, Theme.MUTED);
            Render.drawCentered(g, Math.round(target.scale * 100) + "%", cx + scaleW / 2 - 4, 8, Theme.accent());
            Render.drawCentered(g, "+", cx + scaleW - 15, 8, Theme.MUTED);
            toolbarHits.add(new Hit(cx, 4, 14, 16, () -> target.scale = Math.max(0.5f, target.scale - 0.1f)));
            toolbarHits.add(new Hit(cx + scaleW - 22, 4, 14, 16, () -> target.scale = Math.min(2f, target.scale + 0.1f)));
            cx += scaleW;
        }

        boolean resetHover = lmx >= cx && lmy >= 5 && lmx < cx + resetW && lmy < 19;
        Render.round(g, cx, 5, resetW, 14, 3, resetHover ? 0x18FFFFFF : 0x0AFFFFFF);
        Render.draw(g, "Reset", cx + 7, 8, Theme.TEXT);
        toolbarHits.add(new Hit(cx, 5, resetW, 14, () -> {
            for (HudElement element : elements) element.reset();
            selected = null;
        }));
        cx += resetW + gap;
        Render.round(g, cx, 5, doneW, 14, 3, Theme.accent());
        Render.draw(g, "Done", cx + 8, 8, 0xFF1A0D11);
        toolbarHits.add(new Hit(cx, 5, doneW, 14, this::onClose));
        g.pose().popMatrix();

        int below = Math.round(barY + h * barScale);
        String help = "Drag to move  \u00b7  Scroll to resize  \u00b7  Arrows nudge  \u00b7  Esc when done";
        Render.drawScaled(g, help, width / 2f - Render.width(help) * 0.35f, below + 5, 0.7f, 0xCCEDF2F0);

        if (!Watermalon.modules().hud.enabled()) {
            String notice = "HUD module is off. Click here to turn it on.";
            int nw = Render.width(notice) + 12, nx = (width - nw) / 2, ny = below + 16;
            Render.round(g, nx, ny, nw, 14, 3, 0xCC2A1F0E);
            Render.draw(g, notice, nx + 6, ny + 3, 0xFFE2C27A);
            hits.add(new Hit(nx, ny, nw, 14, () -> Watermalon.modules().hud.setEnabled(true)));
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != 0) return true;
        double lx = (event.x() - barX) / barScale, ly = (event.y() - barY) / barScale;
        for (int i = toolbarHits.size() - 1; i >= 0; i--) {
            if (toolbarHits.get(i).contains(lx, ly)) {
                toolbarHits.get(i).action().run();
                return true;
            }
        }
        for (int i = hits.size() - 1; i >= 0; i--) {
            if (hits.get(i).contains(event.x(), event.y())) {
                hits.get(i).action().run();
                return true;
            }
        }
        HudElement element = elementAt(event.x(), event.y());
        selected = element;
        dragging = element;
        if (element != null) {
            grabX = event.x() - element.screenX(width);
            grabY = event.y() - element.screenY(height);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (dragging == null) return true;
        float w = dragging.width() * dragging.scale, h = dragging.height() * dragging.scale;
        double left = event.x() - grabX, top = event.y() - grabY;
        guideX = Math.abs(left + w / 2 - width / 2.0) < 4;
        guideY = Math.abs(top + h / 2 - height / 2.0) < 4;
        if (guideX) left = width / 2.0 - w / 2;
        if (guideY) top = height / 2.0 - h / 2;
        dragging.fx = (float) Math.max(0, Math.min(1, left / Math.max(1, width - w)));
        dragging.fy = (float) Math.max(0, Math.min(1, top / Math.max(1, height - h)));
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = null;
        guideX = guideY = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        HudElement element = elementAt(mouseX, mouseY);
        if (element == null) element = selected;
        if (element != null) {
            element.scale = Math.round(Math.max(0.5f, Math.min(2f, element.scale + (float) vertical * 0.05f)) * 20f) / 20f;
            selected = element;
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        if (selected != null && (key == GLFW.GLFW_KEY_LEFT || key == GLFW.GLFW_KEY_RIGHT || key == GLFW.GLFW_KEY_UP || key == GLFW.GLFW_KEY_DOWN)) {
            int step = (event.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0 ? 10 : 1;
            float w = selected.width() * selected.scale, h = selected.height() * selected.scale;
            if (key == GLFW.GLFW_KEY_LEFT) selected.fx -= step / Math.max(1f, width - w);
            if (key == GLFW.GLFW_KEY_RIGHT) selected.fx += step / Math.max(1f, width - w);
            if (key == GLFW.GLFW_KEY_UP) selected.fy -= step / Math.max(1f, height - h);
            if (key == GLFW.GLFW_KEY_DOWN) selected.fy += step / Math.max(1f, height - h);
            selected.fx = Math.max(0, Math.min(1, selected.fx));
            selected.fy = Math.max(0, Math.min(1, selected.fy));
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        Watermalon.saveConfig();
        minecraft.setScreen(parent);
    }
}
