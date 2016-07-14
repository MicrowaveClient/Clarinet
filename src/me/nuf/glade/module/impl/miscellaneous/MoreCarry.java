package me.nuf.glade.module.impl.miscellaneous;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.CloseInventorySubject;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.client.C0DPacketCloseWindow;

/**
 * Created by nuf on 3/25/2016.
 */
public final class MoreCarry extends ToggleableModule {
    public MoreCarry() {
        super(new String[]{"MoreCarry", "carry", "xcarry"}, false, 0xFF68DEA1, Category.MISCELLANEOUS);
        this.addListeners(new Listener<PacketSubject>("more_carry_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof C0DPacketCloseWindow) {
                    C0DPacketCloseWindow packet = (C0DPacketCloseWindow) subject.getPacket();
                    subject.setCancelled(packet.getWindowId() == 0);
                }
            }
        }, new Listener<CloseInventorySubject>("more_carry_close_inventory_listener") {
            @Override
            public void call(CloseInventorySubject subject) {
                subject.setCancelled(true);
            }
        });
    }
}
