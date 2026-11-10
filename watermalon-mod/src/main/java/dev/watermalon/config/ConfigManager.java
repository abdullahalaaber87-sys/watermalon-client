package dev.watermalon.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.watermalon.Watermalon;
import dev.watermalon.gui.Theme;
import dev.watermalon.gui.WindowState;
import dev.watermalon.hud.HudElement;
import dev.watermalon.module.Module;
import dev.watermalon.module.setting.Setting;
import dev.watermalon.spotify.Spotify;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import net.fabricmc.loader.api.FabricLoader;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigManager() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("watermalon.json");
    }

    public static void load() {
        Path path = file();
        if (!Files.exists(path)) return;
        try {
            JsonObject root = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject modules = object(root, "modules");
            if (modules != null) {
                for (Module module : Watermalon.modules().all()) {
                    JsonObject entry = object(modules, module.name());
                    if (entry == null) continue;
                    if (entry.has("key")) module.setKey(entry.get("key").getAsInt());
                    JsonObject settings = object(entry, "settings");
                    if (settings != null) {
                        for (Setting<?> setting : module.settings()) {
                            if (settings.has(setting.name())) setting.load(settings.get(setting.name()));
                        }
                    }
                    if (entry.has("enabled") && module.canToggle()) module.setEnabled(entry.get("enabled").getAsBoolean());
                }
            }
            JsonObject hud = object(root, "hud");
            if (hud != null) {
                for (HudElement element : Watermalon.hud().elements()) {
                    JsonObject entry = object(hud, element.id());
                    if (entry == null) continue;
                    element.fx = clamp(entry.get("fx").getAsFloat());
                    element.fy = clamp(entry.get("fy").getAsFloat());
                    if (entry.has("scale")) element.scale = Math.max(0.5f, Math.min(2f, entry.get("scale").getAsFloat()));
                    if (entry.has("shown")) element.shown = entry.get("shown").getAsBoolean();
                }
            }
            JsonObject theme = object(root, "theme");
            if (theme != null) {
                Theme.accentIndex = intOr(theme, "accent", Theme.accentIndex);
                Theme.opacity = intOr(theme, "opacity", Theme.opacity);
                Theme.radius = intOr(theme, "radius", Theme.radius);
                Theme.blur = !theme.has("blur") || theme.get("blur").getAsBoolean();
                Theme.interFont = !theme.has("inter") || theme.get("inter").getAsBoolean();
            }
            JsonObject window = object(root, "window");
            if (window != null) {
                WindowState.x = window.get("x").getAsFloat();
                WindowState.y = window.get("y").getAsFloat();
                WindowState.w = window.get("w").getAsInt();
                WindowState.h = window.get("h").getAsInt();
                WindowState.scale = Math.max(0.6f, Math.min(1.6f, window.get("scale").getAsFloat()));
            }
            JsonObject spotify = object(root, "spotify");
            String clientId = Watermalon.modules().spotify.clientId.get();
            Spotify.restore(clientId, spotify != null && spotify.has("refresh") ? spotify.get("refresh").getAsString() : "");
        } catch (Exception failure) {
            Watermalon.LOGGER.warn("Could not read watermalon.json, using defaults", failure);
        }
    }

    public static synchronized void save() {
        JsonObject root = new JsonObject();
        JsonObject modules = new JsonObject();
        for (Module module : Watermalon.modules().all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.enabled());
            entry.addProperty("key", module.key());
            JsonObject settings = new JsonObject();
            for (Setting<?> setting : module.settings()) {
                JsonElement value = setting.save();
                if (value != null) settings.add(setting.name(), value);
            }
            entry.add("settings", settings);
            modules.add(module.name(), entry);
        }
        root.add("modules", modules);

        JsonObject hud = new JsonObject();
        for (HudElement element : Watermalon.hud().elements()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("fx", element.fx);
            entry.addProperty("fy", element.fy);
            entry.addProperty("scale", element.scale);
            entry.addProperty("shown", element.shown);
            hud.add(element.id(), entry);
        }
        root.add("hud", hud);

        JsonObject theme = new JsonObject();
        theme.addProperty("accent", Theme.accentIndex);
        theme.addProperty("opacity", Theme.opacity);
        theme.addProperty("radius", Theme.radius);
        theme.addProperty("blur", Theme.blur);
        theme.addProperty("inter", Theme.interFont);
        root.add("theme", theme);

        JsonObject window = new JsonObject();
        window.addProperty("x", WindowState.x);
        window.addProperty("y", WindowState.y);
        window.addProperty("w", WindowState.w);
        window.addProperty("h", WindowState.h);
        window.addProperty("scale", WindowState.scale);
        root.add("window", window);

        JsonObject spotify = new JsonObject();
        spotify.addProperty("refresh", Spotify.savedRefreshToken());
        root.add("spotify", spotify);

        try {
            Path path = file();
            Path temp = path.resolveSibling("watermalon.json.tmp");
            Files.writeString(temp, GSON.toJson(root), StandardCharsets.UTF_8);
            Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (Exception failure) {
            Watermalon.LOGGER.warn("Could not save watermalon.json", failure);
        }
    }

    private static JsonObject object(JsonObject parent, String key) {
        return parent.has(key) && parent.get(key).isJsonObject() ? parent.getAsJsonObject(key) : null;
    }

    private static int intOr(JsonObject parent, String key, int fallback) {
        return parent.has(key) ? parent.get(key).getAsInt() : fallback;
    }

    private static float clamp(float value) {
        return Math.max(0f, Math.min(1f, value));
    }
}
