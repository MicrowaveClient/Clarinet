package net.minecraft.network.play.server;

import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;

import java.io.IOException;

public class S39PacketPlayerAbilities implements Packet {
    private boolean invulnerable;
    private boolean flying;
    private boolean allowFlying;
    private boolean creativeMode;
    private float flySpeed;
    private float walkSpeed;

    public S39PacketPlayerAbilities() {
    }

    public S39PacketPlayerAbilities(PlayerCapabilities capabilities) {
        this.setInvulnerable(capabilities.disableDamage);
        this.setFlying(capabilities.isFlying);
        this.setAllowFlying(capabilities.allowFlying);
        this.setCreativeMode(capabilities.isCreativeMode);
        this.setFlySpeed(capabilities.getFlySpeed());
        this.setWalkSpeed(capabilities.getWalkSpeed());
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        byte var2 = buf.readByte();
        this.setInvulnerable((var2 & 1) > 0);
        this.setFlying((var2 & 2) > 0);
        this.setAllowFlying((var2 & 4) > 0);
        this.setCreativeMode((var2 & 8) > 0);
        this.setFlySpeed(buf.readFloat());
        this.setWalkSpeed(buf.readFloat());
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        byte var2 = 0;

        if (this.isInvulnerable()) {
            var2 = (byte) (var2 | 1);
        }

        if (this.isFlying()) {
            var2 = (byte) (var2 | 2);
        }

        if (this.isAllowFlying()) {
            var2 = (byte) (var2 | 4);
        }

        if (this.isCreativeMode()) {
            var2 = (byte) (var2 | 8);
        }

        buf.writeByte(var2);
        buf.writeFloat(this.flySpeed);
        buf.writeFloat(this.walkSpeed);
    }

    public void processPacket(INetHandlerPlayClient handler) {
        handler.handlePlayerAbilities(this);
    }

    public boolean isInvulnerable() {
        return this.invulnerable;
    }

    public void setInvulnerable(boolean isInvulnerable) {
        this.invulnerable = isInvulnerable;
    }

    public boolean isFlying() {
        return this.flying;
    }

    public void setFlying(boolean isFlying) {
        this.flying = isFlying;
    }

    public boolean isAllowFlying() {
        return this.allowFlying;
    }

    public void setAllowFlying(boolean isAllowFlying) {
        this.allowFlying = isAllowFlying;
    }

    public boolean isCreativeMode() {
        return this.creativeMode;
    }

    public void setCreativeMode(boolean isCreativeMode) {
        this.creativeMode = isCreativeMode;
    }

    public float getFlySpeed() {
        return this.flySpeed;
    }

    public void setFlySpeed(float flySpeedIn) {
        this.flySpeed = flySpeedIn;
    }

    public float getWalkSpeed() {
        return this.walkSpeed;
    }

    public void setWalkSpeed(float walkSpeedIn) {
        this.walkSpeed = walkSpeedIn;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandler handler) {
        this.processPacket((INetHandlerPlayClient) handler);
    }
}
