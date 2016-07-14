package me.nuf.glade.module.impl.combat;

import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.TickSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

/**
 * Created by nuf on 3/27/2016.
 */
public final class AutoArmor extends ToggleableModule {
    private final NumberProperty<Integer> delay = new NumberProperty<>(500, "Delay", "D");

    private final Stopwatch stopwatch = new Stopwatch();

    public AutoArmor() {
        super(new String[]{"AutoArmor", "aa", "armor"}, false, 0xFF803545, Category.COMBAT);
        this.offerProperties(delay);
        this.addListeners(new Listener<TickSubject>("auto_armor_tick_listener") {
            @Override
            public void call(TickSubject subject) {
                if (!stopwatch.hasReached(delay.getValue()) || minecraft.thePlayer.capabilities.isCreativeMode
                        || !(minecraft.currentScreen == null || minecraft.currentScreen instanceof GuiChat))
                    return;
                for (byte b = 5; b <= 8; b++)
                    if (equipArmor(b)) {
                        stopwatch.reset();
                        break;
                    }
            }
        });
    }

    private boolean equipArmor(byte b) {
        int currentProtection = -1;
        byte slot = -1;
        ItemArmor current = null;
        if (minecraft.thePlayer.inventoryContainer.getSlot(b).getStack() != null
                && minecraft.thePlayer.inventoryContainer.getSlot(b).getStack().getItem() instanceof ItemArmor) {
            current = (ItemArmor) minecraft.thePlayer.inventoryContainer.getSlot(b).getStack().getItem();
            currentProtection = current.damageReduceAmount + EnchantmentHelper.getEnchantmentLevel(
                    Enchantment.protection.effectId, minecraft.thePlayer.inventoryContainer.getSlot(b).getStack());
        }
        for (byte i = 9; i <= 44; i++) {
            ItemStack stack = minecraft.thePlayer.inventoryContainer.getSlot(i).getStack();
            if (stack != null)
                if (stack.getItem() instanceof ItemArmor) {
                    ItemArmor armor = (ItemArmor) stack.getItem();
                    int armorProtection = armor.damageReduceAmount
                            + EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
                    if (checkArmor(armor, b) && (current == null || currentProtection < armorProtection)) {
                        currentProtection = armorProtection;
                        current = armor;
                        slot = i;
                    }
                }
        }
        if (slot != -1) {
            boolean isNull = minecraft.thePlayer.inventoryContainer.getSlot(b).getStack() == null;
            if (!isNull)
                clickSlot(b, 0, false);
            clickSlot(slot, 0, true);
            if (!isNull)
                clickSlot(slot, 0, false);
            return true;
        }
        return false;
    }

    private boolean checkArmor(ItemArmor item, byte b) {
        return b == 5 && item.getUnlocalizedName().startsWith("item.helmet") || b == 6 && item.getUnlocalizedName().startsWith("item.chestplate") || b == 7 && item.getUnlocalizedName().startsWith("item.leggings")
                || b == 8 && item.getUnlocalizedName().startsWith("item.boots");
    }

    private void clickSlot(int slot, int mouseButton, boolean shiftClick) {
        minecraft.playerController.windowClick(minecraft.thePlayer.inventoryContainer.windowId, slot, mouseButton, shiftClick ? 1 : 0,
                minecraft.thePlayer);
    }
}
