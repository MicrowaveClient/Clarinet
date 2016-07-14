package me.nuf.glade.screens.clickgui;

import me.nuf.api.render.CustomFont;
import me.nuf.glade.screens.clickgui.item.panels.CommonsPanel;
import me.nuf.glade.screens.clickgui.item.panels.KillAuraPanel;
import net.minecraft.client.gui.GuiScreen;

import java.util.ArrayList;

public final class ClickGui extends GuiScreen {

    private final ArrayList<Panel> panels = new ArrayList<>();

    public final CustomFont guiFont = new CustomFont("Verdana");

    public ClickGui() {
        if (getPanels().isEmpty())
            load();
    }

    private void load() {
        int x = -100;
        panels.add(new CommonsPanel(x += 104, 4));
        panels.add(new KillAuraPanel(x += 104, 4));
        panels.forEach(panel -> panel.getItems().sort((item1, item2) -> item1.getLabel().compareTo(item2.getLabel())));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        panels.forEach(panel -> panel.drawScreen(mouseX, mouseY, partialTicks));
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int clickedButton) {
        panels.forEach(panel -> panel.mouseClicked(mouseX, mouseY, clickedButton));
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {
        panels.forEach(panel -> panel.mouseReleased(mouseX, mouseY, releaseButton));
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public final ArrayList<Panel> getPanels() {
        return panels;
    }
}
