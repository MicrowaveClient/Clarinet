package net.minecraft.client.renderer.texture;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadersmod.client.ShadersTex;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class SimpleTexture extends AbstractTexture {
    private static final Logger logger = LogManager.getLogger();
    protected final ResourceLocation textureLocation;

    public SimpleTexture(ResourceLocation textureResourceLocation) {
        this.textureLocation = textureResourceLocation;
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        this.deleteGlTexture();
        InputStream var2 = null;

        try {
            IResource var3 = resourceManager.getResource(this.textureLocation);
            var2 = var3.getInputStream();
            BufferedImage var4 = TextureUtil.readBufferedImage(var2);
            boolean var5 = false;
            boolean var6 = false;

            if (var3.hasMetadata()) {
                try {
                    TextureMetadataSection var11 = (TextureMetadataSection) var3.getMetadata("texture");

                    if (var11 != null) {
                        var5 = var11.getTextureBlur();
                        var6 = var11.getTextureClamp();
                    }
                } catch (RuntimeException var111) {
                    logger.warn("Failed reading metadata of: " + this.textureLocation, var111);
                }
            }

            if (Config.isShaders()) {
                ShadersTex.loadSimpleTexture(this.getGlTextureId(), var4, var5, var6, resourceManager, this.textureLocation, this.getMultiTexID());
            } else {
                TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), var4, var5, var6);
            }
        } finally {
            if (var2 != null) {
                var2.close();
            }
        }
    }
}
