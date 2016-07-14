package me.nuf.glade.module.impl.miscellaneous;

import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.item.ItemStack;

/**
 * Created by nuf on 3/26/2016.
 */
public final class Container extends ToggleableModule {
    private final NumberProperty<Integer> delay = new NumberProperty<>(150, 10, 250, "Delay", "D");
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.Steal, "Mode", "m");

    private final Stopwatch stopwatch = new Stopwatch();

    public Container() {
        super(new String[]{"Container", "contain", "steal", "drop"}, true, 0xFF5FC9AB, Category.MISCELLANEOUS);
        this.offerProperties(mode, delay);
        this.addListeners(new Listener<MotionUpdateSubject>("container_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                setTag(mode.getValue().toString());
                setColor(mode.getValue() != Mode.Steal ? 0xFF5FC9AB : 0xFF94C95F);
                if (!(minecraft.currentScreen instanceof GuiChest))
                    return;

                GuiChest chest = (GuiChest) minecraft.currentScreen;
                for (int index = 0; index < chest.getLowerChestInventory().getSizeInventory(); index++) {
                    ItemStack stack = chest.getLowerChestInventory().getStackInSlot(index);
                    if (stack == null)
                        continue;
                    if (stopwatch.hasReached(delay.getValue())) {
                        switch (mode.getValue()) {
                            case Steal:
                                minecraft.playerController.windowClick(chest.inventorySlots.windowId, index, 0, 1, minecraft.thePlayer);
                                break;
                            case Drop:
                                minecraft.playerController.windowClick(chest.inventorySlots.windowId, index, 0, 4, minecraft.thePlayer);
                                break;
                        }
                        stopwatch.reset();
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        stopwatch.reset();
    }

    public enum Mode {
        Drop, Steal
    }
}
