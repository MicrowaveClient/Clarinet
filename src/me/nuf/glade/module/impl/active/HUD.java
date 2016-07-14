package me.nuf.glade.module.impl.active;

import me.nuf.api.interfaces.Toggleable;
import me.nuf.api.minecraft.EntityHelper;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Module;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.module.impl.combat.AutoPot;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.GameOverlaySubject;
import me.nuf.glade.subjects.HurtcamSubject;
import me.nuf.glade.subjects.RenderFireSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.Collection;
import java.util.List;

/**
 * Created by nuf on 3/19/2016.
 */
public final class HUD extends Module {

    private final Property<Boolean> watermarkColor = new Property<>(false, "Watermark-Color", "watermarkcolor", "wmc", "wc"), direction = new Property<>(true, "Direction", "dir"), noHurtcam = new Property<>(true, "NoHurtcam", "hurtcam", "nh"), armor = new Property<>(true, "Armor", "armour"), noFire = new Property<>(true, "NoFire", "fire", "nf"), potionEffects = new Property<>(true, "Potion-Effects", "pe", "potion"), serverBrand = new Property<>(false, "Server-Brand", "server", "sb"), watermark = new Property<>(true, "Watermark", "wm"), durability = new Property<>(true, "Durability", "dura", "d"), coordinates = new Property<>(true, "Coords", "coord", "c"), arraylist = new Property<>(true, "ArrayList", "al", "array", "list");
    public final Property<Boolean> shadow = new Property<>(true, "Shadow", "ss"), capes = new Property<>(true, "Capes", "cape", "c");
    public final EnumProperty<Color> color = new EnumProperty<>(Color.Red, "Color", "c");
    private final EnumProperty<Ordering> ordering = new EnumProperty<>(Ordering.ABC, "Ordering", "order", "o");

    public HUD() {
        super("HUD", "overlay");
        this.offerProperties(watermark, watermarkColor, direction, noHurtcam, armor, noFire, capes, potionEffects, serverBrand, ordering, color, shadow, coordinates, durability, arraylist);
        Glade.getInstance().getSubjectManager().register(new Listener<GameOverlaySubject>("hud_game_overlay_listener") {
            @Override
            public void call(GameOverlaySubject subject) {
                if (minecraft.gameSettings.showDebugInfo)
                    return;
                subject.y = -7;
                if (watermark.getValue())
                    renderWatermark(subject);
                if (serverBrand.getValue())
                    renderServerBrand(subject);
                if (arraylist.getValue())
                    renderArrayList(subject);
                if (coordinates.getValue())
                    renderCoordinates(subject);
                if (armor.getValue())
                    renderArmor(subject);
                if (direction.getValue())
                    renderDirection(subject);

                AutoPot autoPot = (AutoPot) Glade.getInstance().getModuleManager().getModuleByAlias("AutoPot");

                if (autoPot != null && autoPot.isEnabled())
                    minecraft.fontRendererObj.drawStringWithShadow(String.format("Potions: %s", autoPot.getPotionCount()), 2, subject.y += 9, 0xFFFFFFFF);

                if (durability.getValue())
                    renderDurability(subject);
                if (potionEffects.getValue())
                    renderPotionEffects(subject);
            }
        });
        Glade.getInstance().getSubjectManager().register(new Listener<RenderFireSubject>("hud_render_fire_listener") {
            @Override
            public void call(RenderFireSubject subject) {
                if (noFire.getValue())
                    subject.setCancelled(true);
            }
        });
        Glade.getInstance().getSubjectManager().register(new Listener<HurtcamSubject>("hud_hurtcam_listener") {
            @Override
            public void call(HurtcamSubject subject) {
                if (noHurtcam.getValue())
                    subject.setCancelled(true);
            }
        });
    }

    private void renderWatermark(GameOverlaySubject subject) {
        minecraft.fontRendererObj.drawStringWithShadow(String.format("%s%s b%s", watermarkColor.getValue() ? EnumChatFormatting.DARK_AQUA : EnumChatFormatting.WHITE, Glade.TITLE, Glade.BUILD), 2, subject.y += 9, 0xFFFFFFFF);
    }

    private void renderDurability(GameOverlaySubject subject) {
        if (minecraft.thePlayer.inventory.getCurrentItem() != null)
            if (minecraft.thePlayer.inventory.getCurrentItem().isItemStackDamageable()) {
                int color = 0xFF00FF00;
                int damage = minecraft.thePlayer.inventory.getCurrentItem().getMaxDamage() - minecraft.thePlayer.inventory.getCurrentItem().getItemDamage();
                if (damage < 50)
                    color = 0xFFFF0000;
                minecraft.fontRendererObj.drawStringWithShadow(String.format("{%s}", Integer.toString(damage)), 2, subject.y += 9, color);
            }
    }

