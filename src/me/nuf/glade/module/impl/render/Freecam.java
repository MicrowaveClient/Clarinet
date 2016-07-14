package me.nuf.glade.module.impl.render;

import com.mojang.authlib.GameProfile;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.glade.subjects.RenderHandSubject;
import me.nuf.glade.subjects.TickSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.play.client.C03PacketPlayer;

import java.util.List;

/**
 * Created by nuf on 3/27/2016.
 */
public final class Freecam extends ToggleableModule {
    private double startX, startY, startZ;
    private float yaw, pitch;

    private EntityOtherPlayerMP entity;

    public Freecam() {
        super(new String[]{"Freecam", "camera"}, false, 0xFFCEE880, Category.RENDER);
        this.addListeners(new Listener<TickSubject>("freecam_tick_listener") {
            @Override
            public void call(TickSubject subject) {
                if (!minecraft.thePlayer.capabilities.isFlying)
                    minecraft.thePlayer.capabilities.isFlying = true;
            }
        }, new Listener<MotionUpdateSubject>("freecam_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                List boxes = minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer,
                        minecraft.thePlayer.getEntityBoundingBox().expand(0.5D, 0.5D, 0.5D));
                minecraft.thePlayer.noClip = !boxes.isEmpty();
            }
        }, new Listener<PacketSubject>("freecam_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof C03PacketPlayer)
                    if (!subject.isCancelled())
                        subject.setCancelled(true);
            }
        }, new Listener<RenderHandSubject>("freecam_render_hand_listener") {
            @Override
            public void call(RenderHandSubject subject) {
                subject.setCancelled(true);
            }
        });
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        minecraft.renderGlobal.loadRenderers();

        startX = minecraft.thePlayer.posX;
        startY = minecraft.thePlayer.posY;
        startZ = minecraft.thePlayer.posZ;

        yaw = minecraft.thePlayer.rotationYaw;
        pitch = minecraft.thePlayer.rotationPitch;
        entity = new EntityOtherPlayerMP(minecraft.theWorld,
                new GameProfile(minecraft.thePlayer.getUniqueID(), minecraft.thePlayer.getCommandSenderEntity().getCommandSenderName()));
        minecraft.theWorld.addEntityToWorld(-1337, entity);
        entity.setPositionAndRotation(startX, minecraft.thePlayer.getEntityBoundingBox().minY, startZ, yaw, pitch);
        entity.setSneaking(minecraft.thePlayer.isSneaking());
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        minecraft.renderGlobal.loadRenderers();
        minecraft.thePlayer.setPositionAndRotation(startX, startY, startZ, yaw, pitch);
        minecraft.thePlayer.noClip = false;
        minecraft.theWorld.removeEntityFromWorld(-1337);
        minecraft.thePlayer.capabilities.isFlying = false;
    }
}
