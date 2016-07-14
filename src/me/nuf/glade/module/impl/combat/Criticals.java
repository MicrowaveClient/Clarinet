package me.nuf.glade.module.impl.combat;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.module.impl.movement.Speed;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * Created by nuf on 3/21/2016.
 */
public final class Criticals extends ToggleableModule {
    private final Property<Boolean> kangaroo = new Property<>(false, "Kangaroo", "hop");

    private boolean next = false;

    public Criticals() {
        super(new String[]{"Criticals", "critical", "crit", "crits"}, true, 0xFFCCFCB8, Category.COMBAT);
        this.offerProperties(kangaroo);
        this.addListeners(new Listener<PacketSubject>("criticals_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof C02PacketUseEntity) {
                    C02PacketUseEntity packet = (C02PacketUseEntity) subject.getPacket();
                    Speed speed = (Speed) Glade.getInstance().getModuleManager().getModuleByAlias("Speed");
                    if (speed != null && speed.isEnabled() && speed.speed.getValue() && (speed.mode.getValue() == Speed.Mode.OnGround || speed.mode.getValue() == Speed.Mode.Lucid))
                        return;
                    next = !next;
                    if (next && shouldCritical() && packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                        if (kangaroo.getValue()) {
                            if (minecraft.thePlayer.onGround)
                                minecraft.thePlayer.motionY = 0.4D;
                        } else {
                            minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(minecraft.thePlayer.posX, minecraft.thePlayer.posY + 0.05F, minecraft.thePlayer.posZ, false));
                            minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(minecraft.thePlayer.posX, minecraft.thePlayer.posY, minecraft.thePlayer.posZ, false));
                            minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(minecraft.thePlayer.posX, minecraft.thePlayer.posY + 0.012511F, minecraft.thePlayer.posZ, false));
                            minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(minecraft.thePlayer.posX, minecraft.thePlayer.posY, minecraft.thePlayer.posZ, false));
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        next = false;
    }

    private boolean shouldCritical() {
        return minecraft.thePlayer.onGround && minecraft.thePlayer.isCollidedVertically && !minecraft.thePlayer.isInLava() && !minecraft.thePlayer.isInWater();
    }
}
