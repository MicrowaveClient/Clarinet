package me.nuf.api.interfaces;

/**
 * Created by nuf on 3/19/2016.
 */
public interface Toggleable {

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void toggle();

}
