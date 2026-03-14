package com.mamiyaotaru.voxelmap.textures;

import com.google.common.collect.Maps;
import com.mamiyaotaru.voxelmap.util.GLShim;
import com.mamiyaotaru.voxelmap.util.ImageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ReportedException;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class TextureAtlas extends AbstractTexture {
    private static final Logger logger = LogManager.getLogger();
    private final Map<String, Sprite> mapRegisteredSprites;
    private final Map<String, Sprite> mapUploadedSprites;
    private final String basePath;
    private final IIconCreator iconCreator;
    private final int mipmapLevels = 0;
    private final Sprite missingImage;
    private final Sprite failedImage;
    private Stitcher stitcher;

    public TextureAtlas(String basePath) {
        this(basePath, null);
    }

    public TextureAtlas(String basePath, IIconCreator iconCreator) {
        this.mapRegisteredSprites = Maps.newHashMap();
        this.mapUploadedSprites = Maps.newHashMap();
        this.missingImage = new Sprite("missingno");
        this.failedImage = new Sprite("notfound");
        this.basePath = basePath;
        this.iconCreator = iconCreator;
    }

    private void initMissingImage() {
        int[] missingTextureData = new int[1];
        Arrays.fill(missingTextureData, 0);
        this.missingImage.setIconWidth(1);
        this.missingImage.setIconHeight(1);
        this.missingImage.setTextureData(missingTextureData);
        this.failedImage.copyFrom(this.missingImage);
        this.failedImage.setTextureData(missingTextureData);
    }

    public void loadTexture(IResourceManager resourceManager) throws IOException {
        if (this.iconCreator != null) {
            this.loadTextureAtlas(this.iconCreator);
        }
    }

    public void reset() {
        this.mapRegisteredSprites.clear();
        this.mapUploadedSprites.clear();
        this.initMissingImage();
        int glMaxTextureSize = Minecraft.getGLMaximumTextureSize();
        this.stitcher = new Stitcher(glMaxTextureSize, glMaxTextureSize, 0);
    }

    public void loadTextureAtlas(IIconCreator iconCreator) {
        this.reset();
        iconCreator.addIcons(this);
        this.stitch();
    }

    public void stitch() {
        for (Entry<String, Sprite> entry : this.mapRegisteredSprites.entrySet()) {
            Sprite icon = entry.getValue();
            this.stitcher.addSprite(icon);
        }

        try {
            this.stitcher.doStitch();
        } catch (StitcherException e) {
            throw e;
        }

        logger.info("Created: {}x{} {}-atlas", new Object[]{this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight(), this.basePath});
        TextureUtilLegacy.allocateTextureImpl(this.getGlTextureId(), 0, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
        int[] zeros = new int[this.stitcher.getCurrentImageWidth() * this.stitcher.getCurrentImageHeight()];
        Arrays.fill(zeros, 0);
        TextureUtilLegacy.uploadTexture(this.getGlTextureId(), zeros, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
        HashMap<String, Sprite> tempMapRegisteredSprites = Maps.newHashMap(this.mapRegisteredSprites);

        for (Sprite icon : this.stitcher.getStitchSlots()) {
            String iconName = icon.getIconName();
            tempMapRegisteredSprites.remove(iconName);
            this.mapUploadedSprites.put(iconName, icon);

            try {
                TextureUtilLegacy.uploadTextureMipmap(
                        new int[][]{icon.getTextureData()}, icon.getIconWidth(), icon.getIconHeight(), icon.getOriginX(), icon.getOriginY(), false, false
                );
            } catch (Throwable var19) {
                CrashReport crashReport = CrashReport.makeCrashReport(var19, "Stitching texture atlas");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("Texture being stitched together");
                crashReportCategory.addDetail("Atlas path", this.basePath);
                crashReportCategory.addDetail("Sprite", icon);
                throw new ReportedException(crashReport);
            }
        }

        for (Sprite icon : tempMapRegisteredSprites.values()) {
            icon.copyFrom(this.missingImage);
        }

        this.mapRegisteredSprites.clear();
        this.missingImage.initSprite(this.getHeight(), this.getWidth(), 0, 0);
        this.failedImage.initSprite(this.getHeight(), this.getWidth(), 0, 0);
        ImageUtils.saveImage(
                this.basePath.replaceAll("/", "_"), this.getGlTextureId(), 0, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight()
        );
    }

    public void stitchNew() {
        for (Entry<String, Sprite> entry : this.mapRegisteredSprites.entrySet()) {
            Sprite icon = entry.getValue();
            this.stitcher.addSprite(icon);
        }

        int oldWidth = this.stitcher.getCurrentImageWidth();
        int oldHeight = this.stitcher.getCurrentImageHeight();

        try {
            this.stitcher.doStitchNew();
        } catch (StitcherException var20) {
            throw var20;
        }

        if (oldWidth == this.stitcher.getCurrentImageWidth() && oldHeight == this.stitcher.getCurrentImageHeight()) {
            GLShim.glBindTexture(3553, this.glTextureId);
        } else {
            logger.info("Resized to: {}x{} {}-atlas", new Object[]{this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight(), this.basePath});
            TextureUtilLegacy.allocateTextureImpl(this.getGlTextureId(), 0, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
            int[] zeros = new int[this.stitcher.getCurrentImageWidth() * this.stitcher.getCurrentImageHeight()];
            Arrays.fill(zeros, 0);
            TextureUtilLegacy.uploadTexture(this.getGlTextureId(), zeros, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight());
        }

        HashMap<String, Sprite> tempMapRegisteredSprites = Maps.newHashMap(this.mapRegisteredSprites);

        for (Sprite icon : this.stitcher.getStitchSlots()) {
            String iconName = icon.getIconName();
            tempMapRegisteredSprites.remove(iconName);
            this.mapUploadedSprites.put(iconName, icon);

            try {
                TextureUtilLegacy.uploadTextureMipmap(
                        new int[][]{icon.getTextureData()}, icon.getIconWidth(), icon.getIconHeight(), icon.getOriginX(), icon.getOriginY(), false, false
                );
            } catch (Throwable var19) {
                CrashReport crashReport = CrashReport.makeCrashReport(var19, "Stitching texture atlas");
                CrashReportCategory crashReportCategory = crashReport.makeCategory("Texture being stitched together");
                crashReportCategory.addDetail("Atlas path", this.basePath);
                crashReportCategory.addDetail("Sprite", icon);
                throw new ReportedException(crashReport);
            }
        }

        for (Sprite icon : tempMapRegisteredSprites.values()) {
            icon.copyFrom(this.missingImage);
        }

        this.mapRegisteredSprites.clear();
        this.missingImage.initSprite(this.getHeight(), this.getWidth(), 0, 0);
        this.failedImage.initSprite(this.getHeight(), this.getWidth(), 0, 0);
        if (oldWidth != this.stitcher.getCurrentImageWidth() || oldHeight != this.stitcher.getCurrentImageHeight()) {
            ImageUtils.saveImage(
                    this.basePath.replaceAll("/", "_"), this.getGlTextureId(), 0, this.stitcher.getCurrentImageWidth(), this.stitcher.getCurrentImageHeight()
            );
        }
    }

    public Sprite getIconAt(float x, float y) {
        Iterator<Entry<String, Sprite>> uploadedSpritesEntriesIterator = this.mapUploadedSprites.entrySet().iterator();

        while (uploadedSpritesEntriesIterator.hasNext()) {
            Sprite icon = uploadedSpritesEntriesIterator.next().getValue();
            if (x >= icon.originX && x < icon.originX + icon.width && y >= icon.originY && y < icon.originY + icon.height) {
                return icon;
            }
        }

        return this.missingImage;
    }

    public Sprite getAtlasSprite(String name) {
        Sprite icon = this.mapUploadedSprites.get(name);
        if (icon == null) {
            icon = this.missingImage;
        }

        return icon;
    }

    public Sprite getAtlasSpriteIncludingYetToBeStitched(String name) {
        Sprite icon = this.mapUploadedSprites.get(name);
        if (icon == null) {
            icon = this.mapRegisteredSprites.get(name);
        }

        if (icon == null) {
            icon = this.missingImage;
        }

        return icon;
    }

    public Sprite registerIconForResource(ResourceLocation resourceLocation, IResourceManager resourceManager) {
        if (resourceLocation == null) {
            throw new IllegalArgumentException("Location cannot be null!");
        }

        Sprite icon = this.mapRegisteredSprites.get(resourceLocation.toString());
        if (icon == null) {
            icon = Sprite.spriteFromResourceLocation(resourceLocation);

            try {
                IResource entryResource = resourceManager.getResource(resourceLocation);
                BufferedImage entryBufferedImage = TextureUtilLegacy.readBufferedImage(entryResource.getInputStream());
                icon.bufferedImageToIntData(entryBufferedImage);
                entryBufferedImage.flush();
            } catch (RuntimeException var23) {
                logger.error("Unable to parse metadata from " + resourceLocation, var23);
            } catch (IOException var24) {
                logger.error("Using missing texture, unable to load " + resourceLocation, var24);
            }

            this.mapRegisteredSprites.put(resourceLocation.toString(), icon);
        }

        return icon;
    }

    public Sprite registerIconForBufferedImage(String name, BufferedImage bufferedImage) {
        if (name != null && !name.equals("")) {
            Sprite icon = this.mapRegisteredSprites.get(name);
            if (icon == null) {
                icon = Sprite.spriteFromString(name);
                icon.bufferedImageToIntData(bufferedImage);
                bufferedImage.flush();
                this.mapRegisteredSprites.put(name, icon);
            }

            return icon;
        } else {
            throw new IllegalArgumentException("Name cannot be null!");
        }
    }

    public void registerOrOverwriteSprite(String name, BufferedImage bufferedImage) {
        if (name != null && !name.equals("")) {
            Sprite icon = this.mapRegisteredSprites.get(name);
            if (icon != null) {
                icon.bufferedImageToIntData(bufferedImage);
            } else {
                icon = this.getAtlasSprite(name);
                if (icon != null) {
                    icon.bufferedImageToIntData(bufferedImage);

                    try {
                        GLShim.glBindTexture(3553, this.glTextureId);
                        TextureUtilLegacy.uploadTextureMipmap(
                                new int[][]{icon.getTextureData()}, icon.getIconWidth(), icon.getIconHeight(), icon.getOriginX(), icon.getOriginY(), false, false
                        );
                    } catch (Throwable var19) {
                        CrashReport crashReport = CrashReport.makeCrashReport(var19, "Stitching texture atlas");
                        CrashReportCategory crashReportCategory = crashReport.makeCategory("Texture being stitched together");
                        crashReportCategory.addDetail("Atlas path", this.basePath);
                        crashReportCategory.addDetail("Sprite", icon);
                        throw new ReportedException(crashReport);
                    }
                }
            }

            bufferedImage.flush();
        } else {
            throw new IllegalArgumentException("Name cannot be null!");
        }
    }

    public Sprite getMissingImage() {
        return this.missingImage;
    }

    public Sprite getFailedImage() {
        return this.failedImage;
    }

    public void registerFailedIcon(String name) {
        this.mapUploadedSprites.put(name, this.failedImage);
    }

    public void registerMaskedIcon(String name, Sprite originalIcon) {
        Sprite existingIcon = this.mapUploadedSprites.get(name);
        if (existingIcon == null) {
            existingIcon = this.mapRegisteredSprites.get(name);
        }

        if (existingIcon == null) {
            this.mapUploadedSprites.put(name, originalIcon);
        }
    }

    public int getWidth() {
        return this.stitcher.getCurrentWidth();
    }

    public int getHeight() {
        return this.stitcher.getCurrentHeight();
    }

    public int getImageWidth() {
        return this.stitcher.getCurrentImageWidth();
    }

    public int getImageHeight() {
        return this.stitcher.getCurrentImageHeight();
    }
}
