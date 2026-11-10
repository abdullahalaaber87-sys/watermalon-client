package dev.watermalon.module.setting;

import com.google.gson.JsonElement;

public abstract class Setting<T> {
    private final String name;
    private final String description;
    protected T value;
    private final T defaultValue;

    protected Setting(String name, String description, T value) {
        this.name = name;
        this.description = description;
        this.value = value;
        this.defaultValue = value;
    }

    public String name() { return name; }
    public String description() { return description; }
    public T get() { return value; }
    public void set(T value) { this.value = value; }
    public void reset() { this.value = defaultValue; }

    /** Settings that only store state, such as actions, return null. */
    public abstract JsonElement save();
    public abstract void load(JsonElement element);
}
