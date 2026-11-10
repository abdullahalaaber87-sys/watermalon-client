package dev.watermalon.gui;

import com.mojang.blaze3d.platform.InputConstants;
import dev.watermalon.Watermalon;
import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import dev.watermalon.module.setting.ActionSetting;
import dev.watermalon.module.setting.BoolSetting;
import dev.watermalon.module.setting.ModeSetting;
import dev.watermalon.module.setting.NumberSetting;
import dev.watermalon.module.setting.Setting;
import dev.watermalon.module.setting.TextSetting;
import dev.watermalon.util.Colors;
import dev.watermalon.util.Render;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleSupplier;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/** The Watermalon click GUI. Everything is drawn in window-local units, then scaled. */
public final class ClickGuiScreen extends Screen {
    private static final int HEADER = 24;
    private static final int SIDEBAR = 92;
    private static final int ROW = 26;
    private static final int SUB = 17;
    private static final int PAD = 10;
    private static final int MIN_W = 330;
    private static final int MIN_H = 210;

    private enum Tab { MODULES, THEME }

    private static Tab tab = Tab.MODULES;
    private static Category category = null;
    private static String search = "";
    private static Module expanded = null;

    private record Hit(int x, int y, int w, int h, Runnable action) {
        boolean contains(double px, double py) { return px >= x && py >= y && px < x + w && py < y + h; }
    }

    private record Slider(double min, double max, double step, DoubleSupplier get, DoubleConsumer set) {}

    private record SliderHit(int x, int y, int w, int h, Slider slider) {
        boolean contains(double px, double py) { return px >= x - 3 && py >= y && px < x + w + 3 && py < y + h; }
    }

    private final List<Hit> hits = new ArrayList<>();
    private final List<SliderHit> sliderHits = new ArrayList<>();
    private boolean searchFocused;
    private Module binding;
    private TextSetting editing;
    private SliderHit activeSlider;
    private float scroll;
    private float scrollTarget;
    private int maxScroll;
    private int dragMode;
    private double grabX, grabY;
    private int startW, startH;
    private double mx, my;
    private long lastFrame = System.nanoTime();
    private float dt;

    public ClickGuiScreen() {
        super(Component.literal("Watermalon"));
    }

