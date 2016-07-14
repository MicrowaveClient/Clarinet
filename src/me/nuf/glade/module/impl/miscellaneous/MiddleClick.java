package me.nuf.glade.module.impl.miscellaneous;

import me.nuf.glade.core.Glade;
import me.nuf.glade.friend.Friend;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.printing.Printer;
import me.nuf.glade.subjects.ActionSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

/**
 * Created by nuf on 3/26/2016.
 */
public final class MiddleClick extends ToggleableModule {
    public MiddleClick() {
        super(new String[]{"MiddleClick", "mcf"}, false, 0xFF665500, Category.MISCELLANEOUS);
        this.addListeners(new Listener<ActionSubject>("middle_click_action_listener") {
            @Override
            public void call(ActionSubject subject) {
                if (subject.getType() == ActionSubject.Type.MIDDLE_CLICK)
                    if (minecraft.objectMouseOver != null
                            && minecraft.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                        Entity entity = minecraft.objectMouseOver.entityHit;
                        if (entity instanceof EntityPlayer) {
                            String name = entity.getCommandSenderName();
                            if (Glade.getInstance().getFriendManager().isFriend(name)) {
                                Glade.getInstance().getFriendManager()
                                        .unregister(Glade.getInstance().getFriendManager().getFriendByLabel(name));
                                Printer.getPrinter().printToChat("Removed from friends.");
                            } else {
                                Glade.getInstance().getFriendManager().register(new Friend(name, name));
                                Printer.getPrinter().printToChat("Added to friends.");
                            }
                        }
                    }
            }
        });
        setEnabled(true);
    }
}
