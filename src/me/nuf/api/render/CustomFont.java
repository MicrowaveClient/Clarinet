package me.nuf.api.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

/**
 * @author Nahr.
 * @author nuf
 */
public class CustomFont {

    private Font theFont;
    private Graphics2D theGraphics;
    private FontMetrics theMetrics;
    private float fontSize;
    private int startChar, endChar;
    private float[] xPos, yPos;
    private BufferedImage bufferedImage;
    private float extraSpacing = 0.0F;
    private DynamicTexture dynamicTexture;
    private ResourceLocation resourceLocation;
    private final Pattern patternControlCode = Pattern.compile("(?i)\\u00A7[0-9A-FK-OG]"),
            patternUnsupported = Pattern.compile("(?i)\\u00A7[K-O]");

    public CustomFont(Object font, float size) {
        this(font, size, 0F);
    }

    public CustomFont(Object font) {
        this(font, 18F, 0F);
    }

    public CustomFont(Object font, float size, float spacing) {
        this.fontSize = size;
        this.startChar = 32;
        this.endChar = 255;
        this.extraSpacing = spacing;
        this.xPos = new float[this.endChar - this.startChar];
        this.yPos = new float[this.endChar - this.startChar];
        setupGraphics2D();
        createFont(font, size);
    }

    private final void setupGraphics2D() {
        this.bufferedImage = new BufferedImage(256, 256, 2);
        this.theGraphics = ((Graphics2D) this.bufferedImage.getGraphics());
        this.theGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    }

    private final void createFont(Object font, float size) {
        try {
            if ((font instanceof Font))
                this.theFont = ((Font) font);
            else if ((font instanceof File))
                this.theFont = Font.createFont(0, (File) font).deriveFont(size);
            else if ((font instanceof InputStream))
                this.theFont = Font.createFont(0, (InputStream) font).deriveFont(size);
            else if ((font instanceof String))
                this.theFont = new Font((String) font, 0, Math.round(size));
            else {
                this.theFont = new Font("New Times Roman", 0, Math.round(size));
            }
            this.theGraphics.setFont(this.theFont);
        } catch (Exception e) {
            e.printStackTrace();
            this.theFont = new Font("New Times Roman", 0, Math.round(size));
            this.theGraphics.setFont(this.theFont);
        }
        this.theGraphics.setColor(new Color(255, 255, 255, 0));
        this.theGraphics.fillRect(0, 0, 256, 256);
        this.theGraphics.setColor(Color.red);
        this.theMetrics = this.theGraphics.getFontMetrics();

        float x = 5.0F;
        float y = 5.0F;
        for (int i = this.startChar; i < this.endChar; i++) {
            this.theGraphics.drawString(Character.toString((char) i), x, y + this.theMetrics.getAscent());
            this.xPos[(i - this.startChar)] = x;
            this.yPos[(i - this.startChar)] = (y - this.theMetrics.getMaxDescent());
            x += this.theMetrics.stringWidth(Character.toString((char) i)) + 2.0F;
            if (x >= 250 - this.theMetrics.getMaxAdvance()) {
                x = 5.0F;
                y += this.theMetrics.getMaxAscent() + this.theMetrics.getMaxDescent() + this.fontSize / 2.0F;
            }
        }
        this.resourceLocation = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(
                "font" + font.toString() + size, this.dynamicTexture = new DynamicTexture(this.bufferedImage));
    }

