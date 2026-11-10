package dev.watermalon.module.setting;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

public final class NumberSetting extends Setting<Double> {
    private final double min, max, step;

    public NumberSetting(String name, String description, double value, double min, double max, double step) {
        super(name, description, value);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public double min() { return min; }
    public double max() { return max; }
    public double step() { return step; }
    public float getFloat() { return value.floatValue(); }
    public int getInt() { return (int) Math.round(value); }

    @Override
    public void set(Double next) {
        double snapped = Math.round(next / step) * step;
        value = Math.max(min, Math.min(max, Math.round(snapped * 1000.0) / 1000.0));
    }

    public String display() {
        return step >= 1 ? Integer.toString(getInt()) : String.format(java.util.Locale.ROOT, step >= 0.1 ? "%.1f" : "%.2f", value);
    }

    @Override public JsonElement save() { return new JsonPrimitive(value); }
    @Override public void load(JsonElement element) { if (element.isJsonPrimitive()) set(element.getAsDouble()); }
}
