package me.nuf.glade.module.impl.miscellaneous;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.glade.subjects.PlayerMoveSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;

/**
 * Created by nuf on 3/22/2016.
 */
public final class Sneak extends ToggleableModule {
    private final Property<Boolean> safewalk = new Property<>(false, "SafeWalk", "sw", "safe");

    public Sneak() {
        super(new String[]{"Sneak"}, true, 0xFFB6BAA4, Category.MISCELLANEOUS);
        this.offerProperties(safewalk);
        this.addListeners(new Listener<MotionUpdateSubject>("sneak_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                boolean moving = minecraft.thePlayer.motionX != 0D && minecraft.thePlayer.motionZ != 0D;
                if (!moving) {
                    minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                } else {
                    switch (subject.getTime()) {
                        case PRE:
                            minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                            minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
                            break;
                        case POST:
                            minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
                            break;
                    }
                }
            }
        }, new Listener<PacketSubject>("sneak_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof C08PacketPlayerBlockPlacement)
                    minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
            }
        }, new Listener<PlayerMoveSubject>("sneak_player_move_listener") {
            @Override
            public void call(PlayerMoveSubject subject) {
                if (safewalk.getValue())
                    subject.setSafety(true);
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        if (!minecraft.gameSettings.keyBindSneak.isKeyDown())
            minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
    }
}
