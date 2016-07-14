package me.nuf.glade.command.impl.player;

import me.nuf.glade.command.Argument;
import me.nuf.glade.command.Command;

import java.awt.*;
import java.awt.datatransfer.StringSelection;

/**
 * Created by nuf on 4/4/2016.
 */
public final class Grab extends Command {
    public Grab() {
        super(new String[]{"grab", "grabip", "grabcoords"}, new Argument(String.class, "ip|coords"));
    }

    @Override
    public String dispatch() {
        String type = getArgument("ip|coords").getValue();
        switch (type) {
            case "ip":
            case "i":
                String address = minecraft.getCurrentServerData().serverIP;
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(address), null);
                break;
            case "coords":
            case "coord":
            case "coordinates":
            case "coordinate":
            case "c":
                String coords = String.format("X: %s, Y: %s, Z: %s", (int) minecraft.thePlayer.posX, (int) minecraft.thePlayer.posY, (int) minecraft.thePlayer.posZ);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(coords), null);
                break;
            default:
                return "Incorrect type.";
        }
        return "Copied the selected type.";
    }
}
