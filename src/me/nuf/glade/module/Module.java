package me.nuf.glade.module;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.nuf.glade.core.Glade;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * Created by nuf on 3/19/2016.
 */
public class Module {

    private final String[] aliases;

    private String tag;

    private final List<Property> properties = new ArrayList<>();

    protected Minecraft minecraft = Minecraft.getMinecraft();

    public Module(String... aliases) {
        this.aliases = aliases;
        this.tag = aliases[0];
    }

    public final String[] getAliases() {
        return aliases;
    }

    public final List<Property> getProperties() {
        return properties;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    protected void offerProperties(Property... properties) {
        for (Property property : properties)
            this.properties.add(property);
    }

    public Property getPropertyByAlias(String label) {
        for (Property property : properties)
            for (String alias : property.getAliases())
                if (label.equalsIgnoreCase(alias))
                    return property;
        return null;
    }

    public void loadConfig(JsonObject node) {
        File modsFolder = new File(Glade.getInstance().getDirectory(), "modules");
        if (!modsFolder.exists())
            modsFolder.mkdir();
        node.entrySet().forEach(entry -> {
            Optional<Property> property1 = null;
            for (Property prop : this.getProperties())
                if (property1 == null)
                    if (prop.getAliases()[0].equalsIgnoreCase(entry.getKey().toLowerCase()))
                        property1 = Optional.ofNullable(prop);
            if (property1 != null) {
                if (property1.isPresent()) {
                    Object type = (entry.getValue()).getAsString();
                    if (property1.get().getValue() instanceof Number) {
                        if (property1.get().getValue() instanceof Integer)
                            type = (entry.getValue()).getAsJsonPrimitive().getAsInt();
                        else if (property1.get().getValue() instanceof Long)
                            type = (entry.getValue()).getAsJsonPrimitive().getAsLong();
                        else if (property1.get().getValue() instanceof Boolean)
                            type = (entry.getValue()).getAsJsonPrimitive().getAsBoolean();
                        else if (property1.get().getValue() instanceof Double)
                            type = (entry.getValue()).getAsJsonPrimitive().getAsDouble();
                        else if (property1.get().getValue() instanceof Float)
                            type = (entry.getValue()).getAsJsonPrimitive().getAsFloat();
                    } else if (property1.get().getValue() instanceof Enum) {
                        type = (entry.getValue()).getAsJsonPrimitive().getAsString();
                        ((EnumProperty) property1.get()).setViaString(type.toString());
                        return;
                    } else if (property1.get().getValue() instanceof Boolean) {
                        type = (entry.getValue()).getAsJsonPrimitive().getAsBoolean();
                    } else if (property1.get().getValue() instanceof String) {
                        type = (entry.getValue()).getAsJsonPrimitive().getAsString();
                    }
                    property1.get().setValue(type);
                }
            }
        });
    }

    public void saveConfig() {
        File modsFolder = new File(Glade.getInstance().getDirectory(), "modules");
        if (!modsFolder.exists())
            modsFolder.mkdir();
        if (this.getProperties().size() < 1)
            return;
        File jsonFile = new File(modsFolder, getAliases()[0].toLowerCase().replace(" ", "") + ".json");
        if (jsonFile.exists()) {
            jsonFile.delete();
        } else {
            try {
                jsonFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File file = jsonFile;
        JsonObject node = new JsonObject();
        Collection<Property> settings1 = Collections.unmodifiableCollection(this.getProperties());
        settings1.forEach(setting -> {
            if (setting instanceof NumberProperty)
                return;
            node.addProperty(setting.getAliases()[0], setting.getValue().toString());
        });
        if (node.entrySet().isEmpty())
            return;
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        try {
            FileWriter writer = new FileWriter(file);
            Throwable throwable = null;
            try {
                writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(node));
            } catch (Throwable var6_9) {
                throwable = var6_9;
                throw var6_9;
            } finally {
                if (writer != null) {
                    if (throwable != null) {
                        try {
                            writer.close();
                        } catch (Throwable var6_8) {
                            throwable.addSuppressed(var6_8);
                        }
                    } else {
                        writer.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            file.delete();
        }
    }
}
