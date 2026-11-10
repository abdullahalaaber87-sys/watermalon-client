package dev.watermalon.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import java.util.List;

public final class ModeSetting extends Setting<String> {
    private final List<String> modes;

    public ModeSetting(String name, String description, String value, String... modes) {
        super(name, description, value);
        this.modes = List.of(modes);
    }

    public List<String> modes() { return modes; }
    public boolean is(String mode) { return value.equals(mode); }

    public void cycle(int direction) {
        int index = modes.indexOf(value);
        value = modes.get(Math.floorMod(index + direction, modes.size()));
    }

    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement element) {
        if (element.isJsonPrimitive() && modes.contains(element.getAsString())) value = element.getAsString();
    }
}