    private void renderArmor(GameOverlaySubject subject) {
        int x = 15;
        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        for (int index = 3; index >= 0; index--) {
            ItemStack stack = minecraft.thePlayer.inventory.armorInventory[index];
            if (stack != null) {
                int y;
                if (minecraft.thePlayer.isInsideOfMaterial(Material.water) && !minecraft.thePlayer.capabilities.isCreativeMode) {
                    y = 65;
                } else if (minecraft.thePlayer.capabilities.isCreativeMode) {
                    y = 38;
                } else {
                    y = 55;
                }
                minecraft.getRenderItem().renderItemIntoGUI(stack,
                        subject.getScaledResolution().getScaledWidth() / 2 + x,
                        subject.getScaledResolution().getScaledHeight() - y);
                minecraft.getRenderItem().renderItemOverlays(minecraft.fontRendererObj, stack,
                        subject.getScaledResolution().getScaledWidth() / 2 + x,
                        subject.getScaledResolution().getScaledHeight() - y);
                x += 18;
            }
        }
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void renderServerBrand(GameOverlaySubject subject) {
        minecraft.fontRendererObj.drawStringWithShadow(minecraft.getCurrentServerData() == null ? "Vanilla" : minecraft.getCurrentServerData().gameVersion, 2, subject.y += 9, 0xFFAAAAAA);
    }

    private void renderPotionEffects(GameOverlaySubject subject) {
        Collection<PotionEffect> effects = minecraft.thePlayer.getActivePotionEffects();
        if (!effects.isEmpty()) {
            int y = 0;
            for (PotionEffect effect : effects) {
                Potion potion = Potion.potionTypes[effect.getPotionID()];
                String name = StatCollector.translateToLocal(potion.getName());
                name += String.format(" (%s / %s)", effect.getAmplifier() + 1, Potion.getDurationString(effect));
                int align = subject.getScaledResolution().getScaledWidth() - minecraft.fontRendererObj.getStringWidth(name) - 2;
                minecraft.fontRendererObj.drawStringWithShadow(name, align, (subject.getScaledResolution().getScaledHeight() - (minecraft.currentScreen instanceof GuiChat ? 23 : 9)) + y, potion.getLiquidColor());
                y -= 9;
            }
        }
    }

    private void renderArrayList(GameOverlaySubject subject) {
        List<Module> modules = Glade.getInstance().getModuleManager().getElements();

        switch (ordering.getValue()) {
            case ABC:
                modules.sort((mod1, mod2) -> mod1.getTag().compareTo(mod2.getTag()));
                break;
            case Length:
                modules.sort((mod1, mod2) -> minecraft.fontRendererObj.getStringWidth(mod2.getTag()) - minecraft.fontRendererObj.getStringWidth(mod1.getTag()));
                break;
        }

        int posY = -7;
        for (Module module : modules)
            if (module instanceof Toggleable) {
                ToggleableModule toggleableModule = (ToggleableModule) module;
                if (toggleableModule.isEnabled() && toggleableModule.isDrawn()) {
                    int tagWidth = minecraft.fontRendererObj.getStringWidth(toggleableModule.getTag());
                    minecraft.fontRendererObj.drawStringWithShadow(toggleableModule.getTag(), (subject.getScaledResolution().getScaledWidth() - tagWidth) - 2, posY += 9, toggleableModule.getColor());
                }
            }
    }

    private void renderCoordinates(GameOverlaySubject subject) {
        minecraft.fontRendererObj.drawStringWithShadow(String.format("XYZ \2477%s, %s, %s", (int) minecraft.thePlayer.posX, (int) minecraft.thePlayer.posY, (int) minecraft.thePlayer.posZ), 2, subject.getScaledResolution().getScaledHeight() - (minecraft.currentScreen instanceof GuiChat ? 23 : 9), 0xFFFFFFFF);
    }

    private void renderDirection(GameOverlaySubject subject) {
        if (minecraft.currentScreen instanceof GuiChat)
            return;
        minecraft.fontRendererObj.drawStringWithShadow(String.format("\2477%s", EntityHelper.getDirection().toUpperCase()), 2, subject.getScaledResolution().getScaledHeight() - (!coordinates.getValue() ? 9 : 18), 0xFFFFFFFF);
    }

    public enum Color {
        Red, Green, Blue, Health
    }

    public enum Ordering {
        ABC, Length
    }
}
