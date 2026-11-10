package dev.watermalon.module.impl;

import dev.watermalon.gui.HudEditorScreen;
import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import dev.watermalon.module.setting.ActionSetting;
import net.minecraft.client.Minecraft;

public final class HudModule extends Module {
    public HudModule() {
        super("HUD", "Watermark, arraylist, info, and keystrokes.", Category.CLIENT);
        add(new ActionSetting("Layout", "Move and resize HUD elements", () -> "Edit HUD",
                () -> Minecraft.getInstance().setScreen(new HudEditorScreen(Minecraft.getInstance().screen))));
        setEnabled(true);
    }
}
