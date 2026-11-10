package dev.watermalon.module.impl;

import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import dev.watermalon.module.setting.BoolSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class AutoWalk extends Module {
    public final BoolSetting sprint = add(new BoolSetting("Sprint", "Sprint while walking", false));

    public AutoWalk() {
        super("AutoWalk", "Walks forward for you.", Category.MOVEMENT);
        setKey(GLFW.GLFW_KEY_G);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        boolean active = mc.screen == null;
        mc.options.keyUp.setDown(active);
        if (sprint.get()) mc.options.keySprint.setDown(active);
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        mc.options.keyUp.setDown(false);
        if (sprint.get()) mc.options.keySprint.setDown(false);
    }
}
