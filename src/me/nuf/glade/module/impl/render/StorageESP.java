package me.nuf.glade.module.impl.render;

import me.nuf.api.render.RenderMethods;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.RenderSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created by nuf on 3/28/2016.
 */
public final class StorageESP extends ToggleableModule {
    private final Property<Boolean> signs = new Property<>(false, "Signs", "sign", "s"), chests = new Property<>(true, "Chests", "chest", "c");

    public StorageESP() {
        super(new String[]{"StorageESP", "sesp", "chestesp"}, false, 0xFF90D4C0, Category.RENDER);
        this.offerProperties(signs, chests);
        this.addListeners(new Listener<RenderSubject>("storage_esp_render_listener") {
            @Override
            public void call(RenderSubject subject) {
                RenderMethods.enableGL3D();
                for (TileEntity tileEntity : (List<TileEntity>) Minecraft
                        .getMinecraft().theWorld.loadedTileEntityList) {
                    if (!shouldDraw(tileEntity))
                        continue;
                    double x = tileEntity.getPos().getX() - minecraft.getRenderManager().renderPosX;
                    double y = tileEntity.getPos().getY() - minecraft.getRenderManager().renderPosY;
                    double z = tileEntity.getPos().getZ() - minecraft.getRenderManager().renderPosZ;
                    float[] color = getColor(tileEntity);
                    AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);

                    if (tileEntity instanceof TileEntityChest) {
                        TileEntityChest chest = TileEntityChest.class.cast(tileEntity);
                        if (chest.adjacentChestZPos != null) {
                            box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.875, z + 1.9375);
                        } else if (chest.adjacentChestXPos != null) {
                            box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 1.9375, y + 0.875, z + 0.9375);
                        } else if (chest.adjacentChestZPos == null && chest.adjacentChestXPos == null
                                && chest.adjacentChestZNeg == null && chest.adjacentChestXNeg == null) {
                            box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.875, z + 0.9375);
                        } else {
                            continue;
                        }
                    } else if (tileEntity instanceof TileEntityEnderChest) {
                        box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 0.875, z + 0.9375);
                    } else if (tileEntity instanceof TileEntitySign) {
                        box = new AxisAlignedBB(x + 0.0625, y, z + 0.0625, x + 0.9375, y + 1.05, z + 0.9375);
                    }
                    GL11.glLineWidth(1.3F);
                    GlStateManager.color(color[0], color[1], color[2], 0.25F);
                    RenderMethods.drawBox(box);
                    GlStateManager.color(color[0], color[1], color[2], 0.6F);
                    RenderMethods.renderCrosses(box);
                    RenderMethods.drawOutlinedBox(box);
                }
                RenderMethods.disableGL3D();
            }
        });
    }

    private boolean shouldDraw(TileEntity tileEntity) {
        return (chests.getValue() && tileEntity instanceof TileEntityEnderChest) || (chests.getValue() && tileEntity instanceof TileEntityChest) || (signs.getValue() && tileEntity instanceof TileEntitySign);
    }

    private float[] getColor(TileEntity tileEntity) {
        if (tileEntity instanceof TileEntityChest) {
            Block block = tileEntity.getBlockType();
            if (block == Blocks.chest) {
                return new float[]{0.27F, 0.70F, 0.92F};
            } else if (block == Blocks.trapped_chest) {
                return new float[]{0.5F, 0.5F, 0.7F};
            }
        }

        if (tileEntity instanceof TileEntityEnderChest)
            return new float[]{1, 0, 1};

        if (tileEntity instanceof TileEntitySign)
            return new float[]{0.1F, 0.6F, 0.1F};

        return new float[]{1, 1, 1};
    }
}
