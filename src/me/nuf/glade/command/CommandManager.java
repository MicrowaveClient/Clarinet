package me.nuf.glade.command;

import me.nuf.api.management.ListManager;
import me.nuf.glade.command.impl.client.*;
import me.nuf.glade.command.impl.player.Damage;
import me.nuf.glade.command.impl.player.Grab;
import me.nuf.glade.command.impl.player.HClip;
import me.nuf.glade.command.impl.player.VClip;
import me.nuf.glade.command.impl.server.Crash;
import me.nuf.glade.config.Config;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Module;
import me.nuf.glade.printing.Printer;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.client.C01PacketChatMessage;

import java.io.*;
import java.util.ArrayList;
import java.util.StringJoiner;

/**
 * Created by nuf on 3/20/2016.
 */
public final class CommandManager extends ListManager<Command> {

    private String prefix = "-";

    public CommandManager() {
        elements = new ArrayList<>();
        register(new Toggle());
        register(new Keybind());
        register(new Prefix());
        register(new Legit());
        register(new Help());
        register(new Modules());
        register(new Friends.Add());
        register(new Grab());
        register(new Friends.Remove());
        register(new Drawn());
        register(new Crash());
        register(new Damage());
        register(new VClip());
        register(new HClip());

        elements.sort((cmd1, cmd2) -> cmd1.getAliases()[0].compareTo(cmd2.getAliases()[0]));
        Glade.getInstance().getSubjectManager().register(new Listener<PacketSubject>("main_command_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof C01PacketChatMessage) {
                    C01PacketChatMessage packet = (C01PacketChatMessage) subject.getPacket();
                    String message = packet.getMessage().trim();
                    if (message.startsWith(prefix)) {
                        subject.setCancelled(true);

                        boolean exists = false;

                        String[] arguments = message.split(" ");

                        if (message.length() < 1) {
                            Printer.getPrinter().printToChat("No command was entered.");
                            return;
                        }

                        String execute = message.contains(" ") ? arguments[0] : message;
                        for (Command command : getElements())
                            for (String alias : command.getAliases())
                                if (execute.replace(getPrefix(), "").equalsIgnoreCase(alias.replaceAll(" ", ""))) {
                                    exists = true;
                                    try {
                                        Printer.getPrinter().printToChat(command.dispatch(arguments));
                                    } catch (Exception e) {
                                        Printer.getPrinter().printToChat(
                                                String.format("%s%s %s", prefix, alias, command.getSyntax()));
                                    }
                                }

                        String[] argz = message.split(" ");
                        for (Module mod : Glade.getInstance().getModuleManager().getElements()) {
                            for (String alias : mod.getAliases()) {
                                try {
                                    if (argz[0].equalsIgnoreCase(getPrefix() + alias.replace(" ", ""))) {
                                        exists = true;
                                        String propertyName = argz[1];
                                        if (argz[1].equalsIgnoreCase("list")) {
                                            if (mod.getProperties().size() > 0) {
                                                StringJoiner stringJoiner = new StringJoiner(", ");
                                                for (Property property : mod.getProperties())
                                                    stringJoiner.add(String.format("%s&e[%s]&7",
                                                            property.getAliases()[0], property.getValue()));
                                                Printer.getPrinter()
                                                        .printToChat(String.format("Properties (%s) %s.",
                                                                mod.getProperties().size(), stringJoiner.toString()));
                                            } else {
                                                Printer.getPrinter().printToChat(String
                                                        .format("&e%s&7 has no properties.", mod.getAliases()[0]));
                                            }
                                        } else {
                                            Property property = mod.getPropertyByAlias(propertyName);
                                            if (property != null) {
                                                if (property.getValue() instanceof Number) {
                                                    if (property.getValue() instanceof Double)
                                                        property.setValue(Double.parseDouble(argz[2]));
                                                    if (property.getValue() instanceof Integer)
                                                        property.setValue(Integer.parseInt(argz[2]));
                                                    if (property.getValue() instanceof Float)
                                                        property.setValue(Float.parseFloat(argz[2]));
                                                    if (property.getValue() instanceof Long)
                                                        property.setValue(Long.parseLong(argz[2]));
                                                    Printer.getPrinter()
                                                            .printToChat(String.format(
                                                                    "%s has been set to %s for %s.",
                                                                    property.getAliases()[0], property.getValue(),
                                                                    mod.getAliases()[0]));
                                                } else if (property.getValue() instanceof Enum) {
                                                    if (!argz[2].equalsIgnoreCase("list")) {
                                                        ((EnumProperty) property).setViaString(argz[2]);
                                                        Printer.getPrinter()
                                                                .printToChat(String.format(
                                                                        "%s has been set to %s for %s.",
                                                                        property.getAliases()[0], property.getValue(),
                                                                        mod.getAliases()[0]));
                                                    } else {
                                                        StringJoiner stringJoiner = new StringJoiner(", ");
                                                        Enum[] array;
                                                        for (int length = (array = (Enum[]) ((property)
                                                                .getValue()).getClass()
                                                                .getEnumConstants()).length, i = 0; i < length; i++)
                                                            stringJoiner.add(String.format("%s%s&7",
                                                                    array[i].name().equalsIgnoreCase(
                                                                            property.getValue().toString()) ? "&a"
                                                                            : "&c", array[i].name()));
                                                        Printer.getPrinter()
                                                                .printToChat(String.format("Modes (%s) %s.",
                                                                        array.length, stringJoiner.toString()));
                                                    }
                                                } else if (property.getValue() instanceof String) {
                                                    property.setValue(argz[2]);
                                                    Printer.getPrinter()
                                                            .printToChat(String.format(
                                                                    "%s has been set to \"%s\" for %s.",
                                                                    property.getAliases()[0], property.getValue(),
                                                                    mod.getAliases()[0]));
                                                } else if (property.getValue() instanceof Boolean) {
                                                    property.setValue(!(Boolean) property.getValue());
                                                    Printer.getPrinter().printToChat(String.format(
                                                            "%s toggled %s&7 for %s.", property.getAliases()[0],
                                                            (Boolean) property.getValue() ? "&aon" : "&coff",
                                                            mod.getAliases()[0]));
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                        if (!exists)
                            Printer.getPrinter()
                                    .printToChat("Invalid command.");
                    }
                }
            }
        });
        new Config("command_prefix.txt") {
            @Override
            public void load(Object... source) {
                try {
                    if (!getFile().exists())
                        getFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    BufferedReader br = new BufferedReader(new FileReader(getFile()));
                    String readLine;
                    while ((readLine = br.readLine()) != null) {
                        try {
                            String[] split = readLine.split(":");
                            prefix = split[0];
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
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    BufferedWriter bw = new BufferedWriter(new FileWriter(getFile()));
                    bw.write(prefix);
                    bw.newLine();
                    bw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
