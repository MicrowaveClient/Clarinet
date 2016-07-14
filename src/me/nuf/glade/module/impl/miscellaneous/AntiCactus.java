package me.nuf.glade.module.impl.miscellaneous;

import me.nuf.api.interfaces.Buggy;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.subjects.BlockBoundingBoxSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.block.BlockCactus;
import net.minecraft.util.AxisAlignedBB;

/**
 * Created by nuf on 3/25/2016.
 */
@Buggy(reason = "Doesn't work correctly.")
public final class AntiCactus extends ToggleableModule {
    public AntiCactus() {
        super(new String[]{"AntiCactus", "cactus", "ac"}, true, 0xFF555500, Category.MISCELLANEOUS);
        this.addListeners(new Listener<BlockBoundingBoxSubject>("anti_cactus_block_bounding_box_listener") {
            @Override
            public void call(BlockBoundingBoxSubject subject) {
                if (subject.getBlock() instanceof BlockCactus)
                    subject.setBoundingBox(AxisAlignedBB.fromBounds(subject.getBlockPos().getX(), subject.getBlockPos().getY(), subject.getBlockPos().getZ(), subject.getBlockPos().getX() + 1D, subject.getBlockPos().getY() + 1D, subject.getBlockPos().getZ() + 1D));
            }
        });
    }
}
