package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

/**
 * Created by nuf on 3/24/2016.
 */
public class BlockBoundingBoxSubject extends Subject {

    private Block block;
    private AxisAlignedBB boundingBox;
    private BlockPos blockPos;

    public BlockBoundingBoxSubject(Block block, AxisAlignedBB boundingBox, BlockPos blockPos) {
        this.block = block;
        this.boundingBox = boundingBox;
        this.blockPos = blockPos;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    public void setBlockPos(BlockPos blockPos) {
        this.blockPos = blockPos;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public AxisAlignedBB getBoundingBox() {
        return boundingBox;
    }

    public void setBoundingBox(AxisAlignedBB boundingBox) {
        this.boundingBox = boundingBox;
    }
}
