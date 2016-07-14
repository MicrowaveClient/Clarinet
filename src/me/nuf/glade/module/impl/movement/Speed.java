package me.nuf.glade.module.impl.movement;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import me.nuf.api.minecraft.BlockHelper;
import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.module.impl.render.Freecam;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PacketSubject;
import me.nuf.glade.subjects.PlayerMoveSubject;
import me.nuf.glade.subjects.TickSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.MovementInput;

/**
 * Created by nuf on 3/19/2016.
 *
 * @author Aristhena (Lucid mode)
 * @author DoubleParalax (Smallhop(Deluge) mode)
 * @author Latematt & Capsar (Capsar mode)
 */
public final class Speed extends ToggleableModule {
    private int state, cooldownHops, delay;
    private double moveSpeed, lastDist;
    private boolean wasOnWater = false, hasJumped = false, nextTick, lag = false;

    private final Property<Boolean> ladders = new Property<>(true, "Ladders"), sonic = new Property<>(true, "Sonic"), smooth = new Property<>(true, "Smooth"), autoSprint = new Property<>(true, "AutoSprint", "as", "sprint");
    public final Property<Boolean> speed = new Property<>(true, "Speed", "spd");
    public final EnumProperty<Mode> mode = new EnumProperty<>(Mode.Lucid, "Mode", "m");
    private final NumberProperty<Double> ladderSpeed = new NumberProperty<>(1.9D, 1.1D, 5D, "Ladder-Speed", "ladderspeed", "lspeed");

    private final Stopwatch stopwatch = new Stopwatch();

