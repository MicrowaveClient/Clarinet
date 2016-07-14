package me.nuf.glade.screens.clickgui.item.panels;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.impl.combat.KillAura;
import me.nuf.glade.screens.clickgui.Button;
import me.nuf.glade.screens.clickgui.Panel;

/**
 * Created by nuf on 4/8/2016.
 */
public final class KillAuraPanel extends Panel {
    public KillAuraPanel(int x, int y) {
        super("Kill Aura", x, y, true);
    }

    @Override
    public void setupItems() {
        KillAura killAura = (KillAura) Glade.getInstance().getModuleManager().getModuleByAlias("KillAura");
        if (killAura != null)
            killAura.getProperties().forEach(property -> {
                if (property.getValue() instanceof Boolean && !property.getAliases()[0].equalsIgnoreCase("lockview") && !property.getAliases()[0].equalsIgnoreCase("nodamage") && !property.getAliases()[0].equalsIgnoreCase("prioritize")) {
                    this.addButton(new Button(property.getAliases()[0], (Boolean) property.getValue()) {
                        @Override
                        public void onClicked() {
                            property.setValue(!((Boolean) property.getValue()));
                            setEnabled(((Boolean) property.getValue()));
                        }
                    });
                }
            });
    }
}
