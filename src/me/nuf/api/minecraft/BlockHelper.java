package me.nuf.api.minecraft;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;

public class BlockHelper {

    public static Block getBlock(double x, double y, double z) {
        return Minecraft.getMinecraft().theWorld.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static Block getBlockUnder(float height) {
        return getBlock(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY - height, Minecraft.getMinecraft().thePlayer.posZ);
    }

    public static Block getBlockAbove(float height) {
        return getBlock(Minecraft.getMinecraft().thePlayer.posX, Minecraft.getMinecraft().thePlayer.posY + height, Minecraft.getMinecraft().thePlayer.posZ);
    }

    public static boolean isOnBlock(EntityPlayer entity, Block blockOn) {
        int y = (int) entity.getEntityBoundingBox().offset(0.0D, -0.01D, 0.0D).minY;
        for (int x = MathHelper.floor_double(entity.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(entity.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(entity.getEntityBoundingBox().minZ); z < MathHelper
                    .floor_double(entity.getEntityBoundingBox().maxZ) + 1; z++) {
                Block block = getBlock(x, y, z);
                if (block != null && !(block instanceof BlockAir))
                    if (block == blockOn)
                        return true;
            }
        }
        return false;
    }

    public static boolean isInsideBlock(EntityPlayer entity) {
        for (int x = MathHelper.floor_double(entity.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(entity.getEntityBoundingBox().maxX) + 1; x++) {
            for (int y = MathHelper.floor_double(entity.getEntityBoundingBox().minY); y < MathHelper
                    .floor_double(entity.getEntityBoundingBox().maxY) + 1; y++) {
                for (int z = MathHelper.floor_double(entity.getEntityBoundingBox().minZ); z < MathHelper
                        .floor_double(entity.getEntityBoundingBox().maxZ) + 1; z++) {
                    Block block = getBlock(x, y, z);
                    if (block == null || block instanceof BlockAir)
                        continue;
                    AxisAlignedBB boundingBox = block.getSelectedBoundingBox(Minecraft.getMinecraft().theWorld,
                            new BlockPos(x, y, z));
                    if (boundingBox != null && entity.getEntityBoundingBox().intersectsWith(boundingBox))
                        return true;
                }
            }
        }
        return false;
    }

    public static byte findBestTool(int x, int y, int z) {
        byte bestItem = -1;
        float strength = 1.0F;
        Block block = getBlock(x, y, z);
        if (block.getMaterial() == Material.air)
            return -1;
        for (byte index = 0; index < 36; index++) {
            try {
                ItemStack item = Minecraft.getMinecraft().thePlayer.inventory.getStackInSlot(index);
                if (item != null)
                    if (item.getStrVsBlock(block) > strength) {
                        strength = item.getStrVsBlock(block);
                        bestItem = index;
                    }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bestItem;
    }

    public static boolean isOnLiquid(EntityPlayer entity) {
        int y = (int) entity.getEntityBoundingBox().offset(0.0D, -0.01D, 0.0D).minY;
        for (int x = MathHelper.floor_double(entity.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(entity.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(entity.getEntityBoundingBox().minZ); z < MathHelper
                    .floor_double(entity.getEntityBoundingBox().maxZ) + 1; z++) {
                Block block = getBlock(x, y, z);
                if (block != null && !(block instanceof BlockAir))
                    if (block instanceof BlockLiquid)
                        return true;
            }
        }
        return false;
    }

    public static boolean isInLiquid(EntityPlayer entity) {
        int y = (int) entity.getEntityBoundingBox().minY;
        for (int x = MathHelper.floor_double(entity.getEntityBoundingBox().minX); x < MathHelper
                .floor_double(entity.getEntityBoundingBox().maxX) + 1; x++) {
            for (int z = MathHelper.floor_double(entity.getEntityBoundingBox().minZ); z < MathHelper
                    .floor_double(entity.getEntityBoundingBox().maxZ) + 1; z++) {
                Block block = getBlock(x, y, z);
                if (block != null && !(block instanceof BlockAir))
                    if (block instanceof BlockLiquid)
                        return true;
            }
        }
        return false;
    }

}