package me.nuf.glade.module.impl.plugins;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.server.S02PacketChat;

/**
 * Created by nuf on 3/25/2016.
 */
public final class AutoAccept extends ToggleableModule {
    public AutoAccept() {
        super(new String[]{"AutoAccept", "accept", "tpaccept", "tpa"}, false, 0xFF5555AA, Category.PLUGINS);
        this.addListeners(new Listener<PacketSubject>("auto_accept_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (subject.getPacket() instanceof S02PacketChat) {
                    S02PacketChat chat = (S02PacketChat) subject.getPacket();
                    String message = chat.getChatComponent().getUnformattedText();
                    if (message.contains("has requested")) {
                        Glade.getInstance().getFriendManager().getElements().forEach(friend -> {
                            if (message.contains(friend.getAlias()))
                                minecraft.thePlayer.sendChatMessage("/tpaccept");
                        });
                    }
                }
            }
        });
        setEnabled(true);
    }
}
