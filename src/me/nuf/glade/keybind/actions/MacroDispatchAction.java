package me.nuf.glade.keybind.actions;

import me.nuf.glade.keybind.Action;
import net.minecraft.client.Minecraft;

/**
 * Created by nuf on 3/20/2016.
 */
public class MacroDispatchAction extends Action {

    private final String action;

    public MacroDispatchAction(String action) {
        this.action = action;
    }

    @Override
    public void dispatch() {
        Minecraft.getMinecraft().thePlayer.sendChatMessage(action);
    }

    public final String getAction() {
        return action;
    }
}
