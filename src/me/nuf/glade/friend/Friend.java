package me.nuf.glade.friend;

import me.nuf.api.interfaces.Labeled;

/**
 * Created by nuf on 3/20/2016.
 */
public class Friend implements Labeled {

    private final String label, alias;

    public Friend(String label, String alias) {
        this.label = label;
        this.alias = alias;
    }

    public final String getAlias() {
        return alias;
    }

    @Override
    public final String getLabel() {
        return label;
    }
}
