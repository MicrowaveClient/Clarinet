package me.nuf.glade.screens.clickgui.item.panels;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.impl.combat.Criticals;
import me.nuf.glade.module.impl.miscellaneous.Sneak;
import me.nuf.glade.module.impl.movement.AntiVelocity;
import me.nuf.glade.module.impl.movement.Speed;
import me.nuf.glade.module.impl.render.Retard;
import me.nuf.glade.module.impl.world.FastPlace;
import me.nuf.glade.module.impl.world.Speedmine;
import me.nuf.glade.screens.clickgui.Button;
import me.nuf.glade.screens.clickgui.Panel;

/**
 * Created by nuf on 4/8/2016.
 */
public final class CommonsPanel extends Panel {
    public CommonsPanel(int x, int y) {
        super("Commons", x, y, true);
    }

    @Override
    public void setupItems() {
        Criticals criticals = (Criticals) Glade.getInstance().getModuleManager().getModuleByAlias("Criticals");
        AntiVelocity antiVelocity = (AntiVelocity) Glade.getInstance().getModuleManager().getModuleByAlias("AntiVelocity");
        Speedmine speedmine = (Speedmine) Glade.getInstance().getModuleManager().getModuleByAlias("Speedmine");
        Speed speed = (Speed) Glade.getInstance().getModuleManager().getModuleByAlias("Speed");
        Sneak sneak = (Sneak) Glade.getInstance().getModuleManager().getModuleByAlias("Sneak");
        Retard retard = (Retard) Glade.getInstance().getModuleManager().getModuleByAlias("Retard");
        FastPlace fastPlace = (FastPlace) Glade.getInstance().getModuleManager().getModuleByAlias("FastPlace");
        if (criticals == null || antiVelocity == null || speedmine == null || speed == null || sneak == null || retard == null || fastPlace == null)
            return;
        this.addButton(new Button(criticals.getAliases()[0], criticals.isEnabled()) {
            @Override
            public void onClicked() {
                criticals.toggle();
                setEnabled(criticals.isEnabled());
            }
        });
        this.addButton(new Button(antiVelocity.getAliases()[0], antiVelocity.isEnabled()) {
            @Override
            public void onClicked() {
                antiVelocity.toggle();
                setEnabled(antiVelocity.isEnabled());
            }
        });
        this.addButton(new Button(speedmine.getAliases()[0], speedmine.isEnabled()) {
            @Override
            public void onClicked() {
                speedmine.toggle();
                setEnabled(speedmine.isEnabled());
            }
        });
        this.addButton(new Button(speed.getAliases()[0], speed.isEnabled()) {
            @Override
            public void onClicked() {
                speed.toggle();
                setEnabled(speed.isEnabled());
            }
        });
        this.addButton(new Button(sneak.getAliases()[0], sneak.isEnabled()) {
            @Override
            public void onClicked() {
                sneak.toggle();
                setEnabled(sneak.isEnabled());
            }
        });
        this.addButton(new Button(retard.getAliases()[0], retard.isEnabled()) {
            @Override
            public void onClicked() {
                retard.toggle();
                setEnabled(retard.isEnabled());
            }
        });
        this.addButton(new Button(fastPlace.getAliases()[0], fastPlace.isEnabled()) {
            @Override
            public void onClicked() {
                fastPlace.toggle();
                setEnabled(fastPlace.isEnabled());
            }
        });
    }
}
