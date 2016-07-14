package me.nuf.glade.screens.accountmanager;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.Session;
import org.lwjgl.input.Keyboard;

public class AccountLogin extends GuiScreen {

    private GuiScreen parentScreen;
    private GuiTextField usernameTextField;
    private GuiPasswordField guiPasswordField;
    private String error;
    private Minecraft mc = Minecraft.getMinecraft();

    public AccountLogin(GuiScreen guiscreen) {
        parentScreen = guiscreen;
    }

    @Override
    public void updateScreen() {
        usernameTextField.updateCursorCounter();
        guiPasswordField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void actionPerformed(GuiButton guibutton) {
        if (!guibutton.enabled)
            return;
        if (guibutton.id == 1) {
            mc.displayGuiScreen(parentScreen);
        } else if (guibutton.id == 0) {
            if (guiPasswordField.getText().length() > 0) {
                String s = usernameTextField.getText();
                String s1 = guiPasswordField.getText();
                try {
                    String result = Minecraft.getMinecraft().processLogin(s, s1).trim();
                    if (result == null || !result.contains(":")) {
                        error = result;
                        return;
                    }
                    String[] values = result.split(":");
                    if (values.length > 1)
                        mc.setSession(new Session(values[2], values[4], values[3], "mojang"));
                    mc.displayGuiScreen(parentScreen);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mc.setSession(new Session(usernameTextField.getText(), "", "", "mojang"));
            }
            mc.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void keyTyped(char c, int i) {
        usernameTextField.textboxKeyTyped(c, i);
        guiPasswordField.textboxKeyTyped(c, i);
        if (c == '\t') {
            if (usernameTextField.isFocused()) {
                usernameTextField.isFocused = false;
                guiPasswordField.isFocused = true;
            } else {
                usernameTextField.isFocused = true;
                guiPasswordField.isFocused = false;
            }
        }
        if (c == '\r') {
            actionPerformed((GuiButton) buttonList.get(0));
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        usernameTextField.mouseClicked(mouseX, mouseY, mouseButton);
        guiPasswordField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + 12, "Done"));
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + 12, "Cancel"));
        usernameTextField = new GuiTextField(6, fontRendererObj, width / 2 - 100, 76, 200, 20);
        guiPasswordField = new GuiPasswordField(fontRendererObj, width / 2 - 100, 116, 200, 20);
        usernameTextField.setMaxStringLength(512);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Direct Login", width / 2, 12, 0xffffff);
        drawString(fontRendererObj, "Username", width / 2 - 100, 63, 0xa0a0a0);
        drawString(fontRendererObj, "Password", width / 2 - 100, 104, 0xa0a0a0);
        usernameTextField.drawTextBox();
        guiPasswordField.drawTextBox();
        if (error != null)
            drawCenteredString(fontRendererObj, (new StringBuilder("\247cLogin Failed: ")).append(error).toString(),
                    width / 2, height / 4 + 72 + 12, 0xffffff);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

}
