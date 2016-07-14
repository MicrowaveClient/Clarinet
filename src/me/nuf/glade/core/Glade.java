package me.nuf.glade.core;

import java.io.File;
import java.util.logging.Level;

import org.lwjgl.opengl.Display;

import me.nuf.glade.command.CommandManager;
import me.nuf.glade.config.ConfigManager;
import me.nuf.glade.friend.FriendManager;
import me.nuf.glade.keybind.KeybindManager;
import me.nuf.glade.macro.MacroManager;
import me.nuf.glade.module.ModuleManager;
import me.nuf.glade.printing.Printer;
import me.nuf.glade.screens.accountmanager.AccountManager;
import me.nuf.glade.screens.clickgui.ClickGui;
import me.nuf.subjectapi.Listener;
import me.nuf.subjectapi.basic.BasicSubjectManager;
import me.nuf.subjectapi.events.system.ShutdownSubject;

/**
 * Created by nuf on 3/19/2016.
 */
public final class Glade {

    private static Glade instance;

    public static final String TITLE = "GOJIRAZILLA";
    public static final int BUILD = 1;

    private File directory;

    private BasicSubjectManager subjectManager;
    private ModuleManager moduleManager;
    private KeybindManager keybindManager;
    private MacroManager macroManager;
    private CommandManager commandManager;
    private FriendManager friendManager;
    private ConfigManager configManager;
    private AccountManager accountManager;
    private ClickGui clickGui;

    public Glade() {
        Printer.getPrinter().print(Level.INFO, "Initiated client startup.");

        instance = this;

        directory = new File(System.getProperty("user.home"), TITLE);
        if (!directory.exists())
            Printer.getPrinter().print(Level.INFO, String.format("%s client directory.", directory.mkdir() ? "Created" : "Failed to create"));

        subjectManager = new BasicSubjectManager();
        configManager = new ConfigManager();
        keybindManager = new KeybindManager();
        commandManager = new CommandManager();
        moduleManager = new ModuleManager();
        macroManager = new MacroManager();
        friendManager = new FriendManager();
        accountManager = new AccountManager();
        clickGui = new ClickGui();

        getConfigManager().getElements().forEach(config -> config.load());

        getSubjectManager().register(new Listener<ShutdownSubject>("main_shutdown_listener") {
            @Override
            public void call(ShutdownSubject subject) {
                Printer.getPrinter().print(Level.INFO, "Initiated client shutdown.");
                getConfigManager().getElements().forEach(config -> config.save());
                Printer.getPrinter().print(Level.INFO, "Finished client shutdown.");
            }
        });

        Display.setTitle(String.format("%s b%s (master-1.8)", TITLE, BUILD));

        Printer.getPrinter().print(Level.INFO, "Finished client startup.");
    }

    public static Glade getInstance() {
        return instance;
    }

    public BasicSubjectManager getSubjectManager() {
        return subjectManager;
    }

    public ModuleManager getModuleManager() {
        return moduleManager;
    }

    public KeybindManager getKeybindManager() {
        return keybindManager;
    }

    public MacroManager getMacroManager() {
        return macroManager;
    }

    public CommandManager getCommandManager() {
        return commandManager;
    }

    public FriendManager getFriendManager() {
        return friendManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public File getDirectory() {
        return directory;
    }

    public ClickGui getClickGui() {
        return clickGui;
    }
}
