package me.nuf.glade.screens.clickgui;

import me.nuf.api.interfaces.Labeled;
import me.nuf.api.render.CustomFont;
import me.nuf.api.render.RenderMethods;
import me.nuf.glade.core.Glade;
import me.nuf.glade.screens.clickgui.item.Item;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;

public abstract class Panel implements Labeled {

    private final String label;
    private int x, y, x2, y2, width, height;
    private boolean open;
    public boolean drag;
    private final ArrayList<Item> items = new ArrayList<>();

    public Panel(String label, int x, int y, boolean open) {
        this.label = label;
        this.x = x;
        this.y = y;
        this.width = 100;
        this.height = 18;
        this.open = open;
        setupItems();
    }

    /**
     * dont remove, actually has a use (ClickGui.java)
     */
    public abstract void setupItems();

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drag(mouseX, mouseY);
        int totalItemHeight = open ? getTotalItemHeight() - 2 : 0;
        RenderMethods.drawGradientBorderedRectReliant(x, y - 1.5F, (x + width), y + height - 5, 1.5F, 0xdd111111, 0xff33C4F5, 0xcc0078A1);
        if (open)
            RenderMethods.drawBorderedRectReliant(x, y + 14, (x + width), open ? (y + height + totalItemHeight) : y + height - 1, 1.5F,
                    0xcc1f1f1f, 0xaa111111);
        Glade.getInstance().getClickGui().guiFont.drawString(getLabel(), x + 4.2F, y - 2.45F, CustomFont.FontType.SHADOW_THIN,
                open ? 0xffffffff : 0xccffffff);
        if (open) {
            float y = getY() + getHeight() - 2;
            for (Item item : getItems()) {
                item.setLocation(x + 2F, y);
                item.setWidth(getWidth() - 4);
                item.drawScreen(mouseX, mouseY, partialTicks);
                y += item.getHeight() + 3;
            }
        }
    }

    private void drag(int mouseX, int mouseY) {
        if (!drag)
            return;
        x = x2 + mouseX;
        y = y2 + mouseY;
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovering(mouseX, mouseY)) {
            x2 = x - mouseX;
            y2 = y - mouseY;
            Glade.getInstance().getClickGui().getPanels().forEach(panel -> {
                if (panel.drag)
                    panel.drag = false;
            });
            drag = true;
            return;
        }
        if (mouseButton == 1 && isHovering(mouseX, mouseY)) {
            open = !open;
            Minecraft.getMinecraft().getSoundHandler().playSound(
                    PositionedSoundRecord.create(new ResourceLocation("random.click"), 1F));
            return;
        }
        if (!open)
            return;
        getItems().forEach(item -> item.mouseClicked(mouseX, mouseY, mouseButton));
    }

    public void addButton(Button button) {
        items.add(button);
    }

    public void mouseReleased(final int mouseX, int mouseY, int releaseButton) {
        if (releaseButton == 0)
            drag = false;
        if (!open)
            return;
        getItems().forEach(item -> item.mouseReleased(mouseX, mouseY, releaseButton));
    }

    @Override
    public final String getLabel() {
        return label;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean getOpen() {
        return open;
    }

    public final ArrayList<Item> getItems() {
        return items;
    }

    private boolean isHovering(int mouseX, int mouseY) {
        return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY()
                && mouseY <= getY() + getHeight() - (open ? 2 : 0);
    }

    private int getTotalItemHeight() {
        int height = 0;
        for (Item item : getItems())
            height += item.getHeight() + 3;
        return height;
    }

    public void setX(int dragX) {
        this.x = dragX;
    }

    public void setY(int dragY) {
        this.y = dragY;
    }
}
