package me.nuf.glade.module.impl.movement;

import com.mojang.authlib.GameProfile;
import me.nuf.api.render.RenderMethods;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.glade.subjects.RenderSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuf on 3/28/2016.
 */
public final class Fakelag extends ToggleableModule {
    private final List<Packet> reservedPackets = new ArrayList<>();
    private final List<double[]> crumbs = new ArrayList<>();

    public Fakelag() {
        super(new String[]{"Fakelag", "blink", "fl"}, true, 0xFF94B33E, Category.MOVEMENT);
        this.addListeners(new Listener<MotionUpdateSubject>("blink_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                if (!isRecorded(minecraft.thePlayer.posX, minecraft.thePlayer.posY, minecraft.thePlayer.posZ))
                    crumbs.add(new double[]{minecraft.thePlayer.posX, minecraft.thePlayer.posY, minecraft.thePlayer.posZ});
            }
        }, new Listener<PacketSubject>("blink_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof C03PacketPlayer
                        || subject.getPacket() instanceof C08PacketPlayerBlockPlacement
                        || subject.getPacket() instanceof C07PacketPlayerDigging) {
                    reservedPackets.add(subject.getPacket());
                    subject.setCancelled(true);
                }
            }
        }, new Listener<RenderSubject>("blink_render_listener") {
            @Override
            public void call(RenderSubject subject) {
                RenderMethods.enableGL3D();
                GL11.glColor4f(0.92F, 0F, 0F, 1F);
                GL11.glBegin(3);
                crumbs.forEach(crumb -> {
                    double x = crumb[0] - minecraft.getRenderManager().renderPosX;
                    double y = crumb[1] - minecraft.thePlayer.height + 3 - minecraft.getRenderManager().renderPosY;
                    double z = crumb[2] - minecraft.getRenderManager().renderPosZ;
                    GL11.glVertex3d(x, y, z);
                });
                GL11.glEnd();
                RenderMethods.disableGL3D();
            }
        });
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        EntityOtherPlayerMP entity = new EntityOtherPlayerMP(minecraft.theWorld,
                new GameProfile(minecraft.thePlayer.getUniqueID(), minecraft.thePlayer.getCommandSenderEntity().getCommandSenderName()));
        minecraft.theWorld.addEntityToWorld(-1337, entity);
        entity.setPositionAndRotation(minecraft.thePlayer.posX, minecraft.thePlayer.getEntityBoundingBox().minY, minecraft.thePlayer.posZ,
                minecraft.thePlayer.rotationYaw, minecraft.thePlayer.rotationPitch);
        entity.onLivingUpdate();
        this.crumbs.clear();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        reservedPackets.forEach(packet -> minecraft.getNetHandler().addToSendQueue(packet));
        reservedPackets.clear();
        crumbs.clear();
        minecraft.theWorld.removeEntityFromWorld(-1337);
    }

    private boolean isRecorded(double x, double y, double z) {
        for (double[] crumb : crumbs)
            return crumb[0] == x && crumb[1] == y && crumb[2] == z;
        return false;
    }
}
