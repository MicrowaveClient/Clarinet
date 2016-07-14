package me.nuf.glade.module;

import com.google.gson.*;
import me.nuf.api.interfaces.Toggleable;
import me.nuf.api.management.ListManager;
import me.nuf.glade.config.Config;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.impl.active.HUD;
import me.nuf.glade.module.impl.active.NoRotate;
import me.nuf.glade.module.impl.combat.*;
import me.nuf.glade.module.impl.exploits.*;
import me.nuf.glade.module.impl.miscellaneous.*;
import me.nuf.glade.module.impl.movement.*;
import me.nuf.glade.module.impl.plugins.AutoAccept;
import me.nuf.glade.module.impl.plugins.Revive;
import me.nuf.glade.module.impl.render.*;
import me.nuf.glade.module.impl.world.FastPlace;
import me.nuf.glade.module.impl.world.Speedmine;
import me.nuf.glade.subjects.ActionSubject;
import me.nuf.subjectapi.Listener;
import org.lwjgl.input.Keyboard;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nuf on 3/19/2016.
 */
public final class ModuleManager extends ListManager<Module> {

    public ModuleManager() {
        elements = new ArrayList<>();
        register(new HUD());
        register(new Speed());
        register(new KillAura());
        register(new NameProtect());
        register(new ClickGui());
        register(new Regen());
        register(new Firion());
        register(new NoSlow());
        register(new StorageESP());
        register(new Criticals());
        register(new FastConsume());
        register(new Chat());
        register(new TriggerBot());
        register(new Step());
        register(new Tracers());
        register(new Retard());
        register(new Fakelag());
        register(new AntiVelocity());
        register(new Sneak());
        register(new AutoLog());
        register(new Waypoints());
        register(new Paralyze());
        register(new FastPlace());
        register(new AutoPot());
        register(new NoDeathScreen());
        register(new AutoArmor());
        register(new BowAimbot());
        register(new Nametags());
        register(new Jesus());
        register(new Revive());
        register(new NoRotate());
        register(new Fullbright());
        register(new NoHunger());
        register(new Flight());
        register(new MoreCarry());
        register(new Freecam());
        register(new Speedmine());
        register(new Phase());
        register(new AutoAccept());
        register(new AntiCactus());
        register(new PingSpoof());
        register(new NoFall());
        register(new MiddleClick());
        register(new Container());

        elements.sort((mod1, mod2) -> mod1.getAliases()[0].compareTo(mod2.getAliases()[0]));

        Glade.getInstance().getSubjectManager().register(new Listener<ActionSubject>("main_keybind_action_listener") {
            @Override
            public void call(ActionSubject subject) {
                if (subject.getType() == ActionSubject.Type.KEY_PRESS)
                    Glade.getInstance().getKeybindManager().getElements().forEach(keybind -> {
                        if (keybind.getKey() != Keyboard.KEY_NONE && subject.getKey() == keybind.getKey())
                            keybind.getAction().dispatch();
                    });
            }
        });

        new Config("module_configurations.json") {
            @Override
            public void load(Object... source) {
                try {
                    if (!getFile().exists())
                        getFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File modDirectory = new File(Glade.getInstance().getDirectory(), "modules");
                if (!modDirectory.exists())
                    modDirectory.mkdir();
                Glade.getInstance().getModuleManager().getElements().forEach(mod -> {
                    File file = new File(modDirectory, mod.getAliases()[0].toLowerCase().replaceAll(" ", "") + ".json");
                    if (!file.exists())
                        return;
                    try {
                        FileReader reader = new FileReader(file);
                        Throwable throwable = null;
                        try {
                            JsonElement node = new JsonParser().parse(reader);
                            if (!node.isJsonObject())
                                return;
                            mod.loadConfig(node.getAsJsonObject());
                        } catch (Throwable node) {
                            throwable = node;
                            throw node;
                        } finally {
                            if (reader != null) {
                                if (throwable != null) {
                                    try {
                                        reader.close();
                                    } catch (Throwable var6_9) {
                                        throwable.addSuppressed(var6_9);
                                    }
                                } else {
                                    reader.close();
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
                loadConfig();
            }

            @Override
            public void save(Object... destination) {
                try {
                    if (!getFile().exists())
                        getFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Glade.getInstance().getModuleManager().getElements().forEach(me.nuf.glade.module.Module::saveConfig);
                saveConfig();
            }

            private void loadConfig() {
                File modsFile = new File(getFile().getAbsolutePath());
                if (!modsFile.exists())
                    return;
                JsonElement root;
                try (FileReader reader = new FileReader(modsFile)) {
                    root = new JsonParser().parse(reader);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                if (!(root instanceof JsonArray))
                    return;
                JsonArray mods = (JsonArray) root;
                mods.forEach(node -> {
                    if (!(node instanceof JsonObject))
                        return;
                    try {
                        JsonObject modNode = (JsonObject) node;
                        Glade.getInstance().getModuleManager().getElements().forEach(mod -> {
                            if (mod.getAliases()[0].equalsIgnoreCase(modNode.get("module-label").getAsString())) {
                                if (mod instanceof Toggleable) {
                                    ToggleableModule toggleableModule = (ToggleableModule) mod;
                                    if (modNode.get("module-state").getAsBoolean())
                                        toggleableModule.setEnabled(true);
                                    toggleableModule.setDrawn(modNode.get("module-drawn").getAsBoolean());
                                    Glade.getInstance().getKeybindManager().getKeybindByLabel(String.format("%sToggle", toggleableModule.getAliases()[0].replace(" ", ""))).setKey(modNode.get("module-keybind").getAsInt());
                                }
                            }
                        });
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
            }

            private void saveConfig() {
                File modsFile = new File(getFile().getAbsolutePath());
                if (modsFile.exists())
                    modsFile.delete();
                if (Glade.getInstance().getModuleManager().getElements().isEmpty())
                    return;
                JsonArray mods = new JsonArray();
                Glade.getInstance().getModuleManager().getElements().forEach(mod -> {
                    try {
                        JsonObject modObject = new JsonObject();
                        JsonObject properties = modObject;
                        properties.addProperty("module-label", mod.getAliases()[0]);
                        if (mod instanceof Toggleable) {
                            ToggleableModule toggleableModule = (ToggleableModule) mod;
                            properties.addProperty("module-state", toggleableModule.isEnabled());
                            properties.addProperty("module-drawn", toggleableModule.isDrawn());
                            properties.addProperty("module-keybind", Glade.getInstance().getKeybindManager().getKeybindByLabel(String.format("%sToggle", toggleableModule.getAliases()[0].replace(" ", ""))).getKey());
                        }
                        mods.add(properties);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                try (FileWriter writer = new FileWriter(modsFile)) {
                    writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(mods));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public Module getModuleByAlias(String label) {
        for (Module module : elements)
            for (String alias : module.getAliases())
                if (label.equalsIgnoreCase(alias.replace(" ", "")))
                    return module;
        return null;
    }
}
