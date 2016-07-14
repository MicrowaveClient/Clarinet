package net.minecraft.client.renderer.entity.layers;

import me.nuf.glade.core.Glade;
import me.nuf.glade.module.impl.active.HUD;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StringUtils;

public class LayerCape implements LayerRenderer {
    private final RenderPlayer playerRenderer;

    public LayerCape(RenderPlayer playerRendererIn) {
        this.playerRenderer = playerRendererIn;
    }

    public void doRenderLayer(AbstractClientPlayer clientPlayer, float p_177166_2_, float p_177166_3_, float p_177166_4_, float p_177166_5_, float p_177166_6_, float p_177166_7_, float p_177166_8_) {
        boolean player = Minecraft.getMinecraft().getSession().getUsername()
                .equals(StringUtils.stripControlCodes(clientPlayer.getCommandSenderName()));
        boolean friend = Glade.getInstance().getFriendManager()
                .isFriend(StringUtils.stripControlCodes(clientPlayer.getCommandSenderName()));
        if ((clientPlayer.hasPlayerInfo() || player || friend) && !clientPlayer.isInvisible()
                && clientPlayer.isWearing(EnumPlayerModelParts.CAPE)) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            HUD hud = (HUD) Glade.getInstance().getModuleManager().getModuleByAlias("HUD");
            this.playerRenderer.bindTexture(hud.capes.getValue() ? (friend || player) ? new ResourceLocation("textures/glade.png") : clientPlayer.getLocationCape() : clientPlayer.getLocationCape());
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, 0.125F);
            double var9 = clientPlayer.prevChasingPosX + (clientPlayer.chasingPosX - clientPlayer.prevChasingPosX) * (double) p_177166_4_ - (clientPlayer.prevPosX + (clientPlayer.posX - clientPlayer.prevPosX) * (double) p_177166_4_);
            double var11 = clientPlayer.prevChasingPosY + (clientPlayer.chasingPosY - clientPlayer.prevChasingPosY) * (double) p_177166_4_ - (clientPlayer.prevPosY + (clientPlayer.posY - clientPlayer.prevPosY) * (double) p_177166_4_);
            double var13 = clientPlayer.prevChasingPosZ + (clientPlayer.chasingPosZ - clientPlayer.prevChasingPosZ) * (double) p_177166_4_ - (clientPlayer.prevPosZ + (clientPlayer.posZ - clientPlayer.prevPosZ) * (double) p_177166_4_);
            float var15 = clientPlayer.prevRenderYawOffset + (clientPlayer.renderYawOffset - clientPlayer.prevRenderYawOffset) * p_177166_4_;
            double var16 = (double) MathHelper.sin(var15 * (float) Math.PI / 180.0F);
            double var18 = (double) (-MathHelper.cos(var15 * (float) Math.PI / 180.0F));
            float var20 = (float) var11 * 10.0F;
            var20 = MathHelper.clamp_float(var20, -6.0F, 32.0F);
            float var21 = (float) (var9 * var16 + var13 * var18) * 100.0F;
            float var22 = (float) (var9 * var18 - var13 * var16) * 100.0F;

            if (var21 < 0.0F) {
                var21 = 0.0F;
            }

            if (var21 > 165.0F) {
                var21 = 165.0F;
            }

            float var23 = clientPlayer.prevCameraYaw + (clientPlayer.cameraYaw - clientPlayer.prevCameraYaw) * p_177166_4_;
            var20 += MathHelper.sin((clientPlayer.prevDistanceWalkedModified + (clientPlayer.distanceWalkedModified - clientPlayer.prevDistanceWalkedModified) * p_177166_4_) * 6.0F) * 32.0F * var23;

            if (clientPlayer.isSneaking()) {
                var20 += 25.0F;
                GlStateManager.translate(0.0F, 0.142F, -0.0178F);
            }

            GlStateManager.rotate(6.0F + var21 / 2.0F + var20, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(var22 / 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-var22 / 2.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            this.playerRenderer.getPlayerModel().renderCape(0.0625F);
            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures() {
        return false;
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
        this.doRenderLayer((AbstractClientPlayer) entitylivingbaseIn, p_177141_2_, p_177141_3_, partialTicks, p_177141_5_, p_177141_6_, p_177141_7_, scale);
    }
}
