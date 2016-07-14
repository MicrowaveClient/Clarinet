package me.nuf.subjectapi;

/**
 * Created by nuf on 2/27/2016.
 */
public class Subject {

    private boolean cancelled = false;

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
