package dev.watermalon.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class BoolSetting extends Setting<Boolean> {
    public BoolSetting(String name, String description, boolean value) { super(name, description, value); }
    public void toggle() { value = !value; }
    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement element) { if (element.isJsonPrimitive()) value = element.getAsBoolean(); }
}
