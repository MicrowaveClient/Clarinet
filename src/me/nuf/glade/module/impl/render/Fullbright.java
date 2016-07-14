package me.nuf.glade.module.impl.render;

import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.GammaSubject;
import me.nuf.subjectapi.Listener;

/**
 * Created by nuf on 3/24/2016.
 */
public final class Fullbright extends ToggleableModule {
    public Fullbright() {
        super(new String[]{"Fullbright", "fb", "bright", "brightness"}, true, 0xFFFFA46B, Category.RENDER);
        this.addListeners(new Listener<GammaSubject>("fullbright_gamma_listener") {
            @Override
            public void call(GammaSubject subject) {
                subject.setGamma(100F);
            }
        });
    }
}
