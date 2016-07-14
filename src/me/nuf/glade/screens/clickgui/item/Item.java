package me.nuf.glade.screens.clickgui.item;

import me.nuf.api.interfaces.Labeled;

public class Item implements Labeled {
    private final String label;
    protected float x, y;
    protected int width, height;

    public Item(String label) {
        this.label = label;
    }

    public void setLocation(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {

    }

    public void mouseReleased(int mouseX, int mouseY, int releaseButton) {

    }

    @Override
    public final String getLabel() {
        return label;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

}