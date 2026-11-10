package dev.watermalon.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class TextSetting extends Setting<String> {
    private final int maxLength;

    public TextSetting(String name, String description, String value, int maxLength) {
        super(name, description, value);
        this.maxLength = maxLength;
    }

    public int maxLength() { return maxLength; }
    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement element) { if (element.isJsonPrimitive()) value = element.getAsString(); }
}
