package me.nuf.glade.module.impl.render;
import me.nuf.api.render.RenderMethods;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.RenderSubject;
import me.nuf.glade.subjects.SpecialRenderSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;

/**
 * Created by nuf and Anodise on 3/23/2016.
 */
public final class Nametags extends ToggleableModule {

    private final Property<Boolean> armor = new Property<>(true, "Armor", "a"), health = new Property<>(true, "Health", "h");
    private final NumberProperty<Float> scaling = new NumberProperty<>(0.0030F, 0.001F, 0.0100F, "Scaling", "scale", "s");

    public Nametags() {
        super(new String[]{"Nametags", "tags", "tag", "nt"}, true, 0xFF6252df, Category.RENDER);
        this.offerProperties(armor, health, scaling);
        this.addListeners(new Listener<SpecialRenderSubject>("nametags_special_render_listener") {
            @Override
            public void call(SpecialRenderSubject subject) {
                subject.setCancelled(true);
            }
        }, new Listener<RenderSubject>("nametags_render_listener") {
            @Override
            public void call(RenderSubject subject) {
                for (Object o : minecraft.theWorld.playerEntities) {
                    Entity entity = (Entity) o;
                    if (entity instanceof EntityPlayer) {
                        if (entity != minecraft.thePlayer && entity.isEntityAlive()) {
                            double x = interpolate(entity.lastTickPosX, entity.posX, subject.getPartialTicks())
                                    - minecraft.getRenderManager().renderPosX;
                            double y = interpolate(entity.lastTickPosY, entity.posY, subject.getPartialTicks())
                                    - minecraft.getRenderManager().renderPosY;
                            double z = interpolate(entity.lastTickPosZ, entity.posZ, subject.getPartialTicks())
                                    - minecraft.getRenderManager().renderPosZ;
                            renderNameTag((EntityPlayer) entity, x, y, z, subject.getPartialTicks());
                        }
                    }
                }
            }
        });
        setDrawn(false);
        setEnabled(true);
    }

    private void renderNameTag(EntityPlayer player, double x, double y, double z, float delta) {
        double tempY = y;
        tempY += (player.isSneaking() ? 0.5D : 0.7D);

        Entity camera = minecraft.getRenderViewEntity();
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = interpolate(camera.prevPosZ, camera.posZ, delta);

        double distance = camera.getDistance(x + minecraft.getRenderManager().viewerPosX, y + minecraft.getRenderManager().viewerPosY,
                z + minecraft.getRenderManager().viewerPosZ);
        int width = minecraft.fontRendererObj.getStringWidth(this.getDisplayName(player)) / 2;
        double scale = 0.0018 + scaling.getValue() * distance;

        if (distance <= 8)
            scale = 0.0245D;

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1, -1500000);
        GlStateManager.disableLighting();
        GlStateManager.translate((float) x, (float) tempY + 1.4F, (float) z);
        GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(minecraft.getRenderManager().playerViewX, minecraft.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F,
                0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        GlStateManager.disableAlpha();
        RenderMethods.drawBorderedRectReliant(-width - 2, -(minecraft.fontRendererObj.FONT_HEIGHT + 1), width + 2F, 1, 1.8F, 0x55000400, 0x33000000);
        GlStateManager.enableAlpha();

        minecraft.fontRendererObj.drawStringWithShadow(this.getDisplayName(player), -width,
                -(minecraft.fontRendererObj.FONT_HEIGHT - 1), this.getDisplayColour(player));

        if (armor.getValue()) {
            GlStateManager.pushMatrix();
            int xOffset = 0;
            for (int index = 3; index >= 0; index--) {
                ItemStack stack = player.inventory.armorInventory[index];
                if (stack != null) {
                    xOffset -= 8;
                }
            }

            if (player.getCurrentEquippedItem() != null) {
                xOffset -= 8;
                ItemStack renderStack = player.getCurrentEquippedItem().copy();
                if (renderStack.hasEffect()
                        && (renderStack.getItem() instanceof ItemTool || renderStack.getItem() instanceof ItemArmor)) {
                    renderStack.stackSize = 1;
                }

                this.renderItemStack(renderStack, xOffset, -26);
                xOffset += 16;
            }

            for (int index = 3; index >= 0; index--) {
                ItemStack stack = player.inventory.armorInventory[index];
                if (stack != null) {
                    ItemStack armourStack = stack.copy();
                    if (armourStack.hasEffect() && (armourStack.getItem() instanceof ItemTool
                            || armourStack.getItem() instanceof ItemArmor)) {
                        armourStack.stackSize = 1;
                    }

                    this.renderItemStack(armourStack, xOffset, -26);
                    xOffset += 16;
                }
            }

            GlStateManager.popMatrix();
        }

        camera.posX = originalPositionX;
        camera.posY = originalPositionY;
        camera.posZ = originalPositionZ;

        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.disablePolygonOffset();
        GlStateManager.doPolygonOffset(1, 1500000);
        GlStateManager.popMatrix();
    }

