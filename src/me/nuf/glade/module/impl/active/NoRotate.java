package me.nuf.glade.module.impl.active;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Module;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

/**
 * Created by nuf on 3/24/2016.
 */
public final class NoRotate extends Module {
    public NoRotate() {
        super("NoRotate");
        Glade.getInstance().getSubjectManager().register(new Listener<PacketSubject>("no_rotate_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof S08PacketPlayerPosLook) {
                    S08PacketPlayerPosLook packet = (S08PacketPlayerPosLook) subject.getPacket();
                    if (minecraft.thePlayer.rotationYaw != -180 && minecraft.thePlayer.rotationPitch != 0) {
                        packet.setYaw(minecraft.thePlayer.rotationYaw);
                        packet.setPitch(minecraft.thePlayer.rotationPitch);
                    }
                }
            }
        });
    }
}
