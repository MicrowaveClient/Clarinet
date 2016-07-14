package me.nuf.glade.module.impl.movement;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.glade.subjects.PushedByWaterSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S27PacketExplosion;

/**
 * Created by nuf on 3/21/2016.
 */
public final class AntiVelocity extends ToggleableModule {
    private final Property<Boolean> water = new Property<>(true, "Water", "w");

    public AntiVelocity() {
        super(new String[]{"AntiVelocity", "av", "velocity", "knockback"}, true, 0xFF73868C, Category.MOVEMENT);
        this.offerProperties(water);
        this.addListeners(new Listener<PacketSubject>("anti_velocity_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof S12PacketEntityVelocity) {
                    S12PacketEntityVelocity entity = (S12PacketEntityVelocity) subject.getPacket();
                    if (entity.getEntityID() == minecraft.thePlayer.getEntityId())
                        subject.setCancelled(true);
                }
                if (subject.getPacket() instanceof S27PacketExplosion)
                    subject.setCancelled(true);
            }
        }, new Listener<PushedByWaterSubject>("anti_velocity_pushed_by_water_listener") {
            @Override
            public void call(PushedByWaterSubject subject) {
                if (water.getValue())
                    subject.setCancelled(true);
            }
        });
        setEnabled(true);
    }
}
