package me.nuf.glade.module.impl.movement;

import me.nuf.api.minecraft.BlockHelper;
import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.StepSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.network.play.client.C03PacketPlayer;

/**
 * Created by nuf on 3/28/2016.
 */
public final class Step extends ToggleableModule {
    private final NumberProperty<Float> stepHeight = new NumberProperty<>(1.1F, 1.1F, 10F, "Step-Height", "height", "sh");
    private final Stopwatch stopwatch = new Stopwatch();
    private int fix;
    private double oldY;

    public Step() {
        super(new String[]{"Step", "autojump"}, false, 0xFF90D4CB, Category.MOVEMENT);
        this.offerProperties(stepHeight);
        this.addListeners(new Listener<MotionUpdateSubject>("step_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                Speed speed = (Speed) Glade.getInstance().getModuleManager().getModuleByAlias("Speed");
                if (speed != null && speed.speed.getValue() && speed.isEnabled() && speed.mode.getValue() == Speed.Mode.Lucid)
                    return;
                switch (subject.getTime()) {
                    case PRE:
                        if (subject.getY() - subject.getOriginalY() >= 0.75
                                && !minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer,
                                minecraft.thePlayer.getEntityBoundingBox().addCoord(0.0, -0.1, 0.0)).isEmpty())
                            stopwatch.reset();
                        if (fix > 0) {
                            subject.setCancelled(true);
                            fix--;
                        }
                        break;
                }
            }
        }, new Listener<StepSubject>("step_player_step_listener") {
            @Override
            public void call(StepSubject subject) {
                Speed speed = (Speed) Glade.getInstance().getModuleManager().getModuleByAlias("Speed");
                if (speed != null && speed.speed.getValue() && speed.isEnabled() && speed.mode.getValue() == Speed.Mode.Lucid)
                    return;
                switch (subject.getTime()) {
                    case PRE:
                        if (fix == 0) {
                            oldY = minecraft.thePlayer.posY;
                            subject.setStepHeight(canStep() ? stepHeight.getValue() : 0.5F);
                        }
                        break;
                    case POST:
                        double offset = minecraft.thePlayer.getEntityBoundingBox().minY - oldY;
                        if (offset > 0.6 && fix == 0 && canStep() && stopwatch.hasReached(65)) {
                            minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(minecraft.thePlayer.posX, minecraft.thePlayer.posY + 0.41D, minecraft.thePlayer.posZ, true));
                            minecraft.getNetHandler().addToSendQueue(new C03PacketPlayer.C04PacketPlayerPosition(minecraft.thePlayer.posX, minecraft.thePlayer.posY + 0.75D, minecraft.thePlayer.posZ, true));
                            fix = 2;
                        }
                        break;
                }
            }
        });
        setEnabled(true);
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        minecraft.thePlayer.stepHeight = 0.5F;
    }

    private boolean canStep() {
        return !BlockHelper.isOnLiquid(minecraft.thePlayer) && !BlockHelper.isInLiquid(minecraft.thePlayer) && minecraft.thePlayer.onGround && !minecraft.gameSettings.keyBindJump.isKeyDown() && minecraft.thePlayer.isCollidedVertically && minecraft.thePlayer.isCollidedHorizontally;
    }
}
