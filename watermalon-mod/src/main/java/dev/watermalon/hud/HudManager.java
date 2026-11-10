package dev.watermalon.hud;

import dev.watermalon.gui.HudEditorScreen;
import dev.watermalon.gui.Theme;
import dev.watermalon.util.Colors;
import dev.watermalon.util.Render;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.glfw.GLFW;

public final class HudManager {
    public final WatermarkElement watermark = new WatermarkElement();
    public final InfoElement info = new InfoElement();
    public final ArrayListElement arrayList = new ArrayListElement();
    public final SpotifyElement spotify = new SpotifyElement();
    public final KeystrokesElement keystrokes = new KeystrokesElement();
    private final List<HudElement> elements = List.of(watermark, info, arrayList, spotify, keystrokes);

    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean leftWas, rightWas;
    private long lastFrame = System.nanoTime();
    public float frameSeconds = 0.016f;

    public List<HudElement> elements() { return elements; }

    public HudElement byId(String id) {
        for (HudElement element : elements) if (element.id().equals(id)) return element;
        return null;
    }

    public int leftCps() { return leftClicks.size(); }
    public int rightCps() { return rightClicks.size(); }

    /** Called by the Fabric HUD layer every frame while in game. */
    public void renderInGame(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        tickFrame(mc);
        if (mc.options.hideGui || mc.screen instanceof HudEditorScreen) return;
        render(g, false, null, null);
    }

    public void tickFrame(Minecraft mc) {
        long now = System.nanoTime();
        frameSeconds = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        long window = mc.getWindow().handle();
        boolean inGame = mc.screen == null;
        boolean left = inGame && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean right = inGame && GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_RIGHT) == GLFW.GLFW_PRESS;
        long millis = System.currentTimeMillis();
        if (left && !leftWas) leftClicks.addLast(millis);
        if (right && !rightWas) rightClicks.addLast(millis);
        leftWas = left;
        rightWas = right;
        while (!leftClicks.isEmpty() && millis - leftClicks.peekFirst() > 1000) leftClicks.removeFirst();
        while (!rightClicks.isEmpty() && millis - rightClicks.peekFirst() > 1000) rightClicks.removeFirst();
    }

    public void render(GuiGraphics g, boolean editing, HudElement selected, HudElement hovered) {
        int sw = g.guiWidth();
        int sh = g.guiHeight();
        for (HudElement element : elements) {
            if (!element.shown) continue;
            boolean available = element.available();
            if (!editing && !available) continue;
            int x = element.screenX(sw);
            int y = element.screenY(sh);
            g.pose().pushMatrix();
            g.pose().translate(x, y);
            g.pose().scale(element.scale, element.scale);
            element.render(g, editing);
            g.pose().popMatrix();
            if (editing) {
                int w = Math.round(element.width() * element.scale);
                int h = Math.round(element.height() * element.scale);
                boolean active = element == selected || element == hovered;
                if (!available) Render.rect(g, x, y, w, h, 0x88000000);
                Render.outline(g, x - 2, y - 2, w + 4, h + 4, 3, active ? Theme.accent() : 0x55FFFFFF);
                if (active) {
                    String tag = element.label().toUpperCase() + (available ? "" : "  (OFF)");
                    int tw = Render.width(tag);
                    int ty = y - 13 < 0 ? y + h + 3 : y - 13;
                    Render.round(g, x - 2, ty, tw + 8, 11, 2, Theme.accent());
                    Render.draw(g, tag, x + 2, ty + 2, 0xFF1A0D11);
                }
            }
        }
    }

    public static void card(GuiGraphics g, int w, int h) {
        Render.round(g, 0, 0, w, h, 4, Theme.hudCard());
        Render.outline(g, 0, 0, w, h, 4, Colors.alpha(0xFFFFFF, 0.08f));
    }
}
