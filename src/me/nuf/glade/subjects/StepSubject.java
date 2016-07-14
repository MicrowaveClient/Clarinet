package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;

/**
 * Created by nuf on 3/28/2016.
 */
public class StepSubject extends Subject {
    private float stepHeight;
    private Time time;

    public StepSubject(Time time, float stepHeight) {
        this.time = time;
        this.stepHeight = stepHeight;
    }

    public float getStepHeight() {
        return stepHeight;
    }

    public void setStepHeight(float stepHeight) {
        this.stepHeight = stepHeight;
    }

    public Time getTime() {
        return time;
    }

    public enum Time {
        PRE, POST
    }
}
