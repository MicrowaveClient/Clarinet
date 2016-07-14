package me.nuf.glade.command.impl.client;

import me.nuf.glade.command.Argument;
import me.nuf.glade.command.Command;
import me.nuf.glade.core.Glade;

/**
 * Created by nuf on 3/22/2016.
 */
public final class Prefix extends Command {
    public Prefix() {
        super(new String[]{"prefix", "pref"}, new Argument(Character.class, "char"));
    }

    @Override
    public String dispatch() {
        String prefix = getArgument("char").getValue();
        if (prefix.equalsIgnoreCase(Glade.getInstance().getCommandManager().getPrefix()))
            return "That is already your prefix.";

        Glade.getInstance().getCommandManager().setPrefix(prefix);
        return String.format("Command prefix set to %s.", prefix);
    }
}
