package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;
import net.minecraft.util.EnumFacing;

/**
 * Created by nuf on 3/27/2016.
 */
public class DamageBlockSubject extends Subject {
    private int x, y, z, blockHitDelay;
    private float curBlockDamageMP;
    private EnumFacing enumFacing;

    public DamageBlockSubject(int x, int y, int z, int blockHitDelay, float curBlockDamageMP, EnumFacing enumFacing) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.blockHitDelay = blockHitDelay;
        this.curBlockDamageMP = curBlockDamageMP;
        this.enumFacing = enumFacing;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getZ() {
        return z;
    }

    public void setZ(int z) {
        this.z = z;
    }

    public EnumFacing getEnumFacing() {
        return enumFacing;
    }

    public void setEnumFacing(EnumFacing enumFacing) {
        this.enumFacing = enumFacing;
    }

    public float getCurBlockDamageMP() {
        return curBlockDamageMP;
    }

    public void setCurBlockDamageMP(float curBlockDamageMP) {
        this.curBlockDamageMP = curBlockDamageMP;
    }

    public int getBlockHitDelay() {
        return blockHitDelay;
    }

    public void setBlockHitDelay(int blockHitDelay) {
        this.blockHitDelay = blockHitDelay;
    }
}
