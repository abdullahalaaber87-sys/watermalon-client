package dev.watermalon.hud;

import dev.watermalon.Watermalon;
import dev.watermalon.gui.Theme;
import dev.watermalon.util.Render;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.client.gui.GuiGraphics;

public final class InfoElement extends HudElement {
    public InfoElement() { super("info", "Info", 0.01f, 0.12f); }

    @Override public boolean available() { return Watermalon.modules().hud.enabled(); }

    private String[] parts() {
        Minecraft mc = Minecraft.getInstance();
        String ping = "0";
        String xyz = "0 0 0";
        String facing = "N";
        if (mc.player != null) {
            BlockPos pos = mc.player.blockPosition();
            xyz = pos.getX() + " " + pos.getY() + " " + pos.getZ();
            facing = switch (mc.player.getDirection()) {
                case NORTH -> "N";
                case SOUTH -> "S";
                case EAST -> "E";
                case WEST -> "W";
                default -> "N";
            };
            if (mc.getConnection() != null) {
                PlayerInfo info = mc.getConnection().getPlayerInfo(mc.player.getUUID());
                if (info != null) ping = Integer.toString(info.getLatency());
            }
        }
        return new String[] {mc.getFps() + "", "FPS", ping, "MS", xyz, "XYZ", facing, ""};
    }

    @Override
    public int width() {
        String[] p = parts();
        int w = 4;
        for (int i = 0; i < p.length; i += 2) w += Render.width(p[i]) + (p[i + 1].isEmpty() ? 0 : Render.width(p[i + 1]) + 3) + 10;
        return w;
    }

    @Override public int height() { return 14; }

    @Override
    public void render(GuiGraphics g, boolean editing) {
        String[] p = parts();
        HudManager.card(g, width(), height());
        int x = 7;
        for (int i = 0; i < p.length; i += 2) {
            if (i > 0) Render.rect(g, x - 5, 3, 1, 8, 0x22FFFFFF);
            Render.draw(g, p[i], x, 3, Theme.TEXT);
            x += Render.width(p[i]);
            if (!p[i + 1].isEmpty()) {
                Render.draw(g, p[i + 1], x + 3, 3, Theme.MUTED);
                x += Render.width(p[i + 1]) + 3;
            }
            x += 10;
        }
    }
}
