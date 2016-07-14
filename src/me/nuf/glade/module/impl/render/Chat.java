package me.nuf.glade.module.impl.render;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.module.impl.render.chat.GuiCustomChat;
import net.minecraft.client.gui.GuiNewChat;

/**
 * Created by nuf on 3/30/2016.
 */
public final class Chat extends ToggleableModule {
    public Chat() {
        super(new String[]{"Chat"}, false, 0xFFFFFFFF, Category.RENDER);
        setEnabled(true);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        minecraft.ingameGUI.persistantChatGUI = new GuiCustomChat(minecraft);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        minecraft.ingameGUI.persistantChatGUI = new GuiNewChat(minecraft);
    }
}
