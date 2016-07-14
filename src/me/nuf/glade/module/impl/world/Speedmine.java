package me.nuf.glade.module.impl.world;

import me.nuf.api.minecraft.BlockHelper;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.module.impl.combat.KillAura;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.DamageBlockSubject;
import me.nuf.glade.subjects.TickSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.BlockPos;

/**
 * Created by nuf on 3/27/2016.
 */
public final class Speedmine extends ToggleableModule {
    private final Property<Boolean> fastfall = new Property<>(true, "Fast-Fall", "fall", "ff");
    private final NumberProperty<Float> speed = new NumberProperty<>(0.25F, 0.1F, 1F, "Speed", "S", "Spd");

    public Speedmine() {
        super(new String[]{"Speedy Gonzales", "speedmine", "fastbreak", "sg", "sm", "fb"}, true, 0xFF98A671, Category.WORLD);
        this.offerProperties(speed, fastfall);
        this.addListeners(new Listener<DamageBlockSubject>("speed_mine_damage_block_listener") {
            @Override
            public void call(DamageBlockSubject subject) {
                KillAura killAura = (KillAura) Glade.getInstance().getModuleManager().getModuleByAlias("KillAura");
                if (killAura != null && killAura.isEnabled())
                    return;
                Block block = BlockHelper.getBlock(subject.getX(), subject.getY(), subject.getZ());

                if (BlockHelper.getBlockUnder(1F).equals(block) && fastfall.getValue() && minecraft.thePlayer.onGround)
                    minecraft.thePlayer.motionY--;

                if (block.getMaterial() != Material.air)
                    subject.setCurBlockDamageMP(subject.getCurBlockDamageMP()
                            + block.getPlayerRelativeBlockHardness(minecraft.thePlayer, minecraft.theWorld,
                            new BlockPos(subject.getX(), subject.getY(), subject.getZ())) * speed.getValue());
                subject.setBlockHitDelay(0);
                byte slot = BlockHelper.findBestTool(subject.getX(), subject.getY(), subject.getZ());
                if (slot == -1)
                    return;
                if (slot < 9) {
                    minecraft.thePlayer.inventory.currentItem = slot;
                    minecraft.playerController.syncCurrentPlayItem();
                } else {
                    minecraft.playerController.windowClick(0, slot, minecraft.thePlayer.inventory.currentItem, 2, minecraft.thePlayer);
                }
            }
        }, new Listener<TickSubject>("speed_mine_tick_listener") {
            @Override
            public void call(TickSubject subject) {
                setColor(0xFF98A671);
                KillAura killAura = (KillAura) Glade.getInstance().getModuleManager().getModuleByAlias("KillAura");
                if (killAura != null && killAura.isEnabled())
                    setColor(0xFFFF7396);
            }
        });
    }
}
