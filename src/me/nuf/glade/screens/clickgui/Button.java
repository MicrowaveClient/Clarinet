package me.nuf.glade.screens.clickgui;

import me.nuf.api.interfaces.Labeled;
import me.nuf.api.render.CustomFont;
import me.nuf.api.render.RenderMethods;
import me.nuf.glade.core.Glade;
import me.nuf.glade.screens.clickgui.item.Item;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public abstract class Button extends Item implements Labeled {
    private boolean enabled;

    public Button(String label, boolean enabled) {
        super(label);
        this.enabled = enabled;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderMethods.drawBorderedRectReliant(x, y, x + width, y + height + 15, 1.5F,
                enabled ? (!isHovering(mouseX, mouseY) ? 0xaa38A9F8 : 0xaa1266A2)
                        : !isHovering(mouseX, mouseY) ? 0x661f1f1f : 0x663f3f3f, 0xaa111111);
        Glade.getInstance().getClickGui().guiFont.drawString(getLabel(), x + 2.3F, y - 0.3F, CustomFont.FontType.SHADOW_THIN,
                0xFFFFFFFF);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && isHovering(mouseX, mouseY)) {
            onClicked();
            Minecraft.getMinecraft().getSoundHandler().playSound(
                    PositionedSoundRecord.create(new ResourceLocation("random.click"), 1F));
        }
    }

    @Override
    public int getHeight() {
        return 14;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public abstract void onClicked();

    private boolean isHovering(int mouseX, int mouseY) {
        for (Panel panel : Glade.getInstance().getClickGui().getPanels())
            if (panel.drag)
                return false;
        return mouseX >= getX() && mouseX <= getX() + getWidth() && mouseY >= getY() && mouseY <= getY() + getHeight();
    }
}