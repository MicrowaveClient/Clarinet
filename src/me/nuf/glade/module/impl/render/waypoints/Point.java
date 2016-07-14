package me.nuf.glade.module.impl.render.waypoints;

/**
 * Created by nuf on 4/4/2016.
 */
public class Point {
    private int x, y, z;

    public Point(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }
}
