package me.nuf.glade.module.impl.render;

import me.nuf.api.render.RenderMethods;
import me.nuf.glade.command.Argument;
import me.nuf.glade.command.Command;
import me.nuf.glade.core.Glade;
import me.nuf.glade.module.Category;
import me.nuf.glade.module.ToggleableModule;
import me.nuf.glade.module.impl.render.waypoints.Point;
import me.nuf.glade.properties.NumberProperty;
import me.nuf.glade.subjects.RenderSubject;
import me.nuf.subjectapi.Listener;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by nuf on 4/4/2016.
 */
public final class Waypoints extends ToggleableModule {
    private final NumberProperty<Float> scaling = new NumberProperty<>(0.0030F, 0.001F, 0.0100F, "Scaling", "scale", "s");
    private final List<Point> points = new ArrayList<>();

    public Waypoints() {
        super(new String[]{"Waypoints", "points", "wp"}, false, 0xFF008888, Category.RENDER);
        this.offerProperties(scaling);
        Glade.getInstance().getCommandManager().register(new Command(new String[]{"waypoints", "waypoint", "point", "wps", "wp"}, new Argument(Integer.class, "x"), new Argument(Integer.class, "y"), new Argument(Integer.class, "z")) {
            @Override
            public String dispatch() {
                int x = Integer.parseInt(getArgument("x").getValue());
                int y = Integer.parseInt(getArgument("y").getValue());
                int z = Integer.parseInt(getArgument("z").getValue());
                Point point = getPoint(x, y, z);

                if (point != null) {
                    points.remove(point);
                    return String.format("Point at %s, %s, %s has been removed.", x, y, z);
                }

                points.add(new Point(x, y, z));
                return String.format("Point at %s, %s, %s has been added.", x, y, z);
            }
        });
        this.addListeners(new Listener<RenderSubject>("waypoints_render_listener") {
            @Override
            public void call(RenderSubject subject) {
                RenderMethods.enableGL3D();
                double x, y, z;
                for (Point point : points) {
                    x = point.getX() + 0.5F - minecraft.getRenderManager().renderPosX;
                    y = point.getY() - minecraft.getRenderManager().renderPosY;
                    z = point.getZ() + 0.5F - minecraft.getRenderManager().renderPosZ;

                    AxisAlignedBB box = AxisAlignedBB.fromBounds(x - 0.5D, y, z - 0.5D, x + 0.5D, y + 1.0D, z + 0.5D);

                    GlStateManager.color(0.5F, 0.7F, 0.5F, 0.9F);
                    GL11.glLoadIdentity();
                    GL11.glLineWidth(1.6F);
                    boolean bobbing = minecraft.gameSettings.viewBobbing;
                    minecraft.gameSettings.viewBobbing = false;
                    minecraft.entityRenderer.orientCamera(subject.getPartialTicks());
                    GL11.glBegin(GL11.GL_LINES);
                    GL11.glVertex3d(0, minecraft.thePlayer.getEyeHeight(), 0);
                    GL11.glVertex3d(x, y, z);
                    GL11.glEnd();
                    GL11.glTranslated(x, y, z);
                    GL11.glTranslated(-x, -y, -z);
                    RenderMethods.drawOutlinedBox(box);
                    RenderMethods.renderCrosses(box);
                    minecraft.gameSettings.viewBobbing = bobbing;
                }

                for (Point point : points) {
                    x = point.getX() + 0.5F - minecraft.getRenderManager().renderPosX;
                    y = point.getY() - minecraft.getRenderManager().renderPosY;
                    z = point.getZ() + 0.5F - minecraft.getRenderManager().renderPosZ;

                    renderNameTag(point, x, y, z, subject.getPartialTicks());
                }
                RenderMethods.disableGL3D();
            }
        });
    }

    private void renderNameTag(Point point, double x, double y, double z, float delta) {
        Entity camera = minecraft.getRenderViewEntity();
        double originalPositionX = camera.posX;
        double originalPositionY = camera.posY;
        double originalPositionZ = camera.posZ;
        camera.posX = interpolate(camera.prevPosX, camera.posX, delta);
        camera.posY = interpolate(camera.prevPosY, camera.posY, delta);
        camera.posZ = interpolate(camera.prevPosZ, camera.posZ, delta);

        double distance = camera.getDistance(x + minecraft.getRenderManager().viewerPosX, y + minecraft.getRenderManager().viewerPosY,
                z + minecraft.getRenderManager().viewerPosZ);
        int width = minecraft.fontRendererObj.getStringWidth(String.format("XYZ \2477%s, %s, %s", point.getX(), point.getY(), point.getZ())) / 2;
        double scale = 0.0018 + scaling.getValue() * distance;

        if (distance <= 8)
            scale = 0.0245D;

        GlStateManager.pushMatrix();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enablePolygonOffset();
        GlStateManager.doPolygonOffset(1, -1500000);
        GlStateManager.disableLighting();
        GlStateManager.translate((float) x, (float) y + 1.4F, (float) z);
        GlStateManager.rotate(-minecraft.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(minecraft.getRenderManager().playerViewX, minecraft.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F,
                0.0F, 0.0F);
        GlStateManager.scale(-scale, -scale, scale);
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();

        GlStateManager.disableAlpha();
        RenderMethods.drawBorderedRectReliant(-width - 2, -(minecraft.fontRendererObj.FONT_HEIGHT + 1), width + 2F, 1, 1.8F, 0x55000400, 0x33000000);
        GlStateManager.enableAlpha();

        minecraft.fontRendererObj.drawStringWithShadow(String.format("XYZ \2477%s, %s, %s", point.getX(), point.getY(), point.getZ()), -width,
                -(minecraft.fontRendererObj.FONT_HEIGHT - 1), 0xFFFFFFFF);

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

    private double interpolate(double previous, double current, float delta) {
        return (previous + (current - previous) * delta);
    }

    private Point getPoint(int x, int y, int z) {
        for (Point point : points)
            if (point.getX() == x && point.getY() == y && point.getZ() == z)
                return point;
        return null;
    }
}
