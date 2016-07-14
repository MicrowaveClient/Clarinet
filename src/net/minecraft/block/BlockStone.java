package net.minecraft.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import java.util.List;
import java.util.Random;

public class BlockStone extends Block {
    public static final PropertyEnum VARIANT = PropertyEnum.create("variant", BlockStone.EnumType.class);

    public BlockStone() {
        super(Material.rock);
        this.setDefaultState(this.blockState.getBaseState().withProperty(VARIANT, BlockStone.EnumType.STONE));
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    /**
     * Get the Item that this Block should drop when harvested.
     *
     * @param fortune the level of the Fortune enchantment on the player's tool
     */
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return state.getValue(VARIANT) == BlockStone.EnumType.STONE ? Item.getItemFromBlock(Blocks.cobblestone) : Item.getItemFromBlock(Blocks.stone);
    }

    /**
     * Gets the metadata of the item this Block can drop. This method is called when the block gets destroyed. It
     * returns the metadata of the dropped item based on the old metadata of the block.
     */
    public int damageDropped(IBlockState state) {
        return ((BlockStone.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    /**
     * returns a list of blocks with the same ID, but different meta (eg: wood returns 4 blocks)
     */
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        BlockStone.EnumType[] var4 = BlockStone.EnumType.values();
        int var5 = var4.length;

        for (int var6 = 0; var6 < var5; ++var6) {
            BlockStone.EnumType var7 = var4[var6];
            list.add(new ItemStack(itemIn, 1, var7.getMetadata()));
        }
    }

    /**
     * Convert the given metadata into a BlockState for this Block
     */
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(VARIANT, BlockStone.EnumType.byMetadata(meta));
    }

    /**
     * Convert the BlockState into the correct metadata value
     */
    public int getMetaFromState(IBlockState state) {
        return ((BlockStone.EnumType) state.getValue(VARIANT)).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, new IProperty[]{VARIANT});
    }

    public static enum EnumType implements IStringSerializable {
        STONE("STONE", 0, 0, "stone"),
        GRANITE("GRANITE", 1, 1, "granite"),
        GRANITE_SMOOTH("GRANITE_SMOOTH", 2, 2, "smooth_granite", "graniteSmooth"),
        DIORITE("DIORITE", 3, 3, "diorite"),
        DIORITE_SMOOTH("DIORITE_SMOOTH", 4, 4, "smooth_diorite", "dioriteSmooth"),
        ANDESITE("ANDESITE", 5, 5, "andesite"),
        ANDESITE_SMOOTH("ANDESITE_SMOOTH", 6, 6, "smooth_andesite", "andesiteSmooth");
        private static final BlockStone.EnumType[] META_LOOKUP = new BlockStone.EnumType[values().length];
        private final int meta;
        private final String name;
        private final String unlocalizedName;

        private EnumType(String p_i45680_1_, int p_i45680_2_, int meta, String name) {
            this(p_i45680_1_, p_i45680_2_, meta, name, name);
        }

        private EnumType(String p_i45681_1_, int p_i45681_2_, int meta, String name, String unlocalizedName) {
            this.meta = meta;
            this.name = name;
            this.unlocalizedName = unlocalizedName;
        }

        public int getMetadata() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public static BlockStone.EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName() {
            return this.name;
        }

        public String getUnlocalizedName() {
            return this.unlocalizedName;
        }

        static {
            BlockStone.EnumType[] var0 = values();
            int var1 = var0.length;

            for (int var2 = 0; var2 < var1; ++var2) {
                BlockStone.EnumType var3 = var0[var2];
                META_LOOKUP[var3.getMetadata()] = var3;
            }
        }
    }
}
