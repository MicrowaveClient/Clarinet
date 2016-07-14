package net.minecraft.tileentity;

import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EnumFacing;

import java.util.Iterator;
import java.util.List;

public class TileEntityPiston extends TileEntity implements IUpdatePlayerListBox {
    private IBlockState pistonState;
    private EnumFacing pistonFacing;

    /**
     * if this piston is extending or not
     */
    private boolean extending;
    private boolean shouldHeadBeRendered;
    private float progress;

    /**
     * the progress in (de)extending
     */
    private float lastProgress;
    private List field_174933_k = Lists.newArrayList();

    public TileEntityPiston() {
    }

    public TileEntityPiston(IBlockState pistonStateIn, EnumFacing pistonFacingIn, boolean extendingIn, boolean shouldHeadBeRenderedIn) {
        this.pistonState = pistonStateIn;
        this.pistonFacing = pistonFacingIn;
        this.extending = extendingIn;
        this.shouldHeadBeRendered = shouldHeadBeRenderedIn;
    }

    public IBlockState getPistonState() {
        return this.pistonState;
    }

    public int getBlockMetadata() {
        return 0;
    }

    /**
     * Returns true if a piston is extending
     */
    public boolean isExtending() {
        return this.extending;
    }

    public EnumFacing getFacing() {
        return this.pistonFacing;
    }

    public boolean shouldPistonHeadBeRendered() {
        return this.shouldHeadBeRendered;
    }

    /**
     * Get interpolated progress value (between lastProgress and progress) given the fractional time between ticks as an
     * argument
     */
    public float getProgress(float ticks) {
        if (ticks > 1.0F) {
            ticks = 1.0F;
        }

        return this.lastProgress + (this.progress - this.lastProgress) * ticks;
    }

    public float getOffsetX(float ticks) {
        return this.extending ? (this.getProgress(ticks) - 1.0F) * (float) this.pistonFacing.getFrontOffsetX() : (1.0F - this.getProgress(ticks)) * (float) this.pistonFacing.getFrontOffsetX();
    }

    public float getOffsetY(float ticks) {
        return this.extending ? (this.getProgress(ticks) - 1.0F) * (float) this.pistonFacing.getFrontOffsetY() : (1.0F - this.getProgress(ticks)) * (float) this.pistonFacing.getFrontOffsetY();
    }

    public float getOffsetZ(float ticks) {
        return this.extending ? (this.getProgress(ticks) - 1.0F) * (float) this.pistonFacing.getFrontOffsetZ() : (1.0F - this.getProgress(ticks)) * (float) this.pistonFacing.getFrontOffsetZ();
    }

    private void launchWithSlimeBlock(float p_145863_1_, float p_145863_2_) {
        if (this.extending) {
            p_145863_1_ = 1.0F - p_145863_1_;
        } else {
            --p_145863_1_;
        }

        AxisAlignedBB var3 = Blocks.piston_extension.getBoundingBox(this.worldObj, this.pos, this.pistonState, p_145863_1_, this.pistonFacing);

        if (var3 != null) {
            List var4 = this.worldObj.getEntitiesWithinAABBExcludingEntity((Entity) null, var3);

            if (!var4.isEmpty()) {
                this.field_174933_k.addAll(var4);
                Iterator var5 = this.field_174933_k.iterator();

                while (var5.hasNext()) {
                    Entity var6 = (Entity) var5.next();

                    if (this.pistonState.getBlock() == Blocks.slime_block && this.extending) {
                        switch (TileEntityPiston.SwitchAxis.field_177248_a[this.pistonFacing.getAxis().ordinal()]) {
                            case 1:
                                var6.motionX = (double) this.pistonFacing.getFrontOffsetX();
                                break;

                            case 2:
                                var6.motionY = (double) this.pistonFacing.getFrontOffsetY();
                                break;

                            case 3:
                                var6.motionZ = (double) this.pistonFacing.getFrontOffsetZ();
                        }
                    } else {
                        var6.moveEntity((double) (p_145863_2_ * (float) this.pistonFacing.getFrontOffsetX()), (double) (p_145863_2_ * (float) this.pistonFacing.getFrontOffsetY()), (double) (p_145863_2_ * (float) this.pistonFacing.getFrontOffsetZ()));
                    }
                }

                this.field_174933_k.clear();
            }
        }
    }

    /**
     * removes a piston's tile entity (and if the piston is moving, stops it)
     */
    public void clearPistonTileEntity() {
        if (this.lastProgress < 1.0F && this.worldObj != null) {
            this.lastProgress = this.progress = 1.0F;
            this.worldObj.removeTileEntity(this.pos);
            this.invalidate();

            if (this.worldObj.getBlockState(this.pos).getBlock() == Blocks.piston_extension) {
                this.worldObj.setBlockState(this.pos, this.pistonState, 3);
                this.worldObj.notifyBlockOfStateChange(this.pos, this.pistonState.getBlock());
            }
        }
    }

    /**
     * Like the old updateEntity(), except more generic.
     */
    public void update() {
        this.lastProgress = this.progress;

        if (this.lastProgress >= 1.0F) {
            this.launchWithSlimeBlock(1.0F, 0.25F);
            this.worldObj.removeTileEntity(this.pos);
            this.invalidate();

            if (this.worldObj.getBlockState(this.pos).getBlock() == Blocks.piston_extension) {
                this.worldObj.setBlockState(this.pos, this.pistonState, 3);
                this.worldObj.notifyBlockOfStateChange(this.pos, this.pistonState.getBlock());
            }
        } else {
            this.progress += 0.5F;

            if (this.progress >= 1.0F) {
                this.progress = 1.0F;
            }

            if (this.extending) {
                this.launchWithSlimeBlock(this.progress, this.progress - this.lastProgress + 0.0625F);
            }
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        this.pistonState = Block.getBlockById(compound.getInteger("blockId")).getStateFromMeta(compound.getInteger("blockData"));
        this.pistonFacing = EnumFacing.getFront(compound.getInteger("facing"));
        this.lastProgress = this.progress = compound.getFloat("progress");
        this.extending = compound.getBoolean("extending");
    }

    public void writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setInteger("blockId", Block.getIdFromBlock(this.pistonState.getBlock()));
        compound.setInteger("blockData", this.pistonState.getBlock().getMetaFromState(this.pistonState));
        compound.setInteger("facing", this.pistonFacing.getIndex());
        compound.setFloat("progress", this.lastProgress);
        compound.setBoolean("extending", this.extending);
    }

    static final class SwitchAxis {
        static final int[] field_177248_a = new int[EnumFacing.Axis.values().length];

        static {
            try {
                field_177248_a[EnumFacing.Axis.X.ordinal()] = 1;
            } catch (NoSuchFieldError var3) {
                ;
            }

            try {
                field_177248_a[EnumFacing.Axis.Y.ordinal()] = 2;
            } catch (NoSuchFieldError var2) {
                ;
            }

            try {
                field_177248_a[EnumFacing.Axis.Z.ordinal()] = 3;
            } catch (NoSuchFieldError var1) {
                ;
            }
        }
    }
}
