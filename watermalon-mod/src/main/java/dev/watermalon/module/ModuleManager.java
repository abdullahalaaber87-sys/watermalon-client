package dev.watermalon.module;

import com.mojang.blaze3d.platform.InputConstants;
import dev.watermalon.module.impl.AutoWalk;
import dev.watermalon.module.impl.CoordSnapper;
import dev.watermalon.module.impl.CustomCrosshair;
import dev.watermalon.module.impl.FullBright;
import dev.watermalon.module.impl.HudModule;
import dev.watermalon.module.impl.SpotifyHud;
import dev.watermalon.module.impl.WeatherNotifier;
import dev.watermalon.module.impl.Zoom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;

public final class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    private final Map<Module, Boolean> wasDown = new HashMap<>();

    public final FullBright fullBright = add(new FullBright());
    public final CustomCrosshair crosshair = add(new CustomCrosshair());
    public final Zoom zoom = add(new Zoom());
    public final AutoWalk autoWalk = add(new AutoWalk());
    public final WeatherNotifier weather = add(new WeatherNotifier());
    public final CoordSnapper coordSnapper = add(new CoordSnapper());
    public final HudModule hud = add(new HudModule());
    public final SpotifyHud spotify = add(new SpotifyHud());

    private <M extends Module> M add(M module) {
        modules.add(module);
        return module;
    }

    public List<Module> all() { return modules; }

    public Module byName(String name) {
        for (Module module : modules) if (module.name().equalsIgnoreCase(name)) return module;
        return null;
    }

    public void tick(Minecraft mc) {
        boolean inGame = mc.screen == null && mc.player != null;
        for (Module module : modules) {
            if (module.key() < 0) continue;
            boolean down = inGame && InputConstants.isKeyDown(mc.getWindow(), module.key());
            boolean was = wasDown.getOrDefault(module, false);
            wasDown.put(module, down);
            if (!module.holdKey() && module.canToggle() && down && !was) module.toggle();
        }
        for (Module module : modules) {
            if (module.enabled()) module.onTick(mc);
        }
    }
}
