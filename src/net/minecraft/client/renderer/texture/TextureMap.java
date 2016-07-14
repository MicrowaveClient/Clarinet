package net.minecraft.client.renderer.texture;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.StitcherException;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.src.Config;
import net.minecraft.src.Reflector;
import net.minecraft.src.TextureUtils;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadersmod.client.ShadersTex;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;

public class TextureMap extends AbstractTexture implements ITickableTextureObject {
    private static final Logger logger = LogManager.getLogger();
    public static final ResourceLocation LOCATION_MISSING_TEXTURE = new ResourceLocation("missingno");
    public static final ResourceLocation locationBlocksTexture = new ResourceLocation("textures/atlas/blocks.png");
    private final List listAnimatedSprites;
    private final Map mapRegisteredSprites;
    private final Map mapUploadedSprites;
    private final String basePath;
    private final IIconCreator iconCreator;
    private int mipmapLevels;
    private final TextureAtlasSprite missingImage;
    private TextureAtlasSprite[] iconGrid;
    private int iconGridSize;
    private int iconGridCountX;
    private int iconGridCountY;
    private double iconGridSizeU;
    private double iconGridSizeV;
    private static final boolean ENABLE_SKIP = Boolean.parseBoolean(System.getProperty("fml.skipFirstTextureLoad", "true"));
    private boolean skipFirst;
    public int atlasWidth;
    public int atlasHeight;

    public TextureMap(String p_i46099_1_) {
        this(p_i46099_1_, (IIconCreator) null);
    }

    public TextureMap(String p_i46099_1_, boolean skipFirst) {
        this(p_i46099_1_, (IIconCreator) null, skipFirst);
    }

    public TextureMap(String p_i46100_1_, IIconCreator iconCreatorIn) {
        this(p_i46100_1_, iconCreatorIn, false);
    }

    public TextureMap(String p_i46100_1_, IIconCreator iconCreatorIn, boolean skipFirst) {
        this.iconGrid = null;
        this.iconGridSize = -1;
        this.iconGridCountX = -1;
        this.iconGridCountY = -1;
        this.iconGridSizeU = -1.0D;
        this.iconGridSizeV = -1.0D;
        this.skipFirst = false;
        this.atlasWidth = 0;
        this.atlasHeight = 0;
        this.listAnimatedSprites = Lists.newArrayList();
        this.mapRegisteredSprites = Maps.newHashMap();
        this.mapUploadedSprites = Maps.newHashMap();
        this.missingImage = new TextureAtlasSprite("missingno");
        this.basePath = p_i46100_1_;
        this.iconCreator = iconCreatorIn;
        this.skipFirst = skipFirst && ENABLE_SKIP;
    }

    private void initMissingImage() {
        int size = this.getMinSpriteSize();
        int[] var1 = this.getMissingImageData(size);
        this.missingImage.setIconWidth(size);
        this.missingImage.setIconHeight(size);
        int[][] var2 = new int[this.mipmapLevels + 1][];
        var2[0] = var1;
        this.missingImage.setFramesTextureData(Lists.newArrayList(new int[][][]{var2}));
        this.missingImage.setIndexInMap(0);
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        ShadersTex.resManager = resourceManager;

        if (this.iconCreator != null) {
            this.loadSprites(resourceManager, this.iconCreator);
        }
    }

    public void loadSprites(IResourceManager resourceManager, IIconCreator p_174943_2_) {
        this.mapRegisteredSprites.clear();
        p_174943_2_.registerSprites(this);

        if (this.mipmapLevels >= 4) {
            this.mipmapLevels = this.detectMaxMipmapLevel(this.mapRegisteredSprites, resourceManager);
            Config.log("Mipmap levels: " + this.mipmapLevels);
        }

        this.initMissingImage();
        this.deleteGlTexture();
        this.loadTextureAtlas(resourceManager);
    }

