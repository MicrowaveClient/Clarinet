package me.nuf.glade.module.impl.combat;

import me.nuf.api.minecraft.EntityHelper;
import me.nuf.api.stopwatch.Stopwatch;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.MotionUpdateSubject;
import me.nuf.glade.subjects.PlayerSprintingSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by nuf on 3/19/2016.
 */
public final class KillAura extends ToggleableModule {

    private final Stopwatch stopwatch = new Stopwatch();

    private EntityLivingBase target = null;

    private final List<EntityLivingBase> entities = new CopyOnWriteArrayList<>();

    private final Property<Boolean> nodamage = new Property<>(false, "NoDamage"), twohit = new Property<>(false, "2hit", "twohit"), para = new Property<>(false, "Para", "parapvp"), aac = new Property<>(false, "AAC"), mineplex = new Property<>(false, "Mineplex", "plex"), prioritize = new Property<>(false, "Prioritize", "prior"), autoblock = new Property<>(true, "AutoBlock", "block", "ab"), rayTrace = new Property<>(true, "Ray-Trace", "trace", "rt"), sprinting = new Property<>(true, "Sprinting", "sprint"), friendProtect = new Property<>(true, "Friend-Protect", "protect", "friendprotect", "friend", "fp"), player = new Property<>(true, "Players", "player", "p"), lockview = new Property<>(false, "Lockview", "lv"), monsters = new Property<>(false, "Monsters", "monster", "mon"), animals = new Property<>(false, "Animals", "ani", "animal"), invisibles = new Property<>(true, "Invisibles");
    private final NumberProperty<Integer> delay = new NumberProperty<>(100, 10, 1500, "Delay", "d"), ticksExisted = new NumberProperty<>(5, 0, 1000, "Ticks-Existed", "te", "ticks");
    private final NumberProperty<Float> range = new NumberProperty<>(4F, 3F, 6.5F, "Range", "reach", "r", "distance", "dist");
    private final EnumProperty<Targeting> targeting = new EnumProperty<>(Targeting.Switch, "Targeting", "target", "mode", "tar");
    private final EnumProperty<Prioritization> prioritization = new EnumProperty<>(Prioritization.Health, "Prioritization", "priority", "pri");

    private boolean next = false;

