package me.nuf.glade.command.impl.player;

import me.nuf.glade.command.Argument;
import me.nuf.glade.command.Command;

/**
 * Created by nuf on 3/23/2016.
 */
public final class VClip extends Command {
    public VClip() {
        super(new String[]{"vclip", "vc", "v"}, new Argument(Float.class, "blocks"));
    }

    @Override
    public String dispatch() {
        double blocks = Double.parseDouble(getArgument("blocks").getValue());
        minecraft.thePlayer.setEntityBoundingBox(minecraft.thePlayer.getEntityBoundingBox().offset(0, blocks, 0));
        return String.format("Teleported %s &e%s&7 block(s).", blocks < 0 ? "down" : "up", blocks);
    }
}
