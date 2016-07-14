package me.nuf.glade.macro;

import me.nuf.glade.keybind.actions.MacroDispatchAction;
import net.minecraft.client.Minecraft;

/**
 * Created by nuf on 3/20/2016.
 */
public class Macro {

    private final int key;
    private final MacroDispatchAction action;

    public Macro(int key, String action) {
        this.key = key;
        this.action = new MacroDispatchAction(action);
    }

    public final int getKey() {
        return key;
    }

    public final MacroDispatchAction getAction() {
        return action;
    }

    public void dispatch() {
        Minecraft.getMinecraft().thePlayer.sendChatMessage(action.getAction().replace("_", " "));
    }

}
