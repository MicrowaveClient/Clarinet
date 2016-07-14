package net.minecraft.world.biome;

import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;
import net.minecraft.world.gen.feature.WorldGenIcePath;
import net.minecraft.world.gen.feature.WorldGenIceSpike;
import net.minecraft.world.gen.feature.WorldGenTaiga2;

import java.util.Random;

public class BiomeGenSnow extends BiomeGenBase {
    private boolean field_150615_aC;
    private WorldGenIceSpike field_150616_aD = new WorldGenIceSpike();
    private WorldGenIcePath field_150617_aE = new WorldGenIcePath(4);

    public BiomeGenSnow(int p_i45378_1_, boolean p_i45378_2_) {
        super(p_i45378_1_);
        this.field_150615_aC = p_i45378_2_;

        if (p_i45378_2_) {
            this.topBlock = Blocks.snow.getDefaultState();
        }

        this.spawnableCreatureList.clear();
    }

    public void decorate(World worldIn, Random rand, BlockPos pos) {
        if (this.field_150615_aC) {
            int var4;
            int var5;
            int var6;

            for (var4 = 0; var4 < 3; ++var4) {
                var5 = rand.nextInt(16) + 8;
                var6 = rand.nextInt(16) + 8;
                this.field_150616_aD.generate(worldIn, rand, worldIn.getHeight(pos.add(var5, 0, var6)));
            }

            for (var4 = 0; var4 < 2; ++var4) {
                var5 = rand.nextInt(16) + 8;
                var6 = rand.nextInt(16) + 8;
                this.field_150617_aE.generate(worldIn, rand, worldIn.getHeight(pos.add(var5, 0, var6)));
            }
        }

        super.decorate(worldIn, rand, pos);
    }

    public WorldGenAbstractTree genBigTreeChance(Random rand) {
        return new WorldGenTaiga2(false);
    }

    protected BiomeGenBase createMutatedBiome(int p_180277_1_) {
        BiomeGenBase var2 = (new BiomeGenSnow(p_180277_1_, true)).func_150557_a(13828095, true).setBiomeName(this.biomeName + " Spikes").setEnableSnow().setTemperatureRainfall(0.0F, 0.5F).setHeight(new BiomeGenBase.Height(this.minHeight + 0.1F, this.maxHeight + 0.1F));
        var2.minHeight = this.minHeight + 0.3F;
        var2.maxHeight = this.maxHeight + 0.4F;
        return var2;
    }
}
