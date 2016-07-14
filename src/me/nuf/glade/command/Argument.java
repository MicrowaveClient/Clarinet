package me.nuf.glade.command;

import me.nuf.api.interfaces.Labeled;

/**
 * Created by nuf on 3/20/2016.
 */
public class Argument implements Labeled {

    private final Class<?> type;
    private final String label;
    private String value;
    private boolean present;

    public Argument(Class<?> type, String label) {
        this.type = type;
        this.label = label;
    }

    public final Class<?> getType() {
        return type;
    }

    @Override
    public final String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isPresent() {
        return present;
    }

    public void setPresent(boolean present) {
        this.present = present;
    }
}
