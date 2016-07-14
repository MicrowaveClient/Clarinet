package me.nuf.glade.macro;

import me.nuf.api.management.ListManager;
import me.nuf.glade.core.Glade;
import me.nuf.glade.subjects.ActionSubject;
import me.nuf.subjectapi.Listener;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

/**
 * Created by nuf on 3/20/2016.
 */
public final class MacroManager extends ListManager<Macro> {
    public MacroManager() {
        elements = new ArrayList<>();
        Glade.getInstance().getSubjectManager().register(new Listener<ActionSubject>("main_macro_action_listener") {
            @Override
            public void call(ActionSubject subject) {
                if (subject.getType() == ActionSubject.Type.KEY_PRESS)
                    getElements().forEach(macro -> {
                        if (macro.getKey() != Keyboard.KEY_NONE && macro.getKey() == subject.getKey())
                            macro.dispatch();
                    });
            }
        });
    }

    public Macro getMacroByKey(int key) {
        for (Macro macro : elements)
            if (key == macro.getKey())
                return macro;
        return null;
    }

    public boolean isMacro(int key) {
        for (Macro macro : elements)
            if (key == macro.getKey())
                return true;
        return false;
    }

    public void remove(int key) {
        Macro macro = getMacroByKey(key);
        if (macro != null)
            elements.remove(macro);
    }
}
