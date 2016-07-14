package net.minecraft.world.gen;

import net.minecraft.block.BlockBush;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenerator;

import java.util.Random;

public class GeneratorBushFeature extends WorldGenerator {
    private BlockBush field_175908_a;

    public GeneratorBushFeature(BlockBush p_i45633_1_) {
        this.field_175908_a = p_i45633_1_;
    }

    public boolean generate(World worldIn, Random rand, BlockPos position) {
        for (int var4 = 0; var4 < 64; ++var4) {
            BlockPos var5 = position.add(rand.nextInt(8) - rand.nextInt(8), rand.nextInt(4) - rand.nextInt(4), rand.nextInt(8) - rand.nextInt(8));

            if (worldIn.isAirBlock(var5) && (!worldIn.provider.getHasNoSky() || var5.getY() < 255) && this.field_175908_a.canBlockStay(worldIn, var5, this.field_175908_a.getDefaultState())) {
                worldIn.setBlockState(var5, this.field_175908_a.getDefaultState(), 2);
            }
        }

        return true;
    }
}
