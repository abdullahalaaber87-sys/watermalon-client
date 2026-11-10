package dev.watermalon.module.impl;

import dev.watermalon.module.Category;
import dev.watermalon.module.Module;
import dev.watermalon.module.setting.ActionSetting;
import dev.watermalon.module.setting.BoolSetting;
import dev.watermalon.module.setting.TextSetting;
import dev.watermalon.spotify.Spotify;
import net.minecraft.client.Minecraft;

public final class SpotifyHud extends Module {
    public final TextSetting clientId = add(new TextSetting("Client ID", "From developer.spotify.com", "", 64));
    public final ActionSetting connect = add(new ActionSetting("Account", "Log in with your browser", Spotify::statusLabel, () -> Spotify.connect(clientId.get())));
    public final BoolSetting artwork = add(new BoolSetting("Artwork", "Show album art", true));
    public final BoolSetting progress = add(new BoolSetting("Progress", "Show the progress bar", true));
    public final BoolSetting hideIdle = add(new BoolSetting("Hide when idle", "Hide the card when nothing plays", false));
    public final ActionSetting previous = add(new ActionSetting("Previous", "Needs Spotify Premium", () -> "Previous", () -> Spotify.command("previous")));
    public final ActionSetting pause = add(new ActionSetting("Play / pause", "Needs Spotify Premium", () -> Spotify.playback().playing() ? "Pause" : "Play", () -> Spotify.command("toggle")));
    public final ActionSetting next = add(new ActionSetting("Next", "Needs Spotify Premium", () -> "Next", () -> Spotify.command("next")));
    private int ticks;

    public SpotifyHud() {
        super("SpotifyHUD", "Now playing card for the HUD.", Category.CLIENT);
        setEnabled(true);
    }

    @Override
    public void onTick(Minecraft mc) {
        if (++ticks % 80 == 0) Spotify.poll();
    }
}
