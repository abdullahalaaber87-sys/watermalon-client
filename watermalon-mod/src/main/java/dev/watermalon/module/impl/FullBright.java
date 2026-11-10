package dev.watermalon.module.impl;

import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/** Client-side night vision. The marker amplifier lets us remove only our own effect. */
public final class FullBright extends Module {
    private static final int MARKER = 39;

    public FullBright() {
        super("FullBright", "Maximum brightness everywhere.", Category.RENDER);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (mc.player == null) return;
        MobEffectInstance current = mc.player.getEffect(MobEffects.NIGHT_VISION);
        if (current == null || (current.getAmplifier() == MARKER && current.getDuration() < 400)) {
            mc.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 1000, MARKER, false, false, false));
        }
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        MobEffectInstance current = mc.player.getEffect(MobEffects.NIGHT_VISION);
        if (current != null && current.getAmplifier() == MARKER) mc.player.removeEffect(MobEffects.NIGHT_VISION);
    }
}
