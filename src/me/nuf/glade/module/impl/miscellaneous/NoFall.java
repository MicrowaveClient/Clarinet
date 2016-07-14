package me.nuf.glade.module.impl.miscellaneous;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * Created by nuf on 3/26/2016.
 */
public final class NoFall extends ToggleableModule {
    private boolean onGround;

    public NoFall() {
        super(new String[]{"NoFall", "nf"}, true, 0xFFACA4BA, Category.MISCELLANEOUS);
        this.addListeners(new Listener<PacketSubject>("no_fall_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof C03PacketPlayer) {
                    C03PacketPlayer player = (C03PacketPlayer) subject.getPacket();
                    if (minecraft.thePlayer.fallDistance > 3F)
                        player.setOnGround(true);
                }
            }
        });
    }
}
