package me.nuf.glade.module.impl.world;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.TickSubject;
import me.nuf.subjectapi.Listener;

/**
 * Created by nuf on 3/27/2016.
 */
public final class FastPlace extends ToggleableModule {
    private final NumberProperty<Integer> delay = new NumberProperty<>(1, 0, 1, "Delay", "d");

    public FastPlace() {
        super(new String[]{"FastPlace", "place", "fp"}, true, 0xFFD97625, Category.WORLD);
        this.offerProperties(delay);
        this.addListeners(new Listener<TickSubject>("fast_place_tick_listener") {
            @Override
            public void call(TickSubject subject) {
                if (minecraft.rightClickDelayTimer != delay.getValue())
                    minecraft.rightClickDelayTimer = delay.getValue();
            }
        });
    }
}
