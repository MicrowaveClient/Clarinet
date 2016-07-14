package me.nuf.glade.screens.accountmanager;

import me.nuf.api.management.ListManager;
import me.nuf.glade.config.Config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;

public final class AccountManager extends ListManager<Account> {

    public GuiAccountScreen altScreen = new GuiAccountScreen();
    public int slotHeight = 25;

    public AccountManager() {
        this.elements = new ArrayList<>();
        new Config("accounts.txt") {
            @Override
            public void load(Object... source) {
                try {
                    if (!getFile().exists())
                        getFile().createNewFile();
                    BufferedReader br = new BufferedReader(new FileReader(getFile()));
                    getElements().clear();
                    String readLine;
                    while ((readLine = br.readLine()) != null) {
                        try {
                            String[] split = readLine.split(":");
                            register(new Account(split[0], split[1]));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    br.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void save(Object... destination) {
                try {
                    if (!getFile().exists())
                        getFile().createNewFile();
                    BufferedWriter bw = new BufferedWriter(new FileWriter(getFile()));
                    for (Account account : getElements()) {
                        bw.write(account.getUsername() + ":" + account.getPassword());
                        bw.newLine();
                    }
                    bw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public String makePassChar(String s) {
        return s.replaceAll("(?s).", "*");
    }
}
