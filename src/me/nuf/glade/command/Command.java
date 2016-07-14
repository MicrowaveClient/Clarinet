package me.nuf.glade.command;

import me.nuf.glade.core.Glade;
import net.minecraft.client.Minecraft;

import java.util.StringJoiner;

/**
 * Created by nuf on 3/20/2016.
 */
public abstract class Command {

    private final String[] aliases;
    private final Argument[] arguments;

    protected Minecraft minecraft = Minecraft.getMinecraft();

    public Command(String[] aliases, Argument... arguments) {
        this.arguments = arguments;
        this.aliases = aliases;
    }

    public final String[] getAliases() {
        return aliases;
    }

    public final Argument[] getArguments() {
        return arguments;
    }

    public String dispatch(String[] input) {
        Argument[] arguments = getArguments();
        boolean valid = false;
        if (input.length < arguments.length) {
            return String.format("%s%s %s", !input[0].startsWith(".") ? Glade.getInstance().getCommandManager().getPrefix() : "", input[0],
                    getSyntax());
        } else if ((input.length - 1) > arguments.length) {
            return "Too many arguments inserted.";
        }
        if (arguments.length > 0) {
            for (int index = 0; index < arguments.length; index++) {
                Argument argument = arguments[index];
                argument.setPresent(index < input.length);
                argument.setValue(input[index + 1]);
                valid = argument.isPresent();
            }
        } else {
            valid = true;
        }
        return valid ? dispatch() : "Invalid command argument(s).";
    }

    public Argument getArgument(String label) {
        for (Argument argument : arguments)
            if (argument.getLabel().equalsIgnoreCase(label))
                return argument;
        return null;
    }

    public String getSyntax() {
        StringJoiner stringJoiner = new StringJoiner(" ");
        for (Argument argument : arguments)
            stringJoiner.add(String.format("%s&e[%s]&7", argument.getLabel(), argument.getType().getSimpleName()));
        return stringJoiner.toString();
    }

    public abstract String dispatch();
}
