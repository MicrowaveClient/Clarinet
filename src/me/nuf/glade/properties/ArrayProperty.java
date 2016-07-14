package me.nuf.glade.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuf on 3/19/2016.
 */
public class ArrayProperty<T> extends Property<T> {

    private final List<T> items = new ArrayList<>();

    public ArrayProperty(T value, String... aliases) {
        super(value, aliases);
    }

    public ArrayProperty(String... aliases) {
        super(null, aliases);
    }

    @Override
    public void setValue(T value) {
        if (items.contains(value)) {
            items.remove(value);
        } else {
            items.add(value);
        }
    }
}
