package me.nuf.glade.module.impl.plugins;

import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.DeathSubject;
import me.nuf.subjectapi.Listener;

/**
 * Created by nuf on 3/30/2016.
 */
public final class Revive extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.Back, "Mode", "m", "home");
    private final NumberProperty<Integer> delay = new NumberProperty<>(500, 10, 1000, "Delay", "d");
    private final Stopwatch stopwatch = new Stopwatch();

    public Revive() {
        super(new String[]{"Revive", "back"}, true, 0xFF32BF6F, Category.PLUGINS);
        this.offerProperties(mode, delay);
        this.addListeners(new Listener<DeathSubject>("revive_death_listener") {
            @Override
            public void call(DeathSubject subject) {
                subject.setCancelled(true);
                switch (mode.getValue()) {
                    case Back:
                        if (stopwatch.hasReached(500)) {
                            minecraft.thePlayer.respawnPlayer();
                            minecraft.thePlayer.sendChatMessage("/back");
                            stopwatch.reset();
                        }
                        break;
                    case Home:
                        if (stopwatch.hasReached(500)) {
                            minecraft.thePlayer.sendChatMessage("/sethome");
                            minecraft.thePlayer.respawnPlayer();
                            minecraft.thePlayer.sendChatMessage("/home");
                            stopwatch.reset();
                        }
                        break;
                    case Ghost:
                        minecraft.thePlayer.isDead = false;
                        minecraft.thePlayer.setHealth(20F);
                        break;
                }
            }
        });
    }

    public enum Mode {
        Back, Home, Ghost
    }
}
