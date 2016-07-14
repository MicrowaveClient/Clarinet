package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;

/**
 * Created by nuf on 3/19/2016.
 */
public class MotionUpdateSubject extends Subject {

    private final Time time;

    private float rotationYaw, rotationPitch, oldRotationYaw, oldRotationPitch;

    private double x, originalX, y, originalY, z, originalZ;

    private boolean onGround;

    public MotionUpdateSubject(Time time, float rotationYaw, float rotationPitch, double x, double y, double z, boolean onGround) {
        this.time = time;
        this.rotationYaw = oldRotationYaw = rotationYaw;
        this.rotationPitch = oldRotationPitch = rotationPitch;
        this.x = originalX = x;
        this.y = originalY = y;
        this.z = originalZ = z;
        this.onGround = onGround;
    }

    public MotionUpdateSubject(Time time) {
        this.time = time;
    }

    public float getRotationYaw() {
        return rotationYaw;
    }

    public void setRotationYaw(float rotationYaw) {
        this.rotationYaw = rotationYaw;
    }

    public float getRotationPitch() {
        return rotationPitch;
    }

    public void setRotationPitch(float rotationPitch) {
        this.rotationPitch = rotationPitch;
    }

    public float getOldRotationYaw() {
        return oldRotationYaw;
    }

    public float getOldRotationPitch() {
        return oldRotationPitch;
    }

    public double getX() {
        return x;
    }

    public double getOriginalY() {
        return originalY;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public final Time getTime() {
        return time;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }

    public enum Time {
        PRE, POST
    }

}