    @Override public boolean isPauseScreen() { return false; }

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        if (Theme.blur) super.renderBackground(g, mouseX, mouseY, partialTick);
        else g.fill(0, 0, width, height, 0x55000000);
    }

    private void clampWindow() {
        if (WindowState.x < 0 || WindowState.y < 0) {
            float fit = Math.min(width * 0.62f / WindowState.w, height * 0.72f / WindowState.h);
            WindowState.scale = Math.round(Math.max(0.6f, Math.min(1f, fit)) * 20f) / 20f;
        }
        float s = WindowState.scale;
        WindowState.w = Math.max(MIN_W, Math.min(WindowState.w, (int) ((width - 8) / s)));
        WindowState.h = Math.max(MIN_H, Math.min(WindowState.h, (int) ((height - 8) / s)));
        float vw = WindowState.w * s, vh = WindowState.h * s;
        if (WindowState.x < 0 || WindowState.y < 0) {
            WindowState.x = (width - vw) / 2f;
            WindowState.y = (height - vh) / 2f;
        }
        WindowState.x = Math.max(4, Math.min(WindowState.x, width - vw - 4));
        WindowState.y = Math.max(4, Math.min(WindowState.y, height - vh - 4));
    }

    private double localX(double screenX) { return (screenX - WindowState.x) / WindowState.scale; }
    private double localY(double screenY) { return (screenY - WindowState.y) / WindowState.scale; }

    private boolean hover(int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        long now = System.nanoTime();
        dt = Math.min(0.1f, (now - lastFrame) / 1_000_000_000f);
        lastFrame = now;
        clampWindow();
        hits.clear();
        sliderHits.clear();
        mx = localX(mouseX);
        my = localY(mouseY);
        scroll += (scrollTarget - scroll) * (1f - (float) Math.exp(-16f * dt));

        g.pose().pushMatrix();
        g.pose().translate(WindowState.x, WindowState.y);
        g.pose().scale(WindowState.scale, WindowState.scale);
        drawWindow(g);
        g.pose().popMatrix();
    }

    private void drawWindow(GuiGraphics g) {
        int W = WindowState.w, H = WindowState.h, r = Theme.radius;
        Render.shadow(g, 0, 0, W, H, r);
        Render.round(g, 0, 0, W, H, r, Theme.window());
        Render.outline(g, 0, 0, W, H, r, Theme.BORDER);

        // Header: brand, tabs, size controls, close.
        Render.icon(g, 9, 6, 12);
        Render.drawTwoTone(g, "WATER", "MALON", 25, 8, Theme.TEXT, Theme.MUTED);
        Render.rect(g, 0, HEADER, W, 1, Theme.LINE);

        int tx = SIDEBAR + PAD;
        tx = headerTab(g, "Modules", tx, tab == Tab.MODULES, () -> tab = Tab.MODULES);
        tx = headerTab(g, "HUD Editor", tx, false, () -> minecraft.setScreen(new HudEditorScreen(this)));
        headerTab(g, "Theme", tx, tab == Tab.THEME, () -> tab = Tab.THEME);

        int rx = W - 8;
        int closeX = rx - 12;
        boolean closeHover = hover(closeX, 5, 12, 14);
        Render.drawCentered(g, "\u00d7", closeX + 6, 8, closeHover ? Theme.accent() : Theme.MUTED);
        hits.add(new Hit(closeX, 5, 12, 14, this::onClose));
        Render.rect(g, closeX - 6, 7, 1, 10, Theme.LINE);

        String pct = Math.round(WindowState.scale * 100) + "%";
        int stepperW = 12 + Render.width("100%") + 12 + 4;
        int sx = closeX - 10 - stepperW;
        Render.round(g, sx, 5, stepperW, 14, 3, 0x0AFFFFFF);
        Render.outline(g, sx, 5, stepperW, 14, 3, 0x1AFFFFFF);
        Render.drawCentered(g, "-", sx + 7, 8, hover(sx, 5, 14, 14) ? Theme.accent() : Theme.MUTED);
        Render.drawCentered(g, pct, sx + stepperW / 2, 8, Theme.TEXT);
        Render.drawCentered(g, "+", sx + stepperW - 7, 8, hover(sx + stepperW - 14, 5, 14, 14) ? Theme.accent() : Theme.MUTED);
        hits.add(new Hit(sx, 5, 14, 14, () -> setScale(WindowState.scale - 0.05f)));
        hits.add(new Hit(sx + stepperW - 14, 5, 14, 14, () -> setScale(WindowState.scale + 0.05f)));

        // Sidebar.
        Render.rect(g, SIDEBAR, HEADER + 1, 1, H - HEADER - 1, Theme.LINE);
        int y = HEADER + 8;
        y = sidebarRow(g, "All modules", Watermalon.modules().all().size(), y, tab == Tab.MODULES && category == null, () -> selectCategory(null));
        for (Category c : Category.values()) {
            int count = 0;
            for (Module m : Watermalon.modules().all()) if (m.category() == c) count++;
            final Category target = c;
            y = sidebarRow(g, c.label(), count, y, tab == Tab.MODULES && category == c, () -> selectCategory(target));
        }
        Render.rect(g, 10, y + 4, SIDEBAR - 20, 1, Theme.LINE);
        sidebarRow(g, "Theme", -1, y + 10, tab == Tab.THEME, () -> tab = Tab.THEME);
        Render.drawScaled(g, "RShift to close", 10, H - 12, 0.65f, Theme.DIM);

        if (tab == Tab.MODULES) drawModules(g, W, H);
        else drawTheme(g, W, H);

        // Resize grip.
        int gx = W - 9, gy = H - 9;
        int gripColor = hover(gx - 2, gy - 2, 11, 11) || dragMode == 2 ? Theme.accent() : 0x44FFFFFF;
        Render.rect(g, gx + 5, gy + 1, 1, 5, gripColor);
        Render.rect(g, gx + 1, gy + 5, 5, 1, gripColor);
        Render.rect(g, gx + 3, gy + 3, 1, 1, gripColor);
    }

    private int headerTab(GuiGraphics g, String label, int x, boolean active, Runnable action) {
        int w = Render.width(label);
        boolean hovered = hover(x - 3, 4, w + 6, HEADER - 4);
        Render.draw(g, label, x, 8, active || hovered ? Theme.TEXT : Theme.MUTED);
        if (active) Render.rect(g, x, HEADER - 1, w, 1, Theme.accent());
        hits.add(new Hit(x - 3, 2, w + 6, HEADER - 2, action));
        return x + w + 13;
    }

    private int sidebarRow(GuiGraphics g, String label, int count, int y, boolean selected, Runnable action) {
        int x = 6, w = SIDEBAR - 12, h = 15;
        boolean hovered = hover(x, y, w, h);
        if (selected) {
            Render.round(g, x, y, w, h, 3, Theme.accent(0.13f));
            Render.rect(g, 0, y + 3, 2, h - 6, Theme.accent());
        } else if (hovered) {
            Render.round(g, x, y, w, h, 3, 0x0AFFFFFF);
        }
        Render.draw(g, label, x + 6, y + 4, selected || hovered ? Theme.TEXT : Theme.MUTED);
        if (count >= 0) {
            String c = String.format(Locale.ROOT, "%02d", count);
            Render.drawScaled(g, c, x + w - 6 - Render.width(c) * 0.7f, y + 5, 0.7f, selected ? Theme.accent() : Theme.DIM);
        }
        hits.add(new Hit(x, y, w, h, action));
        return y + h + 2;
    }

    private void selectCategory(Category next) {
        tab = Tab.MODULES;
        category = next;
        scrollTarget = 0;
    }

    private void setScale(float next) {
        WindowState.scale = Math.round(Math.max(0.6f, Math.min(1.6f, next)) * 20f) / 20f;
    }

    private List<Module> filtered() {
        List<Module> list = new ArrayList<>();
        String query = search.trim().toLowerCase(Locale.ROOT);
        for (Module module : Watermalon.modules().all()) {
            if (category != null && module.category() != category) continue;
            if (!query.isEmpty() && !(module.name() + " " + module.description()).toLowerCase(Locale.ROOT).contains(query)) continue;
            list.add(module);
        }
        return list;
    }

    private void drawModules(GuiGraphics g, int W, int H) {
        int cx = SIDEBAR + PAD, cw = W - SIDEBAR - PAD * 2;
        Render.draw(g, category == null ? "Modules" : category.label(), cx, HEADER + 10, Theme.TEXT);
        Render.draw(g, ".", cx + Render.width(category == null ? "Modules" : category.label()), HEADER + 10, Theme.accent());

        int sw = Math.min(120, cw / 2), sx = cx + cw - sw, sy = HEADER + 6;
        Render.round(g, sx, sy, sw, 15, 3, 0x08FFFFFF);
        Render.outline(g, sx, sy, sw, 15, 3, searchFocused ? Theme.accent(0.7f) : 0x22FFFFFF);
        boolean caret = searchFocused && (System.currentTimeMillis() / 500) % 2 == 0;
        if (search.isEmpty() && !searchFocused) Render.draw(g, "Search...", sx + 6, sy + 4, Theme.DIM);
        else Render.draw(g, Render.trim(search, sw - 14) + (caret ? "_" : ""), sx + 6, sy + 4, Theme.TEXT);
        hits.add(new Hit(sx, sy, sw, 15, () -> { searchFocused = true; editing = null; }));

        int top = HEADER + 28, bottom = H - 8;
        int viewport = bottom - top;
        List<Module> list = filtered();
        int content = 0;
        for (Module module : list) content += ROW + (module == expanded ? (module.settings().size() + 1) * SUB + 6 : 0);
        maxScroll = Math.max(0, content - viewport);
        scrollTarget = Math.max(0, Math.min(scrollTarget, maxScroll));
        scroll = Math.max(0, Math.min(scroll, maxScroll));

        if (list.isEmpty()) {
            Render.drawCentered(g, "No modules found", cx + cw / 2, top + 30, Theme.MUTED);
            return;
        }

        g.enableScissor(cx - 4, top, cx + cw + 4, bottom);
        int y = top - Math.round(scroll);
        for (Module module : list) {
            module.anim += ((module.enabled() ? 1f : 0f) - module.anim) * (1f - (float) Math.exp(-16f * dt));
            boolean visible = y + ROW > top && y < bottom;
            boolean rowHover = visible && hover(cx, Math.max(y, top), cw, Math.min(y + ROW, bottom) - Math.max(y, top));
            if (rowHover || module == expanded) Render.round(g, cx - 4, y + 1, cw + 8, ROW - 2, 4, 0x08FFFFFF);
            Render.circle(g, cx + 3, y + ROW / 2, 2, Colors.mix(0xFF3A4440, Theme.accent(), module.anim));
            Render.draw(g, module.name(), cx + 11, y + 5, Theme.TEXT);
            int toggleX = cx + cw - 20;
            String cat = module.category().label().toUpperCase(Locale.ROOT);
            float catW = Render.width(cat) * 0.6f;
            Render.drawScaled(g, cat, toggleX - 8 - catW, y + 10, 0.6f, Theme.DIM);
            Render.drawScaled(g, Render.trim(module.description(), (int) ((toggleX - 14 - catW - cx - 11) / 0.75f)), cx + 11, y + 15, 0.75f, Theme.MUTED);
            if (module.canToggle()) Render.toggle(g, toggleX, y + 8, module.enabled(), module.anim);
            Render.rect(g, cx, y + ROW - 1, cw, 1, Theme.LINE);
            if (visible) {
                hits.add(new Hit(cx, Math.max(y, top), toggleX - cx - 4, Math.min(y + ROW, bottom) - Math.max(y, top), () -> {
                    expanded = expanded == module ? null : module;
                }));
                if (module.canToggle()) hits.add(new Hit(toggleX - 3, y + 5, 24, 16, module::toggle));
            }
            y += ROW;
            if (module == expanded) y = drawSettings(g, module, cx, cw, y, top, bottom);
        }
        g.disableScissor();

        if (maxScroll > 0) {
            int trackH = viewport;
            int thumbH = Math.max(16, trackH * viewport / (viewport + maxScroll));
            int thumbY = top + Math.round((trackH - thumbH) * (scroll / maxScroll));
            Render.round(g, W - 5, thumbY, 2, thumbH, 1, 0x40FFFFFF);
        }
    }

    private int drawSettings(GuiGraphics g, Module module, int cx, int cw, int y, int top, int bottom) {
        int left = cx + 14, right = cx + cw - 6;
        Render.round(g, cx - 4, y, cw + 8, (module.settings().size() + 1) * SUB + 4, 4, 0x06FFFFFF);
        Render.rect(g, cx + 4, y + 3, 1, (module.settings().size() + 1) * SUB - 2, Theme.accent(0.4f));
        y += 2;

        boolean inView = y + SUB > top && y < bottom;
        Render.draw(g, "Keybind", left, y + 4, Theme.MUTED);
        String keyLabel = binding == module ? "Press a key..." : keyName(module.key());
        int kw = Render.width(keyLabel) + 10;
        boolean kh = inView && hover(right - kw, y + 1, kw, 14);
        Render.round(g, right - kw, y + 1, kw, 14, 3, binding == module ? Theme.accent(0.18f) : kh ? 0x14FFFFFF : 0x0AFFFFFF);
        Render.draw(g, keyLabel, right - kw + 5, y + 4, binding == module ? Theme.accent() : Theme.TEXT);
        if (inView) hits.add(new Hit(right - kw, y + 1, kw, 14, () -> { binding = module; editing = null; searchFocused = false; }));
        y += SUB;

        for (Setting<?> setting : module.settings()) {
            inView = y + SUB > top && y < bottom;
            Render.draw(g, setting.name(), left, y + 4, Theme.MUTED);
            if (setting instanceof BoolSetting bool) {
                Render.toggle(g, right - 18, y + 3, bool.get(), bool.get() ? 1f : 0f);
                if (inView) hits.add(new Hit(right - 22, y, 26, SUB, bool::toggle));
            } else if (setting instanceof NumberSetting number) {
                int trackW = 80, trackX = right - trackW;
                drawSlider(g, trackX, y + 7, trackW, (number.get() - number.min()) / (number.max() - number.min()));
                String value = number.display();
                Render.draw(g, value, trackX - 6 - Render.width(value), y + 4, Theme.accent());
                if (inView) sliderHits.add(new SliderHit(trackX, y, trackW, SUB,
                        new Slider(number.min(), number.max(), number.step(), number::get, v -> number.set(v))));
            } else if (setting instanceof ModeSetting mode) {
                String value = "< " + mode.get() + " >";
                int w = Render.width(value) + 10;
                boolean mh = inView && hover(right - w, y + 1, w, 14);
                Render.round(g, right - w, y + 1, w, 14, 3, mh ? 0x14FFFFFF : 0x0AFFFFFF);
                Render.draw(g, value, right - w + 5, y + 4, Theme.TEXT);
                if (inView) hits.add(new Hit(right - w, y + 1, w, 14, () -> mode.cycle(1)));
            } else if (setting instanceof TextSetting text) {
                int w = Math.min(130, cw / 2);
                boolean focused = editing == text;
                Render.round(g, right - w, y + 1, w, 14, 3, 0x08FFFFFF);
                Render.outline(g, right - w, y + 1, w, 14, 3, focused ? Theme.accent(0.7f) : 0x22FFFFFF);
                boolean caret = focused && (System.currentTimeMillis() / 500) % 2 == 0;
                String shown = text.get().isEmpty() && !focused ? "Click, then Ctrl+V" : Render.trim(text.get(), w - 14) + (caret ? "_" : "");
                Render.draw(g, shown, right - w + 5, y + 4, text.get().isEmpty() && !focused ? Theme.DIM : Theme.TEXT);
                if (inView) hits.add(new Hit(right - w, y + 1, w, 14, () -> { editing = text; searchFocused = false; binding = null; }));
            } else if (setting instanceof ActionSetting action) {
                String label = action.label();
                int w = Render.width(label) + 12;
                boolean ah = inView && hover(right - w, y + 1, w, 14);
                Render.round(g, right - w, y + 1, w, 14, 3, ah ? Theme.accent() : Theme.accent(0.16f));
                Render.draw(g, label, right - w + 6, y + 4, ah ? 0xFF1A0D11 : Theme.accent());
                if (inView) hits.add(new Hit(right - w, y + 1, w, 14, action::run));
            }
            y += SUB;
        }
        return y + 4;
    }

    private void drawSlider(GuiGraphics g, int x, int y, int w, double t) {
        t = Math.max(0, Math.min(1, t));
        Render.round(g, x, y, w, 3, 1, 0xFF333C38);
        int filled = (int) Math.round(w * t);
        Render.round(g, x, y, Math.max(3, filled), 3, 1, Theme.accent());
        Render.circle(g, x + filled, y + 1, 3, Theme.accent());
    }

    private void drawTheme(GuiGraphics g, int W, int H) {
        int cx = SIDEBAR + PAD, cw = W - SIDEBAR - PAD * 2, right = cx + cw;
        int y = HEADER + 10;
        Render.draw(g, "Theme", cx, y, Theme.TEXT);
        Render.draw(g, ".", cx + Render.width("Theme"), y, Theme.accent());
        y += 18;

        Render.draw(g, "Accent", cx, y + 4, Theme.MUTED);
        int sx = right;
        for (int i = Theme.ACCENTS.length - 1; i >= 0; i--) {
            Theme.Accent accent = Theme.ACCENTS[i];
            int w = Render.width(accent.name()) + 16;
            sx -= w;
            boolean selected = Theme.accentIndex == i;
            boolean hovered = hover(sx, y, w - 4, 15);
            Render.round(g, sx, y, w - 4, 15, 3, selected ? Colors.alpha(accent.color(), 0.18f) : hovered ? 0x10FFFFFF : 0x08FFFFFF);
            if (selected) Render.outline(g, sx, y, w - 4, 15, 3, accent.color());
            Render.circle(g, sx + 6, y + 7, 3, accent.color());
            Render.draw(g, accent.name(), sx + 12, y + 4, selected ? Theme.TEXT : Theme.MUTED);
            final int index = i;
            hits.add(new Hit(sx, y, w - 4, 15, () -> Theme.accentIndex = index));
        }
        y += 24;

        y = themeSlider(g, "GUI size", Math.round(WindowState.scale * 100) + "%", cx, right, y,
                new Slider(0.6, 1.6, 0.05, () -> WindowState.scale, v -> WindowState.scale = (float) v));
        y = themeSlider(g, "Opacity", Theme.opacity + "%", cx, right, y,
                new Slider(60, 100, 1, () -> Theme.opacity, v -> Theme.opacity = (int) Math.round(v)));
        y = themeSlider(g, "Corner radius", Theme.radius + "px", cx, right, y,
                new Slider(0, 10, 1, () -> Theme.radius, v -> Theme.radius = (int) Math.round(v)));
        y = themeToggle(g, "Blur the world behind the menu", Theme.blur, cx, right, y, () -> Theme.blur = !Theme.blur);
        y = themeToggle(g, "Inter font (off = Minecraft font)", Theme.interFont, cx, right, y, () -> Theme.interFont = !Theme.interFont);
        y += 6;

        int bx = cx;
        bx = button(g, "Reset window", bx, y, () -> { WindowState.reset(); clampWindow(); }) + 6;
        bx = button(g, "Reset theme", bx, y, Theme::reset) + 6;
        button(g, "Edit HUD", bx, y, () -> minecraft.setScreen(new HudEditorScreen(this)));
    }

    private int themeSlider(GuiGraphics g, String label, String value, int cx, int right, int y, Slider slider) {
        Render.draw(g, label, cx, y + 4, Theme.MUTED);
        int trackW = Math.min(140, (right - cx) / 2), trackX = right - trackW;
        drawSlider(g, trackX, y + 7, trackW, (slider.get().getAsDouble() - slider.min()) / (slider.max() - slider.min()));
        Render.draw(g, value, trackX - 8 - Render.width(value), y + 4, Theme.accent());
        sliderHits.add(new SliderHit(trackX, y, trackW, SUB, slider));
        return y + SUB + 3;
    }

    private int themeToggle(GuiGraphics g, String label, boolean value, int cx, int right, int y, Runnable toggle) {
        Render.draw(g, label, cx, y + 4, Theme.MUTED);
        Render.toggle(g, right - 18, y + 3, value, value ? 1f : 0f);
        hits.add(new Hit(right - 22, y, 26, SUB, toggle));
        return y + SUB + 3;
    }

    private int button(GuiGraphics g, String label, int x, int y, Runnable action) {
        int w = Render.width(label) + 14;
        boolean hovered = hover(x, y, w, 16);
        Render.round(g, x, y, w, 16, 3, hovered ? Theme.accent() : 0x0EFFFFFF);
        Render.outline(g, x, y, w, 16, 3, hovered ? Theme.accent() : 0x22FFFFFF);
        Render.draw(g, label, x + 7, y + 4, hovered ? 0xFF1A0D11 : Theme.TEXT);
        hits.add(new Hit(x, y, w, 16, action));
        return x + w;
    }

    private static String keyName(int key) {
        if (key < 0) return "None";
        return InputConstants.Type.KEYSYM.getOrCreate(key).getDisplayName().getString();
    }

    private void updateSlider(double localXPos) {
        if (activeSlider == null) return;
        Slider slider = activeSlider.slider();
        double t = Math.max(0, Math.min(1, (localXPos - activeSlider.x()) / activeSlider.w()));
        double raw = slider.min() + t * (slider.max() - slider.min());
        double snapped = Math.round(raw / slider.step()) * slider.step();
        slider.set().accept(Math.max(slider.min(), Math.min(slider.max(), snapped)));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double lx = localX(event.x()), ly = localY(event.y());
        if (event.button() != 0) return true;
        searchFocused = false;
        editing = null;
        if (binding != null) {
            binding = null;
            return true;
        }
        for (SliderHit hit : sliderHits) {
            if (hit.contains(lx, ly)) {
                activeSlider = hit;
                updateSlider(lx);
                return true;
            }
        }
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit hit = hits.get(i);
            if (hit.contains(lx, ly)) {
                hit.action().run();
                return true;
            }
        }
        boolean inside = lx >= 0 && ly >= 0 && lx < WindowState.w && ly < WindowState.h;
        if (!inside) return true;
        if (lx >= WindowState.w - 12 && ly >= WindowState.h - 12) {
            dragMode = 2;
            grabX = event.x();
            grabY = event.y();
            startW = WindowState.w;
            startH = WindowState.h;
        } else if (ly < HEADER) {
            dragMode = 1;
            grabX = event.x() - WindowState.x;
            grabY = event.y() - WindowState.y;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (activeSlider != null) {
            updateSlider(localX(event.x()));
            return true;
        }
        if (dragMode == 1) {
            WindowState.x = (float) (event.x() - grabX);
            WindowState.y = (float) (event.y() - grabY);
            return true;
        }
        if (dragMode == 2) {
            WindowState.w = startW + (int) Math.round((event.x() - grabX) / WindowState.scale);
            WindowState.h = startH + (int) Math.round((event.y() - grabY) / WindowState.scale);
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        activeSlider = null;
        dragMode = 0;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        scrollTarget = (float) Math.max(0, Math.min(maxScroll, scrollTarget - vertical * 22));
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int key = event.key();
        boolean ctrl = (event.modifiers() & (GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SUPER)) != 0;
        if (binding != null) {
            if (key == GLFW.GLFW_KEY_BACKSPACE || key == GLFW.GLFW_KEY_DELETE) binding.setKey(-1);
            else if (key != GLFW.GLFW_KEY_ESCAPE) binding.setKey(key);
            binding = null;
            return true;
        }
        if (editing != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) editing = null;
            else if (key == GLFW.GLFW_KEY_BACKSPACE && !editing.get().isEmpty()) editing.set(editing.get().substring(0, editing.get().length() - 1));
            else if (ctrl && key == GLFW.GLFW_KEY_V) appendTo(editing, minecraft.keyboardHandler.getClipboard());
            return true;
        }
        if (searchFocused) {
            if (key == GLFW.GLFW_KEY_ESCAPE || key == GLFW.GLFW_KEY_ENTER) searchFocused = false;
            else if (key == GLFW.GLFW_KEY_BACKSPACE && !search.isEmpty()) search = search.substring(0, search.length() - 1);
            else if (ctrl && key == GLFW.GLFW_KEY_V) search = (search + minecraft.keyboardHandler.getClipboard()).replaceAll("[\\r\\n]", "");
            scrollTarget = 0;
            return true;
        }
        if (ctrl && key == GLFW.GLFW_KEY_F) {
            tab = Tab.MODULES;
            searchFocused = true;
            return true;
        }
        if (Watermalon.openGuiKey().matches(event)) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        String typed = event.codepointAsString();
        if (editing != null) {
            appendTo(editing, typed);
            return true;
        }
        if (searchFocused && search.length() < 32) {
            search += typed;
            scrollTarget = 0;
            return true;
        }
        if (!searchFocused && binding == null && tab == Tab.MODULES && Character.isLetterOrDigit(event.codepoint())) {
            searchFocused = true;
            search = typed;
            return true;
        }
        return false;
    }

    private static void appendTo(TextSetting setting, String value) {
        String clean = value.replaceAll("[\\r\\n\\t]", "").trim();
        String next = setting.get() + clean;
        setting.set(next.length() > setting.maxLength() ? next.substring(0, setting.maxLength()) : next);
    }

    @Override
    public void onClose() {
        Watermalon.saveConfig();
        super.onClose();
    }
}
