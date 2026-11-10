package dev.watermalon.module.setting;

import com.google.gson.JsonElement;
import java.util.function.Supplier;

/** A button in the settings list. The label can change, e.g. "Connect" vs "Connected". */
public final class ActionSetting extends Setting<Runnable> {
    private final Supplier<String> label;

    public ActionSetting(String name, String description, Supplier<String> label, Runnable action) {
        super(name, description, action);
        this.label = label;
    }

    public String label() { return label.get(); }
    public void run() { value.run(); }
    @Override public JsonElement save() { return null; }
    @Override public void load(JsonElement element) {}
}
