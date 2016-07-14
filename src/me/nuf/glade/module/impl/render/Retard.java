package me.nuf.glade.module.impl.render;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.module.impl.combat.KillAura;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.subjectapi.Listener;

/**
 * Created by nuf on 3/30/2016.
 */
public final class Retard extends ToggleableModule {
    private final EnumProperty<Mode> mode = new EnumProperty<>(Mode.Spin, "Mode", "m");
    private final NumberProperty<Integer> spinSpeed = new NumberProperty<>(18, 5, 35, "Spin-Speed", "speed", "ss", "spin", "spinspeed");

    private int spin = 0;

    public Retard() {
        super(new String[]{"Retard", "derp"}, true, 0xFF25c0aa, Category.RENDER);
        this.offerProperties(mode, spinSpeed);
        this.addListeners(new Listener<MotionUpdateSubject>("retard_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                KillAura killAura = (KillAura) Glade.getInstance().getModuleManager().getModuleByAlias("KillAura");
                if (killAura != null && killAura.isEnabled())
                    return;
                switch (mode.getValue()) {
                    case Spin:
                        spin += spinSpeed.getValue();
                        if (spin >= 360)
                            spin = 0;

                        subject.setRotationYaw(spin);
                        break;
                    case Fakedown:
                        subject.setRotationPitch(95F);
                        break;
                    case Fakeup:
                        subject.setRotationPitch(-95F);
                        break;
                    case Backwards:
                        subject.setRotationYaw(subject.getRotationYaw() - 180F);
                        break;
                }
            }
        });
    }

    public enum Mode {
        Spin, Fakedown, Fakeup, Backwards
    }
}
