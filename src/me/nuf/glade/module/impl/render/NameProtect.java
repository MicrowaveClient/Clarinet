package me.nuf.glade.module.impl.render;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.ChatMessageSubject;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.client.C01PacketChatMessage;
import net.minecraft.util.EnumChatFormatting;

/**
 * Created by nuf on 3/20/2016.
 */
public final class NameProtect extends ToggleableModule {
    private final Property<Boolean> dashNames = new Property<>(true, "DashNames", "dn");
    public final EnumProperty<EnumChatFormatting> color = new EnumProperty<>(EnumChatFormatting.DARK_AQUA, "Color", "c");

    public NameProtect() {
        super(new String[]{"NameProtect", "protect", "np"}, false, 0xFF660000, Category.RENDER);
        this.offerProperties(dashNames, color);
        this.addListeners(new Listener<ChatMessageSubject>("name_protect_chat_message_listener") {
            @Override
            public void call(ChatMessageSubject subject) {
                subject.setCancelled(true);
            }
        }, new Listener<PacketSubject>("name_protect_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (!dashNames.getValue())
                    return;
                if (subject.getPacket() instanceof C01PacketChatMessage) {
                    C01PacketChatMessage chat = (C01PacketChatMessage) subject.getPacket();
                    Glade.getInstance().getFriendManager().getElements().forEach(friend -> {
                        if (chat.getMessage().contains(String.format("-%s", friend.getAlias())))
                            chat.setMessage(chat.getMessage().replace(String.format("-%s", friend.getAlias()), friend.getLabel()));
                    });
                }
            }
        });
        setEnabled(true);
    }
}
