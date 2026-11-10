package dev.watermalon.hud;

import dev.watermalon.Watermalon;
import dev.watermalon.gui.Theme;
import dev.watermalon.module.impl.SpotifyHud;
import dev.watermalon.spotify.Spotify;
import dev.watermalon.util.Render;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;

public final class SpotifyElement extends HudElement {
    public SpotifyElement() { super("spotify", "Spotify", 0.01f, 0.42f); }

    @Override
    public boolean available() {
        SpotifyHud module = Watermalon.modules().spotify;
        if (!module.enabled()) return false;
        return !(module.hideIdle.get() && Spotify.connected() && !Spotify.playback().active());
    }

    @Override public int width() { return 150; }
    @Override public int height() { return Watermalon.modules().spotify.artwork.get() ? 38 : 32; }

    @Override
    public void render(GuiGraphics g, boolean editing) {
        SpotifyHud module = Watermalon.modules().spotify;
        Spotify.Playback playback = Spotify.playback();
        int w = width(), h = height();
        HudManager.card(g, w, h);
        boolean art = module.artwork.get();
        int textX = 6;
        if (art) {
            if (Spotify.artReady()) {
                g.blit(RenderPipelines.GUI_TEXTURED, Spotify.ART, 4, 4, 0f, 0f, 30, 30, 64, 64, 64, 64);
            } else {
                Render.round(g, 4, 4, 30, 30, 3, 0xFF1C2324);
                Render.circle(g, 19, 19, 6, Theme.accent(0.35f));
                Render.circle(g, 19, 19, 2, 0xFF1C2324);
            }
            textX = 40;
        }
        int textW = w - textX - 6;
        String overline;
        String title;
        String artist;
        if (!Spotify.connected()) {
            overline = "SPOTIFY";
            title = "Not connected";
            artist = "Open ClickGUI > SpotifyHUD";
        } else if (!playback.active()) {
            overline = "SPOTIFY";
            title = "Nothing playing";
            artist = Spotify.status();
        } else {
            overline = playback.playing() ? "NOW PLAYING" : "PAUSED";
            title = playback.title();
            artist = playback.artist();
        }
        Render.drawScaled(g, overline, textX, 4, 0.7f, Theme.MUTED);
        Render.circle(g, w - 8, 7, 3, 0xFF72E6AC);
        Render.draw(g, Render.trim(title, textW), textX, 11, Theme.TEXT);
        Render.drawScaled(g, Render.trim(artist, (int) (textW / 0.75f)), textX, 21, 0.75f, Theme.MUTED);
        if (module.progress.get() && playback.active() && playback.durationMs() > 0) {
            int barY = h - 5;
            Render.round(g, textX, barY, textW, 2, 1, 0xFF3A4540);
            int filled = Math.round(textW * (playback.positionNow() / (float) playback.durationMs()));
            Render.round(g, textX, barY, Math.max(2, filled), 2, 1, Theme.accent());
        }
    }
}
