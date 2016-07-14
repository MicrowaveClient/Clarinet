package me.nuf.glade.command.impl.client;

import me.nuf.glade.command.Command;
import me.nuf.glade.core.Glade;

import java.util.StringJoiner;

/**
 * Created by nuf on 3/22/2016.
 */
public final class Help extends Command {
    public Help() {
        super(new String[]{"help", "halp", "autist"});
    }

    @Override
    public String dispatch() {
        StringJoiner stringJoiner = new StringJoiner(", ");
        Glade.getInstance().getCommandManager().getElements().forEach(command -> stringJoiner.add(command.getAliases()[0]));
        return String.format("Commands (%s) %s.", Glade.getInstance().getCommandManager().getElements().size(), stringJoiner.toString());
    }
}