    public KillAura() {
        super(new String[]{"Kill Aura", "aura", "ka", "ff"}, true, 0xFFFF2500, Category.COMBAT);
        this.offerProperties(/*para, */nodamage, twohit, rayTrace, aac, mineplex, prioritize, prioritization, autoblock, player, targeting, friendProtect, invisibles, lockview, animals, monsters, delay, ticksExisted, range);
        this.addListeners(new Listener<MotionUpdateSubject>("kill_aura_motion_update_listener") {
                              @Override
                              public void call(MotionUpdateSubject subject) {
                                  switch (subject.getTime()) {
                                      case PRE:
                                          gatherTargets();

                                          entities.forEach(entity -> {
                                              if (!isValidTarget(entity))
                                                  entities.remove(entity);
                                          });

                                          if (prioritize.getValue())
                                              switch (prioritization.getValue()) {
                                                  case Health:
                                                      entities.sort((ent1, ent2) -> (int) ent1.getHealth() - (int) ent2.getHealth());
                                                      break;
                                                  case Distance:
                                                      entities.sort((ent1, ent2) -> (int) minecraft.thePlayer.getDistanceToEntity(ent1) - (int) minecraft.thePlayer.getDistanceToEntity(ent2));
                                                      break;
                                              }

                                          if (targeting.getValue() != Targeting.Multi)
                                              entities.forEach(entity -> target = entity);

                                          if (isValidTarget(target)) {
                                              AutoPot autoPot = (AutoPot) Glade.getInstance().getModuleManager().getModuleByAlias("AutoPot");
                                              if (autoPot != null && autoPot.isEnabled() && autoPot.isPotting())
                                                  return;

                                              if (para.getValue())
                                                  next = !next;

                                              while (minecraft.thePlayer.rotationYaw - minecraft.thePlayer.prevRotationYaw < -180F)
                                                  minecraft.thePlayer.prevRotationYaw -= 360F;

                                              while (minecraft.thePlayer.rotationYaw - minecraft.thePlayer.prevRotationYaw >= 180F)
                                                  minecraft.thePlayer.prevRotationYaw += 360F;

                                              float[] rotations = getRotations(target);
                                              if (lockview.getValue()) {
                                                  minecraft.thePlayer.rotationYaw = !para.getValue() ? rotations[0] : rotations[0] - (next ? 1 : -2);
                                                  minecraft.thePlayer.rotationPitch = !para.getValue() ? rotations[1] : rotations[1] + (next ? 2 : -1);
                                              } else {
                                                  switch (targeting.getValue()) {
                                                      case Single:
                                                      case Switch:
                                                          subject.setRotationYaw(!para.getValue() ? rotations[0] : rotations[0] - (next ? 1 : -2));
                                                          subject.setRotationPitch(!para.getValue() ? rotations[1] : rotations[1] + (next ? 2 : -1));
                                                          break;
                                                      case Multi:
                                                          //subject.setRotationPitch(subject.getRotationPitch() - 360);
                                                          break;
                                                  }
                                              }
                                          } else {
                                              entities.remove(target);
                                              target = null;
                                          }
                                          break;
                                      case POST:
                                          switch (targeting.getValue()) {
                                              case Single:
                                              case Switch:
                                                  if (isValidTarget(target)) {
                                                      AutoPot autoPot = (AutoPot) Glade.getInstance().getModuleManager().getModuleByAlias("AutoPot");
                                                      if (autoPot != null && autoPot.isEnabled() && autoPot.isPotting())
                                                          return;
                                                      attack(target);
                                                      if (twohit.getValue())
                                                          attack(target);
                                                      if (targeting.getValue() == Targeting.Switch && entities.size() > 1)
                                                          entities.remove(target);
                                                      if (autoblock.getValue())
                                                          EntityHelper.rightClickSword();
                                                      if (entities.size() > 1 && targeting.getValue() == Targeting.Switch)
                                                          target = null;
                                                  } else {
                                                      entities.remove(target);
                                                      target = null;
                                                  }
                                                  break;
                                              case Multi:
                                                  entities.forEach(entity -> {
                                                      if (isValidTarget(entity)) {
                                                          attack(entity);
                                                          if (twohit.getValue())
                                                              attack(entity);
                                                          if (entities.size() > 1)
                                                              entities.remove(entity);
                                                      } else {
                                                          entities.remove(entity);
                                                      }
                                                      if (autoblock.getValue())
                                                          EntityHelper.rightClickSword();
                                                  });
                                                  break;
                                          }
                                          break;
                                  }
                              }
                          }

                , new Listener<PlayerSprintingSubject>("kill_aura_player_sprinting_listener") {
                    @Override
                    public void call(PlayerSprintingSubject subject) {
                        if (sprinting.getValue())
                            subject.setCancelled(true);
                    }
                }
        );
    }

