package me.nuf.glade.screens.accountmanager;

import me.nuf.glade.core.Glade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import org.lwjgl.opengl.GL11;

public class AccountSlot extends GuiSlot {

    private GuiAccountScreen aList;
    int selected;

    public AccountSlot(GuiAccountScreen aList) {
        super(Minecraft.getMinecraft(), aList.width, aList.height, 32, aList.height - 60, 40);
        this.aList = aList;
        this.selected = 0;
    }

    @Override
    protected int getContentHeight() {
        return this.getSize() * 40;
    }

    @Override
    protected int getSize() {
        return Glade.getInstance().getAccountManager().getElements().size();
    }

    @Override
    protected void elementClicked(int var1, boolean var2, int var3, int var4) {
        this.selected = var1;
        if (var2) {
            Account theAlt = Glade.getInstance().getAccountManager().getElements().get(var1);
            try {
                Minecraft.getMinecraft().processLogin(theAlt.getUsername(), theAlt.getPassword());
            } catch (AccountException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected boolean isSelected(int var1) {
        return this.selected == var1;
    }

    protected int getSelected() {
        return this.selected;
    }

    @Override
    protected void drawBackground() {
        aList.drawDefaultBackground();
    }

    @Override
    protected void drawSlot(int selectedIndex, int x, int y, int var5, int var6, int var7) {
        try {
            String user = Glade.getInstance().getAccountManager().getElements().get(selectedIndex).getUsername();
            Account theAlt = Glade.getInstance().getAccountManager().getElements().get(selectedIndex);
            mc.fontRendererObj.drawStringWithShadow(user, x + 4, y + 2, 0xFFFFFFFF);
            if (theAlt.isPremium()) {
                mc.fontRendererObj.drawString(
                        Glade.getInstance().getAccountManager().makePassChar(theAlt.getPassword()), x + 4, y + 15,
                        0xFF808080);
                mc.fontRendererObj.drawString("\247aPremium", x + 4, y + 25, 0xFFFFFFFF);
            } else {
                mc.fontRendererObj.drawString("Not Available", x + 4, y + 14, 0xFF808080);
                mc.fontRendererObj.drawString("\247cNon-Premium", x + 4, y + 24, 0xFFFFFFFF);
            }
            GL11.glScalef(1F, 0.5F, 0.5F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GL11.glScalef(1, 2, 2);
        } catch (AccountException error) {
            error.printStackTrace();
        }
    }

}
