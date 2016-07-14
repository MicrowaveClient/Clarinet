package net.minecraft.world.gen.feature;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class WorldGenFire extends WorldGenerator {

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (int var4 = 0; var4 < 64; ++var4) {
            BlockPos var5 = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(var5) && worldIn.getBlockState(var5.down()).getBlock() == Blocks.netherrack) {
                worldIn.setBlockState(var5, Blocks.fire.getDefaultState(), 2);
            }
        }

        return true;
    }
}