    private void attack(EntityLivingBase entityLivingBase) {
        if (!isValidTarget(entityLivingBase)) {
            entities.remove(entityLivingBase);
            return;
        }
        if (!stopwatch.hasReached(delay.getValue()))
            return;

        boolean wasBlocking = minecraft.thePlayer.isBlocking();
        boolean wasSneaking = minecraft.thePlayer.isSneaking();
        boolean wasSprinting = minecraft.thePlayer.isSprinting();

        if (wasBlocking)
            minecraft.getNetHandler().addToSendQueue(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, new BlockPos(-1, -1, -1), EnumFacing.DOWN));
        if (wasSneaking)
            minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.STOP_SNEAKING));
        if (wasSprinting)
            minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.STOP_SPRINTING));
        int damage = 0;
        if (nodamage.getValue() && minecraft.thePlayer.inventory.getCurrentItem() != null)
            damage = minecraft.thePlayer.inventory.getCurrentItem().getItemDamage();
        minecraft.thePlayer.swingItem();
        minecraft.getNetHandler().addToSendQueue(new C02PacketUseEntity(entityLivingBase, C02PacketUseEntity.Action.ATTACK));
        if (nodamage.getValue() && minecraft.thePlayer.inventory.getCurrentItem() != null)
            minecraft.thePlayer.inventory.getCurrentItem().setItemDamage(damage);
        if (wasBlocking)
            minecraft.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(minecraft.thePlayer.getCurrentEquippedItem()));
        if (wasSneaking)
            minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.START_SNEAKING));
        if (wasSprinting)
            minecraft.getNetHandler().addToSendQueue(new C0BPacketEntityAction(minecraft.thePlayer, C0BPacketEntityAction.Action.START_SPRINTING));

        if (targeting.getValue() == Targeting.Multi && !entities.isEmpty())
            return;

        stopwatch.reset();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        target = null;
        stopwatch.reset();
        entities.clear();
    }

    private void gatherTargets() {
        for (Object object : minecraft.theWorld.loadedEntityList) {
            Entity entity = (Entity) object;
            if (entity instanceof EntityLivingBase) {
                EntityLivingBase entityLivingBase = (EntityLivingBase) entity;
                if (isValidTarget(entityLivingBase))
                    entities.add(entityLivingBase);
            }
        }
    }

    private boolean isValidTarget(EntityLivingBase entityLivingBase) {
        if (entityLivingBase == null)
            return false;
        if (!entityLivingBase.isEntityAlive())
            return false;
        if (entityLivingBase.ticksExisted < ticksExisted.getValue())
            return false;
        if (minecraft.thePlayer.getDistanceToEntity(entityLivingBase) > range.getValue())
            return false;
        if (!minecraft.thePlayer.canEntityBeSeen(entityLivingBase) && !rayTrace.getValue())
            return false;
        if (aac.getValue() && !entityLivingBase.onGround && entityLivingBase.fallDistance == 0D && entityLivingBase.noClip)
            return false;
        if (entityLivingBase instanceof IAnimals && animals.getValue())
            return true;
        if (entityLivingBase instanceof IMob && monsters.getValue())
            return true;
        if (entityLivingBase instanceof EntityPlayer) {
            EntityPlayer entityPlayer = (EntityPlayer) entityLivingBase;
            if (mineplex.getValue() && entityPlayer.getCommandSenderName().contains("Dead"))
                return false;
            if (entityLivingBase.equals(minecraft.thePlayer))
                return false;
            if (friendProtect.getValue() && Glade.getInstance().getFriendManager().isFriend(entityPlayer.getCommandSenderName()))
                return false;
            if (entityPlayer.capabilities.isCreativeMode)
                return false;
            if (entityPlayer.isInvisible() && !invisibles.getValue())
                return false;
            return player.getValue();
        }
        return false;
    }

    private float[] getRotations(EntityLivingBase entity) {
        final double var4 = entity.posX - minecraft.thePlayer.posX;
        final double var6 = entity.posZ - minecraft.thePlayer.posZ;
        final double var8 = entity.posY + entity.getEyeHeight() / 1.3 - (minecraft.thePlayer.posY + minecraft.thePlayer.getEyeHeight());
        final double var14 = MathHelper.sqrt_double(var4 * var4 + var6 * var6);
        final float yaw = (float) (Math.atan2(var6, var4) * 180.0D / Math.PI) - 90.0F;
        final float pitch = (float) -(Math.atan2(var8, var14) * 180.0D / Math.PI);
        return new float[]{yaw, pitch};
    }

    public enum Prioritization {
        Health, Distance
    }

    public enum Targeting {
        Switch, Single, Multi
    }

    /**
     * private EntityLivingBase getClosest() {
     double range = reach.getValue();
     EntityLivingBase closest = null;
     for (EntityLivingBase entity : entities) {
     float distance = minecraft.thePlayer.getDistanceToEntity(entity);
     if (distance < range) {
     if (isValidTarget(entity)) {
     closest = entity;
     range = distance;
     } else {
     entities.remove(entity);
     }
     }
     }
     return closest;
     }
     */
}
