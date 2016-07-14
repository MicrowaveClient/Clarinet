package me.nuf.glade.module.impl.render;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.DeathSubject;
import me.nuf.subjectapi.Listener;

/**
 * Created by nuf on 3/31/2016.
 */
public final class NoDeathScreen extends ToggleableModule {
    public NoDeathScreen() {
        super(new String[]{"NoDeathScreen"}, false, 0xFF115599, Category.RENDER);
        this.addListeners(new Listener<DeathSubject>("no_death_screen_death_listener") {
            @Override
            public void call(DeathSubject subject) {
                subject.setCancelled(true);
                minecraft.thePlayer.respawnPlayer();
            }
        });
        setEnabled(true);
    }
}
