package net.minecraft.client.gui;

import me.nuf.api.render.RenderMethods;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;

public class GuiButtonLanguage extends GuiButton {
    public GuiButtonLanguage(int buttonID, int xPos, int yPos) {
        super(buttonID, xPos, yPos, 20, 20, "");
    }

    /**
     * Draws this button to the screen.
     */
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (this.visible) {
            mc.getTextureManager().bindTexture(GuiButton.buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            boolean var4 = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
            // int var5 = 106;

            // if (var4) {
            // var5 += this.height;
            // }

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            RenderMethods.drawBorderedRectReliant(this.xPosition - (var4 & enabled ? 1 : 0), this.yPosition - (var4 & enabled ? 1 : 0), xPosition + width + (var4 & enabled ? 1 : 0), yPosition + height + (var4 & enabled ? 1 : 0), 1.8F, enabled ? !var4 ? 0x55111111 : 0x99222222 : 0x55444444, 0x33000000);
            drawCenteredString(mc.fontRendererObj, "L", this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, enabled ? var4 ? 0xFFCCCCCC : 0xCCAAAAAA : 0xCCAAAAAA);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
            //this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, var5, this.width, this.height);
        }
    }
}
