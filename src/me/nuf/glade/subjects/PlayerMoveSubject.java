package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;
import net.minecraft.client.Minecraft;

/**
 * Created by nuf on 3/23/2016.
 */
public class PlayerMoveSubject extends Subject {
    private double x, y, z;
    private boolean safety;

    public PlayerMoveSubject(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        if (Minecraft.getMinecraft().thePlayer != null)
            safety = Minecraft.getMinecraft().thePlayer.isSneaking();
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
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

    public void setZ(double z) {
        this.z = z;
    }

    public boolean isSafety() {
        return safety;
    }

    public void setSafety(boolean safety) {
        this.safety = safety;
    }
}