    public final void drawString(String text, float x, float y, FontType fontType, int color, int color2) {
        GL11.glPushMatrix();
        text = stripUnsupported(text);

        GL11.glEnable(3042);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(3553 /* GL_TEXTURE_2D */);
        GL11.glEnable(3042 /* GL_BLEND */);
        GL11.glDisable(3008 /* GL_ALPHA_TEST */);
        GL11.glBlendFunc(770, 771);
        GL11.glShadeModel(7425 /* GL_SMOOTH */);
        GL11.glScalef(0.5F, 0.5F, 0.5F);
        String text2 = stripControlCodes(text);
        switch (fontType.ordinal()) {
            case 1:
                drawer(text2, x + 0.5F, y, color2);
                drawer(text2, x - 0.5F, y, color2);
                drawer(text2, x, y + 0.5F, color2);
                drawer(text2, x, y - 0.5F, color2);
                break;
            case 2:
                drawer(text2, x + 0.5F, y + 0.5F, color2);
                break;
            case 3:
                drawer(text2, x + 0.5F, y + 1.0F, color2);
                break;
            case 4:
                drawer(text2, x, y + 0.5F, color2);
                break;
            case 5:
                drawer(text2, x, y - 0.5F, color2);
                break;
            case 6:
                break;
        }

        drawer(text, x, y, color);
        GL11.glScalef(2.0F, 2.0F, 2.0F);
        GL11.glShadeModel(7424 /* GL_FLAT */);
        GL11.glDisable(3042 /* GL_BLEND */);
        GL11.glEnable(3008 /* GL_ALPHA_TEST */);
        GL11.glEnable(3553 /* GL_TEXTURE_2D */);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glPopMatrix();
    }

    public void drawCenteredString(String text, float x, float y, int color) {
        drawString(text, (x - getStringWidth(text) / 2), y, FontType.SHADOW_THIN, color);
    }

    public final void drawString(String text, float x, float y, FontType fontType, int color) {
        drawString(text, x, y, fontType, color, 0xBB000000);
    }

