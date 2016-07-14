package me.nuf.glade.screens.accountmanager;

import me.nuf.glade.core.Glade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import org.lwjgl.input.Keyboard;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.util.Scanner;

public class GuiAccountScreen extends GuiScreen implements GuiYesNoCallback {

    public String dispErrorString = "";
    public boolean deleteMenuOpen = false;
    private AccountSlot accountSlot;

    private int timer = 0;

    private Minecraft mc = Minecraft.getMinecraft();

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        accountSlot.handleMouseInput();
    }

    @Override
    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(2, width / 2 - 76, height - 48, 73, 20, "Login"));
        buttonList.add(new GuiButton(5, width / 2, height - 48, 73, 20, "Direct Login"));
        buttonList.add(new GuiButton(1, width / 2 - 154, height - 48, 73, 20, "Add"));
        buttonList.add(new GuiButton(3, width / 2 + 78, height - 48, 73, 20, "Remove"));
        buttonList.add(new GuiButton(4, width / 2 - 76, height - 26, 149, 20, "Back"));
        buttonList.add(new GuiButton(6, width / 2 - 154, height - 26, 73, 20, "Random"));
        buttonList.add(new GuiButton(7, width / 2 + 78, height - 26, 73, 20, "Import"));
        accountSlot = new AccountSlot(this);
    }

    @Override
    public void confirmClicked(boolean result, int id) {
        super.confirmClicked(result, id);
        if (deleteMenuOpen) {
            deleteMenuOpen = false;
            if (result)
                Glade.getInstance().getAccountManager().getElements().remove(id);
            mc.displayGuiScreen(this);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (keyCode == Keyboard.KEY_UP) {
            accountSlot.selected--;
        }
        if (keyCode == Keyboard.KEY_DOWN) {
            accountSlot.selected++;
        }
        if (keyCode == Keyboard.KEY_RETURN) {
            Account account = Glade.getInstance().getAccountManager().getElements().get(accountSlot.selected);
            try {
                Minecraft.getMinecraft().processLogin(account.getUsername(), account.getPassword());
            } catch (AccountException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void actionPerformed(GuiButton button) {
        try {
            super.actionPerformed(button);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (button.id == 1) {
            GuiAccountAdd gaa = new GuiAccountAdd();
            mc.displayGuiScreen(gaa);
        }
        if (button.id == 2) {
            try {
                Account a1 = Glade.getInstance().getAccountManager().getElements().get(accountSlot.getSelected());
                if (a1.isPremium()) {
                    try {
                        HashMap map = new HashMap(3, 1);
                        map.put("user", a1.getUsername());
                        map.put("password", a1.getPassword());
                        map.put("version", 13);
                        Minecraft.getMinecraft().processLogin(a1.getUsername(), a1.getPassword());
                    } catch (Exception error) {
                        dispErrorString = "".concat("\247cBad Login \2477(").concat(a1.getUsername()).concat(")");
                    }
                } else {
                    return;
                }
            } catch (Exception e) {
            }
        }
        if (button.id == 3) {
            try {
                String s1 = "Are you sure you want to delete the alt " + "\"" + Glade.getInstance().getAccountManager()
                        .getElements().get(accountSlot.getSelected()).getUsername() + "\"" + "?";
                String s3 = "Delete";
                String s4 = "Cancel";
                GuiYesNo guiyesno = new GuiYesNo(this, s1, "", s3, s4, accountSlot.getSelected());
                deleteMenuOpen = true;
                mc.displayGuiScreen(guiyesno);
            } catch (Exception e) {
            }
        }
        if (button.id == 4) {
            mc.displayGuiScreen(new GuiMainMenu());
        }
        if (button.id == 5) {
            AccountLogin gdl = new AccountLogin(this);
            mc.displayGuiScreen(gdl);
        }
        if (button.id == 6) {
            Random random = new Random();
            Account a1 = Glade.getInstance().getAccountManager().getElements()
                    .get(random.nextInt(Glade.getInstance().getAccountManager().getElements().size()));
            try {
                if (a1.isPremium()) {
                    try {
                        HashMap map = new HashMap(3, 1);
                        map.put("user", a1.getUsername());
                        map.put("password", a1.getPassword());
                        map.put("version", 13);
                        Minecraft.getMinecraft().processLogin(a1.getUsername(), a1.getPassword());
                    } catch (Exception error) {
                        dispErrorString = "".concat("\247cBad Login \2477(").concat(a1.getUsername()).concat(")");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (button.id == 7) {
            this.importAlts();
        }
    }

    private void importAlts() {
        JFileChooser chooser = new JFileChooser();
        chooser.setVisible(true);
        chooser.setSize(500, 400);
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.setFileFilter(new FileNameExtensionFilter("File", new String[]{"txt"}));
        JFrame frame = new JFrame("Select a file");
        chooser.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if ((e.getActionCommand().equals("ApproveSelection")) && (chooser.getSelectedFile() != null)) {
                    try {
                        Scanner scanner = new Scanner(new FileReader(chooser.getSelectedFile()));
                        scanner.useDelimiter("\n");
                        while (scanner.hasNext()) {
                            String[] split = scanner.next().trim().split(":");
                            String name = split[0];
                            String pass = split[1];
                            Glade.getInstance().getAccountManager().getElements().add(new Account(name, pass));
                        }
                        scanner.close();
                    } catch (FileNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    try {
                        StringBuilder data = new StringBuilder();
                        for (Account alt : Glade.getInstance().getAccountManager().getElements())
                            data.append(alt.getFileLine() + "\n");
                        BufferedWriter writer = new BufferedWriter(
                                new FileWriter(Glade.getInstance().getDirectory() + "/accounts.txt"));
                        writer.write(data.toString());
                        writer.close();
                    } catch (Exception localException) {
                        localException.printStackTrace();
                    }
                    frame.setVisible(false);
                    frame.dispose();
                }
                if (e.getActionCommand().equals("CancelSelection")) {
                    frame.setVisible(false);
                    frame.dispose();
                }
            }
        });
        frame.setAlwaysOnTop(true);
        frame.add(chooser);
        frame.setVisible(true);
        frame.setSize(750, 600);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        accountSlot.drawScreen(mouseX, mouseY, partialTicks);
        String name = Minecraft.getMinecraft().getSession().getUsername();
        mc.fontRendererObj.drawStringWithShadow(name, this.width - mc.fontRendererObj.getStringWidth(name) - 2, 2,
                0xffffffff);
        mc.fontRendererObj.drawStringWithShadow(
                "Accounts: " + Glade.getInstance().getAccountManager().getElements().size(), 2, 2, 0xffffffff);
        if (dispErrorString.length() > 1) {
            timer += 1;
            if (timer > 100) {
                dispErrorString = "";
                timer = 0;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        try {
            super.mouseClicked(mouseX, mouseY, mouseButton);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
