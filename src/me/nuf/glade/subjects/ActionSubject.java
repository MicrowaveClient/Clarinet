package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;
import org.lwjgl.input.Keyboard;

/**
 * Created by nuf on 3/19/2016.
 */
public class ActionSubject extends Subject {

    private int key;
    private final Type type;

    public ActionSubject(Type type) {
        key = Keyboard.getEventKey();
        this.type = type;
    }

    public final Type getType() {
        return type;
    }

    public int getKey() {
        return key;
    }

    public enum Type {
        KEY_PRESS, LEFT_CLICK, RIGHT_CLICK, MIDDLE_CLICK
    }

}
