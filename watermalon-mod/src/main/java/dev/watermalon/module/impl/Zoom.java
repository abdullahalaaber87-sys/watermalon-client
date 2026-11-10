package dev.watermalon.module.impl;

import com.mojang.blaze3d.platform.InputConstants;
import dev.watermalon.Watermalon;
import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import dev.watermalon.module.setting.BoolSetting;
import dev.watermalon.module.setting.NumberSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

public final class Zoom extends Module {
    public final NumberSetting amount = add(new NumberSetting("Amount", "How far to zoom in", 4, 2, 10, 0.5));
    public final BoolSetting smooth = add(new BoolSetting("Smooth", "Ease in and out", true));
    private float factor = 1f;
    private long lastNanos = System.nanoTime();

    public Zoom() {
        super("Zoom", "Hold the key to zoom in.", Category.RENDER);
        setKey(GLFW.GLFW_KEY_C);
        setEnabled(true);
    }

    @Override public boolean holdKey() { return true; }

    public boolean active() {
        Minecraft mc = Minecraft.getInstance();
        return enabled() && key() >= 0 && mc.screen == null && mc.player != null && InputConstants.isKeyDown(mc.getWindow(), key());
    }

    /** Called from the GameRenderer mixin with the vanilla field of view. */
    public static float apply(float fov) {
        Zoom zoom = Watermalon.modules().zoom;
        long now = System.nanoTime();
        float dt = Math.min(0.1f, (now - zoom.lastNanos) / 1_000_000_000f);
        zoom.lastNanos = now;
        float target = zoom.active() ? zoom.amount.getFloat() : 1f;
        zoom.factor = zoom.smooth.get() ? zoom.factor + (target - zoom.factor) * (1f - (float) Math.exp(-14f * dt)) : target;
        if (Math.abs(zoom.factor - 1f) < 0.001f) {
            zoom.factor = 1f;
            return fov;
        }
        return fov / zoom.factor;
    }
}
