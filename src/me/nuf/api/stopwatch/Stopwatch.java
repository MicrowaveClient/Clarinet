package me.nuf.api.stopwatch;

/**
 * Created by nuf on 3/19/2016.
 */
public class Stopwatch {

    private long previousMS;

    public Stopwatch() {
        reset();
    }

    public boolean hasReached(float milliseconds) {
        return getCurrentMS() - previousMS >= milliseconds;
    }

    public void reset() {
        previousMS = getCurrentMS();
    }

    public long getPreviousMS() {
        return previousMS;
    }

    public long getCurrentMS() {
        return System.nanoTime() / 1000000;
    }

}