    private final void drawer(String text, float x, float y, int color) {
        x *= 2.0F;
        y *= 2.0F;
        GL11.glEnable(3553);
        Minecraft.getMinecraft().getTextureManager().bindTexture(this.resourceLocation);
        // bindTexture
        float alpha = (color >> 24 & 0xFF) / 255.0F;
        float red = (color >> 16 & 0xFF) / 255.0F;
        float green = (color >> 8 & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        GL11.glColor4f(red, green, blue, alpha);
        float startX = x;
        for (int i = 0; i < text.length(); i++)
            if ((text.charAt(i) == '\247') && (i + 1 < text.length())) {
                char oneMore = Character.toLowerCase(text.charAt(i + 1));
                if (oneMore == 'n') {
                    y += this.theMetrics.getAscent() + 2;
                    x = startX;
                }
                int colorCode = "0123456789abcdefklmnorg".indexOf(oneMore);
                if (colorCode < 16)
                    try {
                        int newColor = Minecraft.getMinecraft().fontRendererObj.colorCode[colorCode];
                        GL11.glColor4f((newColor >> 16) / 255.0F, (newColor >> 8 & 0xFF) / 255.0F,
                                (newColor & 0xFF) / 255.0F, alpha);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                else if (oneMore == 'f')
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, alpha);
                else if (oneMore == 'r')
                    GL11.glColor4f(red, green, blue, alpha);
                else if (oneMore == 'g') {
                    GL11.glColor4f(0.47F, 0.67F, 0.27F, alpha);
                }
                i++;
            } else {
                try {
                    char c = text.charAt(i);
                    drawChar(c, x, y);
                    x += getStringWidth(Character.toString(c)) * 2.0F;
                } catch (ArrayIndexOutOfBoundsException indexException) {
                    char c = text.charAt(i);
                    System.out.println("Can't draw character: " + c + " (" + Character.getNumericValue(c) + ")");

                }
            }
    }

    public final float getStringWidth(String text) {
        return (float) (getBounds(text).getWidth() + this.extraSpacing) / 2.0F;
    }

    public final float getStringHeight(String text) {
        return (float) getBounds(text).getHeight() / 2.0F;
    }

    private final Rectangle2D getBounds(String text) {
        return this.theMetrics.getStringBounds(text, this.theGraphics);
    }

    private final void drawChar(char character, float x, float y) throws ArrayIndexOutOfBoundsException {
        Rectangle2D bounds = this.theMetrics.getStringBounds(Character.toString(character), this.theGraphics);
        drawTexturedModalRect(x, y, this.xPos[(character - this.startChar)], this.yPos[(character - this.startChar)],
                (float) bounds.getWidth(), (float) bounds.getHeight() + this.theMetrics.getMaxDescent() + 1.0F);
    }

    private final List listFormattedStringToWidth(String s, int width) {
        return Arrays.asList(wrapFormattedStringToWidth(s, width).split("\n"));
    }

    private final String wrapFormattedStringToWidth(String s, float width) {
        int wrapWidth = sizeStringToWidth(s, width);

        if (s.length() <= wrapWidth) {
            return s;
        }
        String split = s.substring(0, wrapWidth);
        String split2 = getFormatFromString(split)
                + s.substring(wrapWidth + ((s.charAt(wrapWidth) == ' ') || (s.charAt(wrapWidth) == '\n') ? 1 : 0));
        try {
            return split + "\n" + wrapFormattedStringToWidth(split2, width);
        } catch (Exception e) {
            System.out.println("Cannot wrap string to width.");
        }
        return "";
    }

    private final int sizeStringToWidth(String par1Str, float par2) {
        int var3 = par1Str.length();
        float var4 = 0.0F;
        int var5 = 0;
        int var6 = -1;

        for (boolean var7 = false; var5 < var3; var5++) {
            char var8 = par1Str.charAt(var5);

            switch (var8) {
                case '\n':
                    var5--;
                    break;
                case '\247':
                    if (var5 < var3 - 1) {
                        var5++;
                        char var9 = par1Str.charAt(var5);

                        if ((var9 != 'l') && (var9 != 'L')) {
                            if ((var9 == 'r') || (var9 == 'R') || (isFormatColor(var9)))
                                var7 = false;
                        } else
                            var7 = true;
                    }
                    break;
                case ' ':
                    var6 = var5;
                case '-':
                    var6 = var5;
                case '_':
                    var6 = var5;
                case ':':
                    var6 = var5;
                default:
                    String text = String.valueOf(var8);
                    var4 += getStringWidth(text);

                    if (var7) {
                        var4 += 1.0F;
                    }
                    break;
            }
            if (var8 == '\n') {
                var5++;
                var6 = var5;
            } else {
                if (var4 > par2) {
                    break;
                }
            }
        }
        return (var5 != var3) && (var6 != -1) && (var6 < var5) ? var6 : var5;
    }

    private final String getFormatFromString(String par0Str) {
        String var1 = "";
        int var2 = -1;
        int var3 = par0Str.length();

        while ((var2 = par0Str.indexOf('\247', var2 + 1)) != -1) {
            if (var2 < var3 - 1) {
                char var4 = par0Str.charAt(var2 + 1);

                if (isFormatColor(var4))
                    var1 = "\247" + var4;
                else if (isFormatSpecial(var4)) {
                    var1 = var1 + "\247" + var4;
                }
            }
        }

        return var1;
    }

    private final boolean isFormatColor(char par0) {
        return ((par0 >= '0') && (par0 <= '9')) || ((par0 >= 'a') && (par0 <= 'f')) || ((par0 >= 'A') && (par0 <= 'F'));
    }

    private final boolean isFormatSpecial(char par0) {
        return ((par0 >= 'k') && (par0 <= 'o')) || ((par0 >= 'K') && (par0 <= 'O')) || (par0 == 'r') || (par0 == 'R');
    }

    private final void drawTexturedModalRect(float x, float y, float u, float v, float width, float height) {
        float scale = 0.0039063F;
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.startDrawingQuads();
        renderer.addVertexWithUV(x + 0.0F, y + height, 0.0D, (u + 0.0F) * scale, (v + height) * scale);
        renderer.addVertexWithUV(x + width, y + height, 0.0D, (u + width) * scale, (v + height) * scale);
        renderer.addVertexWithUV(x + width, y + 0.0F, 0.0D, (u + width) * scale, (v + 0.0F) * scale);
        renderer.addVertexWithUV(x + 0.0F, y + 0.0F, 0.0D, (u + 0.0F) * scale, (v + 0.0F) * scale);
        tessellator.draw();
    }

    public final String stripControlCodes(String s) {
        return this.patternControlCode.matcher(s).replaceAll("");
    }

    public final String stripUnsupported(String s) {
        return this.patternUnsupported.matcher(s).replaceAll("");
    }

    public final Graphics2D getGraphics() {
        return this.theGraphics;
    }

    public final Font getFont() {
        return theFont;
    }

    public enum FontType {
        NORMAL, SHADOW_THICK, SHADOW_THIN, OUTLINE_THIN, EMBOSS_TOP, EMBOSS_BOTTOM;
    }

}
