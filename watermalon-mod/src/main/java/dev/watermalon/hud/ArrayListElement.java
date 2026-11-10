package dev.watermalon.hud;

import dev.watermalon.Watermalon;
import dev.watermalon.gui.Theme;
import dev.watermalon.module.Module;
import dev.watermalon.util.Colors;
import dev.watermalon.util.Render;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

public final class ArrayListElement extends HudElement {
    private static final int ROW = 11;

    public ArrayListElement() { super("arraylist", "ArrayList", 0.99f, 0.11f); }

    @Override public boolean available() { return Watermalon.modules().hud.enabled(); }

    private List<Module> entries() {
        float dt = Watermalon.hud().frameSeconds;
        List<Module> list = new ArrayList<>();
        for (Module module : Watermalon.modules().all()) {
            if (!module.showInArrayList()) continue;
            float target = module.enabled() ? 1f : 0f;
            module.anim += (target - module.anim) * (1f - (float) Math.exp(-14f * dt));
            if (module.anim > 0.02f) list.add(module);
        }
        list.sort(Comparator.comparingInt((Module m) -> Render.width(m.name())).reversed());
        return list;
    }

    @Override
    public int width() {
        int max = 60;
        for (Module module : Watermalon.modules().all()) {
            if (module.showInArrayList() && module.enabled()) max = Math.max(max, Render.width(module.name()) + 10);
        }
        return max;
    }

    @Override
    public int height() {
        int count = 0;
        for (Module module : Watermalon.modules().all()) if (module.showInArrayList() && module.enabled()) count++;
        return Math.max(ROW, count * ROW);
    }

    @Override
    public void render(GuiGraphics g, boolean editing) {
        boolean right = fx > 0.5f;
        int w = width();
        float y = 0;
        List<Module> list = entries();
        if (list.isEmpty() && editing) {
            Render.draw(g, "No modules", right ? w - Render.width("No modules") - 4 : 4, 2, Theme.MUTED);
            return;
        }
        for (Module module : list) {
            float t = module.anim;
            int textW = Render.width(module.name());
            int rowW = textW + 9;
            int slide = Math.round((1f - t) * (rowW + 6));
            int x = right ? w - rowW + slide : -slide;
            int ry = Math.round(y);
            Render.rect(g, x, ry, rowW, ROW, Colors.alpha(0x090C0D, 0.72f * t));
            Render.rect(g, right ? x + rowW - 1 : x, ry, 1, ROW, Colors.alpha(Theme.accent(), t));
            Render.drawShadow(g, module.name(), right ? x + 4 : x + 5, ry + 2, Colors.alpha(0xF1F6F3, t));
            y += ROW * t;
        }
    }
}
