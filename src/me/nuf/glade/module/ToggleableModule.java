package me.nuf.glade.module;

import me.nuf.api.interfaces.Buggy;
import me.nuf.api.interfaces.Toggleable;
import me.nuf.glade.core.Glade;
import me.nuf.glade.keybind.Keybind;
import me.nuf.glade.keybind.actions.ModuleToggleAction;
import me.nuf.glade.printing.Printer;
import me.nuf.subjectapi.Listener;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuf on 3/19/2016.
 */
public class ToggleableModule extends Module implements Toggleable {

    private final List<Listener> listeners = new ArrayList<>();

    private boolean enabled;
    private boolean drawn;

    private int color;

    private final Category category;

    public ToggleableModule(String[] aliases, boolean drawn, int color, Category category) {
        super(aliases);
        this.color = color;
        this.category = category;
        this.drawn = drawn;
        Glade.getInstance().getKeybindManager().register(new Keybind(String.format("%sToggle", getAliases()[0].toLowerCase().replace(" ", "")), new ModuleToggleAction(this), Keyboard.KEY_NONE));
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;

        if (this.enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    @Override
    public void toggle() {
        setEnabled(!enabled);
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public final Category getCategory() {
        return category;
    }

    public boolean isDrawn() {
        return drawn;
    }

    public void setDrawn(boolean drawn) {
        this.drawn = drawn;
    }

    protected void onEnable() {
        listeners.forEach(listener -> Glade.getInstance().getSubjectManager().register(listener));
        if (getClass().isAnnotationPresent(Buggy.class)) {
            Buggy buggy = getClass().getAnnotation(Buggy.class);
            Printer.getPrinter().printToChat(String.format("&cNOTE&7 %s", buggy.reason()));
        }
    }

    protected void onDisable() {
        listeners.forEach(listener -> Glade.getInstance().getSubjectManager().unregister(listener));
    }

    protected void addListeners(Listener... listeners) {
        for (Listener listener : listeners)
            this.listeners.add(listener);
    }
}
