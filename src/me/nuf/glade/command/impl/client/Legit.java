package me.nuf.glade.command.impl.client;

import me.nuf.glade.command.Command;

/**
 * Created by nuf on 4/6/2016.
 */
public final class Legit extends Command {
    public Legit() {
        super(new String[]{"legit"});
    }

    @Override
    public String dispatch() {
        return "me";
    }
}
