package me.nuf.glade.keybind;

import me.nuf.api.interfaces.Labeled;

/**
 * Created by nuf on 3/19/2016.
 */
public class Keybind implements Labeled {

    private final String label;
    private final Action action;
    private int key;

    public Keybind(String label, Action action, int key) {
        this.label = label;
        this.action = action;
        this.key = key;
    }

    @Override
    public final String getLabel() {
        return label;
    }

    public final Action getAction() {
        return action;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }
}
