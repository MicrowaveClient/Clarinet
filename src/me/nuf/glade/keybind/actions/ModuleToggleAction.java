package me.nuf.glade.keybind.actions;

import me.nuf.glade.keybind.Action;
import me.nuf.glade.module.ToggleableModule;

/**
 * Created by nuf on 3/19/2016.
 */
public class ModuleToggleAction extends Action {

    private final ToggleableModule toggleableModule;

    public ModuleToggleAction(ToggleableModule toggleableModule) {
        this.toggleableModule = toggleableModule;
    }

    @Override
    public void dispatch() {
        toggleableModule.toggle();
    }

    public final ToggleableModule getToggleableModule() {
        return toggleableModule;
    }
}
