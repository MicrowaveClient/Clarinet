package me.nuf.glade.subjects;

import me.nuf.subjectapi.Subject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

/**
 * Created by nuf on 3/19/2016.
 */
public class GameOverlaySubject extends Subject {

    public int y = 2;

    public ScaledResolution getScaledResolution() {
        return new ScaledResolution(Minecraft.getMinecraft(), Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    }

}
