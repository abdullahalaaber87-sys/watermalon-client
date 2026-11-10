package dev.watermalon.module.impl;

import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;

public final class WeatherNotifier extends Module {
    private int last = -1;

    public WeatherNotifier() {
        super("WeatherNotifier", "Shows a toast when the weather changes.", Category.WORLD);
    }

    @Override
    protected void onEnable() { last = -1; }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.level == null) { last = -1; return; }
        int state = mc.level.isThundering() ? 2 : mc.level.isRaining() ? 1 : 0;
        if (last != -1 && state != last) {
            String message = switch (state) {
                case 2 -> "A thunderstorm is rolling in.";
                case 1 -> "It started raining.";
                default -> "The sky is clearing up.";
            };
            SystemToast.add(mc.getToastManager(), SystemToast.SystemToastId.PERIODIC_NOTIFICATION, Component.literal("Watermalon"), Component.literal(message));
        }
        last = state;
    }
}
