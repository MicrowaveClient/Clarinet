package me.nuf.glade.module.impl.movement;

import me.nuf.api.minecraft.BlockHelper;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.screens.clickgui.ClickGui;
import me.nuf.glade.subjects.ItemUseSubject;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PlayerMoveSubject;
import me.nuf.glade.subjects.SoulSandSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.block.BlockSoulSand;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

/**
 * Created by nuf on 3/21/2016.
 */
public final class NoSlow extends ToggleableModule {
    private final Property<Boolean> soulSand = new Property<>(false, "Soul-Sand", "ss", "soulsand", "sand"), inventoryMove = new Property<>(true, "Inventory-Move", "invmove", "inventory"), webs = new Property<>(true, "Webs", "web", "w"), items = new Property<>(true, "Items", "item", "i");
    public final Property<Boolean> screenMove = new Property<>(false, "Screen-Move", "screenmove", "scm");
    private final NumberProperty<Double> webSpeed = new NumberProperty<>(3.5D, 1.1D, 5D, "Web-Speed", "wspeed", "webspeed", "webs");

    public NoSlow() {
        super(new String[]{"NoSlow", "noslowdown", "ns"}, false, 0xFFFFC852, Category.MOVEMENT);
        this.offerProperties(inventoryMove, soulSand, webSpeed, webs, items, screenMove);
        this.addListeners(new Listener<ItemUseSubject>("no_slow_item_use_listener") {
            @Override
            public void call(ItemUseSubject subject) {
                if (!minecraft.thePlayer.isSneaking() && items.getValue())
                    subject.setSpeed(1.7F);
            }
        }, new Listener<MotionUpdateSubject>("no_slow_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                if (inventoryMove.getValue() || screenMove.getValue()) {
                    KeyBinding[] keys = {minecraft.gameSettings.keyBindForward, minecraft.gameSettings.keyBindBack,
                            minecraft.gameSettings.keyBindLeft, minecraft.gameSettings.keyBindRight, minecraft.gameSettings.keyBindJump};
                    if (minecraft.currentScreen instanceof GuiContainer || minecraft.currentScreen instanceof ClickGui || (minecraft.currentScreen instanceof GuiIngameMenu && screenMove.getValue())) {
                        for (KeyBinding bind : keys)
                            KeyBinding.setKeyBindState(bind.getKeyCode(), Keyboard.isKeyDown(bind.getKeyCode()));

                        if (Keyboard.isKeyDown(Keyboard.KEY_UP))
                            minecraft.thePlayer.rotationPitch -= 4F;

                        if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
                            minecraft.thePlayer.rotationPitch += 4F;

                        if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
                            minecraft.thePlayer.rotationYaw -= 5F;

                        if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
                            minecraft.thePlayer.rotationYaw += 5F;

                        if (minecraft.thePlayer.rotationPitch > 90F)
                            minecraft.thePlayer.rotationPitch = 90F;

                        if (minecraft.thePlayer.rotationPitch < -90F)
                            minecraft.thePlayer.rotationPitch = -90F;
                    } else if (minecraft.currentScreen == null) {
                        for (KeyBinding bind : keys)
                            if (!Keyboard.isKeyDown(bind.getKeyCode()))
                                KeyBinding.setKeyBindState(bind.getKeyCode(), false);
                    }
                }
                if ((minecraft.thePlayer.moveForward != 0D || minecraft.thePlayer.moveStrafing != 0D) && items.getValue() && !minecraft.thePlayer.isSneaking()) {
                    switch (subject.getTime()) {
                        case PRE:
                            if (minecraft.thePlayer.isBlocking())
                                minecraft.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(
                                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), EnumFacing.DOWN));
                            break;
                        case POST:
                            if (minecraft.thePlayer.isBlocking())
                                minecraft.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(minecraft.thePlayer.getCurrentEquippedItem()));

                            if (BlockHelper.getBlockUnder(0.3F) instanceof BlockSoulSand && soulSand.getValue())
                                subject.setY(subject.getY() - 0.125D);
                            break;
                    }
                }
            }
        }, new Listener<PlayerMoveSubject>("no_slow_player_move_listener") {
            @Override
            public void call(PlayerMoveSubject subject) {
                if (webs.getValue())
                    if (minecraft.thePlayer.isInWeb)
                        if (minecraft.thePlayer.onGround) {
                            subject.setX(subject.getX() * webSpeed.getValue());
                            subject.setZ(subject.getZ() * webSpeed.getValue());
                        } else {
                            if (minecraft.gameSettings.keyBindSneak.isKeyDown()) {
                                double speed = webSpeed.getValue() + 10D;
                                subject.setY(subject.getY() * speed);
                            }
                        }
            }
        }, new Listener<SoulSandSubject>("no_slow_soul_sand_listener") {
            @Override
            public void call(SoulSandSubject subject) {
                if (soulSand.getValue())
                    subject.setCancelled(true);
            }
        });
        setEnabled(true);
    }
}
