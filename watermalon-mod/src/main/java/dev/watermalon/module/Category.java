package dev.watermalon.module;

public enum Category {
    RENDER("Render"),
    MOVEMENT("Movement"),
    WORLD("World"),
    CLIENT("Client");

    private final String label;

    Category(String label) { this.label = label; }

    public String label() { return label; }
}
