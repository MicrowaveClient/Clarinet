package me.nuf.glade.keybind;

import me.nuf.api.management.ListManager;
import me.nuf.glade.printing.Printer;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Created by nuf on 3/19/2016.
 */
public final class KeybindManager extends ListManager<Keybind> {

    public KeybindManager() {
        elements = new ArrayList<>();
    }

    @Override
    public void register(Keybind keybind) {
        Printer.getPrinter().print(Level.INFO, String.format("Added keybind %s.", keybind.getLabel()));
        super.register(keybind);
    }

    public Keybind getKeybindByLabel(String label) {
        for (Keybind keybind : elements)
            if (label.equalsIgnoreCase(keybind.getLabel()))
                return keybind;
        return null;
    }
}
