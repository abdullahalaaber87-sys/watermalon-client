package dev.watermalon;

import com.mojang.blaze3d.platform.InputConstants;
import dev.watermalon.config.ConfigManager;
import dev.watermalon.gui.ClickGuiScreen;
import dev.watermalon.hud.HudManager;
import dev.watermalon.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Watermalon implements ClientModInitializer {
    public static final String ID = "watermalon";
    public static final Logger LOGGER = LoggerFactory.getLogger("Watermalon");

    private static ModuleManager modules;
    private static HudManager hud;
    private static KeyMapping openGui;

    public static ModuleManager modules() { return modules; }
    public static HudManager hud() { return hud; }
    public static KeyMapping openGuiKey() { return openGui; }

    public static void saveConfig() {
        if (modules != null && hud != null) ConfigManager.save();
    }

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        hud = new HudManager();
        ConfigManager.load();

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(ID, "main"));
        openGui = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.watermalon.open_gui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.consumeClick()) {
                if (client.screen == null) client.setScreen(new ClickGuiScreen());
            }
            modules.tick(client);
        });

        HudElementRegistry.attachElementBefore(VanillaHudElements.CHAT, Identifier.fromNamespaceAndPath(ID, "hud"), (graphics, tick) -> hud.renderInGame(graphics));
        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, original -> (graphics, tick) -> {
            if (!modules.crosshair.render(graphics)) original.render(graphics, tick);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveConfig());
        LOGGER.info("Watermalon loaded with {} modules", modules.all().size());
    }
}
