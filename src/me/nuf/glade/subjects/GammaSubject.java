package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;

/**
 * Created by nuf on 3/24/2016.
 */
public class GammaSubject extends Subject {
    private float gamma;

    public GammaSubject(float gamma) {
        this.gamma = gamma;
    }

    public float getGamma() {
        return gamma;
    }

    public void setGamma(float gamma) {
        this.gamma = gamma;
    }
}
