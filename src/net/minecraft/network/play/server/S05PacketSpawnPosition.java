package net.minecraft.network.play.server;

import net.minecraft.network.INetHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.util.BlockPos;

import java.io.IOException;

public class S05PacketSpawnPosition implements Packet {
    private BlockPos spawnBlockPos;

    public S05PacketSpawnPosition() {
    }

    public S05PacketSpawnPosition(BlockPos spawnBlockPosIn) {
        this.spawnBlockPos = spawnBlockPosIn;
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.spawnBlockPos = buf.readBlockPos();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeBlockPos(this.spawnBlockPos);
    }

    public void processPacket(INetHandlerPlayClient p_180752_1_) {
        p_180752_1_.handleSpawnPosition(this);
    }

    public BlockPos getSpawnPos() {
        return this.spawnBlockPos;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(INetHandler handler) {
        this.processPacket((INetHandlerPlayClient) handler);
    }
}
