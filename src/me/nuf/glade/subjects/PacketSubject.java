package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;
import net.minecraft.network.Packet;

/**
 * Created by nuf on 3/20/2016.
 */
public class PacketSubject extends Subject {

    private Packet packet;

    public PacketSubject(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return packet;
    }

    public void setPacket(Packet packet) {
        this.packet = packet;
    }
}
