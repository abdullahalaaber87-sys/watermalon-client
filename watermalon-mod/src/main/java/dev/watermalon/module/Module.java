package dev.watermalon.module;

import dev.watermalon.module.setting.Setting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;

public abstract class Module {
    private final String name;
    private final String description;
    private final Category category;
    private final List<Setting<?>> settings = new ArrayList<>();
    private boolean enabled;
    private int key = -1;
    /** 0..1, eased toward the enabled state for GUI and arraylist animation. */
    public float anim;

    protected Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    protected <S extends Setting<?>> S add(S setting) {
        settings.add(setting);
        return setting;
    }

    public String name() { return name; }
    public String description() { return description; }
    public Category category() { return category; }
    public List<Setting<?>> settings() { return Collections.unmodifiableList(settings); }
    public boolean enabled() { return enabled; }
    public int key() { return key; }
    public void setKey(int key) { this.key = key; }

    public void toggle() { setEnabled(!enabled); }

    public void setEnabled(boolean value) {
        if (value == enabled) return;
        enabled = value;
        if (value) onEnable(); else onDisable();
    }

    /** Hold modules act while their key is held instead of toggling. */
    public boolean holdKey() { return false; }
    public boolean showInArrayList() { return category != Category.CLIENT; }
    /** Modules that cannot be switched off, such as the click GUI itself. */
    public boolean canToggle() { return true; }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick(Minecraft mc) {}
}
