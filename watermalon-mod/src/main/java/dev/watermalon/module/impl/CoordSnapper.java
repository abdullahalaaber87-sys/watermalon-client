package dev.watermalon.module.impl;

import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

/** One-shot: copies your coordinates to the clipboard, then switches itself off. */
public final class CoordSnapper extends Module {
    public CoordSnapper() {
        super("CoordSnapper", "Copies your coordinates to the clipboard.", Category.WORLD);
    }

    @Override public boolean showInArrayList() { return false; }

    @Override
    protected void onEnable() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            BlockPos pos = mc.player.blockPosition();
            String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();
            mc.keyboardHandler.setClipboard(coords);
            mc.gui.getChat().addMessage(Component.literal("\u00a7d[Watermalon]\u00a7r Copied " + coords));
        }
        setEnabled(false);
    }
}
