package net.minecraft.entity.ai;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;

import java.util.Iterator;
import java.util.List;

public class EntityAIHurtByTarget extends EntityAITarget {
    private boolean entityCallsForHelp;

    /**
     * Store the previous revengeTimer value
     */
    private int revengeTimerOld;
    private final Class[] targetClasses;

    public EntityAIHurtByTarget(EntityCreature creatureIn, boolean entityCallsForHelpIn, Class... targetClassesIn) {
        super(creatureIn, false);
        this.entityCallsForHelp = entityCallsForHelpIn;
        this.targetClasses = targetClassesIn;
        this.setMutexBits(1);
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        int var1 = this.taskOwner.getRevengeTimer();
        return var1 != this.revengeTimerOld && this.isSuitableTarget(this.taskOwner.getAITarget(), false);
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void startExecuting() {
        this.taskOwner.setAttackTarget(this.taskOwner.getAITarget());
        this.revengeTimerOld = this.taskOwner.getRevengeTimer();

        if (this.entityCallsForHelp) {
            double var1 = this.getTargetDistance();
            List var3 = this.taskOwner.worldObj.getEntitiesWithinAABB(this.taskOwner.getClass(), (new AxisAlignedBB(this.taskOwner.posX, this.taskOwner.posY, this.taskOwner.posZ, this.taskOwner.posX + 1.0D, this.taskOwner.posY + 1.0D, this.taskOwner.posZ + 1.0D)).expand(var1, 10.0D, var1));
            Iterator var4 = var3.iterator();

            while (var4.hasNext()) {
                EntityCreature var5 = (EntityCreature) var4.next();

                if (this.taskOwner != var5 && var5.getAttackTarget() == null && !var5.isOnSameTeam(this.taskOwner.getAITarget())) {
                    boolean var6 = false;
                    Class[] var7 = this.targetClasses;
                    int var8 = var7.length;

                    for (int var9 = 0; var9 < var8; ++var9) {
                        Class var10 = var7[var9];

                        if (var5.getClass() == var10) {
                            var6 = true;
                            break;
                        }
                    }

                    if (!var6) {
                        this.setEntityAttackTarget(var5, this.taskOwner.getAITarget());
                    }
                }
            }
        }

        super.startExecuting();
    }

    protected void setEntityAttackTarget(EntityCreature creatureIn, EntityLivingBase entityLivingBaseIn) {
        creatureIn.setAttackTarget(entityLivingBaseIn);
    }
}
