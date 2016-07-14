package me.nuf.glade.module.impl.miscellaneous;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.subjectapi.Listener;

/**
 * Created by nuf on 3/24/2016.
 */
public final class NoHunger extends ToggleableModule {
    private boolean onGround;

    public NoHunger() {
        super(new String[]{"NoHunger", "nh", "hunger"}, true, 0xFFA7FC51, Category.MISCELLANEOUS);
        this.addListeners(new Listener<MotionUpdateSubject>("no_hunger_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                switch (subject.getTime()) {
                    case PRE:
                        onGround = minecraft.thePlayer.onGround;
                        if (minecraft.thePlayer.posY == minecraft.thePlayer.prevPosY)
                            minecraft.thePlayer.onGround = false;
                        break;
                    case POST:
                        minecraft.thePlayer.onGround = onGround;
                        break;
                }
            }
        });
    }
}
