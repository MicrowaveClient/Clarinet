package me.nuf.glade.module.impl.combat;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.MathHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by nuf on 3/27/2016.
 */
public final class BowAimbot extends ToggleableModule {
    private final NumberProperty<Integer> ticks = new NumberProperty<>(51, 0, 100, "Ticks-Existed", "te", "ticks", "existed");
    private final NumberProperty<Float> reach = new NumberProperty<>(50F, 6F, 80F, "Reach", "range", "r", "distance", "dist");
    private final Property<Boolean> teamProtect = new Property<>(false, "Team-Protect", "tp"), friendProtect = new Property<>(true, "Friend-Protect", "protect", "fp", "friend"), creative = new Property<>(false, "Creative", "creat"), flying = new Property<>(false, "Flying", "fly", "flight"), players = new Property<>(true, "Players", "player", "p", "player"), animals = new Property<>(false, "Animals", "ani", "animal"), invisibles = new Property<>(true, "Invisibles", "invis", "inv", "invisible"), monsters = new Property<>(false, "Monsters", "monster", "mon", "m", "monst"), lockview = new Property<>(false, "LockView", "lv", "lock");

    private final List<EntityLivingBase> entities = new CopyOnWriteArrayList<>();

    private EntityLivingBase currentTarget = null;

    public BowAimbot() {
        super(new String[]{"BowAimbot", "bowaim", "ba"}, true, 0xFFD490CE, Category.COMBAT);
        this.addListeners(new Listener<MotionUpdateSubject>("bow_aimbot_motion_update_listener") {
            @Override
            public void call(MotionUpdateSubject subject) {
                switch (subject.getTime()) {
                    case PRE:
                        if (entities.isEmpty())
                            acquireEntities();

                        entities.forEach(entityLivingBase -> {
                            if (!shouldTarget(entityLivingBase))
                                entities.remove(entityLivingBase);
                        });

                        currentTarget = getClosest();

                        if (shouldTarget(currentTarget)) {
                            float[] rotations = getRotations(currentTarget);

                            if (minecraft.thePlayer.getCurrentEquippedItem() == null)
                                return;
                            if (!(minecraft.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemBow))
                                return;

                            float pitch = rotations[1] + (minecraft.thePlayer.getDistanceToEntity(currentTarget) * -0.15F);

                            if (minecraft.thePlayer.isUsingItem())
                                if (!lockview.getValue()) {
                                    subject.setRotationYaw(rotations[0]);
                                    subject.setRotationPitch(pitch);
                                } else {
                                    minecraft.thePlayer.rotationYaw = rotations[0];
                                    minecraft.thePlayer.rotationPitch = pitch;
                                }
                        } else {
                            entities.remove(currentTarget);
                            currentTarget = null;
                        }
                        break;
                }
            }
        });
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        currentTarget = null;
        entities.clear();
    }

    private void acquireEntities() {
        for (Object object : minecraft.theWorld.loadedEntityList) {
            Entity entity = (Entity) object;
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                if (shouldTarget(entityLivingBase) && entities.size() < 5)
                    entities.add(entityLivingBase);
            }
        }
    }

    private boolean shouldTarget(EntityLivingBase entity) {
        if (entity == null)
            return false;

        if (entity.isDead)
            return false;

        if (!entity.isEntityAlive())
            return false;

        if (minecraft.thePlayer.getDistanceToEntity(entity) > reach.getValue())
            return false;

        if (entity.ticksExisted < ticks.getValue())
            return false;

        if (!minecraft.thePlayer.canEntityBeSeen(entity))
            return false;

        if (entity instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entity;
            if (entityPlayer.equals(minecraft.thePlayer))
                return false;
            if (teamProtect.getValue())
                return !minecraft.thePlayer.isOnSameTeam(entityPlayer);
            if (entityPlayer.capabilities.isCreativeMode)
                return creative.getValue();
            if (entityPlayer.capabilities.isFlying)
                return flying.getValue();
            if (entityPlayer.isInvisible())
                return invisibles.getValue();
            if (friendProtect.getValue()) {
                return !Glade.getInstance().getFriendManager().isFriend(entityPlayer.getCommandSenderName());
            } else {
                return true;
            }
        }

        if (entity instanceof IMob)
            return monsters.getValue();

        if (entity instanceof IAnimals)
            return animals.getValue();
        return true;
    }

    private EntityLivingBase getClosest() {
        double range = reach.getValue();
        EntityLivingBase closest = null;
        for (EntityLivingBase entity : entities) {
            float distance = minecraft.thePlayer.getDistanceToEntity(entity);
            if (distance < range) {
                if (shouldTarget(entity)) {
                    closest = entity;
                    range = distance;
                }
            }
        }
        return closest;
    }

    private float[] getRotations(EntityLivingBase entity) {
        double var4 = entity.posX - minecraft.thePlayer.posX;
        double var6 = entity.posZ - minecraft.thePlayer.posZ;
        double var8 = entity.posY + entity.getEyeHeight() / 1.3D
                - (minecraft.thePlayer.posY + minecraft.thePlayer.getEyeHeight());
        double var14 = MathHelper.sqrt_double(var4 * var4 + var6 * var6);
        float yaw = (float) (Math.atan2(var6, var4) * 180D / 3.141592653589793D) - 90F;
        float pitch = (float) -(Math.atan2(var8, var14) * 180D / 3.141592653589793D);
        return new float[]{yaw, pitch};
    }
}
