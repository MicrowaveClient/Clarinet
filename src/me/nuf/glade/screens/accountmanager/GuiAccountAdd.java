package me.nuf.glade.screens.accountmanager;

import me.nuf.glade.core.Glade;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiAccountAdd extends GuiScreen {

    public GuiTextField usernameBox;
    public GuiPasswordField passwordBox;

    public String errorMessage = "";
    public int errorTime = 0;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96 + 12, "Add"));
        buttonList.add(new GuiButton(2, width / 2 - 100, height / 4 + 96 + 36, "Back"));

        usernameBox = new GuiTextField(6, fontRendererObj, width / 2 - 100, 76, 200, 20);
        passwordBox = new GuiPasswordField(fontRendererObj, width / 2 - 100, 116, 200, 20);

        usernameBox.setMaxStringLength(120);
        passwordBox.setMaxStringLength(100);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public void updateScreen() {
        usernameBox.updateCursorCounter();
        passwordBox.updateCursorCounter();
    }

    @Override
    public void mouseClicked(int x, int y, int b) {
        usernameBox.mouseClicked(x, y, b);
        passwordBox.mouseClicked(x, y, b);
        try {
            super.mouseClicked(x, y, b);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        if (button.id == 1) {
            if (!usernameBox.getText().trim().isEmpty()) {
                if (passwordBox.getText().trim().isEmpty()) {
                    Account theAlt = new Account(usernameBox.getText().trim());
                    if (!Glade.getInstance().getAccountManager().getElements().contains(theAlt))
                        Glade.getInstance().getAccountManager().register(theAlt);
                } else {
                    Account theAlt = new Account(usernameBox.getText().trim(), passwordBox.getText().trim());
                    if (!Glade.getInstance().getAccountManager().getElements().contains(theAlt))
                        Glade.getInstance().getAccountManager().getElements().add(theAlt);
                }
            }
            mc.displayGuiScreen(Glade.getInstance().getAccountManager().altScreen);
        } else if (button.id == 2) {
            mc.displayGuiScreen(Glade.getInstance().getAccountManager().altScreen);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        usernameBox.textboxKeyTyped(typedChar, keyCode);
        passwordBox.textboxKeyTyped(typedChar, keyCode);
        if (typedChar == '\t') {
            if (usernameBox.isFocused()) {
                usernameBox.setFocused(false);
                passwordBox.setFocused(true);
            } else if (passwordBox.isFocused()) {
                usernameBox.setFocused(true);
                passwordBox.setFocused(false);
            }
        }
        if (typedChar == '\r') {
            actionPerformed((GuiButton) buttonList.get(0));
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawString(fontRendererObj, "\247c*\247r Username", width / 2 - 109, 63, 0xa0a0a0);
        drawString(fontRendererObj, "Password", width / 2 - 100, 103, 0xa0a0a0);
        drawString(fontRendererObj, errorMessage, width / 2 - fontRendererObj.getStringWidth(errorMessage), 13,
                0xa0a0a0);
        if (errorMessage.length() > 1) {
            errorTime += 1;
            if (errorTime > 1700) {
                errorMessage = "";
                errorTime = 0;
            }
        }
        try {
            usernameBox.drawTextBox();
            passwordBox.drawTextBox();
        } catch (Exception err) {
            err.printStackTrace();
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
