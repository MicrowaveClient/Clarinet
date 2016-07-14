package me.nuf.glade.module.impl.combat;

import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.MovingObjectPosition;

/**
 * Created by nuf on 4/1/2016.
 */
public final class TriggerBot extends ToggleableModule {
    private final NumberProperty<Integer> delay = new NumberProperty<>(160, 10, 1000, "Delay", "d");
    private final Property<Boolean> friendProtect = new Property<>(true, "Friend-Protect", "friendprotect", "protect", "friend", "fp"), players = new Property<>(true, "Players", "player", "p"), invisibles = new Property<>(true, "Invisibles", "invis", "inv", "i"), monster = new Property<>(false, "Monsters", "monster", "m", "mon"), animals = new Property<>(false, "Animals", "ani", "animal", "a");

    private final Stopwatch stopwatch = new Stopwatch();

    public TriggerBot() {
        super(new String[]{"TriggerBot", "bot", "tb"}, true, 0xFF90D498, Category.COMBAT);
        this.offerProperties(delay, friendProtect, players, invisibles, monster, animals);
        this.addListeners(new Listener<MotionUpdateSubject>("trigger_bot_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                if (minecraft.objectMouseOver != null & minecraft.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) {
                    if (!isValidTarget(minecraft.objectMouseOver.entityHit))
                        return;
                    if (stopwatch.hasReached(delay.getValue())) {
                        minecraft.thePlayer.swingItem();
                        minecraft.playerController.attackEntity(minecraft.thePlayer, minecraft.objectMouseOver.entityHit);
                        stopwatch.reset();
                    }
                }
            }
        });
    }

    private boolean isValidTarget(Entity entity) {
        if (entity instanceof IMob)
            return monster.getValue();
        if (entity instanceof IAnimals)
            return animals.getValue();
        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (entityPlayer.equals(minecraft.thePlayer))
                return false;
            if (!invisibles.getValue() && entityPlayer.isInvisible())
                return false;
            if (friendProtect.getValue() && Glade.getInstance().getFriendManager().isFriend(entityPlayer.getCommandSenderName()))
                return false;
            return players.getValue();
        }
        return false;
    }
}
