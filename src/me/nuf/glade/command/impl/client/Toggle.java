package me.nuf.glade.command.impl.client;

import me.nuf.api.interfaces.Toggleable;
import me.nuf.glade.command.Argument;
import me.nuf.glade.command.Command;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Module;
import me.nuf.glade.module.ToggleableModule;

import java.util.StringJoiner;

/**
 * Created by nuf on 3/20/2016.
 */
public final class Toggle extends Command {
    public Toggle() {
        super(new String[]{"toggle", "t"}, new Argument(String.class, "module"));
    }

    @Override
    public String dispatch() {
        String moduleInput = getArgument("module").getValue();
        Module module = Glade.getInstance().getModuleManager().getModuleByAlias(moduleInput);

        if (module == null) {
            StringJoiner stringJoiner = new StringJoiner(", ");
            for (Module mod : Glade.getInstance().getModuleManager().getElements())
                for (String alias : mod.getAliases())
                    if (alias.contains(moduleInput))
                        stringJoiner.add(String.format("&e%s&7", alias));

            if (stringJoiner.length() < 1) {
                return "That module does not exist.";
            } else {
                return String.format("Did you mean: %s?", stringJoiner.toString());
            }
        }

        if (!(module instanceof Toggleable))
            return "That module is not toggleable.";

        ToggleableModule toggleableModule = (ToggleableModule) module;
        toggleableModule.toggle();
        return String.format("%s toggled %s&7.", toggleableModule.getAliases()[0], toggleableModule.isEnabled() ? "&aon" : "&coff");
    }
}
