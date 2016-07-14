package me.nuf.glade.module.impl.render;

import me.nuf.api.render.RenderMethods;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.properties.EnumProperty;
import me.nuf.glade.properties.Property;
import me.nuf.glade.subjects.RenderSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * Created by nuf on 3/28/2016.
 */
public final class Tracers extends ToggleableModule {
    private final EnumProperty<Type> type = new EnumProperty<>(Type.Body, "Type", "t");
    private final Property<Boolean> invisibles = new Property<>(true, "Invisibles", "invis", "inv", "i"), stem = new Property<>(false, "Stem", "spine", "stems", "spines"), lines = new Property<>(true, "Lines", "line", "l"), box = new Property<>(true, "Box", "b");
    private final EnumProperty<Design> design = new EnumProperty<>(Design.Cross, "Design", "d", "type");

    public Tracers() {
        super(new String[]{"Tracers", "esp", "tracer", "playeresp"}, false, 0xFFFF6B6B, Category.RENDER);
        this.offerProperties(invisibles, type, stem, lines, box, design);
        this.addListeners(new Listener<RenderSubject>("tracers_render_listener") {
            @Override
            public void call(RenderSubject subject) {
                RenderMethods.enableGL3D();
                double x, y, z;
                for (Entity entity : (List<Entity>) minecraft.theWorld.loadedEntityList) {
                    if (!entity.isEntityAlive() || entity instanceof EntityPlayerSP || !(entity instanceof EntityPlayer) || (entity.isInvisible() && !invisibles.getValue()))
                        continue;

                    x = getDiff(entity.lastTickPosX, entity.posX, subject.getPartialTicks(),
                            minecraft.getRenderManager().renderPosX);
                    y = getDiff(entity.lastTickPosY, entity.posY, subject.getPartialTicks(),
                            minecraft.getRenderManager().renderPosY);
                    z = getDiff(entity.lastTickPosZ, entity.posZ, subject.getPartialTicks(),
                            minecraft.getRenderManager().renderPosZ);

                    AxisAlignedBB box;

                    switch (type.getValue()) {
                        case Body:
                            box = new AxisAlignedBB(x - 0.4D, y, z - 0.4D, x + 0.4D, y + 2D, z + 0.4D);
                            break;
                        case Head:
                            box = new AxisAlignedBB(x - 0.25D, y + 1.35D, z - 0.25D, x + 0.25D, y + 1.855D, z + 0.25D);
                            break;
                        default:
                            box = new AxisAlignedBB(x - 0.4D, y, z - 0.4D, x + 0.4D, y + 2D, z + 0.4D);
                            break;
                    }

                    if (Glade.getInstance().getFriendManager().isFriend(entity.getCommandSenderName())) {
                        GlStateManager.color(0.27F, 0.70F, 0.92F, 0.55F);
                    } else {
                        float distance = minecraft.thePlayer.getDistanceToEntity(entity);
                        if (distance <= 32) {
                            GlStateManager.color(1F, distance / 32F, 0F, 0.55F);
                        } else {
                            GlStateManager.color(0F, 0.9F, 0F, 0.55F);
                        }
                    }

                    GL11.glLoadIdentity();
                    GL11.glLineWidth(1.6F);
                    boolean bobbing = minecraft.gameSettings.viewBobbing;
                    minecraft.gameSettings.viewBobbing = false;
                    minecraft.entityRenderer.orientCamera(subject.getPartialTicks());

                    if (lines.getValue() || stem.getValue()) {
                        GL11.glBegin(GL11.GL_LINES);
                        if (stem.getValue()) {
                            GL11.glVertex3d(x, y, z);
                            GL11.glVertex3d(x, y + entity.getEyeHeight(), z);
                        }
                        if (lines.getValue()) {
                            GL11.glVertex3d(0, minecraft.thePlayer.getEyeHeight(), 0);
                            GL11.glVertex3d(x, type.getValue() == Type.Body ? y : y + 1.6D, z);
                        }
                        GL11.glEnd();
                    }
                    GL11.glTranslated(x, y, z);
                    GL11.glTranslated(-x, -y, -z);
                    if (Tracers.this.box.getValue()) {
                        switch (design.getValue()) {
                            case Cross:
                                RenderMethods.drawOutlinedBox(box);
                                RenderMethods.renderCrosses(box);
                                break;
                            case Fill:
                                RenderMethods.drawBox(box);
                                break;
                        }
                    }
                    minecraft.gameSettings.viewBobbing = bobbing;
                }
                RenderMethods.disableGL3D();
            }
        });
    }

    private double getDiff(double lastI, double i, float ticks, double ownI) {
        return (lastI + (i - lastI) * ticks) - ownI;
    }

    public enum Design {
        Cross, Fill
    }

    public enum Type {
        Head, Body
    }
}
