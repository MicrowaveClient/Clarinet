package me.nuf.glade.module.impl.combat;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.subjectapi.Listener;

/**
 * Created by nuf on 3/31/2016.
 */
public final class AutoLog extends ToggleableModule {
    private final NumberProperty<Float> health = new NumberProperty<>(5F, 1F, 19F, "Health", "h");

    public AutoLog() {
        super(new String[]{"AutoLog"}, true, 0xFFB0B0B0, Category.COMBAT);
        this.offerProperties(health);
        this.addListeners(new Listener<MotionUpdateSubject>("auto_log_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                if (minecraft.thePlayer.getHealth() <= health.getValue())
                    minecraft.thePlayer.inventory.currentItem = -6969;
            }
        });
    }
}
