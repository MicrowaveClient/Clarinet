package net.minecraft.world.gen.feature;

import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenReed extends WorldGenerator {

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (int var4 = 0; var4 < 20; ++var4) {
            BlockPos var5 = position.add(rand.nextInt(4) - rand.nextInt(4), 0, rand.nextInt(4) - rand.nextInt(4));

            if (worldIn.isAirBlock(var5)) {
                BlockPos var6 = var5.down();

                if (worldIn.getBlockState(var6.west()).getBlock().getMaterial() == Material.water || worldIn.getBlockState(var6.east()).getBlock().getMaterial() == Material.water || worldIn.getBlockState(var6.north()).getBlock().getMaterial() == Material.water || worldIn.getBlockState(var6.south()).getBlock().getMaterial() == Material.water) {
                    int var7 = 2 + rand.nextInt(rand.nextInt(3) + 1);

                    for (int var8 = 0; var8 < var7; ++var8) {
                        if (Blocks.reeds.canBlockStay(worldIn, var5)) {
                            worldIn.setBlockState(var5.up(var8), Blocks.reeds.getDefaultState(), 2);
                        }
                    }
                }
            }
        }

        return true;
    }
}
