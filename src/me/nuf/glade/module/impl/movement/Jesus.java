package me.nuf.glade.module.impl.movement;

import me.nuf.api.minecraft.BlockHelper;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.BlockBoundingBoxSubject;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.util.AxisAlignedBB;

/**
 * Created by nuf on 3/24/2016.
 */
public final class Jesus extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.Old, "Mode", "m");
    private final Property<Boolean> lava = new Property<>(true, "Lava", "l");

    private boolean nextTick = false;

    public Jesus() {
        super(new String[]{"Jesus", "waterwalk"}, true, 0xFFB8488F, Category.MOVEMENT);
        this.offerProperties(mode, lava);
        this.addListeners(new Listener<BlockBoundingBoxSubject>("jesus_block_bounding_box_listener") {
            @Override
            public void call(BlockBoundingBoxSubject subject) {
                if (subject.getBlock() instanceof BlockLiquid && minecraft.thePlayer.fallDistance < 3F && !minecraft.thePlayer.isSneaking() && BlockHelper.isOnLiquid(minecraft.thePlayer) && !BlockHelper.isInLiquid(minecraft.thePlayer))
                    subject.setBoundingBox(AxisAlignedBB.fromBounds(subject.getBlockPos().getX(), subject.getBlockPos().getY(), subject.getBlockPos().getZ(), subject.getBlockPos().getX() + 1D, subject.getBlockPos().getY() + (mode.getValue() == Mode.Bob ? 0.75D : 1D), subject.getBlockPos().getZ() + 1D));
            }
        }, new Listener<PacketSubject>("jesus_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (mode.getValue() == Mode.Bob)
                    return;
                if (subject.getPacket() instanceof C03PacketPlayer && !minecraft.thePlayer.isSneaking()) {
                    C03PacketPlayer player = (C03PacketPlayer) subject.getPacket();
                    if (lava.getValue() && (!minecraft.gameSettings.keyBindForward.isKeyDown() && !minecraft.gameSettings.keyBindLeft.isKeyDown() && !minecraft.gameSettings.keyBindBack.isKeyDown() && !minecraft.gameSettings.keyBindRight.isKeyDown()) && BlockHelper.getBlockUnder(1F).getMaterial().equals(Material.lava) && BlockHelper.isOnLiquid(minecraft.thePlayer) && !BlockHelper.isInLiquid(minecraft.thePlayer)) {
                        subject.setCancelled(true);
                        return;
                    }
                    if (!player.isMoving())
                        return;
                    if (BlockHelper.getBlockUnder(1F) instanceof BlockLiquid && !BlockHelper.isInLiquid(minecraft.thePlayer)) {
                        nextTick = !nextTick;
                        if (nextTick)
                            player.setY(player.getPositionY() - (mode.getValue() == Mode.Old ? 0.01D : 0.215D));
                    }
                }
            }
        }, new Listener<MotionUpdateSubject>("jesus_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                switch (mode.getValue()) {
                    case Old:
                    case Latest:
                        if (BlockHelper.isInLiquid(minecraft.thePlayer) && !minecraft.thePlayer.isSneaking())
                            minecraft.thePlayer.motionY = 0.08D;
                        break;
                    case Bob:
                        if (BlockHelper.isInLiquid(minecraft.thePlayer) && minecraft.thePlayer.fallDistance < 3F && !minecraft.thePlayer.isSneaking())
                            minecraft.thePlayer.motionY = 0.06D;
                        break;
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        nextTick = false;
    }

    public enum Mode {
        Old, Latest, Bob
    }
}
