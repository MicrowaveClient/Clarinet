package net.minecraft.client.renderer.block.model;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;

public class BreakingFour extends BakedQuad {
    private final TextureAtlasSprite texture;

    public BreakingFour(BakedQuad p_i46217_1_, TextureAtlasSprite textureIn) {
        super(Arrays.copyOf(p_i46217_1_.getVertexData(), p_i46217_1_.getVertexData().length), p_i46217_1_.tintIndex, FaceBakery.getFacingFromVertexData(p_i46217_1_.getVertexData()));
        this.texture = textureIn;
        this.func_178217_e();
    }

    private void func_178217_e() {
        for (int var1 = 0; var1 < 4; ++var1) {
            this.func_178216_a(var1);
        }
    }

    private void func_178216_a(int p_178216_1_) {
        int step = this.vertexData.length / 4;
        int var2 = step * p_178216_1_;
        float var3 = Float.intBitsToFloat(this.vertexData[var2]);
        float var4 = Float.intBitsToFloat(this.vertexData[var2 + 1]);
        float var5 = Float.intBitsToFloat(this.vertexData[var2 + 2]);
        float var6 = 0.0F;
        float var7 = 0.0F;

        switch (BreakingFour.SwitchEnumFacing.FACING_LOOKUP[this.face.ordinal()]) {
            case 1:
                var6 = var3 * 16.0F;
                var7 = (1.0F - var5) * 16.0F;
                break;

            case 2:
                var6 = var3 * 16.0F;
                var7 = var5 * 16.0F;
                break;

            case 3:
                var6 = (1.0F - var3) * 16.0F;
                var7 = (1.0F - var4) * 16.0F;
                break;

            case 4:
                var6 = var3 * 16.0F;
                var7 = (1.0F - var4) * 16.0F;
                break;

            case 5:
                var6 = var5 * 16.0F;
                var7 = (1.0F - var4) * 16.0F;
                break;

            case 6:
                var6 = (1.0F - var5) * 16.0F;
                var7 = (1.0F - var4) * 16.0F;
        }

        this.vertexData[var2 + 4] = Float.floatToRawIntBits(this.texture.getInterpolatedU((double) var6));
        this.vertexData[var2 + 4 + 1] = Float.floatToRawIntBits(this.texture.getInterpolatedV((double) var7));
    }

    static final class SwitchEnumFacing {
        static final int[] FACING_LOOKUP = new int[EnumFacing.values().length];

        static {
            try {
                FACING_LOOKUP[EnumFacing.DOWN.ordinal()] = 1;
            } catch (NoSuchFieldError var6) {
                ;
            }

            try {
                FACING_LOOKUP[EnumFacing.UP.ordinal()] = 2;
            } catch (NoSuchFieldError var5) {
                ;
            }

            try {
                FACING_LOOKUP[EnumFacing.NORTH.ordinal()] = 3;
            } catch (NoSuchFieldError var4) {
                ;
            }

            try {
                FACING_LOOKUP[EnumFacing.SOUTH.ordinal()] = 4;
            } catch (NoSuchFieldError var3) {
                ;
            }

            try {
                FACING_LOOKUP[EnumFacing.WEST.ordinal()] = 5;
            } catch (NoSuchFieldError var2) {
                ;
            }

            try {
                FACING_LOOKUP[EnumFacing.EAST.ordinal()] = 6;
            } catch (NoSuchFieldError var1) {
                ;
            }
        }
    }
}
