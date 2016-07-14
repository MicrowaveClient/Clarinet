package me.nuf.glade.module.impl.movement;

import me.nuf.api.minecraft.EntityHelper;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.AirRunSubject;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PlayerMoveSubject;
import me.nuf.subjectapi.Listener;

/**
 * Created by nuf on 3/24/2016.
 */
public final class Flight extends ToggleableModule {
    private final Property<Boolean> animation = new Property<>(true, "Animation", "ani"), damage = new Property<>(false, "Damage", "d", "dmg"), glide = new Property<>(true, "Glide", "g");
    private final NumberProperty<Double> glideSpeed = new NumberProperty<>(0.01D, 0.0001D, 0.6D, "Glide-Speed", "gs", "glidespeed"), speed = new NumberProperty<>(2.5D, 1D, 10D, "Speed", "spd", "s");

    public Flight() {
        super(new String[]{"Flight", "fly"}, true, 0xFFBAA4B2, Category.MOVEMENT);
        this.offerProperties(damage, glideSpeed, animation, glide, speed);
        this.addListeners(new Listener<MotionUpdateSubject>("flight_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                minecraft.thePlayer.motionX = 0D;
                minecraft.thePlayer.motionY = 0D;
                minecraft.thePlayer.motionZ = 0D;

                if (glide.getValue())
                    minecraft.thePlayer.motionY -= glideSpeed.getValue();

                NoSlow noSlow = (NoSlow) Glade.getInstance().getModuleManager().getModuleByAlias("NoSlow");

                if (minecraft.inGameHasFocus || (noSlow != null && noSlow.isEnabled() && noSlow.screenMove.getValue())) {
                    if (minecraft.gameSettings.keyBindJump.isKeyDown())
                        minecraft.thePlayer.motionY += 0.3D;
                    if (minecraft.gameSettings.keyBindSneak.isKeyDown())
                        minecraft.thePlayer.motionY -= 0.2D;
                }
            }
        }, new Listener<AirRunSubject>("flight_air_run_listener") {
            @Override
            public void call(AirRunSubject subject) {
                if (animation.getValue())
                    subject.setCancelled(true);
            }
        }, new Listener<PlayerMoveSubject>("flight_player_move_listener") {
            @Override
            public void call(PlayerMoveSubject subject) {
                subject.setX(subject.getX() * speed.getValue());
                subject.setZ(subject.getZ() * speed.getValue());
            }
        });
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        if (damage.getValue())
            EntityHelper.damagePlayer();
    }
}
