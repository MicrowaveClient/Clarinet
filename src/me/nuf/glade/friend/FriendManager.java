package me.nuf.glade.friend;

import com.google.gson.*;
import me.nuf.api.management.ListManager;
import me.nuf.glade.config.Config;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.impl.render.NameProtect;
import net.minecraft.util.EnumChatFormatting;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by nuf on 3/20/2016.
 */
public final class FriendManager extends ListManager<Friend> {
    public FriendManager() {
        elements = new ArrayList<>();

        new Config("friends.json") {
            @Override
            public void load(Object... source) {
                try {
                    if (!getFile().exists())
                        getFile().createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JsonElement root;
                try (FileReader reader = new FileReader(getFile())) {
                    root = new JsonParser().parse(reader);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                if (!(root instanceof JsonArray))
                    return;
                JsonArray friends = (JsonArray) root;
                friends.forEach(node -> {
                    if (!(node instanceof JsonObject))
                        return;
                    try {
                        JsonObject friendNode = (JsonObject) node;
                        Glade.getInstance().getFriendManager().getElements().add(new Friend(
                                friendNode.get("friend-label").getAsString(), friendNode.get("friend-alias").getAsString()));
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void save(Object... destination) {
                if (getFile().exists())
                    getFile().delete();
                if (Glade.getInstance().getFriendManager().getElements().isEmpty())
                    return;
                JsonArray friends = new JsonArray();
                Glade.getInstance().getFriendManager().getElements().forEach(friend -> {
                    try {
                        JsonObject accountObject = new JsonObject();
                        JsonObject properties = accountObject;
                        properties.addProperty("friend-label", friend.getLabel());
                        properties.addProperty("friend-alias", friend.getAlias());
                        friends.add(properties);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                try (FileWriter writer = new FileWriter(getFile())) {
                    writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(friends));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public boolean isFriend(String label) {
        for (Friend friend : elements)
            if (label.equalsIgnoreCase(friend.getLabel()) || label.equalsIgnoreCase(friend.getAlias()))
                return true;
        return false;
    }

    public String replace(String text) {
        NameProtect nameProtect = (NameProtect) Glade.getInstance().getModuleManager().getModuleByAlias("NameProtect");
        EnumChatFormatting color = nameProtect.color.getValue();
        for (Friend friend : elements)
            if (text.contains(friend.getLabel()))
                text = text.replace(friend.getLabel(), String.format("%s%s\247r", color, friend.getAlias()));
        return text;
    }

    public Friend getFriendByLabel(String label) {
        for (Friend friend : getElements())
            if (label.equalsIgnoreCase(friend.getLabel()) || label.equalsIgnoreCase(friend.getAlias()))
                return friend;
        return null;
    }
}
