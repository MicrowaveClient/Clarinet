package me.nuf.glade.module;

/**
 * Created by nuf on 3/19/2016.
 */
public enum Category {

    COMBAT("Combat"), EXPLOITS("Exploits"), MISCELLANEOUS("Miscellaneous"), MOVEMENT("Movement"), PLUGINS("Plugins"), RENDER("Render"), WORLD("World");

    private final String label;

    Category(String label) {
        this.label = label;
    }

    public final String getLabel() {
        return label;
    }

}