    private void renderItemStack(ItemStack stack, int x, int y) {
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(GL11.GL_ACCUM);

        RenderHelper.enableStandardItemLighting();
        minecraft.getRenderItem().zLevel = -150.0F;

        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        GlStateManager.disableAlpha();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableAlpha();
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();

        minecraft.getRenderItem().renderItemAndEffectIntoGUI(stack, x, y);

        minecraft.getRenderItem().zLevel = 0.0F;
        RenderHelper.disableStandardItemLighting();

        GlStateManager.disableCull();
        GlStateManager.enableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.disableLighting();
        GlStateManager.scale(0.5F, 0.5F, 0.5F);
        GlStateManager.disableDepth();
        renderEnchantmentText(stack, x, y);
        GlStateManager.enableDepth();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        GlStateManager.popMatrix();
    }

    private void renderEnchantmentText(ItemStack stack, int x, int y) {
        int enchantmentY = y - 24;

        if (stack.getEnchantmentTagList() != null && stack.getEnchantmentTagList().tagCount() >= 6) {
            minecraft.fontRendererObj.drawStringWithShadow("god", x * 2, enchantmentY, 0xFFc34d41);
            return;
        }

        int color = 0xFFAAAAAA;

        if (stack.getItem() instanceof ItemArmor) {
            int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.protection.effectId, stack);
            int projectileProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.projectileProtection.effectId,
                    stack);
            int blastProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.blastProtection.effectId,
                    stack);
            int fireProtectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireProtection.effectId, stack);
            int thornsLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.thorns.effectId, stack);
            int featherFallingLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.featherFalling.effectId, stack);

            if (protectionLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("pr" + protectionLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (projectileProtectionLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("pp" + projectileProtectionLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (blastProtectionLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("bp" + blastProtectionLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (fireProtectionLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("fp" + fireProtectionLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (thornsLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("tho" + thornsLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (featherFallingLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("ff" + featherFallingLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }
        }

        if (stack.getItem() instanceof ItemBow) {
            int powerLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.power.effectId, stack);
            int punchLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.punch.effectId, stack);
            int flameLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.flame.effectId, stack);

            if (powerLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("po" + powerLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (punchLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("pu" + punchLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (flameLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("fl" + flameLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }
        }

        if (stack.getItem() instanceof ItemSword) {
            int sharpnessLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.sharpness.effectId, stack);
            int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.knockback.effectId, stack);
            int fireAspectLevel = EnchantmentHelper.getEnchantmentLevel(Enchantment.fireAspect.effectId, stack);

            if (sharpnessLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("sh" + sharpnessLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (knockbackLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("kn" + knockbackLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }

            if (fireAspectLevel > 0) {
                minecraft.fontRendererObj.drawStringWithShadow("fa" + fireAspectLevel, x * 2, enchantmentY, color);
                enchantmentY += 8;
            }
        }

        if (stack.getItem() == Items.golden_apple && stack.hasEffect()) {
            minecraft.fontRendererObj.drawStringWithShadow("god", x * 2, enchantmentY, 0xFFc34d41);
        }
    }

    private String getDisplayName(EntityPlayer player) {
        String name = player.getDisplayName().getFormattedText();

        if (Glade.getInstance().getFriendManager().isFriend(player.getCommandSenderName())) {
            name = Glade.getInstance().getFriendManager().getFriendByLabel(player.getCommandSenderName()).getAlias();
        }

        if (name.contains(minecraft.getSession().getUsername())) {
            name = "You";
        }

        if (!health.getValue()) {
            return name;
        }

        float health = player.getHealth();

        EnumChatFormatting color;

        if (health > 18) {
            color = EnumChatFormatting.GREEN;
        } else if (health > 16) {
            color = EnumChatFormatting.DARK_GREEN;
        } else if (health > 12) {
            color = EnumChatFormatting.YELLOW;
        } else if (health > 8) {
            color = EnumChatFormatting.GOLD;
        } else if (health > 5) {
            color = EnumChatFormatting.RED;
        } else {
            color = EnumChatFormatting.DARK_RED;
        }

        if (Math.floor(health) == health) {
            name = name + color + " " + (health > 0 ? (int) Math.floor(health * 5) + "%" : "dead");
        } else {
            name = name + color + " " + (health > 0 ? (int) health * 5 + "%" : "dead");
        }

        return name;
    }

    private int getDisplayColour(EntityPlayer player) {
        int colour = 0xFFAAAAAA;
        if (Glade.getInstance().getFriendManager().isFriend(player.getCommandSenderName())) {
            return 0xFF66ffff;
        } else if (player.isInvisible()) {
            colour = 0xFFef0147;
        } else if (player.isSneaking()) {
            colour = 0xFF9d1995;
        }
        return colour;
    }

    private double interpolate(double previous, double current, float delta) {
        return (previous + (current - previous) * delta);
    }
}
