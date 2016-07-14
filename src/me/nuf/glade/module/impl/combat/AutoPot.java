package me.nuf.glade.module.impl.combat;

import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;

/**
 * Created by nuf on 3/30/2016.
 */
public final class AutoPot extends ToggleableModule {
    private boolean potting;

    private final NumberProperty<Float> health = new NumberProperty<>(14F, 1F, 20F, "Health", "h", "hp");

    private final Stopwatch stopwatch = new Stopwatch();

    public AutoPot() {
        super(new String[]{"AutoPot", "autopotion"}, false, 0xFFB9D490, Category.COMBAT);
        this.offerProperties(health);
        this.addListeners(new Listener<MotionUpdateSubject>("auto_pot_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                switch (subject.getTime()) {
                    case PRE:
                        if (minecraft.thePlayer.getHealth() <= health.getValue()) {
                            int currentItem = minecraft.thePlayer.inventory.currentItem;
                            potting = true;
                            if (hotbarHasPots()) {
                                if (minecraft.thePlayer.onGround)
                                    splashPot();
                            } else {
                                getPotsFromInventory();
                            }
                            potting = false;
                            minecraft.thePlayer.inventory.currentItem = currentItem;
                        }
                        break;
                }
            }
        });
    }

    public int getPotionCount() {
        int i = 0;
        for (int i1 = 9; i1 < 45; i1++) {
            ItemStack itemStack = minecraft.thePlayer.inventoryContainer.getSlot(i1).getStack();
            if (itemStack != null)
                if (isItemHealthPotion(itemStack))
                    i += itemStack.stackSize;
        }
        return i;
    }

    private void getPotsFromInventory() {
        int item = -1;
        boolean found = false;
        boolean splash = false;
        for (int i1 = 36; i1 >= 9; i1--) {
            ItemStack itemStack = minecraft.thePlayer.inventoryContainer.getSlot(i1).getStack();
            if (itemStack != null) {
                if (isItemHealthPotion(itemStack)) {
                    item = i1;
                    found = true;
                    splash = ItemPotion.isSplash(itemStack.getItemDamage());
                }
            }
        }
        if (found) {
            if (!splash)
                for (int i1 = 0; i1 < 45; i1++) {
                    ItemStack itemStack = minecraft.thePlayer.inventoryContainer.getSlot(i1).getStack();
                    if (itemStack != null)
                        if ((itemStack.getItem() == Items.glass_bottle) && (i1 >= 36) && (i1 <= 44)) {
                            minecraft.playerController.windowClick(0, i1, 0, 0, minecraft.thePlayer);
                            minecraft.playerController.windowClick(0, -999, 0, 0, minecraft.thePlayer);
                        }
                }
            minecraft.playerController.windowClick(0, item, 0, 1, minecraft.thePlayer);
        }
    }

    private boolean hotbarHasPots() {
        boolean found = false;
        for (int i1 = 36; i1 < 45; i1++) {
            ItemStack itemStack = minecraft.thePlayer.inventoryContainer.getSlot(i1).getStack();
            if (itemStack != null)
                if (isItemHealthPotion(itemStack))
                    found = true;
        }
        return found;
    }


    private void splashPot() {
        int item = -1;
        boolean found = false;
        boolean splash = false;
        for (int i1 = 36; i1 < 45; i1++) {
            ItemStack itemStack = minecraft.thePlayer.inventoryContainer.getSlot(i1).getStack();
            if (itemStack != null)
                if (isItemHealthPotion(itemStack)) {
                    item = i1;
                    found = true;
                    splash = ItemPotion.isSplash(itemStack.getItemDamage());
                    break;
                }
        }
        if (found) {
            if (splash) {
                if (stopwatch.hasReached(250)) {
                    minecraft.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(
                            minecraft.thePlayer.rotationYaw, minecraft.thePlayer.onGround ? 90 : -90, minecraft.thePlayer.onGround));
                    minecraft.thePlayer.inventory.currentItem = (item - 36);
                    minecraft.playerController.updateController();
                    minecraft.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1),
                            -1, minecraft.thePlayer.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F));
                    minecraft.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C05PacketPlayerLook(
                            minecraft.thePlayer.rotationYaw, minecraft.thePlayer.rotationPitch, minecraft.thePlayer.onGround));
                    stopwatch.reset();
                }
            } else if (minecraft.thePlayer.onGround) {
                for (int i1 = 0; i1 < 45; i1++) {
                    ItemStack itemStack = minecraft.thePlayer.inventoryContainer.getSlot(i1).getStack();
                    if (itemStack != null)
                        if ((itemStack.getItem() == Items.glass_bottle) && (i1 >= 36) && (i1 <= 44)) {
                            minecraft.playerController.windowClick(0, i1, 0, 0, minecraft.thePlayer);
                            minecraft.playerController.windowClick(0, -999, 0, 0, minecraft.thePlayer);
                        }
                }
                minecraft.thePlayer.inventory.currentItem = (item - 36);
                minecraft.playerController.updateController();
                minecraft.thePlayer.sendQueue.addToSendQueue(new C08PacketPlayerBlockPlacement(new BlockPos(-1, -1, -1), -1,
                        minecraft.thePlayer.inventory.getCurrentItem(), 0.0F, 0.0F, 0.0F));
                minecraft.thePlayer.sendQueue.addToSendQueue(new C09PacketHeldItemChange(minecraft.thePlayer.inventory.currentItem));
                for (int index = 0; index < 32; index++)
                    minecraft.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(minecraft.thePlayer.onGround));
                minecraft.thePlayer.sendQueue.addToSendQueue(new C07PacketPlayerDigging(
                        C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(0, 0, 0), EnumFacing.DOWN));
                minecraft.thePlayer.stopUsingItem();
            }
        }
    }

    private boolean isItemHealthPotion(ItemStack itemStack) {
        if (itemStack.getItem() instanceof ItemPotion) {
            ItemPotion potion = (ItemPotion) itemStack.getItem();
            if (potion.hasEffect(itemStack))
                for (Object o : potion.getEffects(itemStack)) {
                    PotionEffect effect = (PotionEffect) o;
                    if (effect.getEffectName().equals("potion.heal"))
                        return true;
                }
        }
        return false;
    }

    public boolean isPotting() {
        return potting;
    }
}
