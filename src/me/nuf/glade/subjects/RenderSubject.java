package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;

/**
 * Created by nuf on 3/23/2016.
 */
public class RenderSubject extends Subject {

    private float partialTicks;

    public RenderSubject(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    public float getPartialTicks() {
        return partialTicks;
    }
}
