package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;

/**
 * Created by nuf on 3/21/2016.
 */
public class ItemUseSubject extends Subject {

    private float speed;

    public ItemUseSubject(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }
}