    public Speed() {
        super(new String[]{"Speed", "sprint", "fastrun"}, true, 0xFF37A1C4, Category.MOVEMENT);
        this.offerProperties(autoSprint, ladderSpeed, ladders, sonic, smooth, speed, mode);
        this.addListeners(new Listener<TickSubject>("speed_tick_listener") {
            @Override
            public void call(TickSubject subject) {
                if (autoSprint.getValue())
                    minecraft.thePlayer.setSprinting(canSprint());
            }
        }, new Listener<PlayerMoveSubject>("speed_player_move_listener") {
            @Override
            public void call(PlayerMoveSubject subject) {
                if (ladders.getValue() && minecraft.thePlayer.isOnLadder() && !minecraft.thePlayer.onGround) {
                    subject.setY(!minecraft.thePlayer.isSneaking() ? (subject.getY() * ladderSpeed.getValue()) : 0D);
                    return;
                }

                Freecam freecam = (Freecam) Glade.getInstance().getModuleManager().getModuleByAlias("Freecam");

                if (!speed.getValue() || (freecam != null && freecam.isEnabled()))
                    return;

                switch (mode.getValue()) {
                    case Lucid:
                        Flight flight = (Flight) Glade.getInstance().getModuleManager().getModuleByAlias("Flight");
                        if (flight != null && flight.isEnabled()) {
                            moveSpeed = 0D;
                            return;
                        }
                        NoSlow noSlow = (NoSlow) Glade.getInstance().getModuleManager().getModuleByAlias("NoSlow");
                        if (noSlow != null && noSlow.isEnabled()) {
                            ItemStack itemStack = minecraft.thePlayer.inventory.getCurrentItem();
                            if (itemStack != null)
                                if (minecraft.gameSettings.keyBindLeft.isKeyDown() || minecraft.gameSettings.keyBindRight.isKeyDown())
                                    if (itemStack.getItem() instanceof ItemBucketMilk || itemStack.getItem() instanceof ItemSword || itemStack.getItem() instanceof ItemBow || itemStack.getItem() instanceof ItemPotion || itemStack.getItem() instanceof ItemFood)
                                        if (minecraft.thePlayer.isUsingItem()) {
                                            moveSpeed = 0D;
                                            return;
                                        }
                        }
                        if ((BlockHelper.isInLiquid(minecraft.thePlayer)) || BlockHelper.isOnLiquid(minecraft.thePlayer) || (minecraft.thePlayer.isOnLadder()) || (minecraft.thePlayer.isEntityInsideOpaqueBlock())) {
                            moveSpeed = 0D;
                            wasOnWater = true;
                            return;
                        }
                        if (wasOnWater) {
                            moveSpeed = 0D;
                            wasOnWater = false;
                            return;
                        }
                        if ((minecraft.thePlayer.moveForward == 0F) && (minecraft.thePlayer.moveStrafing == 0F))
                            return;
                        if (minecraft.thePlayer.onGround) {
                            state = 2;
                            minecraft.getTimer().timerSpeed = 1.0F;
                        }
                        if (round(minecraft.thePlayer.posY - (int) minecraft.thePlayer.posY, 3) == round(0.138D, 3)) {
                            minecraft.thePlayer.motionY -= 0.08D;
                            subject.setY(subject.getY() - 0.09316090325960147D);
                            minecraft.thePlayer.posY -= 0.09316090325960147D;
                        }
                        if ((state == 1) && ((minecraft.thePlayer.moveForward != 0.0F) || (minecraft.thePlayer.moveStrafing != 0.0F))) {
                            state = 2;
                            moveSpeed = (2.0D * getBaseMoveSpeed() - 0.01D);
                        } else if (state == 2) {
                            state = 3;
                            if ((minecraft.thePlayer.moveForward != 0.0F) || (minecraft.thePlayer.moveStrafing != 0.0F)) {
                                minecraft.thePlayer.motionY = 0.4D;
                                subject.setY(0.4D);
                                if (cooldownHops > 0)
                                    cooldownHops -= 1;
                                moveSpeed *= 2.149D;
                            }
                        } else if (state == 3) {
                            state = 4;
                            double difference = 0.66D * (lastDist - getBaseMoveSpeed());
                            moveSpeed = (lastDist - difference);
                        } else {
                            if ((minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer, minecraft.thePlayer.getEntityBoundingBox().offset(0.0D, minecraft.thePlayer.motionY, 0.0D)).size() > 0) || (minecraft.thePlayer.isCollidedVertically))
                                state = 1;
                            moveSpeed = (lastDist - lastDist / 159.0D);
                        }
                        moveSpeed = Math.max(moveSpeed, getBaseMoveSpeed());
                        MovementInput movementInput = minecraft.thePlayer.movementInput;
                        float forward = movementInput.moveForward;
                        float strafe = movementInput.moveStrafe;
                        float yaw = minecraft.thePlayer.rotationYaw;
                        if ((forward == 0.0F) && (strafe == 0.0F)) {
                            subject.setX(0D);
                            subject.setZ(0D);
                            moveSpeed = 0D;
                        } else if (forward != 0.0F) {
                            if (strafe >= 1.0F) {
                                yaw += (forward > 0.0F ? -45 : 45);
                                strafe = 0.0F;
                            } else if (strafe <= -1.0F) {
                                yaw += (forward > 0.0F ? 45 : -45);
                                strafe = 0.0F;
                            }
                            if (forward > 0.0F) {
                                forward = 1.0F;
                            } else if (forward < 0.0F) {
                                forward = -1.0F;
                            }
                        }
                        double mx = Math.cos(Math.toRadians(yaw + 90.0F));
                        double mz = Math.sin(Math.toRadians(yaw + 90.0F));
                        if (cooldownHops == 0) {
                            subject.setX(forward * moveSpeed * mx + strafe * moveSpeed * mz);
                            subject.setZ(forward * moveSpeed * mz - strafe * moveSpeed * mx);
                        }
                        minecraft.thePlayer.stepHeight = 0.6F;
                        if ((forward == 0.0F) && (strafe == 0.0F)) {
                            subject.setX(0D);
                            subject.setZ(0D);
                        }
                        break;
                    case Vanilla:
                        if (shouldSmooth()) {
                            subject.setX(0D);
                            subject.setZ(0D);
                            return;
                        }
                        subject.setX(subject.getX() * 2.2D);
                        subject.setZ(subject.getZ() * 2.2D);
                        break;
                    case Exeter:
                        if (shouldSmooth()) {
                            subject.setX(0D);
                            subject.setZ(0D);
                            return;
                        }
                        if (canSpeed()) {
                            minecraft.getTimer().timerSpeed = 1F;
                            if (stopwatch.hasReached(165)) {
                                subject.setX(subject.getX() * 2.3D);
                                subject.setZ(subject.getZ() * 2.3D);
                                stopwatch.reset();
                            } else {
                                minecraft.getTimer().timerSpeed = 1.1F;
                            }
                        } else {
                            minecraft.getTimer().timerSpeed = 1F;
                        }
                        break;
                }
            }
        }, new Listener<MotionUpdateSubject>("speed_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                Freecam freecam = (Freecam) Glade.getInstance().getModuleManager().getModuleByAlias("Freecam");

                if (!speed.getValue() || (freecam != null && freecam.isEnabled()))
                    return;

                double offset = (minecraft.thePlayer.rotationYaw + 90 + (minecraft.thePlayer.moveForward > 0
                        ? (minecraft.thePlayer.moveStrafing > 0 ? -45 : minecraft.thePlayer.moveStrafing < 0 ? 45 : 0)
                        : minecraft.thePlayer.moveForward < 0
                        ? 180 + (minecraft.thePlayer.moveStrafing > 0 ? 45
                        : minecraft.thePlayer.moveStrafing < 0 ? -45 : 0)
                        : (minecraft.thePlayer.moveStrafing > 0 ? -90 : minecraft.thePlayer.moveStrafing < 0 ? 90 : 0)))
                        * Math.PI / 180;

                if (shouldSmooth()) {
                    minecraft.thePlayer.motionX = 0D;
                    minecraft.thePlayer.motionZ = 0D;
                    moveSpeed = 0D;
                    return;
                }

                switch (mode.getValue()) {
                    case Lucid:
                        switch (subject.getTime()) {
                            case PRE:
                                if ((minecraft.thePlayer.moveForward == 0.0F) && (minecraft.thePlayer.moveStrafing == 0.0F) && (minecraft.thePlayer.onGround)) {
                                    cooldownHops = 2;
                                    moveSpeed *= 1.0800000429153442D;
                                    state = 2;
                                    minecraft.getTimer().timerSpeed = 1.6F;
                                }
                                double xDist = minecraft.thePlayer.posX - minecraft.thePlayer.prevPosX;
                                double zDist = minecraft.thePlayer.posZ - minecraft.thePlayer.prevPosZ;
                                lastDist = Math.sqrt(xDist * xDist + zDist * zDist);
                                break;
                        }
                        break;
                    case OnGround:
                        if (BlockHelper.getBlockAbove(1.2F).isCollidable() && BlockHelper.getBlockAbove(1.2F).isFullBlock())
                            return;

                        switch (subject.getTime()) {
                            case PRE:
                                if (canSpeed()) {
                                    delay += 1;
                                } else {
                                    delay = 0;
                                }
                                if (delay == 2)
                                    subject.setY(subject.getY() + 0.4D);
                                break;
                            case POST:
                                if (delay == 1) {
                                    minecraft.thePlayer.motionX *= sonic.getValue() ? 3.50D : 3.8D;
                                    minecraft.thePlayer.motionZ *= sonic.getValue() ? 3.50D : 3.8D;
                                    minecraft.thePlayer.jump();
                                } else if (delay == 2) {
                                    minecraft.thePlayer.motionX /= 1.4D;
                                    minecraft.thePlayer.motionZ /= 1.4D;
                                    minecraft.thePlayer.jump();
                                    delay = 0;
                                }
                                break;
                        }
                        break;
                    case Smallhop:
                        if (!minecraft.gameSettings.keyBindForward.isKeyDown() && !minecraft.gameSettings.keyBindBack.isKeyDown() && !minecraft.gameSettings.keyBindLeft.isKeyDown() && !minecraft.gameSettings.keyBindRight.isKeyDown())
                            return;
                        double xp = Math.cos(offset) * 0.14F;
                        double zp = Math.sin(offset) * 0.14F;
                        if (canSpeed()) {
                            minecraft.thePlayer.motionX += xp;
                            minecraft.thePlayer.motionY = 0.0175F;
                            minecraft.thePlayer.motionZ += zp;
                            if (minecraft.thePlayer.movementInput.moveStrafe != 0) {
                                minecraft.thePlayer.motionX *= 0.975F;
                                minecraft.thePlayer.motionZ *= 0.975F;
                            }
                            minecraft.getTimer().timerSpeed = 2.5F;
                            hasJumped = true;
                            minecraft.thePlayer.jump();
                        } else {
                            minecraft.getTimer().timerSpeed = 1.5F;
                            minecraft.thePlayer.motionY = 0.4F;
                        }
                        if (hasJumped && !minecraft.thePlayer.onGround && !minecraft.thePlayer.isOnLadder()
                                && !minecraft.gameSettings.keyBindJump.isKeyDown()) {
                            minecraft.thePlayer.motionY = -0.1F;
                            hasJumped = false;
                        }
                        break;
                    case Capsar:
                        if (!minecraft.gameSettings.keyBindForward.isKeyDown() && !minecraft.gameSettings.keyBindBack.isKeyDown() && !minecraft.gameSettings.keyBindLeft.isKeyDown() && !minecraft.gameSettings.keyBindRight.isKeyDown())
                            return;
                        double yDifference = minecraft.thePlayer.posY - minecraft.thePlayer.lastTickPosY;
                        boolean groundCheck = minecraft.thePlayer.onGround && yDifference == 0.0D;
                        double speed = 2.433D, slow = 1.5D;
                        boolean strafe = minecraft.thePlayer.moveStrafing != 0.0F;

                        if (!minecraft.thePlayer.isSprinting())
                            speed += 0.4D;
                        if (strafe)
                            speed -= 0.04F;
                        speed = applySpeedModifier(speed);

                        if (canSpeed() && groundCheck) {
                            if (nextTick = !nextTick) {
                                minecraft.thePlayer.motionX *= speed;
                                minecraft.thePlayer.motionZ *= speed;
                            } else {
                                minecraft.thePlayer.motionX /= slow;
                                minecraft.thePlayer.motionZ /= slow;
                            }
                        } else if (nextTick) {
                            minecraft.thePlayer.motionX /= speed;
                            minecraft.thePlayer.motionZ /= speed;
                            nextTick = false;
                        }
                        break;
                    case Lag:
                        if (lag) {
                            minecraft.getTimer().timerSpeed = 10F;
                        } else {
                            minecraft.getTimer().timerSpeed = 1F;
                        }
                        if (stopwatch.hasReached(1500))
                            lag = true;
                        break;
                }
            }
        }, new Listener<PacketSubject>("speed_packet_listener") {
            @Override
            public void call(PacketSubject subject) {
                if (mode.getValue() == Mode.Lag) {
                    if (!lag) {
                        if (subject.getPacket() instanceof C03PacketPlayer)
                            subject.setCancelled(true);
                    }
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        delay = 0;
        minecraft.getTimer().timerSpeed = 1F;
        cooldownHops = 2;
        state = 0;
        lag = false;
        minecraft.thePlayer.stepHeight = 0.5F;
        stopwatch.reset();
    }

    private boolean canSprint() {
        return !minecraft.thePlayer.isCollidedHorizontally && minecraft.thePlayer.getFoodStats().getFoodLevel() > 6 && minecraft.thePlayer.moveForward > 0;
    }

    private double applySpeedModifier(double speed) {
        if (minecraft.thePlayer.isPotionActive(Potion.moveSpeed)) {
            PotionEffect effect = minecraft.thePlayer.getActivePotionEffect(Potion.moveSpeed);
            switch (effect.getAmplifier()) {
                case 0:
                    speed -= 0.2975D;
                    break;
                case 1:
                    speed -= 0.5575D;
                    break;
                case 2:
                    speed -= 0.7858D;
                    break;
                case 3:
                    speed -= 0.9075D;
                    break;
            }
        }
        return speed;
    }

    private boolean canSpeed() {
        Step step = (Step) Glade.getInstance().getModuleManager().getModuleByAlias("Step");
        boolean stepping = false;
        List collidingBoundingBoxes = minecraft.theWorld.getCollidingBoundingBoxes(minecraft.thePlayer,
                minecraft.thePlayer.getEntityBoundingBox().expand(0.5D, 0D, 0.5D));
        if (step != null && step.isEnabled() && !collidingBoundingBoxes.isEmpty())
            stepping = true;
        return minecraft.thePlayer.onGround && !stepping && !BlockHelper.isInLiquid(minecraft.thePlayer) && !BlockHelper.isOnLiquid(minecraft.thePlayer);
    }

    private boolean shouldSmooth() {
        return (smooth.getValue()) && !minecraft.gameSettings.keyBindForward.isKeyDown() && !minecraft.gameSettings.keyBindBack.isKeyDown() && !minecraft.gameSettings.keyBindRight.isKeyDown() && !minecraft.gameSettings.keyBindLeft.isKeyDown();
    }

    private double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private double getBaseMoveSpeed() {
        double baseSpeed = 0.2873D;
        if (minecraft.thePlayer.isPotionActive(Potion.moveSpeed)) {
            int amplifier = minecraft.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier();
            baseSpeed *= (1.0D + 0.2D * (amplifier + 1));
        }
        return baseSpeed;
    }

    public enum Mode {
        Vanilla, OnGround, Lucid, Smallhop, Exeter, Capsar, Lag
    }
}
