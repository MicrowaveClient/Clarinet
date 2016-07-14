package me.nuf.glade.command.impl.client;

import me.nuf.api.interfaces.Toggleable;
import me.nuf.glade.command.Argument;
import me.nuf.glade.command.Command;
import me.nuf.glade.core.Glade;
import me.nuf.glade.macro.Macro;
import me.nuf.glade.module.Module;
import me.nuf.glade.module.ToggleableModule;
import org.lwjgl.input.Keyboard;

/**
 * Created by nuf on 3/20/2016.
 */
public final class Keybind extends Command {
    public Keybind() {
        super(new String[]{"keybind", "bind", "kb"}, new Argument(String.class, "action"), new Argument(String.class, "label"), new Argument(String.class, "key"));
    }

    @Override
    public String dispatch() {
        String action = getArgument("action").getValue();
        String label = getArgument("label").getValue();
        String key = getArgument("key").getValue().toUpperCase();

        if (action.equalsIgnoreCase("module")) {
            Module module = Glade.getInstance().getModuleManager().getModuleByAlias(label);

            if (module == null)
                return "That module does not exist.";

            if (!(module instanceof Toggleable))
                return "That module is not toggleable.";

            ToggleableModule toggleableModule = (ToggleableModule) module;
            if (!key.equalsIgnoreCase("none")) {
                Glade.getInstance().getKeybindManager().getKeybindByLabel(String.format("%sToggle", label)).setKey(Keyboard.getKeyIndex(key));
            } else {
                Glade.getInstance().getKeybindManager().getKeybindByLabel(String.format("%sToggle", label)).setKey(Keyboard.KEY_NONE);
            }
            return String.format("%s keybind has been set to %s.", toggleableModule.getAliases()[0], Keyboard.getKeyName(Keyboard.getKeyIndex(key)));
        } else if (action.equalsIgnoreCase("macro")) {
            if (Glade.getInstance().getMacroManager().isMacro(Keyboard.getKeyIndex(key))) {
                Glade.getInstance().getMacroManager().remove(Keyboard.getKeyIndex(key));
                return String.format("Removed macro with the keybind %s.", Keyboard.getKeyName(Keyboard.getKeyIndex(key)));
            }

            Glade.getInstance().getMacroManager().register(new Macro(Keyboard.getKeyIndex(key), label));
            return String.format("Added a macro with the keybind %s.", Keyboard.getKeyName(Keyboard.getKeyIndex(key)));
        } else {
            return "Invalid action.";
        }
    }
}
