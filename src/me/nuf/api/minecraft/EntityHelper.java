package me.nuf.api.minecraft;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;

public class EntityHelper {

    public static void damagePlayer() {
        for (int index = 0; index < 81; index++) {
            Minecraft.getMinecraft().thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                    Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + 0.05D,
                    Minecraft.getMinecraft().thePlayer.posZ, false));
            Minecraft.getMinecraft().thePlayer.sendQueue
                    .addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(Minecraft.getMinecraft().thePlayer.posX,
                            Minecraft.getMinecraft().thePlayer.posY, Minecraft.getMinecraft().thePlayer.posZ, false));
        }
    }

    public static void getBestWeapon() {
        float damageModifier = -1;
        int newItem = -1;
        for (int slot = 0; slot < 9; slot++) {
            ItemStack stack = Minecraft.getMinecraft().thePlayer.inventory.mainInventory[slot];
            if (stack == null) {
                continue;
            }
            if (stack.getItem() instanceof ItemSword) {
                ItemSword is = (ItemSword) stack.getItem();
                float damage = is.getMaxDamage();
                if (damage > damageModifier) {
                    newItem = slot;
                    damageModifier = damage;
                }
            }
            if (newItem > -1) {
                Minecraft.getMinecraft().thePlayer.inventory.currentItem = newItem;
            }
        }
    }

    public static boolean isTeam(EntityLivingBase entity) {
        if (Minecraft.getMinecraft().thePlayer.isOnSameTeam(entity)) {
            return true;
        }
        return false;
    }

    public static void rightClickSword() {
        if (Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem() != null) {
            if (Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem().getItem() instanceof ItemSword) {
                ItemSword sword = (ItemSword) Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem().getItem();
                sword.onItemRightClick(Minecraft.getMinecraft().thePlayer.inventory.getCurrentItem(),
                        Minecraft.getMinecraft().theWorld, Minecraft.getMinecraft().thePlayer);
            }
        }
    }

    public static String getDirection() {
        return Minecraft.getMinecraft().getRenderViewEntity().getHorizontalFacing().name();
    }

}