    public void loadTextureAtlas(IResourceManager resourceManager) {
        Config.dbg("Multitexture: " + Config.isMultiTexture());

        if (Config.isMultiTexture()) {
            Iterator var2 = this.mapUploadedSprites.values().iterator();

            while (var2.hasNext()) {
                TextureAtlasSprite var3 = (TextureAtlasSprite) var2.next();
                var3.deleteSpriteTexture();
            }
        }

        int var21 = Minecraft.getGLMaximumTextureSize();
        Stitcher var31 = new Stitcher(var21, var21, true, 0, this.mipmapLevels);
        this.mapUploadedSprites.clear();
        this.listAnimatedSprites.clear();
        int var4 = Integer.MAX_VALUE;
        Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPre, new Object[]{this});
        int var5 = 1 << this.mipmapLevels;
        Iterator var6 = this.mapRegisteredSprites.entrySet().iterator();

        while (true) {
            Iterator it1;

            while (var6.hasNext()) {
                Entry var25 = (Entry) var6.next();
                TextureAtlasSprite var26 = (TextureAtlasSprite) var25.getValue();
                ResourceLocation var27 = new ResourceLocation(var26.getIconName());
                ResourceLocation var28 = this.completeResourceLocation(var27, 0);

                if (!var26.hasCustomLoader(resourceManager, var27)) {
                    try {
                        IResource var30 = resourceManager.getResource(var28);
                        BufferedImage[] var311 = new BufferedImage[1 + this.mipmapLevels];
                        var311[0] = TextureUtil.readBufferedImage(var30.getInputStream());
                        TextureMetadataSection sheetWidth = (TextureMetadataSection) var30.getMetadata("texture");

                        if (sheetWidth != null) {
                            List sheetHeight = sheetWidth.getListMipmaps();
                            int listSprites;

                            if (!sheetHeight.isEmpty()) {
                                int it = var311[0].getWidth();
                                listSprites = var311[0].getHeight();

                                if (MathHelper.roundUpToPowerOfTwo(it) != it || MathHelper.roundUpToPowerOfTwo(listSprites) != listSprites) {
                                    throw new RuntimeException("Unable to load extra miplevels, source-texture is not power of two");
                                }
                            }

                            it1 = sheetHeight.iterator();

                            while (it1.hasNext()) {
                                listSprites = ((Integer) it1.next()).intValue();

                                if (listSprites > 0 && listSprites < var311.length - 1 && var311[listSprites] == null) {
                                    ResourceLocation tas = this.completeResourceLocation(var27, listSprites);

                                    try {
                                        var311[listSprites] = TextureUtil.readBufferedImage(resourceManager.getResource(tas).getInputStream());
                                    } catch (IOException var24) {
                                        logger.error("Unable to load miplevel {} from: {}", new Object[]{Integer.valueOf(listSprites), tas, var24});
                                    }
                                }
                            }
                        }

                        AnimationMetadataSection sheetHeight1 = (AnimationMetadataSection) var30.getMetadata("animation");
                        var26.loadSprite(var311, sheetHeight1);
                    } catch (RuntimeException var251) {
                        logger.error("Unable to parse metadata from " + var28, var251);
                        continue;
                    } catch (IOException var261) {
                        logger.error("Using missing texture, unable to load " + var28 + ", " + var261.getClass().getName());
                        continue;
                    }

                    var4 = Math.min(var4, Math.min(var26.getIconWidth(), var26.getIconHeight()));
                    int var301 = Math.min(Integer.lowestOneBit(var26.getIconWidth()), Integer.lowestOneBit(var26.getIconHeight()));

                    if (var301 < var5) {
                        logger.warn("Texture {} with size {}x{} limits mip level from {} to {}", new Object[]{var28, Integer.valueOf(var26.getIconWidth()), Integer.valueOf(var26.getIconHeight()), Integer.valueOf(MathHelper.calculateLogBaseTwo(var5)), Integer.valueOf(MathHelper.calculateLogBaseTwo(var301))});
                        var5 = var301;
                    }

                    var31.addSprite(var26);
                } else if (!var26.load(resourceManager, var27)) {
                    var4 = Math.min(var4, Math.min(var26.getIconWidth(), var26.getIconHeight()));
                    var31.addSprite(var26);
                }
            }

            int var252 = Math.min(var4, var5);
            int var262 = MathHelper.calculateLogBaseTwo(var252);

            if (var262 < 0) {
                var262 = 0;
            }

            if (var262 < this.mipmapLevels) {
                logger.info("{}: dropping miplevel from {} to {}, because of minimum power of two: {}", new Object[]{this.basePath, Integer.valueOf(this.mipmapLevels), Integer.valueOf(var262), Integer.valueOf(var252)});
                this.mipmapLevels = var262;
            }

            Iterator var271 = this.mapRegisteredSprites.values().iterator();

            while (var271.hasNext()) {
                final TextureAtlasSprite var281 = (TextureAtlasSprite) var271.next();

                try {
                    var281.generateMipmaps(this.mipmapLevels);
                } catch (Throwable var23) {
                    CrashReport var311 = CrashReport.makeCrashReport(var23, "Applying mipmap");
                    CrashReportCategory sheetWidth1 = var311.makeCategory("Sprite being mipmapped");
                    sheetWidth1.addCrashSectionCallable("Sprite name", new Callable() {
                        private static final String __OBFID = "CL_00001059";

                        public String call() {
                            return var281.getIconName();
                        }
                    });
                    sheetWidth1.addCrashSectionCallable("Sprite size", new Callable() {
                        private static final String __OBFID = "CL_00001060";

                        public String call() {
                            return var281.getIconWidth() + " x " + var281.getIconHeight();
                        }
                    });
                    sheetWidth1.addCrashSectionCallable("Sprite frames", new Callable() {
                        private static final String __OBFID = "CL_00001061";

                        public String call() {
                            return var281.getFrameCount() + " frames";
                        }
                    });
                    sheetWidth1.addCrashSection("Mipmap levels", Integer.valueOf(this.mipmapLevels));
                    throw new ReportedException(var311);
                }
            }

            this.missingImage.generateMipmaps(this.mipmapLevels);
            var31.addSprite(this.missingImage);

            try {
                var31.doStitch();
            } catch (StitcherException var22) {
                throw var22;
            }

            logger.info("Created: {}x{} {}-atlas", new Object[]{Integer.valueOf(var31.getCurrentWidth()), Integer.valueOf(var31.getCurrentHeight()), this.basePath});
            TextureUtil.allocateTextureImpl(this.getGlTextureId(), this.mipmapLevels, var31.getCurrentWidth(), var31.getCurrentHeight());
            HashMap var282 = Maps.newHashMap(this.mapRegisteredSprites);
            Iterator var302 = var31.getStichSlots().iterator();
            TextureAtlasSprite var312;

            while (var302.hasNext()) {
                var312 = (TextureAtlasSprite) var302.next();
                String sheetWidth2 = var312.getIconName();
                var282.remove(sheetWidth2);
                this.mapUploadedSprites.put(sheetWidth2, var312);

                try {
                    TextureUtil.uploadTextureMipmap(var312.getFrameTextureData(0), var312.getIconWidth(), var312.getIconHeight(), var312.getOriginX(), var312.getOriginY(), false, false);
                } catch (Throwable var211) {
                    CrashReport listSprites1 = CrashReport.makeCrashReport(var211, "Stitching texture atlas");
                    CrashReportCategory it2 = listSprites1.makeCategory("Texture being stitched together");
                    it2.addCrashSection("Atlas path", this.basePath);
                    it2.addCrashSection("Sprite", var312);
                    throw new ReportedException(listSprites1);
                }

                if (var312.hasAnimationMetadata()) {
                    this.listAnimatedSprites.add(var312);
                }
            }

            var302 = var282.values().iterator();

            while (var302.hasNext()) {
                var312 = (TextureAtlasSprite) var302.next();
                var312.copyFrom(this.missingImage);
            }

            if (Config.isMultiTexture()) {
                int sheetWidth3 = var31.getCurrentWidth();
                int sheetHeight2 = var31.getCurrentHeight();
                List listSprites2 = var31.getStichSlots();
                it1 = listSprites2.iterator();

                while (it1.hasNext()) {
                    TextureAtlasSprite tas1 = (TextureAtlasSprite) it1.next();
                    tas1.sheetWidth = sheetWidth3;
                    tas1.sheetHeight = sheetHeight2;
                    TextureAtlasSprite ss = tas1.spriteSingle;

                    if (ss != null) {
                        ss.sheetWidth = sheetWidth3;
                        ss.sheetHeight = sheetHeight2;
                        tas1.bindSpriteTexture();
                        boolean texBlur = false;
                        boolean texClamp = true;
                        TextureUtil.uploadTextureMipmap(ss.getFrameTextureData(0), ss.getIconWidth(), ss.getIconHeight(), ss.getOriginX(), ss.getOriginY(), texBlur, texClamp);
                    }
                }

                Config.getMinecraft().getTextureManager().bindTexture(locationBlocksTexture);
            }

            Reflector.callVoid(Reflector.ForgeHooksClient_onTextureStitchedPost, new Object[]{this});

            if (Config.equals(System.getProperty("saveTextureMap"), "true")) {
                TextureUtil.saveGlTexture(this.basePath.replaceAll("/", "_"), this.getGlTextureId(), this.mipmapLevels, var31.getCurrentWidth(), var31.getCurrentHeight());
            }

            return;
        }
    }


    public ResourceLocation completeResourceLocation(ResourceLocation location, int p_147634_2_) {
        return this.isAbsoluteLocation(location) ? (p_147634_2_ == 0 ? new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + ".png") : new ResourceLocation(location.getResourceDomain(), location.getResourcePath() + "mipmap" + p_147634_2_ + ".png")) : (p_147634_2_ == 0 ? new ResourceLocation(location.getResourceDomain(), String.format("%s/%s%s", new Object[]{this.basePath, location.getResourcePath(), ".png"})) : new ResourceLocation(location.getResourceDomain(), String.format("%s/mipmaps/%s.%d%s", new Object[]{this.basePath, location.getResourcePath(), Integer.valueOf(p_147634_2_), ".png"})));
    }

    public TextureAtlasSprite getAtlasSprite(String iconName) {
        TextureAtlasSprite var2 = (TextureAtlasSprite) this.mapUploadedSprites.get(iconName);

        if (var2 == null) {
            var2 = this.missingImage;
        }

        return var2;
    }

    public void updateAnimations() {
        if (Config.isShaders()) {
            ShadersTex.updatingTex = this.getMultiTexID();
        }

        TextureUtil.bindTexture(this.getGlTextureId());
        Iterator var1 = this.listAnimatedSprites.iterator();

        while (var1.hasNext()) {
            TextureAtlasSprite it = (TextureAtlasSprite) var1.next();

            if (this.isTerrainAnimationActive(it)) {
                it.updateAnimation();
            }
        }

        if (Config.isMultiTexture()) {
            Iterator it1 = this.listAnimatedSprites.iterator();

            while (it1.hasNext()) {
                TextureAtlasSprite ts = (TextureAtlasSprite) it1.next();

                if (this.isTerrainAnimationActive(ts)) {
                    TextureAtlasSprite spriteSingle = ts.spriteSingle;

                    if (spriteSingle != null) {
                        if (ts == TextureUtils.iconClock || ts == TextureUtils.iconCompass) {
                            spriteSingle.frameCounter = ts.frameCounter;
                        }

                        ts.bindSpriteTexture();
                        spriteSingle.updateAnimation();
                    }
                }
            }

            TextureUtil.bindTexture(this.getGlTextureId());
        }

        if (Config.isShaders()) {
            ShadersTex.updatingTex = null;
        }
    }

    public TextureAtlasSprite registerSprite(ResourceLocation location) {
        if (location == null) {
            throw new IllegalArgumentException("Location cannot be null!");
        } else {
            TextureAtlasSprite var2 = (TextureAtlasSprite) this.mapRegisteredSprites.get(location.toString());

            if (var2 == null) {
                var2 = TextureAtlasSprite.makeAtlasSprite(location);
                this.mapRegisteredSprites.put(location.toString(), var2);

                if (var2 instanceof TextureAtlasSprite && var2.getIndexInMap() < 0) {
                    var2.setIndexInMap(this.mapRegisteredSprites.size());
                }
            }

            return var2;
        }
    }

    public void tick() {
        this.updateAnimations();
    }

    public void setMipmapLevels(int mipmapLevelsIn) {
        this.mipmapLevels = mipmapLevelsIn;
    }

    public TextureAtlasSprite getMissingSprite() {
        return this.missingImage;
    }

    public TextureAtlasSprite getTextureExtry(String name) {
        ResourceLocation loc = new ResourceLocation(name);
        return (TextureAtlasSprite) this.mapRegisteredSprites.get(loc.toString());
    }

    public boolean setTextureEntry(String name, TextureAtlasSprite entry) {
        if (!this.mapRegisteredSprites.containsKey(name)) {
            this.mapRegisteredSprites.put(name, entry);

            if (entry.getIndexInMap() < 0) {
                entry.setIndexInMap(this.mapRegisteredSprites.size());
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean isAbsoluteLocation(ResourceLocation loc) {
        String path = loc.getResourcePath();
        return this.isAbsoluteLocationPath(path);
    }

    private boolean isAbsoluteLocationPath(String resPath) {
        String path = resPath.toLowerCase();
        return path.startsWith("mcpatcher/") || path.startsWith("optifine/");
    }

    public TextureAtlasSprite getSpriteSafe(String name) {
        ResourceLocation loc = new ResourceLocation(name);
        return (TextureAtlasSprite) this.mapRegisteredSprites.get(loc.toString());
    }

    private boolean isTerrainAnimationActive(TextureAtlasSprite ts) {
        return ts != TextureUtils.iconWaterStill && ts != TextureUtils.iconWaterFlow ? (ts != TextureUtils.iconLavaStill && ts != TextureUtils.iconLavaFlow ? (ts != TextureUtils.iconFireLayer0 && ts != TextureUtils.iconFireLayer1 ? (ts == TextureUtils.iconPortal ? Config.isAnimatedPortal() : (ts != TextureUtils.iconClock && ts != TextureUtils.iconCompass ? Config.isAnimatedTerrain() : true)) : Config.isAnimatedFire()) : Config.isAnimatedLava()) : Config.isAnimatedWater();
    }

    public int getCountRegisteredSprites() {
        return this.mapRegisteredSprites.size();
    }

    private int detectMaxMipmapLevel(Map mapSprites, IResourceManager rm) {
        int minSize = this.detectMinimumSpriteSize(mapSprites, rm, 20);

        if (minSize < 16) {
            minSize = 16;
        }

        minSize = MathHelper.roundUpToPowerOfTwo(minSize);

        if (minSize > 16) {
            Config.log("Sprite size: " + minSize);
        }

        int minLevel = MathHelper.calculateLogBaseTwo(minSize);

        if (minLevel < 4) {
            minLevel = 4;
        }

        return minLevel;
    }

    private int detectMinimumSpriteSize(Map mapSprites, IResourceManager rm, int percentScale) {
        HashMap mapSizeCounts = new HashMap();
        Set entrySetSprites = mapSprites.entrySet();
        Iterator countSprites = entrySetSprites.iterator();
        int count;

        while (countSprites.hasNext()) {
            Entry setSizes = (Entry) countSprites.next();
            TextureAtlasSprite setSizesSorted = (TextureAtlasSprite) setSizes.getValue();
            ResourceLocation minSize = new ResourceLocation(setSizesSorted.getIconName());
            ResourceLocation countScale = this.completeResourceLocation(minSize, 0);

            if (!setSizesSorted.hasCustomLoader(rm, minSize)) {
                try {
                    IResource countScaleMax = rm.getResource(countScale);

                    if (countScaleMax != null) {
                        InputStream it = countScaleMax.getInputStream();

                        if (it != null) {
                            Dimension size = TextureUtils.getImageSize(it, "png");

                            if (size != null) {
                                count = size.width;
                                int width2 = MathHelper.roundUpToPowerOfTwo(count);

                                if (!mapSizeCounts.containsKey(Integer.valueOf(width2))) {
                                    mapSizeCounts.put(Integer.valueOf(width2), Integer.valueOf(1));
                                } else {
                                    int count1 = ((Integer) mapSizeCounts.get(Integer.valueOf(width2))).intValue();
                                    mapSizeCounts.put(Integer.valueOf(width2), Integer.valueOf(count1 + 1));
                                }
                            }
                        }
                    }
                } catch (Exception var17) {
                    ;
                }
            }
        }

        int countSprites1 = 0;
        Set setSizes1 = mapSizeCounts.keySet();
        TreeSet setSizesSorted1 = new TreeSet(setSizes1);
        int countScale1;
        int countScaleMax1;

        for (Iterator minSize1 = setSizesSorted1.iterator(); minSize1.hasNext(); countSprites1 += countScaleMax1) {
            countScale1 = ((Integer) minSize1.next()).intValue();
            countScaleMax1 = ((Integer) mapSizeCounts.get(Integer.valueOf(countScale1))).intValue();
        }

        int minSize2 = 16;
        countScale1 = 0;
        countScaleMax1 = countSprites1 * percentScale / 100;
        Iterator it1 = setSizesSorted1.iterator();

        do {
            if (!it1.hasNext()) {
                return minSize2;
            }

            int size1 = ((Integer) it1.next()).intValue();
            count = ((Integer) mapSizeCounts.get(Integer.valueOf(size1))).intValue();
            countScale1 += count;

            if (size1 > minSize2) {
                minSize2 = size1;
            }
        }
        while (countScale1 <= countScaleMax1);

        return minSize2;
    }

    private int getMinSpriteSize() {
        int minSize = 1 << this.mipmapLevels;

        if (minSize < 8) {
            minSize = 8;
        }

        return minSize;
    }

    private int[] getMissingImageData(int size) {
        BufferedImage bi = new BufferedImage(16, 16, 2);
        bi.setRGB(0, 0, 16, 16, TextureUtil.missingTextureData, 0, 16);
        BufferedImage bi2 = TextureUtils.scaleToPowerOfTwo(bi, size);
        int[] data = new int[size * size];
        bi2.getRGB(0, 0, size, size, data, 0, size);
        return data;
    }

    public boolean isTextureBound() {
        int boundTexId = GlStateManager.getBoundTexture();
        int texId = this.getGlTextureId();
        return boundTexId == texId;
    }

    private void updateIconGrid(int sheetWidth, int sheetHeight) {
        this.iconGridCountX = -1;
        this.iconGridCountY = -1;
        this.iconGrid = null;

        if (this.iconGridSize > 0) {
            this.iconGridCountX = sheetWidth / this.iconGridSize;
            this.iconGridCountY = sheetHeight / this.iconGridSize;
            this.iconGrid = new TextureAtlasSprite[this.iconGridCountX * this.iconGridCountY];
            this.iconGridSizeU = 1.0D / (double) this.iconGridCountX;
            this.iconGridSizeV = 1.0D / (double) this.iconGridCountY;
            Iterator it = this.mapUploadedSprites.values().iterator();

            while (it.hasNext()) {
                TextureAtlasSprite ts = (TextureAtlasSprite) it.next();
                double uMin = (double) Math.min(ts.getMinU(), ts.getMaxU());
                double vMin = (double) Math.min(ts.getMinV(), ts.getMaxV());
                double uMax = (double) Math.max(ts.getMinU(), ts.getMaxU());
                double vMax = (double) Math.max(ts.getMinV(), ts.getMaxV());
                int iuMin = (int) (uMin / this.iconGridSizeU);
                int ivMin = (int) (vMin / this.iconGridSizeV);
                int iuMax = (int) (uMax / this.iconGridSizeU);
                int ivMax = (int) (vMax / this.iconGridSizeV);

                for (int iu = iuMin; iu <= iuMax; ++iu) {
                    if (iu >= 0 && iu < this.iconGridCountX) {
                        for (int iv = ivMin; iv <= ivMax; ++iv) {
                            if (iv >= 0 && iv < this.iconGridCountX) {
                                int index = iv * this.iconGridCountX + iu;
                                this.iconGrid[index] = ts;
                            } else {
                                Config.warn("Invalid grid V: " + iv + ", icon: " + ts.getIconName());
                            }
                        }
                    } else {
                        Config.warn("Invalid grid U: " + iu + ", icon: " + ts.getIconName());
                    }
                }
            }
        }
    }

    public TextureAtlasSprite getIconByUV(double u, double v) {
        if (this.iconGrid == null) {
            return null;
        } else {
            int iu = (int) (u / this.iconGridSizeU);
            int iv = (int) (v / this.iconGridSizeV);
            int index = iv * this.iconGridCountX + iu;
            return index >= 0 && index <= this.iconGrid.length ? this.iconGrid[index] : null;
        }
    }
}
