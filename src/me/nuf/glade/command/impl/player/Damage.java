package me.nuf.glade.command.impl.player;

import me.nuf.api.minecraft.EntityHelper;
import me.nuf.glade.command.Command;

/**
 * Created by nuf on 3/23/2016.
 */
public final class Damage extends Command {
    public Damage() {
        super(new String[]{"damage", "dmg", "td"});
    }

    @Override
    public String dispatch() {
        EntityHelper.damagePlayer();
        return "Damaged.";
    }
}
