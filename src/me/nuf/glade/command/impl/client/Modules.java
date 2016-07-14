package me.nuf.glade.command.impl.client;

import me.nuf.api.interfaces.Toggleable;
import me.nuf.glade.command.Command;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Module;
import me.nuf.glade.module.ToggleableModule;

import java.util.List;
import java.util.StringJoiner;

/**
 * Created by nuf on 3/22/2016.
 */
public final class Modules extends Command {
    public Modules() {
        super(new String[]{"modules", "mods", "lm", "ml", "list"});
    }

    @Override
    public String dispatch() {
        List<Module> modules = Glade.getInstance().getModuleManager().getElements();

        modules.sort((mod1, mod2) -> mod1.getAliases()[0].compareTo(mod2.getAliases()[0]));

        StringJoiner toggleableModuleJoiner = new StringJoiner(", ");
        StringJoiner moduleJoiner = new StringJoiner(", ");
        modules.forEach(module -> {
            if (module instanceof Toggleable) {
                ToggleableModule toggleableModule = (ToggleableModule) module;
                toggleableModuleJoiner.add(String.format("%s%s&7", toggleableModule.isEnabled() ? "&a" : "&c", toggleableModule.getAliases()[0]));
            } else {
                moduleJoiner.add(module.getAliases()[0]);
            }
        });
        return String.format("Modules (%s) %s, %s.", modules.size(), moduleJoiner.toString(), toggleableModuleJoiner.toString());
    }
}
