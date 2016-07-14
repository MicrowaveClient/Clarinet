package me.nuf.glade.module.impl.render;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;

/**
 * Created by nuf on 4/8/2016.
 */
public final class ClickGui extends ToggleableModule {
    public ClickGui() {
        super(new String[]{"ClickGui", "gui"}, false, 0xFFFFFFFF, Category.RENDER);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        minecraft.displayGuiScreen(Glade.getInstance().getClickGui());
        setEnabled(false);
    }
}
