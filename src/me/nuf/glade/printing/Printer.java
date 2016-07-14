package me.nuf.glade.printing;

import me.nuf.api.interfaces.Printable;
import me.nuf.glade.core.Glade;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;

import java.util.logging.Level;

/**
 * Created by nuf on 3/19/2016.
 */
public final class Printer implements Printable {

    private static final Printer PRINTER = new Printer();

    @Override
    public void print(Level level, String message) {
        System.out.println(String.format("[%s][%s] %s", Glade.TITLE, level.toString(), message));
    }

    @Override
    public void printToChat(String message) {
        if (Minecraft.getMinecraft().thePlayer != null)
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText(String.format("\2473> \2477%s", message.replace("&", "\247"))).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GRAY)));
    }

    public static final Printer getPrinter() {
        return PRINTER;
    }
}
