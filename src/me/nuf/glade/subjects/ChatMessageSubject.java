package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;

/**
 * Created by nuf on 3/20/2016.
 */
public class ChatMessageSubject extends Subject {
    private String text;

    public ChatMessageSubject(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
