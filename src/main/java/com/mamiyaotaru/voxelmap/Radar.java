package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.interfaces.IRadar;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.ornithe.mixins.RenderAccessor;
import com.mamiyaotaru.voxelmap.textures.FontRendererWithAtlas;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.StitcherException;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.entity.DolphinModel;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderTropicalFish;
import net.minecraft.client.renderer.entity.model.*;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.ThreadDownloadImageData;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Radar implements IRadar {
    public static final int CLOTH = 0;
    public static final int CHAIN = 4;
    public static final int IRON = 5;
    public static final int GOLD = 6;
    public static final int DIAMOND = 7;
    public static final int TURTLE = 8;
    public static final int UNKNOWN = EnumMobs.UNKNOWN.ordinal();
    public static final int CUSTOM = EnumMobs.CUSTOM.ordinal();
    public MapSettingsManager minimapOptions = null;
    public RadarSettingsManager options = null;
    public HashMap<String, Integer> mpContactsSkinGetTries = new HashMap<>();
    public HashMap<String, Integer> contactsSkinGetTries = new HashMap<>();
    UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
    private Minecraft game;
    private IVoxelMap master = null;
    private LayoutVariables layoutVariables = null;
    private final FontRendererWithAtlas fontRenderer;
    private final TextureAtlas textureAtlas;
    private boolean newMobs = false;
    private final boolean enabled = true;
    private boolean completedLoading = false;
    private int timer = 500;
    private float direction = 0.0F;
    private final ArrayList<Contact> contacts = new ArrayList<>(40);
    private final BufferedImage[][] mobImages = new BufferedImage[EnumMobs.values().length - 3][2];
    private final boolean[] builtInCustom = new boolean[EnumMobs.values().length];
    private Sprite[] clothIcons = new Sprite[]{null, null};
    private final BufferedImage[][] armorImages = new BufferedImage[9][2];
    private final String[] armorNames = new String[]{"cloth", "clothOverlay", "clothOuter", "clothOverlayOuter", "chain", "iron", "gold", "diamond", "turtle"};
    private boolean randomobsOptifine = false;
    private Class<?> randomEntitiesClass = null;
    private Field mapPropertiesField = null;
    private java.util.Map<String, ?> mapProperties = null;
    private Field randomEntityField = null;
    private Object randomEntity = null;
    private Class<?> iRandomEntityClass = null;
    private Class<?> randomEntityClass = null;
    private Method setEntityMethod = null;
    private Class<?> randomEntitiesPropertiesClass = null;
    private Method getEntityTextureMethod = null;
    private boolean hasCustomNPCs = false;
    private Class<?> entityCustomNpcClass = null;
    private Class<?> modelDataClass = null;
    private Class<?> entityNPCInterfaceClass = null;
    private Field modelDataField = null;
    private Method getEntityMethod = null;
    private Class<?> modelScaleRendererClass = null;
    private boolean lastOutlines = true;

    public Radar(IVoxelMap master) {
        this.master = master;
        this.minimapOptions = master.getMapOptions();
        this.options = master.getRadarOptions();
        this.game = Minecraft.getInstance();
        this.fontRenderer = new FontRendererWithAtlas(this.game.getTextureManager(), new ResourceLocation("textures/font/ascii.png"));
        this.textureAtlas = new TextureAtlas("mobs");
        this.textureAtlas.setBlurMipmapDirect(false, false);

        try {
            this.randomEntitiesClass = Class.forName("net.optifine.RandomEntities");
            this.mapPropertiesField = this.randomEntitiesClass.getDeclaredField("mapProperties");
            this.mapPropertiesField.setAccessible(true);
            this.mapProperties = (java.util.Map<String, ?>) this.mapPropertiesField.get(null);
            this.randomEntityField = this.randomEntitiesClass.getDeclaredField("randomEntity");
            this.randomEntityField.setAccessible(true);
            this.randomEntity = this.randomEntityField.get(null);
            this.iRandomEntityClass = Class.forName("net.optifine.IRandomEntity");
            this.randomEntityClass = Class.forName("net.optifine.RandomEntity");
            Class<?>[] argClasses1 = new Class[]{Entity.class};
            this.setEntityMethod = this.randomEntityClass.getDeclaredMethod("setEntity", argClasses1);
            this.randomEntitiesPropertiesClass = Class.forName("net.optifine.RandomEntityProperties");
            Class<?>[] argClasses2 = new Class[]{ResourceLocation.class, this.iRandomEntityClass};
            this.getEntityTextureMethod = this.randomEntitiesPropertiesClass.getDeclaredMethod("getTextureLocation", argClasses2);
            this.randomobsOptifine = true;
        } catch (ClassNotFoundException e) {
            this.randomobsOptifine = false;
        } catch (NoSuchMethodException ex) {
            this.randomobsOptifine = false;
        } catch (NoSuchFieldException exx) {
            this.randomobsOptifine = false;
        } catch (SecurityException exxx) {
            this.randomobsOptifine = false;
        } catch (IllegalArgumentException exxxx) {
            this.randomobsOptifine = false;
        } catch (IllegalAccessException exxxxx) {
            this.randomobsOptifine = false;
        }

        try {
            this.entityCustomNpcClass = Class.forName("noppes.npcs.entity.EntityCustomNpc");
            this.modelDataClass = Class.forName("noppes.npcs.ModelData");
            this.modelDataField = this.entityCustomNpcClass.getField("modelData");
            this.entityNPCInterfaceClass = Class.forName("noppes.npcs.entity.EntityNPCInterface");
            this.getEntityMethod = this.modelDataClass.getMethod("getEntity", this.entityNPCInterfaceClass);
            this.modelScaleRendererClass = Class.forName("noppes.npcs.client.model.ModelScaleRenderer");
            this.hasCustomNPCs = true;
        } catch (ClassNotFoundException e) {
            this.hasCustomNPCs = false;
        } catch (NoSuchFieldException ex) {
            this.hasCustomNPCs = false;
        } catch (NoSuchMethodException exx) {
            this.hasCustomNPCs = false;
        }
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.loadTexturePackIcons();
        this.fontRenderer.onResourceManagerReload(resourceManager);
    }

    private void loadTexturePackIcons() {
        this.completedLoading = false;

        try {
            this.mpContactsSkinGetTries.clear();
            this.contactsSkinGetTries.clear();
            this.textureAtlas.reset();
            if (ReflectionUtils.classExists("com.prupe.mcpatcher.mob.MobOverlay")
                    && ImageUtils.loadImage(new ResourceLocation("mcpatcher/mob/cow/mooshroom_overlay.png"), 0, 0, 1, 1) != null) {
                EnumMobs.MOOSHROOM.secondaryResourceLocation = new ResourceLocation("mcpatcher/mob/cow/mooshroom_overlay.png");
            } else {
                EnumMobs.MOOSHROOM.secondaryResourceLocation = new ResourceLocation("textures/block/red_mushroom.png");
            }

            Arrays.fill(this.builtInCustom, false);

            for (int t = 0; t < this.mobImages.length; t++) {
                try {
                    int intendedSize = 8;
                    String fullPath = "";
                    InputStream is = null;
                    if (is == null) {
                        fullPath = "textures/icons/" + EnumMobs.values()[t].classPath + ".png";

                        try {
                            is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                        } catch (IOException exx) {
                            is = null;
                        }
                    }

                    if (is == null) {
                        fullPath = "textures/icons/" + EnumMobs.values()[t].classPath + "8.png";

                        try {
                            is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                        } catch (IOException exx) {
                            is = null;
                        }
                    }

                    if (is == null) {
                        intendedSize = 16;
                        fullPath = "textures/icons/" + EnumMobs.values()[t].classPath + "16.png";

                        try {
                            is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                        } catch (IOException exx) {
                            is = null;
                        }
                    }

                    if (is == null) {
                        intendedSize = 32;
                        fullPath = "textures/icons/" + EnumMobs.values()[t].classPath + "32.png";

                        try {
                            is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                        } catch (IOException exx) {
                            is = null;
                        }
                    }

                    if (is != null) {
                        BufferedImage mobSkin = ImageIO.read(is);
                        is.close();
                        mobSkin = ImageUtils.loadImage(mobSkin, 0, 0, mobSkin.getWidth(), mobSkin.getHeight(), mobSkin.getWidth(), mobSkin.getHeight());
                        float scale = (float) mobSkin.getWidth() / intendedSize;
                        this.mobImages[t][0] = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(mobSkin, 1.0F / scale)), this.options.outlines);
                        this.mobImages[t][1] = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(mobSkin, 2.0F / scale)), this.options.outlines);
                        this.builtInCustom[t] = true;
                    }
                } catch (Exception ex) {
                    this.builtInCustom[t] = false;
                }

                if (!this.builtInCustom[t]) {
                    BufferedImage image = null;
                    image = this.createImageFromTypeAndResourceLocations(
                            EnumMobs.values()[t], EnumMobs.values()[t].resourceLocation, EnumMobs.values()[t].secondaryResourceLocation, null
                    );
                    if (image == null) {
                        System.err.println("Failed getting mob " + t + ": " + EnumMobs.values()[t].classPath);
                        image = new BufferedImage(2, 2, 6);
                    }

                    float scale = (float) image.getWidth() / EnumMobs.values()[t].expectedWidth;
                    this.mobImages[t][1] = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(image, 2.0F / scale)), this.options.outlines);
                    this.mobImages[t][0] = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(image, 1.0F / scale)), this.options.outlines);
                }

                String nameWithoutSize = EnumMobs.values()[t].id + EnumMobs.values()[t].resourceLocation.toString();
                nameWithoutSize = nameWithoutSize
                        + (EnumMobs.values()[t].secondaryResourceLocation != null ? EnumMobs.values()[t].secondaryResourceLocation.toString() : "");
                this.textureAtlas.registerIconForBufferedImage(nameWithoutSize + "1", this.mobImages[t][1]);
                this.textureAtlas.registerIconForBufferedImage(nameWithoutSize + "0", this.mobImages[t][0]);
            }

            this.armorImages[0][0] = ImageUtils.loadImage(new ResourceLocation("textures/models/armor/leather_layer_1.png"), 8, 8, 8, 8);
            this.armorImages[1][0] = ImageUtils.loadImage(new ResourceLocation("textures/models/armor/leather_layer_1.png"), 40, 8, 8, 8);
            this.armorImages[2][0] = ImageUtils.loadImage(new ResourceLocation("textures/models/armor/leather_layer_1_overlay.png"), 8, 8, 8, 8);
            this.armorImages[3][0] = ImageUtils.loadImage(new ResourceLocation("textures/models/armor/leather_layer_1_overlay.png"), 40, 8, 8, 8);
            this.armorImages[4][0] = ImageUtils.addImages(
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/chainmail_layer_1.png"), 8, 8, 8, 8),
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/chainmail_layer_1.png"), 40, 8, 8, 8),
                    0.0F,
                    0.0F,
                    8,
                    8
            );
            this.armorImages[5][0] = ImageUtils.addImages(
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/iron_layer_1.png"), 8, 8, 8, 8),
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/iron_layer_1.png"), 40, 8, 8, 8),
                    0.0F,
                    0.0F,
                    8,
                    8
            );
            this.armorImages[6][0] = ImageUtils.addImages(
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/gold_layer_1.png"), 8, 8, 8, 8),
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/gold_layer_1.png"), 40, 8, 8, 8),
                    0.0F,
                    0.0F,
                    8,
                    8
            );
            this.armorImages[7][0] = ImageUtils.addImages(
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/diamond_layer_1.png"), 8, 8, 8, 8),
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/diamond_layer_1.png"), 40, 8, 8, 8),
                    0.0F,
                    0.0F,
                    8,
                    8
            );
            this.armorImages[8][0] = ImageUtils.addImages(
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/turtle_layer_1.png"), 8, 8, 8, 8),
                    ImageUtils.loadImage(new ResourceLocation("textures/models/armor/turtle_layer_1.png"), 40, 8, 8, 8),
                    0.0F,
                    0.0F,
                    8,
                    8
            );

            for (int t = 0; t < this.armorImages.length; t++) {
                float scale = this.armorImages[t][0].getWidth() / 8.0F;
                this.armorImages[t][1] = ImageUtils.fillOutline(
                        ImageUtils.pad(ImageUtils.scaleImage(this.armorImages[t][0], 2.0F / scale)), this.options.outlines, true, 16, t
                );
                this.armorImages[t][0] = ImageUtils.fillOutline(
                        ImageUtils.pad(ImageUtils.scaleImage(this.armorImages[t][0], 1.0F / scale)), this.options.outlines, true, 8, t
                );
                Sprite icon1 = this.textureAtlas.registerIconForBufferedImage("armor " + this.armorNames[t] + " 1", this.armorImages[t][1]);
                Sprite icon0 = this.textureAtlas.registerIconForBufferedImage("armor " + this.armorNames[t] + " 0", this.armorImages[t][0]);
                if (t == 0) {
                    this.clothIcons = new Sprite[]{icon0, icon1};
                }
            }

            BufferedImage sheepFur = ImageUtils.loadImage(new ResourceLocation("textures/entity/sheep/sheep_fur.png"), 6, 6, 6, 6);
            float scale = sheepFur.getWidth() / 6.0F;
            BufferedImage sheepFur1 = ImageUtils.scaleImage(sheepFur, 4.0F / scale * 1.0625F);
            sheepFur1 = ImageUtils.eraseArea(sheepFur1, 2, 2, sheepFur1.getWidth() - 4, sheepFur1.getHeight() - 4, sheepFur1.getWidth(), sheepFur1.getHeight());
            sheepFur1 = ImageUtils.fillOutline(ImageUtils.pad(sheepFur1), this.options.outlines, true, 25, -10);
            sheepFur1 = ImageUtils.fillOutline(ImageUtils.pad(sheepFur1), this.options.outlines, true, 27, -10);
            BufferedImage sheepFur0 = ImageUtils.scaleImage(sheepFur, 2.0F / scale * 1.0625F);
            sheepFur0 = ImageUtils.eraseArea(sheepFur0, 1, 1, sheepFur0.getWidth() - 2, sheepFur0.getHeight() - 2, sheepFur0.getWidth(), sheepFur0.getHeight());
            sheepFur0 = ImageUtils.fillOutline(ImageUtils.pad(sheepFur0), this.options.outlines, true, 13, -10);
            sheepFur0 = ImageUtils.fillOutline(ImageUtils.pad(sheepFur0), this.options.outlines, true, 11, -10);
            this.textureAtlas.registerIconForBufferedImage("sheepfur1", sheepFur1);
            this.textureAtlas.registerIconForBufferedImage("sheepfur0", sheepFur0);
            BufferedImage crown = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/crown.png"), 0, 0, 16, 16, 16, 16);
            BufferedImage crown1 = ImageUtils.fillOutline(crown, this.options.outlines, true, 16, -10);
            BufferedImage crown0 = ImageUtils.fillOutline(ImageUtils.scaleImage(crown, 0.5F), this.options.outlines, true, 8, -10);
            this.textureAtlas.registerIconForBufferedImage("crown1", crown1);
            this.textureAtlas.registerIconForBufferedImage("crown0", crown0);
            BufferedImage glow = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/glow.png"), 0, 0, 16, 16, 16, 16);
            glow = ImageUtils.fillOutline(glow, this.options.outlines, true, 16, -10);
            this.textureAtlas.registerIconForBufferedImage("glow", glow);
            ResourceLocation fontResourceLocation = new ResourceLocation("textures/font/ascii.png");
            BufferedImage fontImage = ImageUtils.loadImage(fontResourceLocation, 0, 0, 128, 128, 128, 128);
            if (fontImage.getWidth() > 1024 || fontImage.getHeight() > 1024) {
                int maxDim = Math.max(fontImage.getWidth(), fontImage.getHeight());
                float scaleBy = 1024.0F / maxDim;
                fontImage = ImageUtils.scaleImage(fontImage, scaleBy);
            }

            fontImage = ImageUtils.addImages(
                    new BufferedImage(fontImage.getWidth() + 2, fontImage.getHeight() + 2, fontImage.getType()),
                    fontImage,
                    1.0F,
                    1.0F,
                    fontImage.getWidth() + 2,
                    fontImage.getHeight() + 2
            );
            Sprite fontSprite = this.textureAtlas.registerIconForBufferedImage(fontResourceLocation.toString(), fontImage);
            this.fontRenderer.setFontSprite(fontSprite);
            this.fontRenderer.setFontRef(this.textureAtlas.getGlTextureId());
            this.textureAtlas.stitch();
            this.completedLoading = true;
        } catch (Exception e) {
            System.err.println("Failed getting mobs " + e.getLocalizedMessage());
            e.printStackTrace();
        }
    }

    private BufferedImage createImageFromTypeAndResourceLocations(
            EnumMobs type, ResourceLocation resourceLocation, ResourceLocation resourceLocationSecondary, Entity entity
    ) {
        BufferedImage mobImage = null;
        BufferedImage mobImageSecondary = null;

        try {
            mobImage = ImageUtils.createBufferedImageFromResourceLocation(resourceLocation);
            if (resourceLocationSecondary != null) {
                mobImageSecondary = ImageUtils.createBufferedImageFromResourceLocation(resourceLocationSecondary);
            }

            return this.createImageFromTypeAndImages(type, mobImage, mobImageSecondary, entity);
        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage createImageFromTypeAndImages(EnumMobs type, BufferedImage mobImage, BufferedImage mobImageSecondary, Entity entity) {
        BufferedImage image = null;
        switch (type) {
            case BLANK:
                image = ImageUtils.blankImage(mobImage, 2, 2);
                break;
            case GENERICHOSTILE:
                image = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/hostile.png"), 0, 0, 16, 16, 16, 16);
                break;
            case GENERICNEUTRAL:
                image = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/neutral.png"), 0, 0, 16, 16, 16, 16);
                break;
            case GENERICTAME:
                image = ImageUtils.loadImage(new ResourceLocation("voxelmap", "images/radar/tame.png"), 0, 0, 16, 16, 16, 16);
                break;
            case BAT:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12, 64, 64), ImageUtils.loadImage(mobImage, 25, 1, 3, 4), 0.0F, 0.0F, 8, 12),
                                ImageUtils.flipHorizontal(ImageUtils.loadImage(mobImage, 25, 1, 3, 4)),
                                5.0F,
                                0.0F,
                                8,
                                12
                        ),
                        ImageUtils.loadImage(mobImage, 6, 6, 6, 6),
                        1.0F,
                        3.0F,
                        8,
                        12
                );
                break;
            case BLAZE:
                image = ImageUtils.loadImage(mobImage, 8, 8, 8, 8);
                break;
            case CAT:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 5, 5), ImageUtils.loadImage(mobImage, 5, 5, 5, 4), 0.0F, 1.0F, 5, 5),
                                        ImageUtils.loadImage(mobImage, 2, 26, 3, 2),
                                        1.0F,
                                        3.0F,
                                        5,
                                        5
                                ),
                                ImageUtils.loadImage(mobImage, 2, 12, 1, 1),
                                1.0F,
                                0.0F,
                                5,
                                5
                        ),
                        ImageUtils.loadImage(mobImage, 8, 12, 1, 1),
                        3.0F,
                        0.0F,
                        5,
                        5
                );
                break;
            case CAVESPIDER:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 8), ImageUtils.loadImage(mobImage, 6, 6, 6, 6), 1.0F, 1.0F, 8, 8),
                        ImageUtils.loadImage(mobImage, 40, 12, 8, 8),
                        0.0F,
                        0.0F,
                        8,
                        8
                );
                break;
            case CHICKEN:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.loadImage(mobImage, 2, 3, 6, 6), ImageUtils.loadImage(mobImage, 16, 2, 4, 2), 1.0F, 2.0F, 6, 6),
                        ImageUtils.loadImage(mobImage, 16, 6, 2, 2),
                        2.0F,
                        4.0F,
                        6,
                        6
                );
                break;
            case COD:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(
                                                        ImageUtils.blankImage(mobImage, 16, 5, 32, 32), ImageUtils.loadImage(mobImage, 15, 3, 1, 3, 32, 32), 1.0F, 1.0F, 16, 5
                                                ),
                                                ImageUtils.loadImage(mobImage, 16, 3, 3, 4, 32, 32),
                                                2.0F,
                                                1.0F,
                                                16,
                                                5
                                        ),
                                        ImageUtils.loadImage(mobImage, 9, 7, 7, 4, 32, 32),
                                        5.0F,
                                        1.0F,
                                        16,
                                        5
                                ),
                                ImageUtils.loadImage(mobImage, 26, 7, 4, 4, 32, 32),
                                12.0F,
                                1.0F,
                                16,
                                5
                        ),
                        ImageUtils.loadImage(mobImage, 26, 0, 6, 1, 32, 32),
                        4.0F,
                        0.0F,
                        16,
                        5
                );
                break;
            case COW:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.blankImage(mobImage, 10, 10), ImageUtils.loadImage(mobImage, 6, 6, 8, 8), 1.0F, 1.0F, 10, 10),
                                ImageUtils.loadImage(mobImage, 23, 1, 1, 3),
                                0.0F,
                                0.0F,
                                10,
                                10
                        ),
                        ImageUtils.loadImage(mobImage, 23, 1, 1, 3),
                        9.0F,
                        0.0F,
                        10,
                        10
                );
                break;
            case CREEPER:
                image = ImageUtils.loadImage(mobImage, 8, 8, 8, 8);
                break;
            case DOLPHIN:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 10, 10), ImageUtils.loadImage(mobImage, 0, 6, 6, 7), 0.0F, 1.0F, 10, 10),
                        ImageUtils.loadImage(mobImage, 0, 17, 4, 2),
                        6.0F,
                        6.0F,
                        10,
                        10
                );
                break;
            case DROWNED:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImageSecondary, 8, 8, 8, 8), 0.0F, 0.0F, 8, 8),
                                ImageUtils.loadImage(mobImage, 40, 8, 8, 8),
                                0.0F,
                                0.0F,
                                8,
                                8
                        ),
                        ImageUtils.loadImage(mobImageSecondary, 40, 8, 8, 8),
                        0.0F,
                        0.0F,
                        8,
                        8
                );
                break;
            case ENDERDRAGON:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(
                                                        ImageUtils.blankImage(mobImage, 16, 20, 256, 256), ImageUtils.loadImage(mobImage, 128, 46, 16, 16, 256, 256), 0.0F, 4.0F, 16, 16
                                                ),
                                                ImageUtils.loadImage(mobImage, 192, 60, 12, 5, 256, 256),
                                                2.0F,
                                                11.0F,
                                                16,
                                                16
                                        ),
                                        ImageUtils.loadImage(mobImage, 192, 81, 12, 4, 256, 256),
                                        2.0F,
                                        16.0F,
                                        16,
                                        16
                                ),
                                ImageUtils.loadImage(mobImage, 6, 6, 2, 4, 256, 256),
                                3.0F,
                                0.0F,
                                16,
                                16
                        ),
                        ImageUtils.flipHorizontal(ImageUtils.loadImage(mobImage, 6, 6, 2, 4, 256, 256)),
                        11.0F,
                        0.0F,
                        16,
                        16
                );
                break;
            case ENDERMAN:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImage, 8, 24, 8, 8), 0.0F, 0.0F, 8, 8),
                                ImageUtils.loadImage(mobImage, 8, 8, 8, 8),
                                0.0F,
                                0.0F,
                                8,
                                8
                        ),
                        ImageUtils.loadImage(mobImage, 8, 12, 8, 1),
                        0.0F,
                        4.0F,
                        8,
                        8
                );
                break;
            case ENDERMITE:
                image = ImageUtils.loadImage(mobImage, 2, 2, 4, 3);
                break;
            case EVOKER:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12), ImageUtils.loadImage(mobImage, 8, 8, 8, 10, 64, 64), 0.0F, 1.0F, 8, 12),
                        ImageUtils.loadImage(mobImage, 26, 2, 2, 4, 64, 64),
                        3.0F,
                        8.0F,
                        8,
                        12
                );
                break;
            case GHAST:
                image = ImageUtils.loadImage(mobImage, 16, 16, 16, 16);
                break;
            case GHASTATTACKING:
                image = ImageUtils.loadImage(mobImage, 16, 16, 16, 16);
                break;
            case GUARDIAN:
                image = ImageUtils.scaleImage(
                        ImageUtils.addImages(ImageUtils.loadImage(mobImage, 16, 16, 12, 12), ImageUtils.loadImage(mobImage, 9, 1, 2, 2), 5.0F, 5.5F, 12, 12), 0.5F
                );
                break;
            case GUARDIANELDER:
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 16, 16, 12, 12), ImageUtils.loadImage(mobImage, 9, 1, 2, 2), 5.0F, 5.5F, 12, 12);
                break;
            case HORSE:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(
                                                        ImageUtils.addImages(
                                                                ImageUtils.blankImage(mobImage, 16, 24, 64, 64), ImageUtils.loadImage(mobImage, 56, 38, 2, 16, 64, 64), 1.0F, 7.0F, 16, 24
                                                        ),
                                                        ImageUtils.loadImage(mobImage, 0, 42, 7, 12, 64, 64),
                                                        3.0F,
                                                        12.0F,
                                                        16,
                                                        24
                                                ),
                                                ImageUtils.loadImage(mobImage, 0, 20, 7, 5, 64, 64),
                                                3.0F,
                                                7.0F,
                                                16,
                                                24
                                        ),
                                        ImageUtils.loadImage(mobImage, 0, 30, 5, 5, 64, 64),
                                        10.0F,
                                        7.0F,
                                        16,
                                        24
                                ),
                                ImageUtils.loadImage(mobImage, 19, 17, 1, 3, 64, 64),
                                3.0F,
                                4.0F,
                                16,
                                24
                        ),
                        ImageUtils.loadImage(mobImage, 0, 13, 1, 7, 64, 64),
                        3.0F,
                        0.0F,
                        16,
                        24
                );
                break;
            case HUSK:
                image = ImageUtils.addImages(
                        ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64), ImageUtils.loadImage(mobImage, 40, 8, 8, 8, 64, 64), 0.0F, 0.0F, 8, 8
                );
                break;
            case ILLUSIONER:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12), ImageUtils.loadImage(mobImage, 8, 8, 8, 10, 64, 64), 0.0F, 1.0F, 8, 12),
                        ImageUtils.loadImage(mobImage, 26, 2, 2, 4, 64, 64),
                        3.0F,
                        8.0F,
                        8,
                        12
                );
                break;
            case IRONGOLEM:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12, 128, 128), ImageUtils.loadImage(mobImage, 8, 8, 8, 10, 128, 128), 0.0F, 1.0F, 8, 12),
                        ImageUtils.loadImage(mobImage, 26, 2, 2, 4, 128, 128),
                        3.0F,
                        8.0F,
                        8,
                        12
                );
                break;
            case LLAMA:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.blankImage(mobImage, 8, 14, 128, 64), ImageUtils.loadImage(mobImage, 6, 20, 8, 8, 128, 64), 0.0F, 3.0F, 8, 14
                                        ),
                                        ImageUtils.loadImage(mobImage, 9, 9, 4, 4, 128, 64),
                                        2.0F,
                                        5.0F,
                                        8,
                                        14
                                ),
                                ImageUtils.loadImage(mobImage, 19, 2, 3, 3, 128, 64),
                                0.0F,
                                0.0F,
                                8,
                                14
                        ),
                        ImageUtils.loadImage(mobImage, 19, 2, 3, 3, 128, 64),
                        5.0F,
                        0.0F,
                        8,
                        14
                );
                break;
            case MAGMA:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImage, 32, 18, 8, 1), 0.0F, 3.0F, 8, 8),
                        ImageUtils.loadImage(mobImage, 32, 27, 8, 1),
                        0.0F,
                        4.0F,
                        8,
                        8
                );
                break;
            case MOOSHROOM:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.blankImage(mobImage, 40, 40), ImageUtils.loadImage(mobImage, 6, 6, 8, 8), 16.0F, 16.0F, 40, 40),
                                ImageUtils.loadImage(mobImage, 23, 1, 1, 3),
                                15.0F,
                                15.0F,
                                40,
                                40
                        ),
                        ImageUtils.loadImage(mobImage, 23, 1, 1, 3),
                        24.0F,
                        15.0F,
                        40,
                        40
                );
                if (mobImageSecondary != null) {
                    BufferedImage mushroomImage;
                    if (mobImageSecondary.getWidth() != mobImageSecondary.getHeight()) {
                        mushroomImage = ImageUtils.loadImage(mobImageSecondary, 32, 0, 16, 16, 48, 16);
                    } else {
                        mushroomImage = ImageUtils.loadImage(mobImageSecondary, 0, 0, 16, 16, 16, 16);
                    }

                    float ratio = (float) image.getWidth() / mushroomImage.getWidth();
                    if (ratio < 2.5) {
                        image = ImageUtils.scaleImage(image, 2.5F / ratio);
                    } else if (ratio > 2.5) {
                        mushroomImage = ImageUtils.scaleImage(mushroomImage, ratio / 2.5F);
                    }

                    image = ImageUtils.addImages(image, mushroomImage, 12.0F, 0.0F, 40, 40);
                }
                break;
            case OCELOT:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 5, 5), ImageUtils.loadImage(mobImage, 5, 5, 5, 4), 0.0F, 1.0F, 5, 5),
                                        ImageUtils.loadImage(mobImage, 2, 26, 3, 2),
                                        1.0F,
                                        3.0F,
                                        5,
                                        5
                                ),
                                ImageUtils.loadImage(mobImage, 2, 12, 1, 1),
                                1.0F,
                                0.0F,
                                5,
                                5
                        ),
                        ImageUtils.loadImage(mobImage, 8, 12, 1, 1),
                        3.0F,
                        0.0F,
                        5,
                        5
                );
                break;
            case PARROT:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(
                                                        ImageUtils.blankImage(mobImage, 8, 8, 32, 32), ImageUtils.loadImage(mobImage, 2, 22, 3, 5, 32, 32), 1.0F, 0.0F, 8, 8
                                                ),
                                                ImageUtils.loadImage(mobImage, 10, 4, 4, 1, 32, 32),
                                                2.0F,
                                                4.0F,
                                                8,
                                                8
                                        ),
                                        ImageUtils.loadImage(mobImage, 2, 4, 2, 3, 32, 32),
                                        2.0F,
                                        5.0F,
                                        8,
                                        8
                                ),
                                ImageUtils.loadImage(mobImage, 11, 8, 1, 2, 32, 32),
                                4.0F,
                                5.0F,
                                8,
                                8
                        ),
                        ImageUtils.loadImage(mobImage, 16, 8, 1, 2, 32, 32),
                        5.0F,
                        5.0F,
                        8,
                        8
                );
                break;
            case PHANTOM:
                image = ImageUtils.loadImage(mobImage, 5, 5, 7, 3, 64, 64);
                break;
            case PIG:
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImage, 16, 17, 6, 3), 1.0F, 4.0F, 8, 8);
                break;
            case PIGZOMBIE:
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImage, 40, 8, 8, 8), 0.0F, 0.0F, 8, 8);
                break;
            case PLAYER:
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImage, 40, 8, 8, 8), 0.0F, 0.0F, 8, 8);
                break;
            case POLARBEAR:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.blankImage(mobImage, 9, 8, 128, 64), ImageUtils.loadImage(mobImage, 27, 1, 2, 2, 128, 64), 0.0F, 0.0F, 9, 8
                                        ),
                                        ImageUtils.flipHorizontal(ImageUtils.loadImage(mobImage, 27, 1, 2, 2, 128, 64)),
                                        7.0F,
                                        0.0F,
                                        9,
                                        8
                                ),
                                ImageUtils.loadImage(mobImage, 7, 7, 7, 7, 128, 64),
                                1.0F,
                                1.0F,
                                9,
                                8
                        ),
                        ImageUtils.loadImage(mobImage, 3, 47, 5, 3, 128, 64),
                        2.0F,
                        5.0F,
                        9,
                        8
                );
                break;
            case PUFFERFISH:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.blankImage(mobImage, 3, 3, 32, 32), ImageUtils.loadImage(mobImage, 3, 30, 3, 2, 32, 32), 0.0F, 1.0F, 3, 3),
                                ImageUtils.loadImage(mobImage, 3, 29, 1, 1, 32, 32),
                                0.0F,
                                0.0F,
                                3,
                                3
                        ),
                        ImageUtils.loadImage(mobImage, 5, 29, 1, 1, 32, 32),
                        2.0F,
                        0.0F,
                        3,
                        3
                );
                break;
            case PUFFERFISHHALF:
                image = ImageUtils.loadImage(mobImage, 17, 27, 5, 5, 32, 32);
                break;
            case PUFFERFISHFULL:
                image = ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 32, 32);
                break;
            case RABBIT:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 5, 10), ImageUtils.loadImage(mobImage, 37, 5, 5, 4), 0.0F, 5.0F, 5, 10),
                                        ImageUtils.loadImage(mobImage, 33, 10, 1, 1),
                                        2.0F,
                                        7.5F,
                                        5,
                                        10
                                ),
                                ImageUtils.loadImage(mobImage, 53, 1, 2, 5),
                                0.0F,
                                0.0F,
                                5,
                                10
                        ),
                        ImageUtils.loadImage(mobImage, 59, 1, 2, 5),
                        3.0F,
                        0.0F,
                        5,
                        10
                );
                break;
            case SALMON:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(
                                                        ImageUtils.addImages(
                                                                ImageUtils.blankImage(mobImage, 26, 7, 32, 32), ImageUtils.loadImage(mobImage, 27, 3, 3, 4, 32, 32), 1.0F, 2.5F, 26, 7
                                                        ),
                                                        ImageUtils.loadImage(mobImage, 11, 8, 8, 5, 32, 32),
                                                        4.0F,
                                                        2.0F,
                                                        26,
                                                        7
                                                ),
                                                ImageUtils.loadImage(mobImage, 11, 21, 8, 5, 32, 32),
                                                12.0F,
                                                2.0F,
                                                26,
                                                7
                                        ),
                                        ImageUtils.loadImage(mobImage, 26, 16, 6, 5, 32, 32),
                                        20.0F,
                                        2.0F,
                                        26,
                                        7
                                ),
                                ImageUtils.loadImage(mobImage, 0, 0, 2, 2, 32, 32),
                                10.0F,
                                0.0F,
                                26,
                                7
                        ),
                        ImageUtils.loadImage(mobImage, 5, 6, 3, 2, 32, 32),
                        12.0F,
                        0.0F,
                        26,
                        7
                );
                break;
            case SHEEP:
                image = ImageUtils.loadImage(mobImage, 8, 8, 6, 6);
                break;
            case SHULKER:
                image = ImageUtils.loadImage(mobImage, 6, 58, 6, 6);
                break;
            case SILVERFISH:
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 22, 20, 6, 6), ImageUtils.loadImage(mobImage, 2, 2, 3, 2), 2.0F, 2.0F, 6, 6);
                break;
            case SKELETON:
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImage, 40, 8, 8, 8), 0.0F, 0.0F, 8, 8);
                break;
            case SKELETONWITHER:
                image = ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImage, 40, 8, 8, 8), 0.0F, 0.0F, 8, 8);
                break;
            case SLIME:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 8), ImageUtils.loadImage(mobImage, 6, 22, 6, 6), 1.0F, 1.0F, 8, 8),
                                                ImageUtils.loadImage(mobImage, 34, 6, 2, 2),
                                                5.0F,
                                                2.0F,
                                                8,
                                                8
                                        ),
                                        ImageUtils.loadImage(mobImage, 34, 2, 2, 2),
                                        1.0F,
                                        2.0F,
                                        8,
                                        8
                                ),
                                ImageUtils.loadImage(mobImage, 33, 9, 1, 1),
                                4.0F,
                                5.0F,
                                8,
                                8
                        ),
                        ImageUtils.loadImage(mobImage, 8, 8, 8, 8),
                        0.0F,
                        0.0F,
                        8,
                        8
                );
                break;
            case SNOWGOLEM:
                image = ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64);
                break;
            case SPIDER:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 8), ImageUtils.loadImage(mobImage, 6, 6, 6, 6), 1.0F, 1.0F, 8, 8),
                        ImageUtils.loadImage(mobImage, 40, 12, 8, 8),
                        0.0F,
                        0.0F,
                        8,
                        8
                );
                break;
            case SQUID:
                image = ImageUtils.scaleImage(ImageUtils.loadImage(mobImage, 12, 12, 12, 16), 0.5F);
                break;
            case STRAY:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.loadImage(mobImage, 8, 8, 8, 8), ImageUtils.loadImage(mobImageSecondary, 8, 8, 8, 8), 0.0F, 0.0F, 8, 8),
                                ImageUtils.loadImage(mobImage, 40, 8, 8, 8),
                                0.0F,
                                0.0F,
                                8,
                                8
                        ),
                        ImageUtils.loadImage(mobImageSecondary, 40, 8, 8, 8),
                        0.0F,
                        0.0F,
                        8,
                        8
                );
                break;
            case TROPICALFISHA:
                float[] primaryColorsA = new float[]{0.9765F, 0.502F, 0.1137F};
                float[] secondaryColorsA = new float[]{0.9765F, 1.0F, 0.9961F};
                if (entity != null && entity instanceof EntityTropicalFish) {
                    EntityTropicalFish fish = (EntityTropicalFish) entity;
                    primaryColorsA = fish.func_204219_dC();
                    secondaryColorsA = fish.func_204222_dD();
                }

                BufferedImage baseA = ImageUtils.colorify(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 10, 6, 32, 32), ImageUtils.loadImage(mobImage, 8, 6, 6, 3, 32, 32), 0.0F, 3.0F, 10, 6),
                                        ImageUtils.loadImage(mobImage, 17, 1, 5, 3, 32, 32),
                                        1.0F,
                                        0.0F,
                                        10,
                                        6
                                ),
                                ImageUtils.loadImage(mobImage, 28, 0, 4, 3, 32, 32),
                                6.0F,
                                3.0F,
                                10,
                                6
                        ),
                        primaryColorsA[0],
                        primaryColorsA[1],
                        primaryColorsA[2]
                );
                BufferedImage patternA = ImageUtils.colorify(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.blankImage(mobImageSecondary, 10, 6, 32, 32), ImageUtils.loadImage(mobImageSecondary, 8, 6, 6, 3, 32, 32), 0.0F, 3.0F, 10, 6
                                        ),
                                        ImageUtils.loadImage(mobImageSecondary, 17, 1, 5, 3, 32, 32),
                                        1.0F,
                                        0.0F,
                                        10,
                                        6
                                ),
                                ImageUtils.loadImage(mobImageSecondary, 28, 0, 4, 3, 32, 32),
                                6.0F,
                                3.0F,
                                10,
                                6
                        ),
                        secondaryColorsA[0],
                        secondaryColorsA[1],
                        secondaryColorsA[2]
                );
                image = ImageUtils.addImages(baseA, patternA, 0.0F, 0.0F, 10, 6);
                break;
            case TROPICALFISHB:
                float[] primaryColorsB = new float[]{0.5373F, 0.1961F, 0.7216F};
                float[] secondaryColorsB = new float[]{0.9961F, 0.8471F, 0.2392F};
                if (entity != null && entity instanceof EntityTropicalFish) {
                    EntityTropicalFish fish = (EntityTropicalFish) entity;
                    primaryColorsB = fish.func_204219_dC();
                    secondaryColorsB = fish.func_204222_dD();
                }

                BufferedImage baseB = ImageUtils.colorify(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(
                                                        ImageUtils.blankImage(mobImage, 12, 12, 32, 32), ImageUtils.loadImage(mobImage, 0, 26, 6, 6, 32, 32), 6.0F, 3.0F, 12, 12
                                                ),
                                                ImageUtils.loadImage(mobImage, 20, 21, 6, 6, 32, 32),
                                                0.0F,
                                                3.0F,
                                                12,
                                                12
                                        ),
                                        ImageUtils.loadImage(mobImage, 20, 18, 5, 3, 32, 32),
                                        6.0F,
                                        0.0F,
                                        12,
                                        12
                                ),
                                ImageUtils.loadImage(mobImage, 20, 27, 5, 3, 32, 32),
                                6.0F,
                                9.0F,
                                12,
                                12
                        ),
                        primaryColorsB[0],
                        primaryColorsB[1],
                        primaryColorsB[2]
                );
                BufferedImage patternB = ImageUtils.colorify(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(
                                                        ImageUtils.blankImage(mobImageSecondary, 12, 12, 32, 32),
                                                        ImageUtils.loadImage(mobImageSecondary, 0, 26, 6, 6, 32, 32),
                                                        6.0F,
                                                        3.0F,
                                                        12,
                                                        12
                                                ),
                                                ImageUtils.loadImage(mobImageSecondary, 20, 21, 6, 6, 32, 32),
                                                0.0F,
                                                3.0F,
                                                12,
                                                12
                                        ),
                                        ImageUtils.loadImage(mobImageSecondary, 20, 18, 5, 3, 32, 32),
                                        6.0F,
                                        0.0F,
                                        12,
                                        12
                                ),
                                ImageUtils.loadImage(mobImageSecondary, 20, 27, 5, 3, 32, 32),
                                6.0F,
                                9.0F,
                                12,
                                12
                        ),
                        secondaryColorsB[0],
                        secondaryColorsB[1],
                        secondaryColorsB[2]
                );
                image = ImageUtils.addImages(baseB, patternB, 0.0F, 0.0F, 12, 12);
                break;
            case TURTLE:
                image = ImageUtils.loadImage(mobImage, 3, 6, 6, 5, 128, 64);
                break;
            case VEX:
                image = ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64);
                break;
            case VEXCHARGING:
                image = ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64);
                break;
            case VILLAGER:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12), ImageUtils.loadImage(mobImage, 8, 8, 8, 10, 64, 64), 0.0F, 1.0F, 8, 12),
                        ImageUtils.loadImage(mobImage, 26, 2, 2, 4, 64, 64),
                        3.0F,
                        8.0F,
                        8,
                        12
                );
                break;
            case VINDICATOR:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12), ImageUtils.loadImage(mobImage, 8, 8, 8, 10, 64, 64), 0.0F, 1.0F, 8, 12),
                        ImageUtils.loadImage(mobImage, 26, 2, 2, 4, 64, 64),
                        3.0F,
                        8.0F,
                        8,
                        12
                );
                break;
            case WITCH:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(
                                                ImageUtils.addImages(
                                                        ImageUtils.blankImage(mobImage, 10, 16, 64, 128), ImageUtils.loadImage(mobImage, 8, 8, 8, 10, 64, 128), 1.0F, 5.0F, 10, 16
                                                ),
                                                ImageUtils.loadImage(mobImage, 26, 2, 2, 4, 64, 128),
                                                4.0F,
                                                12.0F,
                                                10,
                                                16
                                        ),
                                        ImageUtils.loadImage(mobImage, 10, 74, 10, 3, 64, 128),
                                        0.0F,
                                        4.0F,
                                        10,
                                        16
                                ),
                                ImageUtils.loadImage(mobImage, 7, 83, 7, 4, 64, 128),
                                1.5F,
                                0.0F,
                                10,
                                16
                        ),
                        ImageUtils.loadImage(mobImage, 1, 1, 1, 1, 64, 128),
                        5.0F,
                        14.0F,
                        10,
                        16
                );
                break;
            case WITHER:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.blankImage(mobImage, 24, 10, 64, 64), ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64), 8.0F, 0.0F, 24, 10),
                                ImageUtils.loadImage(mobImage, 38, 6, 6, 6, 64, 64),
                                0.0F,
                                2.0F,
                                24,
                                10
                        ),
                        ImageUtils.loadImage(mobImage, 38, 6, 6, 6, 64, 64),
                        18.0F,
                        2.0F,
                        24,
                        10
                );
                break;
            case WITHERINVULNERABLE:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(ImageUtils.blankImage(mobImage, 24, 10, 64, 64), ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64), 8.0F, 0.0F, 24, 10),
                                ImageUtils.loadImage(mobImage, 38, 6, 6, 6, 64, 64),
                                0.0F,
                                2.0F,
                                24,
                                10
                        ),
                        ImageUtils.loadImage(mobImage, 38, 6, 6, 6, 64, 64),
                        18.0F,
                        2.0F,
                        24,
                        10
                );
                break;
            case WOLF:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 6, 8), ImageUtils.loadImage(mobImage, 4, 4, 6, 6), 0.0F, 2.0F, 6, 8),
                                        ImageUtils.loadImage(mobImage, 4, 14, 3, 3),
                                        1.5F,
                                        5.0F,
                                        6,
                                        8
                                ),
                                ImageUtils.loadImage(mobImage, 17, 15, 2, 2),
                                0.0F,
                                0.0F,
                                6,
                                8
                        ),
                        ImageUtils.loadImage(mobImage, 17, 15, 2, 2),
                        4.0F,
                        0.0F,
                        6,
                        8
                );
                break;
            case WOLFANGRY:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 6, 8), ImageUtils.loadImage(mobImage, 4, 4, 6, 6), 0.0F, 2.0F, 6, 8),
                                        ImageUtils.loadImage(mobImage, 4, 14, 3, 3),
                                        1.5F,
                                        5.0F,
                                        6,
                                        8
                                ),
                                ImageUtils.loadImage(mobImage, 17, 15, 2, 2),
                                0.0F,
                                0.0F,
                                6,
                                8
                        ),
                        ImageUtils.loadImage(mobImage, 17, 15, 2, 2),
                        4.0F,
                        0.0F,
                        6,
                        8
                );
                break;
            case WOLFTAME:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(
                                ImageUtils.addImages(
                                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 6, 8), ImageUtils.loadImage(mobImage, 4, 4, 6, 6), 0.0F, 2.0F, 6, 8),
                                        ImageUtils.loadImage(mobImage, 4, 14, 3, 3),
                                        1.5F,
                                        5.0F,
                                        6,
                                        8
                                ),
                                ImageUtils.loadImage(mobImage, 17, 15, 2, 2),
                                0.0F,
                                0.0F,
                                6,
                                8
                        ),
                        ImageUtils.loadImage(mobImage, 17, 15, 2, 2),
                        4.0F,
                        0.0F,
                        6,
                        8
                );
                break;
            case ZOMBIE:
                image = ImageUtils.addImages(
                        ImageUtils.loadImage(mobImage, 8, 8, 8, 8, 64, 64), ImageUtils.loadImage(mobImage, 40, 8, 8, 8, 64, 64), 0.0F, 0.0F, 8, 8
                );
                break;
            case ZOMBIEVILLAGER:
                image = ImageUtils.addImages(
                        ImageUtils.addImages(ImageUtils.blankImage(mobImage, 8, 12), ImageUtils.loadImage(mobImage, 8, 8, 8, 10, 64, 64), 0.0F, 1.0F, 8, 12),
                        ImageUtils.loadImage(mobImage, 26, 2, 2, 4, 64, 64),
                        3.0F,
                        8.0F,
                        8,
                        12
                );
                break;
            default:
                throw new IllegalArgumentException("New mob type: " + type.id + ". Need to construct icon for it!");
        }

        return image;
    }

    @Override
    public void OnTickInGame(Minecraft mc, LayoutVariables layoutVariables) {
        if (this.options.radarAllowed || this.options.radarMobsAllowed || this.options.radarPlayersAllowed) {
            if (this.game == null) {
                this.game = mc;
            }

            this.layoutVariables = layoutVariables;
            if (this.options.isChanged()) {
                this.timer = 500;
                if (this.options.outlines != this.lastOutlines) {
                    this.lastOutlines = this.options.outlines;
                    this.loadTexturePackIcons();
                }
            }

            int guiScale = layoutVariables.scScale;
            guiScale = guiScale >= 4 ? 1 : 0;
            this.direction = GameVariableAccessShim.rotationYaw() + 180.0F;

            while (this.direction >= 360.0F) {
                this.direction -= 360.0F;
            }

            while (this.direction < 0.0F) {
                this.direction += 360.0F;
            }

            if (this.enabled) {
                if (this.completedLoading && this.timer > 95) {
                    this.calculateMobs();
                    this.timer = 0;
                }

                this.timer++;
                if (this.completedLoading) {
                    this.renderMapMobs(this.layoutVariables.mapX, this.layoutVariables.mapY, guiScale);
                }

                GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }
    }

    private int chkLen(String paramStr) {
        return this.fontRenderer.getStringWidth(paramStr);
    }

    private void write(String paramStr, float x, float y, int color) {
        GLShim.glTexParameteri(3553, 10241, 9728);
        GLShim.glTexParameteri(3553, 10240, 9728);
        this.fontRenderer.drawStringWithShadow(paramStr, x, y, color);
    }

    private boolean isEntityShown(Entity entity) {
        return entity != null
                && !entity.isInvisibleToPlayer(this.game.player)
                && (
                this.options.showHostiles && (this.options.radarAllowed || this.options.radarMobsAllowed) && this.isHostile(entity)
                        || this.options.showPlayers && (this.options.radarAllowed || this.options.radarPlayersAllowed) && this.isPlayer(entity)
                        || this.options.showNeutrals && this.options.radarMobsAllowed && this.isNeutral(entity)
        );
    }

    public void calculateMobs() {
        this.contacts.clear();
        List<Entity> entities = this.game.world.loadedEntityList;

        for (int j = 0; j < entities.size(); j++) {
            try {
                Entity entity = entities.get(j);
                if (this.isEntityShown(entity)) {
                    int wayX = GameVariableAccessShim.xCoord() - (int) entity.posX;
                    int wayZ = GameVariableAccessShim.zCoord() - (int) entity.posZ;
                    int wayY = GameVariableAccessShim.yCoord() - (int) entity.posY;
                    double hypot = wayX * wayX + wayZ * wayZ + wayY * wayY;
                    hypot /= this.layoutVariables.zoomScaleAdjusted * this.layoutVariables.zoomScaleAdjusted;
                    if (hypot < 961.0) {
                        if (this.hasCustomNPCs) {
                            try {
                                if (this.entityCustomNpcClass.isInstance(entity)) {
                                    Object modelData = this.modelDataField.get(entity);
                                    EntityLivingBase wrappedEntity = (EntityLivingBase) this.getEntityMethod.invoke(modelData, entity);
                                    if (wrappedEntity != null) {
                                        entity = wrappedEntity;
                                    }
                                }
                            } catch (Exception var15) {
                            }
                        }

                        Contact contact = new Contact(entity, this.getContactTypeStrict(entity));
                        String unscrubbedName = contact.entity.getDisplayName().getFormattedText();
                        contact.setName(unscrubbedName);
                        if (contact.entity.getRidingEntity() != null && this.isEntityShown(contact.entity.getRidingEntity())) {
                            contact.yFudge = 1;
                        }

                        contact.updateLocation();
                        if (contact.type != EnumMobs.UNKNOWN) {
                            contact.icons = new Sprite[]{
                                    this.textureAtlas
                                            .getAtlasSprite(
                                            contact.type.id
                                                    + contact.type.resourceLocation.toString()
                                                    + (contact.type.secondaryResourceLocation != null ? contact.type.secondaryResourceLocation.toString() : "")
                                                    + "0"
                                    ),
                                    this.textureAtlas
                                            .getAtlasSprite(
                                            contact.type.id
                                                    + contact.type.resourceLocation.toString()
                                                    + (contact.type.secondaryResourceLocation != null ? contact.type.secondaryResourceLocation.toString() : "")
                                                    + "1"
                                    )
                            };
                        }

                        if (contact.type == EnumMobs.PLAYER) {
                            this.handleMPplayer(contact);
                        }

                        if (contact.type == EnumMobs.UNKNOWN) {
                            this.tryCustomIcon(contact);
                        }

                        if (contact.type == EnumMobs.UNKNOWN) {
                            this.tryAutoIcon(contact);
                        }

                        if (contact.type == EnumMobs.UNKNOWN) {
                            this.tryFallbackType(contact);
                        }

                        if (!this.builtInCustom[contact.type.ordinal()]
                                && contact.type != EnumMobs.CUSTOM
                                && contact.type != EnumMobs.UNKNOWN
                                && contact.type != EnumMobs.BLANK
                                && contact.type != EnumMobs.GENERICHOSTILE
                                && contact.type != EnumMobs.GENERICNEUTRAL
                                && contact.type != EnumMobs.GENERICTAME
                                && this.options.randomobs) {
                            contact.icons = this.getIconsForRandomob(contact);
                        }

                        if (contact.type == EnumMobs.HORSE) {
                            contact.setRotationFactor(45);
                        }

                        String scrubbedName = TextUtils.scrubCodes(contact.entity.getName().getUnformattedComponentText());
                        if (scrubbedName != null
                                && (scrubbedName.equals("Dinnerbone") || scrubbedName.equals("Grumm"))
                                && (!(contact.entity instanceof EntityPlayer) || ((EntityPlayer) contact.entity).isWearing(EnumPlayerModelParts.CAPE))) {
                            contact.setRotationFactor(contact.rotationFactor + 180);
                        }

                        if (this.options.showHelmetsPlayers && contact.type == EnumMobs.PLAYER
                                || this.options.showHelmetsMobs && contact.type != EnumMobs.PLAYER
                                || contact.type == EnumMobs.SHEEP) {
                            this.getArmor(contact, entity);
                        }

                        if (contact.type == EnumMobs.UNKNOWN || contact.type == EnumMobs.CUSTOM || contact.type == EnumMobs.AUTO) {
                            String type = entity.getType().getTranslationKey();
                            CustomMob customMob = CustomMobsManager.getCustomMobByType(type);
                            if (customMob == null || customMob.enabled) {
                                this.contacts.add(contact);
                            }
                        } else if (contact.type.enabled) {
                            this.contacts.add(contact);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println(e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        if (this.newMobs) {
            try {
                this.textureAtlas.stitchNew();
            } catch (StitcherException ex) {
                System.err.println("Stitcher exception!  Resetting mobs texture atlas.");
                this.loadTexturePackIcons();
            }
        }

        this.newMobs = false;
        Collections.sort(this.contacts, new Comparator<Contact>() {
            public int compare(Contact contact1, Contact contact2) {
                return contact1.y - contact2.y;
            }
        });
    }

    private void tryCustomIcon(Contact contact) {
        Sprite icon = this.textureAtlas.getAtlasSprite(contact.entity.getClass().getName() + "custom0");
        if (icon == this.textureAtlas.getMissingImage()) {
            boolean isHostile = this.isHostile(contact.entity);
            CustomMobsManager.add(contact.entity.getType().getTranslationKey(), isHostile, !isHostile);

            try {
                int intendedSize = 8;
                String fullPath = ("textures/icons/" + contact.entity.getClass().getName() + ".png").toLowerCase();
                InputStream is = null;

                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                } catch (IOException ex) {
                    is = null;
                }

                if (is == null) {
                    fullPath = ("textures/icons/" + contact.entity.getClass().getSimpleName() + ".png").toLowerCase();

                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    } catch (IOException ex) {
                        is = null;
                    }
                }

                if (is == null) {
                    fullPath = ("textures/icons/" + contact.entity.getClass().getName() + "8.png").toLowerCase();

                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    } catch (IOException ex) {
                        is = null;
                    }
                }

                if (is == null) {
                    fullPath = ("textures/icons/" + contact.entity.getClass().getSimpleName() + "8.png").toLowerCase();

                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    } catch (IOException ex) {
                        is = null;
                    }
                }

                if (is == null) {
                    intendedSize = 16;
                    fullPath = ("textures/icons/" + contact.entity.getClass().getName() + "16.png").toLowerCase();

                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    } catch (IOException ex) {
                        is = null;
                    }
                }

                if (is == null) {
                    fullPath = ("textures/icons/" + contact.entity.getClass().getSimpleName() + "16.png").toLowerCase();

                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    } catch (IOException ex) {
                        is = null;
                    }
                }

                if (is == null) {
                    intendedSize = 32;
                    fullPath = ("textures/icons/" + contact.entity.getClass().getName() + "32.png").toLowerCase();

                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    } catch (IOException ex) {
                        is = null;
                    }
                }

                if (is == null) {
                    fullPath = ("textures/icons/" + contact.entity.getClass().getSimpleName() + "32.png").toLowerCase();

                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    } catch (IOException ex) {
                        is = null;
                    }
                }

                if (is != null) {
                    BufferedImage mobSkin = ImageIO.read(is);
                    is.close();
                    mobSkin = ImageUtils.loadImage(mobSkin, 0, 0, mobSkin.getWidth(), mobSkin.getHeight(), mobSkin.getWidth(), mobSkin.getHeight());
                    float scale = (float) mobSkin.getWidth() / intendedSize;
                    BufferedImage mobSkin0 = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(mobSkin, 1.0F / scale)), this.options.outlines);
                    BufferedImage mobSkin1 = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(mobSkin, 2.0F / scale)), this.options.outlines);
                    Sprite icon0 = this.textureAtlas.registerIconForBufferedImage(contact.entity.getClass().getName() + "custom0", mobSkin0);
                    Sprite icon1 = this.textureAtlas.registerIconForBufferedImage(contact.entity.getClass().getName() + "custom1", mobSkin1);
                    this.newMobs = true;
                    contact.icons = new Sprite[]{icon0, icon1};
                    contact.type = EnumMobs.CUSTOM;
                } else {
                    contact.type = EnumMobs.UNKNOWN;
                    this.textureAtlas.registerFailedIcon(contact.entity.getClass().getName() + "custom0");
                }
            } catch (IOException e) {
                contact.type = EnumMobs.UNKNOWN;
                this.textureAtlas.registerFailedIcon(contact.entity.getClass().getName() + "custom0");
            }
        } else if (icon != this.textureAtlas.getFailedImage()) {
            contact.type = EnumMobs.CUSTOM;
            contact.icons = new Sprite[]{icon, this.textureAtlas.getAtlasSprite(contact.entity.getClass().getName() + "custom1")};
        } else {
            contact.type = EnumMobs.UNKNOWN;
        }
    }

    private void tryAutoIcon(Contact contact) {
        Render<? extends Entity> render = this.game.getRenderManager().getEntityRenderObject(contact.entity);
        ResourceLocation resourceLocation = ((RenderAccessor) render).invokerGetEntityTexture(contact.entity);
        String entityName = contact.entity.getClass().getName();
        String resourceLocationString = resourceLocation != null ? resourceLocation.toString() : "";
        String nameMinusSize = entityName + resourceLocationString;
        Sprite icon = this.textureAtlas.getAtlasSprite(nameMinusSize + 0);
        if (icon == this.textureAtlas.getMissingImage()) {
            Integer checkCount = this.contactsSkinGetTries.get(nameMinusSize);
            if (checkCount == null) {
                checkCount = 0;
            }

            BufferedImage mobImage = this.createAutoIconImageFromResourceLocation(contact, render, resourceLocation);
            if (mobImage != null) {
                try {
                    contact.type = EnumMobs.AUTO;
                    BufferedImage[] trimmedImages = this.trimAndOutlineImages(contact.type, mobImage);
                    Sprite icon0 = this.textureAtlas.registerIconForBufferedImage(nameMinusSize + "0", trimmedImages[0]);
                    Sprite icon1 = this.textureAtlas.registerIconForBufferedImage(nameMinusSize + "1", trimmedImages[1]);
                    contact.icons = new Sprite[]{icon0, icon1};
                    this.newMobs = true;
                    this.contactsSkinGetTries.remove(nameMinusSize);
                } catch (Exception e) {
                    contact.type = EnumMobs.UNKNOWN;
                    checkCount = checkCount + 1;
                    if (checkCount > 4) {
                        this.textureAtlas.registerFailedIcon(nameMinusSize + "0");
                    }
                }
            } else {
                contact.type = EnumMobs.UNKNOWN;
                checkCount = checkCount + 1;
                if (checkCount > 4) {
                    this.textureAtlas.registerFailedIcon(nameMinusSize + "0");
                }
            }
        } else if (icon != this.textureAtlas.getFailedImage()) {
            contact.type = EnumMobs.AUTO;
            contact.icons = new Sprite[]{icon, this.textureAtlas.getAtlasSprite(nameMinusSize + 1)};
        } else {
            contact.type = EnumMobs.UNKNOWN;
        }
    }

    private BufferedImage createAutoIconImageFromResourceLocation(Contact contact, Render<? extends Entity> render, ResourceLocation resourceLocation) {
        BufferedImage headImage = null;
        if (GLUtils.fboEnabled && render instanceof RenderLivingBase) {
            try {
                ModelBase model = ((RenderLivingBase) render).getMainModel();
                if (render instanceof RenderTropicalFish) {
                    Object fishModel = null;
                    int size = ((EntityTropicalFish) contact.entity).getSize();
                    if (size == 0) {
                        fishModel = ReflectionUtils.getPrivateFieldValueByType(render, RenderTropicalFish.class, ModelTropicalFishA.class);
                    } else {
                        fishModel = ReflectionUtils.getPrivateFieldValueByType(render, RenderTropicalFish.class, ModelTropicalFishB.class);
                    }

                    if (fishModel != null) {
                        model = (ModelBase) fishModel;
                    }
                }

                ArrayList<Field> submodels = ReflectionUtils.getFieldsByType(model, ModelBase.class, ModelRenderer.class);
                ArrayList<Field> submodelArrays = ReflectionUtils.getFieldsByType(model, ModelBase.class, ModelRenderer[].class);
                ModelRenderer[] headBits = null;
                Properties properties = new Properties();
                String fullPath = "textures/icons/" + contact.entity.getClass().getName().toLowerCase() + ".properties";
                InputStream is = null;

                try {
                    is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                } catch (IOException ex) {
                    is = null;
                }

                if (is == null) {
                    fullPath = "textures/icons/" + contact.entity.getClass().getSimpleName().toLowerCase() + ".properties";

                    try {
                        is = this.game.getResourceManager().getResource(new ResourceLocation(fullPath)).getInputStream();
                    } catch (IOException ex) {
                        is = null;
                    }
                }

                if (is != null) {
                    properties.load(is);
                    is.close();
                    String subModelNames = properties.getProperty("models", "").toLowerCase();
                    String[] submodelNamesArray = subModelNames.split(",");
                    List<String> subModelNamesList = Arrays.asList(submodelNamesArray);
                    HashSet<String> subModelNamesSet = new HashSet<>();
                    subModelNamesSet.addAll(subModelNamesList);
                    ArrayList<ModelRenderer> headPartsArrayList = new ArrayList<>();
                    ArrayList<ModelRenderer> purge = new ArrayList<>();

                    for (Field submodelArray : submodelArrays) {
                        String name = submodelArray.getName().toLowerCase();
                        if (subModelNamesSet.contains(name) || subModelNames.equals("all")) {
                            ModelRenderer[] submodelArrayValue = (ModelRenderer[]) submodelArray.get(model);
                            if (submodelArrayValue != null) {
                                Collections.addAll(headPartsArrayList, submodelArrayValue);
                            }
                        }
                    }

                    for (Field submodel : submodels) {
                        String name = submodel.getName().toLowerCase();
                        if ((subModelNamesSet.contains(name) || subModelNames.equals("all")) && submodel.get(model) != null) {
                            headPartsArrayList.add((ModelRenderer) submodel.get(model));
                        }
                    }

                    for (ModelRenderer bit : headPartsArrayList) {
                        if (bit.childModels != null) {
                            purge.addAll(bit.childModels);
                        }
                    }

                    headPartsArrayList.removeAll(purge);
                    if (headPartsArrayList.size() > 0) {
                        headBits = headPartsArrayList.toArray(new ModelRenderer[headPartsArrayList.size()]);
                    }
                }

                if (headBits == null) {
                    if (model instanceof ModelBat) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelBat.class, ModelRenderer.class)};
                    } else if (model instanceof ModelBiped) {
                        headBits = new ModelRenderer[]{((ModelBiped) model).bipedHead, ((ModelBiped) model).bipedHeadwear};
                    } else if (model instanceof ModelBlaze) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelBlaze.class, ModelRenderer.class)};
                    } else if (model instanceof ModelChicken) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelChicken.class, ModelRenderer.class)};
                    } else if (model instanceof ModelCow) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelCow.class, ModelRenderer.class)};
                    } else if (model instanceof ModelCreeper) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelCreeper.class, ModelRenderer.class)};
                    } else if (model instanceof DolphinModel) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, DolphinModel.class, ModelRenderer.class)};
                    } else if (model instanceof ModelDragon) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelDragon.class, ModelRenderer.class)};
                    } else if (model instanceof ModelGhast) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelGhast.class, ModelRenderer.class)};
                    } else if (model instanceof ModelGuardian) {
                        headBits = new ModelRenderer[]{
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelGuardian.class, ModelRenderer.class, 0),
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelGuardian.class, ModelRenderer.class, 1)
                        };
                    } else if (model instanceof ModelHorseArmorBase) {
                        headBits = new ModelRenderer[]{
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelHorseArmorBase.class, ModelRenderer.class)
                        };
                    } else if (model instanceof ModelIllager) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelIllager.class, ModelRenderer.class)};
                    } else if (model instanceof ModelIronGolem) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelIronGolem.class, ModelRenderer.class)};
                    } else if (model instanceof ModelOcelot) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelOcelot.class, ModelRenderer.class, 6)};
                    } else if (model instanceof ModelPhantom) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelPhantom.class, ModelRenderer.class)};
                    } else if (model instanceof ModelPig) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelPig.class, ModelRenderer.class)};
                    } else if (model instanceof ModelPolarBear) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelPolarBear.class, ModelRenderer.class)};
                    } else if (model instanceof ModelRabbit) {
                        headBits = new ModelRenderer[]{
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelRabbit.class, ModelRenderer.class, 7),
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelRabbit.class, ModelRenderer.class, 8),
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelRabbit.class, ModelRenderer.class, 9),
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelRabbit.class, ModelRenderer.class, 10)
                        };
                    } else if (model instanceof ModelShulker) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelShulker.class, ModelRenderer.class, 2)};
                    } else if (model instanceof ModelSilverfish) {
                        headBits = new ModelRenderer[]{
                                (ModelRenderer) ((Object[]) ReflectionUtils.getPrivateFieldValueByType(model, ModelSilverfish.class, ModelRenderer[].class))[0],
                                (ModelRenderer) ((Object[]) ReflectionUtils.getPrivateFieldValueByType(model, ModelSilverfish.class, ModelRenderer[].class))[1]
                        };
                    } else if (model instanceof ModelSlime) {
                        ModelBase modelOuter = new ModelSlime(0);
                        headBits = new ModelRenderer[]{
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelSlime.class, ModelRenderer.class, 0),
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelSlime.class, ModelRenderer.class, 1),
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelSlime.class, ModelRenderer.class, 2),
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelSlime.class, ModelRenderer.class, 3),
                                (ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(modelOuter, ModelSlime.class, ModelRenderer.class, 0)
                        };
                    } else if (model instanceof ModelSnowMan) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelSnowMan.class, ModelRenderer.class, 2)};
                    } else if (model instanceof ModelSpider) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelSpider.class, ModelRenderer.class)};
                    } else if (model instanceof ModelSquid) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelSquid.class, ModelRenderer.class)};
                    } else if (model instanceof ModelVillager) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelVillager.class, ModelRenderer.class)};
                    } else if (model instanceof ModelWolf) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelWolf.class, ModelRenderer.class)};
                    } else if (model instanceof ModelQuadruped) {
                        headBits = new ModelRenderer[]{(ModelRenderer) ReflectionUtils.getPrivateFieldValueByType(model, ModelQuadruped.class, ModelRenderer.class)};
                    } else {
                        ArrayList<ModelRenderer> headPartsArrayList = new ArrayList<>();
                        ArrayList<ModelRenderer> purge = new ArrayList<>();

                        for (Field submodelArrayx : submodelArrays) {
                            String name = submodelArrayx.getName().toLowerCase();
                            if (name.contains("head")
                                    | name.contains("eye")
                                    | name.contains("mouth")
                                    | name.contains("teeth")
                                    | name.contains("tooth")
                                    | name.contains("tusk")
                                    | name.contains("jaw")
                                    | name.contains("mand")
                                    | name.contains("nose")
                                    | name.contains("beak")
                                    | name.contains("snout")
                                    | name.contains("muzzle")
                                    | (!name.contains("rear") && name.contains("ear"))
                                    | name.contains("trunk")
                                    | name.contains("mane")
                                    | name.contains("horn")
                                    | name.contains("antler")) {
                                ModelRenderer[] submodelArrayValue = (ModelRenderer[]) submodelArrayx.get(model);
                                if (submodelArrayValue != null && submodelArrayValue.length >= 0) {
                                    headPartsArrayList.add(submodelArrayValue[0]);
                                }
                            }
                        }

                        for (Field submodelx : submodels) {
                            String name = submodelx.getName().toLowerCase();
                            String nameS = submodelx.getName();
                            if (name.contains("head")
                                    | name.contains("eye")
                                    | name.contains("mouth")
                                    | name.contains("teeth")
                                    | name.contains("tooth")
                                    | name.contains("tusk")
                                    | name.contains("jaw")
                                    | name.contains("mand")
                                    | name.contains("nose")
                                    | name.contains("beak")
                                    | name.contains("snout")
                                    | name.contains("muzzle")
                                    | (!name.contains("rear") && name.contains("ear"))
                                    | name.contains("trunk")
                                    | name.contains("mane")
                                    | name.contains("horn")
                                    | name.contains("antler")
                                    | nameS.equals("REar")
                                    | nameS.equals("Trout")
                                    && !nameS.equals("LeftSmallEar")
                                    & !nameS.equals("RightSmallEar")
                                    & !nameS.equals("BHead")
                                    & !nameS.equals("BSnout")
                                    & !nameS.equals("BMouth")
                                    & !nameS.equals("BMouthOpen")
                                    & !nameS.equals("BLEar")
                                    & !nameS.equals("BREar")
                                    & !nameS.equals("CHead")
                                    & !nameS.equals("CSnout")
                                    & !nameS.equals("CMouth")
                                    & !nameS.equals("CMouthOpen")
                                    & !nameS.equals("CLEar")
                                    & !nameS.equals("CREar")
                                    && submodelx.get(model) != null) {
                                headPartsArrayList.add((ModelRenderer) submodelx.get(model));
                            }
                        }

                        if (headPartsArrayList.size() == 0) {
                            if (submodels.size() > 0) {
                                if (submodels.get(0).get(model) != null) {
                                    headPartsArrayList.add((ModelRenderer) submodels.get(0).get(model));
                                }
                            } else if (submodelArrays.size() > 0 && submodelArrays.get(0).get(model) != null) {
                                ModelRenderer[] submodelArrayValue = (ModelRenderer[]) submodelArrays.get(0).get(model);
                                if (submodelArrayValue.length > 0) {
                                    headPartsArrayList.add(submodelArrayValue[0]);
                                }
                            }
                        }

                        for (ModelRenderer bitx : headPartsArrayList) {
                            if (bitx.childModels != null) {
                                purge.addAll(bitx.childModels);
                            }
                        }

                        headPartsArrayList.removeAll(purge);
                        headBits = headPartsArrayList.toArray(new ModelRenderer[headPartsArrayList.size()]);
                    }
                }

                for (int t = 0; t < headBits.length; t++) {
                    ModelRenderer bitxx = headBits[t];
                    if (this.hasCustomNPCs && this.modelScaleRendererClass.isInstance(bitxx)) {
                        bitxx.isHidden = false;
                    }
                }

                if (contact.entity != null && model != null && headBits.length > 0 && resourceLocation != null) {
                    String scaleString = properties.getProperty("scale", "1");
                    float scale = Float.parseFloat(scaleString);
                    EnumFacing facing = EnumFacing.NORTH;
                    String facingString = properties.getProperty("facing", "front");
                    if (facingString.equals("top")) {
                        facing = EnumFacing.UP;
                    } else if (facingString.equals("side")) {
                        facing = EnumFacing.EAST;
                    }

                    boolean success = this.drawModel(scale, 1000, (EntityLivingBase) contact.entity, facing, model, resourceLocation, headBits);
                    if (success) {
                        headImage = ImageUtils.createBufferedImageFromGLID(GLUtils.fboTextureID);
                    }
                }
            } catch (Exception e) {
                headImage = null;
            }
        }

        return headImage;
    }

    private boolean drawModel(
            float scale,
            int captureDepth,
            EntityLivingBase par5EntityLivingBase,
            EnumFacing facing,
            ModelBase model,
            ResourceLocation resourceLocation,
            ModelRenderer... headBits
    ) {
        boolean failed = false;
        float size = 32.0F * scale;
        GLShim.glBindTexture(3553, GLUtils.fboTextureID);
        int width = GLShim.glGetTexLevelParameteri(3553, 0, 4096);
        int height = GLShim.glGetTexLevelParameteri(3553, 0, 4097);
        GLShim.glBindTexture(3553, 0);
        GLShim.glPushAttrib(4096);
        GLShim.glViewport(0, 0, width, height);
        GLShim.glMatrixMode(5889);
        GLShim.glPushMatrix();
        GLShim.glLoadIdentity();
        GLShim.glOrtho(0.0, width, height, 0.0, 1000.0, 3000.0);
        GLShim.glMatrixMode(5888);
        GLShim.glPushMatrix();
        GLShim.glLoadIdentity();
        GLShim.glTranslatef(0.0F, 0.0F, -3000.0F + captureDepth);
        GLUtils.bindFrameBuffer();
        GLShim.glDepthMask(true);
        GLShim.glEnable(2929);
        GLShim.glEnable(3553);
        GLShim.glEnable(3042);
        GLShim.glEnable(3008);
        GLShim.glEnable(2977);
        GLShim.glDisable(2884);
        GLShim.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        GLShim.glClear(16640);
        GLShim.glBlendFunc(770, 771);
        GLShim.glPushMatrix();
        GLShim.glTranslatef(width / 2, height / 2, 0.0F);
        GLShim.glScalef(size, size, size);
        GLShim.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
        GLUtils.img(resourceLocation);
        GLShim.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        if (facing == EnumFacing.EAST) {
            GLShim.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
        } else if (facing == EnumFacing.UP) {
            GLShim.glRotatef(90.0F, 1.0F, 0.0F, 0.0F);
        }

        try {
            GLShim.glTranslatef(500.0F, 500.0F, 0.0F);
            model.render(par5EntityLivingBase, 0.0F, 0.0F, 163.0F, 360.0F, 0.0F, 0.0625F);
            GLShim.glTranslatef(-500.0F, -500.0F, 0.0F);
            float offsetByY = 0.0F;
            float maxY = 0.0F;
            float minY = 0.0F;

            for (int t = 0; t < headBits.length; t++) {
                if (headBits[t].rotationPointY < minY) {
                    minY = headBits[t].rotationPointY;
                }

                if (headBits[t].rotationPointY > maxY) {
                    maxY = headBits[t].rotationPointY;
                }
            }

            if (minY < -25.0F) {
                offsetByY = -25.0F - minY;
            } else if (maxY > 25.0F) {
                offsetByY = 25.0F - maxY;
            }

            if (captureDepth == 2) {
                offsetByY = 4.0F;
            }

            for (int t = 0; t < headBits.length; t++) {
                float y = headBits[t].rotationPointY;
                headBits[t].rotationPointY += offsetByY;
                headBits[t].render(0.0625F);
                headBits[t].rotationPointY = y;
            }
        } catch (Exception e) {
            failed = true;
        }

        GLShim.glPopMatrix();
        GLShim.glEnable(2884);
        GLShim.glDisable(2929);
        GLShim.glDepthMask(false);
        GLUtils.unbindFrameBuffer();
        GLShim.glMatrixMode(5889);
        GLShim.glPopMatrix();
        GLShim.glMatrixMode(5888);
        GLShim.glPopMatrix();
        GLShim.glPopAttrib();
        GLShim.glViewport(0, 0, this.game.mainWindow.getFramebufferWidth(), this.game.mainWindow.getFramebufferHeight());
        return !failed;
    }

    private void tryFallbackType(Contact contact) {
        contact.type = this.getContactType(contact.entity);
        if (contact.type == EnumMobs.UNKNOWN) {
            contact.type = this.getUnknownMobNeutrality(contact.entity);
        }

        String resourceLocationString = contact.type.resourceLocation != null ? contact.type.resourceLocation.toString() : "";
        resourceLocationString = resourceLocationString
                + (contact.type.secondaryResourceLocation != null ? contact.type.secondaryResourceLocation.toString() : "");
        String nameWithoutSize = contact.type.id + resourceLocationString;
        contact.icons = new Sprite[]{this.textureAtlas.getAtlasSprite(nameWithoutSize + "0"), this.textureAtlas.getAtlasSprite(nameWithoutSize + "1")};
    }

    private Sprite[] getIconsForRandomob(Contact contact) {
        if (contact.type == EnumMobs.PLAYER && !contact.icons[0].getIconName().equals(EnumMobs.PLAYER.id + EnumMobs.PLAYER.resourceLocation.toString() + "0")) {
            return contact.icons;
        }

        String color = "";
        ResourceLocation resourceLocation = null;
        Render<? extends Entity> render = this.game.getRenderManager().getEntityRenderObject(contact.entity);
        resourceLocation = ((RenderAccessor) render).invokerGetEntityTexture(contact.entity);
        String originalResourceLocationString = resourceLocation != null ? resourceLocation.toString() : "";
        resourceLocation = this.getRandomizedResourceLocationForEntity(resourceLocation, contact.entity);
        ResourceLocation resourceLocationSecondary = null;
        if (contact.type.secondaryResourceLocation != null) {
            if (contact.type == EnumMobs.MOOSHROOM) {
                if (!((EntityMooshroom) contact.entity).isChild()) {
                    resourceLocationSecondary = EnumMobs.MOOSHROOM.secondaryResourceLocation;
                } else {
                    resourceLocationSecondary = null;
                }
            } else {
                resourceLocationSecondary = contact.type.secondaryResourceLocation;
            }

            originalResourceLocationString = originalResourceLocationString + (resourceLocationSecondary != null ? resourceLocationSecondary.toString() : "");
            if (contact.type == EnumMobs.TROPICALFISHA || contact.type == EnumMobs.TROPICALFISHB) {
                EntityTropicalFish fish = (EntityTropicalFish) contact.entity;
                resourceLocationSecondary = fish.getPatternTexture();
                color = fish.func_204219_dC() + " " + fish.func_204222_dD();
            } else if (resourceLocationSecondary != null) {
                resourceLocationSecondary = this.getRandomizedResourceLocationForEntity(resourceLocationSecondary, contact.entity);
            }
        }

        String resourceLocationString = (resourceLocation != null ? resourceLocation.toString() : "")
                + (resourceLocationSecondary != null ? resourceLocationSecondary.toString() : "");
        if (contact.type != EnumMobs.AUTO) {
            String defaultResourceLocationString = contact.type.resourceLocation.toString();
            defaultResourceLocationString = defaultResourceLocationString
                    + (contact.type.secondaryResourceLocation != null ? contact.type.secondaryResourceLocation.toString() : "");
            if (resourceLocationString.equals(defaultResourceLocationString)) {
                return contact.icons;
            }
        } else if (resourceLocationString.equals(originalResourceLocationString)) {
            return contact.icons;
        }

        String entityName = contact.type != EnumMobs.AUTO ? contact.type.id : contact.entity.getClass().getName();
        String nameMinusSize = entityName + color + resourceLocationString;
        Sprite icon = this.textureAtlas.getAtlasSprite(nameMinusSize + "0");
        if (icon == this.textureAtlas.getMissingImage()) {
            BufferedImage mobImage = null;
            if (contact.type == EnumMobs.HORSE) {
                ITextureObject textureObject = GLUtils.textureManager.getTexture(resourceLocation);
                if (textureObject != null) {
                    mobImage = ImageUtils.createBufferedImageFromGLID(textureObject.getGlTextureId());
                    mobImage = this.createImageFromTypeAndImages(contact.type, mobImage, null, null);
                }
            } else if (contact.type != EnumMobs.AUTO) {
                mobImage = this.createImageFromTypeAndResourceLocations(contact.type, resourceLocation, resourceLocationSecondary, contact.entity);
            } else if (contact.type == EnumMobs.AUTO) {
                mobImage = this.createAutoIconImageFromResourceLocation(contact, render, resourceLocation);
            }

            if (mobImage == null) {
                return contact.icons;
            }

            BufferedImage[] trimmedImages = this.trimAndOutlineImages(contact.type, mobImage);
            Sprite icon0 = this.textureAtlas.registerIconForBufferedImage(nameMinusSize + "0", trimmedImages[0]);
            Sprite icon1 = this.textureAtlas.registerIconForBufferedImage(nameMinusSize + "1", trimmedImages[1]);
            this.newMobs = true;
            return new Sprite[]{icon0, icon1};
        } else {
            return new Sprite[]{icon, this.textureAtlas.getAtlasSprite(nameMinusSize + "1")};
        }
    }

    private ResourceLocation getRandomizedResourceLocationForEntity(ResourceLocation resourceLocation, Entity entity) {
        try {
            if (this.randomobsOptifine) {
                Object randomEntitiesProperties = this.mapProperties.get(resourceLocation.getPath());
                if (randomEntitiesProperties != null) {
                    this.setEntityMethod.invoke(this.randomEntityClass.cast(this.randomEntity), entity);
                    resourceLocation = (ResourceLocation) this.getEntityTextureMethod
                            .invoke(this.randomEntitiesPropertiesClass.cast(randomEntitiesProperties), resourceLocation, this.randomEntityClass.cast(this.randomEntity));
                }
            }
        } catch (Exception var4) {
        }

        return resourceLocation;
    }

    private BufferedImage[] trimAndOutlineImages(EnumMobs type, BufferedImage image) {
        if (type == EnumMobs.AUTO) {
            image = ImageUtils.trim(image);
            double acceptableMax = 32.0;
            if (ImageUtils.percentageOfEdgePixelsThatAreSolid(image) < 30.0F) {
                acceptableMax = 64.0;
            }

            int maxDimension = Math.max(image.getWidth(), image.getHeight());
            float scale = (float) (1.0 / Math.ceil(maxDimension / acceptableMax));
            BufferedImage image0 = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(image, scale / 2.0F)), this.options.outlines);
            BufferedImage image1 = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(image, scale)), this.options.outlines);
            return new BufferedImage[]{image0, image1};
        } else {
            float scale = (float) image.getWidth() / type.expectedWidth;
            BufferedImage image0 = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(image, 1.0F / scale)), this.options.outlines);
            BufferedImage image1 = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(image, 2.0F / scale)), this.options.outlines);
            return new BufferedImage[]{image0, image1};
        }
    }

    private void handleMPplayer(Contact contact) {
        AbstractClientPlayer player = (AbstractClientPlayer) contact.entity;
        GameProfile gameProfile = player.getGameProfile();
        UUID uuid = gameProfile.getId();
        contact.setUUID(uuid);
        String playerName = this.scrubCodes(gameProfile.getName());
        Sprite icon0 = this.textureAtlas.getAtlasSprite(playerName + " 0");
        Sprite icon1 = null;
        Integer checkCount = 0;
        if (icon0 == this.textureAtlas.getMissingImage()) {
            checkCount = this.mpContactsSkinGetTries.get(playerName);
            if (checkCount == null) {
                checkCount = 0;
            }

            if (checkCount < 5) {
                ThreadDownloadImageData imageData = null;

                try {
                    if (player.getLocationSkin() == DefaultPlayerSkin.getDefaultSkin(player.getUniqueID())) {
                        throw new Exception("failed to get skin: skin is default");
                    }

                    imageData = AbstractClientPlayer.getDownloadImageSkin(player.getLocationSkin(), player.getName().getString());
                    if (imageData == null) {
                        throw new Exception("failed to get skin: image data was null");
                    }

                    BufferedImage skinImage = ImageUtils.createBufferedImageFromGLID(imageData.getGlTextureId());
                    boolean showHat = player.isWearing(EnumPlayerModelParts.HAT);
                    if (showHat) {
                        skinImage = ImageUtils.addImages(ImageUtils.loadImage(skinImage, 8, 8, 8, 8), ImageUtils.loadImage(skinImage, 40, 8, 8, 8), 0.0F, 0.0F, 8, 8);
                    } else {
                        skinImage = ImageUtils.loadImage(skinImage, 8, 8, 8, 8);
                    }

                    float scale = skinImage.getWidth() / 8.0F;
                    BufferedImage skinImageSmall = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(skinImage, 1.0F / scale)), this.options.outlines);
                    BufferedImage skinImageLarge = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(skinImage, 2.0F / scale)), this.options.outlines);
                    icon0 = this.textureAtlas.registerIconForBufferedImage(playerName + " 0", skinImageSmall);
                    icon1 = this.textureAtlas.registerIconForBufferedImage(playerName + " 1", skinImageLarge);
                    this.newMobs = true;
                    this.mpContactsSkinGetTries.remove(playerName);
                } catch (Exception e) {
                    icon0 = this.textureAtlas.getAtlasSprite(EnumMobs.PLAYER.id + EnumMobs.PLAYER.resourceLocation.toString() + "0");
                    icon1 = this.textureAtlas.getAtlasSprite(EnumMobs.PLAYER.id + EnumMobs.PLAYER.resourceLocation + "1");
                    checkCount = checkCount + 1;
                    this.mpContactsSkinGetTries.put(playerName, checkCount);
                }

                contact.icons = new Sprite[]{icon0, icon1};
            }
        } else {
            contact.icons = new Sprite[]{icon0, this.textureAtlas.getAtlasSprite(playerName + " 1")};
        }
    }

    private void getArmor(Contact contact, Entity entity) {
        Sprite icon0 = null;
        Sprite icon1 = null;
        ItemStack stack = ((EntityLivingBase) entity).getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        Item helmet = null;
        if (stack != null) {
            helmet = stack.getItem();
        }

        if (contact.type == EnumMobs.SHEEP) {
            EntitySheep sheepEntity = (EntitySheep) contact.entity;
            if (!sheepEntity.getSheared()) {
                icon0 = this.textureAtlas.getAtlasSprite("sheepfur0");
                icon1 = this.textureAtlas.getAtlasSprite("sheepfur1");
                float[] sheepColors = EntitySheep.getDyeRgb(sheepEntity.getFleeceColor());
                contact.setArmorColor((int) (sheepColors[0] * 255.0F) << 16 | (int) (sheepColors[1] * 255.0F) << 8 | (int) (sheepColors[2] * 255.0F));
            }
        } else if (helmet != null) {
            if (helmet == Items.SKELETON_SKULL) {
                icon0 = this.textureAtlas.getAtlasSprite(EnumMobs.SKELETON.id + EnumMobs.SKELETON.resourceLocation.toString() + 0);
                icon1 = this.textureAtlas.getAtlasSprite(EnumMobs.SKELETON.id + EnumMobs.SKELETON.resourceLocation + 1);
                contact.icons = new Sprite[]{icon0, icon1};
                icon0 = null;
                icon1 = null;
            } else if (helmet == Items.WITHER_SKELETON_SKULL) {
                icon0 = this.textureAtlas.getAtlasSprite(EnumMobs.SKELETONWITHER.id + EnumMobs.SKELETONWITHER.resourceLocation.toString() + 0);
                icon1 = this.textureAtlas.getAtlasSprite(EnumMobs.SKELETONWITHER.id + EnumMobs.SKELETONWITHER.resourceLocation + 1);
                contact.icons = new Sprite[]{icon0, icon1};
                icon0 = null;
                icon1 = null;
            } else if (helmet == Items.ZOMBIE_HEAD) {
                icon0 = this.textureAtlas.getAtlasSprite(EnumMobs.ZOMBIE.id + EnumMobs.ZOMBIE.resourceLocation.toString() + 0);
                icon1 = this.textureAtlas.getAtlasSprite(EnumMobs.ZOMBIE.id + EnumMobs.ZOMBIE.resourceLocation + 1);
                contact.icons = new Sprite[]{icon0, icon1};
                icon0 = null;
                icon1 = null;
            } else if (helmet == Items.PLAYER_HEAD) {
                GameProfile gameProfile = null;
                if (stack.hasTag()) {
                    NBTTagCompound nbttagcompound = stack.getTag();
                    if (nbttagcompound.contains("SkullOwner", 10)) {
                        gameProfile = NBTUtil.readGameProfile(nbttagcompound.getCompound("SkullOwner"));
                    } else if (nbttagcompound.contains("SkullOwner", 8)) {
                        String name = nbttagcompound.getString("SkullOwner");
                        if (name != null && !name.equals("")) {
                            gameProfile = TileEntitySkull.updateGameProfile(new GameProfile(null, name));
                            nbttagcompound.put("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameProfile));
                        }
                    }
                }

                if (gameProfile != null) {
                    ResourceLocation resourcelocation = DefaultPlayerSkin.getDefaultSkinLegacy();
                    Minecraft minecraft = Minecraft.getInstance();
                    java.util.Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().loadSkinFromCache(gameProfile);
                    if (map.containsKey(Type.SKIN)) {
                        resourcelocation = minecraft.getSkinManager().loadSkin(map.get(Type.SKIN), Type.SKIN);
                    }

                    if (resourcelocation != null) {
                        icon0 = this.textureAtlas.getAtlasSpriteIncludingYetToBeStitched(EnumMobs.PLAYER.id + resourcelocation + "0");
                        icon1 = this.textureAtlas.getAtlasSpriteIncludingYetToBeStitched(EnumMobs.PLAYER.id + resourcelocation + "1");
                        if (icon0 == this.textureAtlas.getMissingImage()) {
                            ITextureObject textureObject = GLUtils.textureManager.getTexture(resourcelocation);
                            if (textureObject != null) {
                                BufferedImage image = ImageUtils.createBufferedImageFromGLID(textureObject.getGlTextureId());
                                if (image != null) {
                                    image = this.createImageFromTypeAndImages(EnumMobs.PLAYER, image, null, null);
                                    BufferedImage[] trimmedImages = this.trimAndOutlineImages(contact.type, image);
                                    icon0 = this.textureAtlas.registerIconForBufferedImage(EnumMobs.PLAYER.id + resourcelocation + "0", trimmedImages[0]);
                                    icon1 = this.textureAtlas.registerIconForBufferedImage(EnumMobs.PLAYER.id + resourcelocation + "1", trimmedImages[1]);
                                    this.newMobs = true;
                                }
                            }
                        }
                    }
                }

                if (icon0 == null || icon1 == null) {
                    icon0 = this.textureAtlas.getAtlasSprite(EnumMobs.PLAYER.id + EnumMobs.PLAYER.resourceLocation.toString() + 0);
                    icon1 = this.textureAtlas.getAtlasSprite(EnumMobs.PLAYER.id + EnumMobs.PLAYER.resourceLocation + 1);
                }

                contact.icons = new Sprite[]{icon0, icon1};
                icon0 = null;
                icon1 = null;
            } else if (helmet == Items.CREEPER_HEAD) {
                icon0 = this.textureAtlas.getAtlasSprite(EnumMobs.CREEPER.id + EnumMobs.CREEPER.resourceLocation.toString() + 0);
                icon1 = this.textureAtlas.getAtlasSprite(EnumMobs.CREEPER.id + EnumMobs.CREEPER.resourceLocation + 1);
                contact.icons = new Sprite[]{icon0, icon1};
                icon0 = null;
                icon1 = null;
            } else if (helmet == Items.DRAGON_HEAD) {
                icon0 = this.textureAtlas.getAtlasSprite(EnumMobs.ENDERDRAGON.id + EnumMobs.ENDERDRAGON.resourceLocation.toString() + 0);
                icon1 = this.textureAtlas.getAtlasSprite(EnumMobs.ENDERDRAGON.id + EnumMobs.ENDERDRAGON.resourceLocation + 1);
                contact.icons = new Sprite[]{icon0, icon1};
                icon0 = null;
                icon1 = null;
            } else if (helmet instanceof ItemArmor) {
                ItemArmor helmetArmor = (ItemArmor) helmet;
                int armorType = this.getArmorType(helmetArmor);
                if (armorType == UNKNOWN) {
                    icon0 = this.textureAtlas.getAtlasSprite("armor " + helmet.getTranslationKey() + " 0");
                    icon1 = this.textureAtlas.getAtlasSprite("armor " + helmet.getTranslationKey() + " 1");
                    if (icon0 == this.textureAtlas.getMissingImage()) {
                        Sprite[] newIcons = this.createUnknownArmorIcons(contact, stack, helmet);
                        icon0 = newIcons[0];
                        icon1 = newIcons[1];
                    } else if (icon0 == this.textureAtlas.getFailedImage()) {
                        icon0 = null;
                        icon1 = null;
                    }
                } else {
                    icon0 = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[armorType] + " 0");
                    icon1 = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[armorType] + " 1");
                }

                if (helmetArmor instanceof ItemArmorDyeable) {
                    contact.setArmorColor(((ItemArmorDyeable) helmetArmor).getColor(stack));
                }
            } else if (helmet instanceof ItemBlock) {
                Block block = ((ItemBlock) helmet).getBlock();
                IBlockState blockState = block.getDefaultState();
                int stateID = Block.getStateId(blockState);
                icon0 = this.textureAtlas.getAtlasSprite("blockArmor " + stateID + " 0");
                icon1 = this.textureAtlas.getAtlasSprite("blockArmor " + stateID + " 1");
                if (icon0 == this.textureAtlas.getMissingImage()) {
                    BufferedImage blockImage = this.master.getColorManager().getBlockImage(blockState, stack, entity.world);
                    if (blockImage != null) {
                        BufferedImage largeImage = ImageUtils.fillOutline(ImageUtils.pad(blockImage), this.options.outlines, true, 16, 0);
                        BufferedImage smallImage = ImageUtils.fillOutline(ImageUtils.pad(ImageUtils.scaleImage(blockImage, 0.5F)), this.options.outlines, true, 8, 0);
                        icon0 = this.textureAtlas.registerIconForBufferedImage("blockArmor " + stateID + " 0", smallImage);
                        icon1 = this.textureAtlas.registerIconForBufferedImage("blockArmor " + stateID + " 1", largeImage);
                        this.newMobs = true;
                    }
                }
            }
        }

        contact.armorIcons = new Sprite[]{icon0, icon1};
    }

    private Sprite[] createUnknownArmorIcons(Contact contact, ItemStack stack, Item helmet) {
        Sprite icon0 = null;
        Sprite icon1 = null;
        Method m = null;

        try {
            Class<?> c = Class.forName("net.minecraftforge.client.ForgeHooksClient");
            m = c.getMethod("getArmorTexture", Entity.class, ItemStack.class, String.class, EntityEquipmentSlot.class, String.class);
        } catch (Exception var19) {
        }

        Method getResourceLocation = m;
        ResourceLocation resourceLocation = null;

        try {
            String texture = ((ItemArmor) helmet).getArmorMaterial().getName();
            String domain = "minecraft";
            int sep = texture.indexOf(58);
            if (sep != -1) {
                domain = texture.substring(0, sep);
                texture = texture.substring(sep + 1);
            }

            String resourcePath = String.format("%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, 1, "");
            if (getResourceLocation != null) {
                resourcePath = (String) getResourceLocation.invoke(null, contact.entity, stack, resourcePath, EntityEquipmentSlot.HEAD, null);
            }

            resourceLocation = new ResourceLocation(resourcePath);
        } catch (Exception var18) {
        }

        m = null;

        try {
            Class<?> c = Class.forName("net.minecraftforge.client.ForgeHooksClient");
            m = c.getMethod("getArmorModel", EntityLivingBase.class, ItemStack.class, EntityEquipmentSlot.class, ModelBiped.class);
        } catch (Exception var17) {
        }

        Method getModel = m;
        ModelBiped modelBiped = null;

        try {
            if (getModel != null) {
                modelBiped = (ModelBiped) getModel.invoke(null, contact.entity, stack, EntityEquipmentSlot.HEAD, null);
            }
        } catch (Exception var16) {
        }

        if (modelBiped != null && resourceLocation != null && GLUtils.fboEnabled) {
            ModelRenderer[] headBits = new ModelRenderer[]{modelBiped.bipedHead, modelBiped.bipedHeadwear};
            this.drawModel(0.888888F, 2, (EntityLivingBase) contact.entity, EnumFacing.NORTH, modelBiped, resourceLocation, headBits);
            BufferedImage armorTexture = ImageUtils.createBufferedImageFromGLID(GLUtils.fboTextureID);
            armorTexture = armorTexture.getSubimage(GLUtils.fboRad - 28, GLUtils.fboRad - 28, 56, 56);
            float scale = 2.0F;
            BufferedImage armorImage0 = ImageUtils.fillOutline(
                    ImageUtils.pad(ImageUtils.scaleImage(armorTexture, 1.0F / scale)), this.options.outlines, true, 8, 0
            );
            BufferedImage armorImage1 = ImageUtils.fillOutline(
                    ImageUtils.pad(ImageUtils.scaleImage(armorTexture, 2.0F / scale)), this.options.outlines, true, 16, 0
            );
            icon0 = this.textureAtlas.registerIconForBufferedImage("armor " + helmet.getTranslationKey() + " 0", armorImage0);
            icon1 = this.textureAtlas.registerIconForBufferedImage("armor " + helmet.getTranslationKey() + " 1", armorImage1);
            this.newMobs = true;
        } else if (resourceLocation != null) {
            BufferedImage armorTexture = ImageUtils.createBufferedImageFromResourceLocation(resourceLocation);
            if (armorTexture != null) {
                armorTexture = ImageUtils.addImages(
                        ImageUtils.loadImage(armorTexture, 8, 8, 8, 8), ImageUtils.loadImage(armorTexture, 40, 8, 8, 8), 0.0F, 0.0F, 8, 8
                );
                float scale = armorTexture.getWidth() / 8.0F;
                BufferedImage armorImage0 = ImageUtils.fillOutline(
                        ImageUtils.pad(ImageUtils.scaleImage(armorTexture, 1.0F / scale)), this.options.outlines, true, 8, 0
                );
                BufferedImage armorImage1 = ImageUtils.fillOutline(
                        ImageUtils.pad(ImageUtils.scaleImage(armorTexture, 2.0F / scale)), this.options.outlines, true, 16, 0
                );
                icon0 = this.textureAtlas.registerIconForBufferedImage("armor " + helmet.getTranslationKey() + " 0", armorImage0);
                icon1 = this.textureAtlas.registerIconForBufferedImage("armor " + helmet.getTranslationKey() + " 1", armorImage1);
                this.newMobs = true;
            }
        }

        if (icon0 == null || icon1 == null) {
            System.out.println("can't get texture for custom armor type: " + helmet.getClass());
            this.textureAtlas.registerFailedIcon("armor " + helmet.getTranslationKey() + " 0");
        }

        return new Sprite[]{icon0, icon1};
    }

    private String scrubCodes(String string) {
        return string.replaceAll("(\\xA7.)", "");
    }

    private EnumMobs getContactTypeStrict(Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        if (entityClass.equals(EntityBat.class)) {
            return EnumMobs.BAT;
        } else if (entityClass.equals(EntityBlaze.class)) {
            return EnumMobs.BLAZE;
        } else if (entityClass.equals(EntityCaveSpider.class)) {
            return EnumMobs.CAVESPIDER;
        } else if (entityClass.equals(EntityChicken.class)) {
            return EnumMobs.CHICKEN;
        } else if (entityClass.equals(EntityMooshroom.class)) {
            return EnumMobs.MOOSHROOM;
        } else if (entityClass.equals(EntityCod.class)) {
            return EnumMobs.COD;
        } else if (entityClass.equals(EntityCow.class)) {
            return EnumMobs.COW;
        } else if (entityClass.equals(EntityCreeper.class)) {
            return EnumMobs.CREEPER;
        } else if (entity instanceof EntityDolphin) {
            return EnumMobs.DOLPHIN;
        } else if (entity instanceof EntityDrowned) {
            return EnumMobs.DROWNED;
        } else if (entityClass.equals(EntityDragon.class)) {
            return EnumMobs.ENDERDRAGON;
        } else if (entityClass.equals(EntityEnderman.class)) {
            return EnumMobs.ENDERMAN;
        } else if (entityClass.equals(EntityEndermite.class)) {
            return EnumMobs.ENDERMITE;
        } else if (entityClass.equals(EntityEvoker.class)) {
            return EnumMobs.EVOKER;
        } else if (entityClass.equals(EntityGhast.class)) {
            return EnumMobs.GHAST;
        } else if (entityClass.equals(EntityGuardian.class)) {
            return EnumMobs.GUARDIAN;
        } else if (entityClass.equals(EntityElderGuardian.class)) {
            return EnumMobs.GUARDIANELDER;
        } else if (entityClass.equals(EntityHorse.class)
                || entityClass.equals(EntityDonkey.class)
                || entityClass.equals(EntityMule.class)
                || entityClass.equals(EntitySkeletonHorse.class)
                || entityClass.equals(EntityZombieHorse.class)) {
            return EnumMobs.HORSE;
        } else if (entityClass.equals(EntityHusk.class)) {
            return EnumMobs.HUSK;
        } else if (entityClass.equals(EntityLlama.class)) {
            return EnumMobs.LLAMA;
        } else if (entityClass.equals(EntityIronGolem.class)) {
            return EnumMobs.IRONGOLEM;
        } else if (entityClass.equals(EntityMagmaCube.class)) {
            return EnumMobs.MAGMA;
        } else if (entityClass.equals(EntityOcelot.class)) {
            boolean tamed = ((EntityOcelot) entity).isTamed();
            return tamed ? EnumMobs.CAT : EnumMobs.OCELOT;
        } else if (entityClass.equals(EntityParrot.class)) {
            return EnumMobs.PARROT;
        } else if (entityClass.equals(EntityPhantom.class)) {
            return EnumMobs.PHANTOM;
        } else if (entityClass.equals(EntityPig.class)) {
            return EnumMobs.PIG;
        } else if (entityClass.equals(EntityPigZombie.class)) {
            return EnumMobs.PIGZOMBIE;
        } else if (entity instanceof EntityOtherPlayerMP) {
            return EnumMobs.PLAYER;
        } else if (entityClass.equals(EntityPolarBear.class)) {
            return EnumMobs.POLARBEAR;
        } else if (entityClass.equals(EntityPufferFish.class)) {
            return EnumMobs.PUFFERFISH;
        } else if (entityClass.equals(EntityRabbit.class)) {
            return EnumMobs.RABBIT;
        } else if (entityClass.equals(EntitySalmon.class)) {
            return EnumMobs.SALMON;
        } else if (entityClass.equals(EntitySheep.class)) {
            return EnumMobs.SHEEP;
        } else if (entityClass.equals(EntityShulker.class)) {
            return EnumMobs.SHULKER;
        } else if (entityClass.equals(EntitySilverfish.class)) {
            return EnumMobs.SILVERFISH;
        } else if (entityClass.equals(EntitySkeleton.class)) {
            return EnumMobs.SKELETON;
        } else if (entityClass.equals(EntityWitherSkeleton.class)) {
            return EnumMobs.SKELETONWITHER;
        } else if (entityClass.equals(EntitySlime.class)) {
            return EnumMobs.SLIME;
        } else if (entityClass.equals(EntitySnowman.class)) {
            return EnumMobs.SNOWGOLEM;
        } else if (entityClass.equals(EntitySpider.class)) {
            return EnumMobs.SPIDER;
        } else if (entityClass.equals(EntitySquid.class)) {
            return EnumMobs.SQUID;
        } else if (entityClass.equals(EntityTropicalFish.class)) {
            return ((EntityTropicalFish) entity).getSize() == 0 ? EnumMobs.TROPICALFISHA : EnumMobs.TROPICALFISHB;
        } else if (entityClass.equals(EntityStray.class)) {
            return EnumMobs.STRAY;
        } else if (entityClass.equals(EntityTurtle.class)) {
            return EnumMobs.TURTLE;
        } else if (entityClass.equals(EntityVex.class)) {
            return EnumMobs.VEX;
        } else if (entityClass.equals(EntityVillager.class)) {
            return EnumMobs.VILLAGER;
        } else if (entityClass.equals(EntityVindicator.class)) {
            return EnumMobs.VINDICATOR;
        } else if (entityClass.equals(EntityWitch.class)) {
            return EnumMobs.WITCH;
        } else if (entityClass.equals(EntityWither.class)) {
            return EnumMobs.WITHER;
        } else if (entityClass.equals(EntityWolf.class)) {
            boolean tamed = ((EntityWolf) entity).isTamed();
            boolean angry = ((EntityWolf) entity).isAngry();
            return tamed ? EnumMobs.WOLFTAME : (angry ? EnumMobs.WOLFANGRY : EnumMobs.WOLF);
        } else if (entityClass.equals(EntityZombie.class) || entityClass.equals(EntityHusk.class)) {
            return EnumMobs.ZOMBIE;
        } else {
            return entityClass.equals(EntityZombieVillager.class) ? EnumMobs.ZOMBIEVILLAGER : EnumMobs.UNKNOWN;
        }
    }

    private EnumMobs getContactType(Entity entity) {
        if (entity instanceof EntityBat) {
            return EnumMobs.BAT;
        } else if (entity instanceof EntityBlaze) {
            return EnumMobs.BLAZE;
        } else if (entity instanceof EntityCaveSpider) {
            return EnumMobs.CAVESPIDER;
        } else if (entity instanceof EntityChicken) {
            return EnumMobs.CHICKEN;
        } else if (entity instanceof EntityMooshroom) {
            return EnumMobs.MOOSHROOM;
        } else if (entity instanceof EntityCow) {
            return EnumMobs.COW;
        } else if (entity instanceof EntityCreeper) {
            return EnumMobs.CREEPER;
        } else if (entity instanceof EntityDolphin) {
            return EnumMobs.DOLPHIN;
        } else if (entity instanceof EntityDragon) {
            return EnumMobs.ENDERDRAGON;
        } else if (entity instanceof EntityEnderman) {
            return EnumMobs.ENDERMAN;
        } else if (entity instanceof EntityEndermite) {
            return EnumMobs.ENDERMITE;
        } else if (entity instanceof EntityEvoker) {
            return EnumMobs.EVOKER;
        } else if (entity instanceof EntityGhast) {
            return EnumMobs.GHAST;
        } else if (entity instanceof EntityElderGuardian) {
            return EnumMobs.GUARDIANELDER;
        } else if (entity instanceof EntityGuardian) {
            return EnumMobs.GUARDIAN;
        } else if (entity instanceof EntityLlama) {
            return EnumMobs.LLAMA;
        } else if (entity instanceof AbstractHorse) {
            return EnumMobs.HORSE;
        } else if (entity instanceof EntityIronGolem) {
            return EnumMobs.IRONGOLEM;
        } else if (entity instanceof EntityMagmaCube) {
            return EnumMobs.MAGMA;
        } else if (entity instanceof EntityOcelot) {
            boolean tamed = ((EntityOcelot) entity).isTamed();
            return tamed ? EnumMobs.CAT : EnumMobs.OCELOT;
        } else if (entity instanceof EntityParrot) {
            return EnumMobs.PARROT;
        } else if (entity instanceof EntityPhantom) {
            return EnumMobs.PHANTOM;
        } else if (entity instanceof EntityPig) {
            return EnumMobs.PIG;
        } else if (entity instanceof EntityPigZombie) {
            return EnumMobs.PIGZOMBIE;
        } else if (entity instanceof EntityOtherPlayerMP) {
            return EnumMobs.PLAYER;
        } else if (entity instanceof EntityPolarBear) {
            return EnumMobs.POLARBEAR;
        } else if (entity instanceof EntityPufferFish) {
            return EnumMobs.PUFFERFISH;
        } else if (entity instanceof EntityRabbit) {
            return EnumMobs.RABBIT;
        } else if (entity instanceof EntitySheep) {
            return EnumMobs.SHEEP;
        } else if (entity instanceof EntityShulker) {
            return EnumMobs.SHULKER;
        } else if (entity instanceof EntitySilverfish) {
            return EnumMobs.SILVERFISH;
        } else if (entity instanceof AbstractSkeleton) {
            return EnumMobs.SKELETON;
        } else if (entity instanceof EntitySlime) {
            return EnumMobs.SLIME;
        } else if (entity instanceof EntitySnowman) {
            return EnumMobs.SNOWGOLEM;
        } else if (entity instanceof EntitySpider) {
            return EnumMobs.SPIDER;
        } else if (entity instanceof EntitySquid) {
            return EnumMobs.SQUID;
        } else if (entity instanceof EntityVex) {
            return EnumMobs.VEX;
        } else if (entity instanceof EntityVillager) {
            return EnumMobs.VILLAGER;
        } else if (entity instanceof EntityVindicator) {
            return EnumMobs.VINDICATOR;
        } else if (entity instanceof EntityWitch) {
            return EnumMobs.WITCH;
        } else if (entity instanceof EntityWither) {
            return EnumMobs.WITHER;
        } else if (entity instanceof EntityWolf) {
            boolean tamed = ((EntityWolf) entity).isTamed();
            boolean angry = ((EntityWolf) entity).isAngry();
            return tamed ? EnumMobs.WOLFTAME : (angry ? EnumMobs.WOLFANGRY : EnumMobs.WOLF);
        } else if (entity instanceof EntityZombieVillager) {
            return EnumMobs.ZOMBIEVILLAGER;
        } else {
            return entity instanceof EntityZombie ? EnumMobs.ZOMBIE : EnumMobs.UNKNOWN;
        }
    }

    private EnumMobs getUnknownMobNeutrality(Entity entity) {
        if (this.isHostile(entity)) {
            return EnumMobs.GENERICHOSTILE;
        } else {
            return !(entity instanceof EntityTameable)
                    || !((EntityTameable) entity).isTamed()
                    || !this.game.isSingleplayer() && !((EntityTameable) entity).getOwner().equals(this.game.player)
                    ? EnumMobs.GENERICNEUTRAL
                    : EnumMobs.GENERICTAME;
        }
    }

    private int getArmorType(ItemArmor helmet) {
        if (helmet.getTranslationKey().equals("item.minecraft.leather_helmet")) {
            return 0;
        } else if (helmet.getTranslationKey().equals("item.minecraft.chainmail_helmet")) {
            return 4;
        } else if (helmet.getTranslationKey().equals("item.minecraft.iron_helmet")) {
            return 5;
        } else if (helmet.getTranslationKey().equals("item.minecraft.golden_helmet")) {
            return 6;
        } else if (helmet.getTranslationKey().equals("item.minecraft.diamond_helmet")) {
            return 7;
        } else {
            return helmet.getTranslationKey().equals("item.minecraft.turtle_helmet") ? 8 : UNKNOWN;
        }
    }

    public void renderMapMobs(int x, int y, int guiScale) {
        double max = this.layoutVariables.zoomScaleAdjusted * 32.0;
        GLUtils.disp(this.textureAtlas.getGlTextureId());
        GLShim.glEnable(3042);
        GLShim.glBlendFunc(770, 771);

        for (Contact contact : this.contacts) {
            contact.updateLocation();
            double contactX = contact.x;
            double contactZ = contact.z;
            int contactY = contact.y;
            double wayX = GameVariableAccessShim.xCoordDouble() - contactX;
            double wayZ = GameVariableAccessShim.zCoordDouble() - contactZ;
            int wayY = GameVariableAccessShim.yCoord() - contactY;
            double adjustedDiff = max - Math.max(Math.abs(wayY), 0);
            contact.brightness = (float) Math.max(adjustedDiff / max, 0.0);
            contact.brightness = contact.brightness * contact.brightness;
            contact.angle = (float) Math.toDegrees(Math.atan2(wayX, wayZ));
            contact.distance = Math.sqrt(wayX * wayX + wayZ * wayZ) / this.layoutVariables.zoomScaleAdjusted;
            if (wayY < 0) {
                GLShim.glColor4f(1.0F, 1.0F, 1.0F, contact.brightness);
            } else {
                GLShim.glColor3f(contact.brightness, contact.brightness, contact.brightness);
            }

            if (this.minimapOptions.rotates) {
                contact.angle = contact.angle + this.direction;
            } else if (this.minimapOptions.oldNorth) {
                contact.angle -= 90.0F;
            }

            boolean inRange = false;
            if (!this.minimapOptions.squareMap) {
                inRange = contact.distance < 31.0;
            } else {
                double radLocate = Math.toRadians(contact.angle);
                double dispX = contact.distance * Math.cos(radLocate);
                double dispY = contact.distance * Math.sin(radLocate);
                inRange = Math.abs(dispX) <= 28.5 && Math.abs(dispY) <= 28.5;
            }

            if (inRange) {
                try {
                    GLShim.glPushMatrix();
                    if (this.options.filtering) {
                        GLShim.glTranslatef(x, y, 0.0F);
                        GLShim.glRotatef(-contact.angle, 0.0F, 0.0F, 1.0F);
                        GLShim.glTranslated(0.0, -contact.distance, 0.0);
                        GLShim.glRotatef(contact.angle + contact.rotationFactor, 0.0F, 0.0F, 1.0F);
                        GLShim.glTranslatef(-x, -y, 0.0F);
                    } else {
                        wayX = Math.sin(Math.toRadians(contact.angle)) * contact.distance;
                        wayZ = Math.cos(Math.toRadians(contact.angle)) * contact.distance;
                        if (this.options.filtering) {
                            GLShim.glTranslated(-wayX, -wayZ, 0.0);
                        } else {
                            GLShim.glTranslated(
                                    (double) Math.round(-wayX * this.layoutVariables.scScale) / this.layoutVariables.scScale,
                                    (double) Math.round(-wayZ * this.layoutVariables.scScale) / this.layoutVariables.scScale,
                                    0.0
                            );
                        }
                    }

                    float yOffset = 0.0F;
                    if (contact.entity.getRidingEntity() != null && this.isEntityShown(contact.entity.getRidingEntity())) {
                        yOffset = -4.0F;
                    }

                    if (contact.uuid != null && contact.uuid.equals(this.devUUID)) {
                        Sprite icon = this.textureAtlas.getAtlasSprite("glow");
                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(icon, x, y + yOffset, (int) (icon.getIconWidth() / 2.0F));
                        GLUtils.drawPost();
                    }

                    if (contact.type == EnumMobs.GHAST
                            || contact.type == EnumMobs.GHASTATTACKING
                            || contact.type == EnumMobs.WITHER
                            || contact.type == EnumMobs.WITHERINVULNERABLE
                            || contact.type == EnumMobs.VEX
                            || contact.type == EnumMobs.VEXCHARGING
                            || contact.type == EnumMobs.PUFFERFISH
                            || contact.type == EnumMobs.PUFFERFISHHALF
                            || contact.type == EnumMobs.PUFFERFISHFULL) {
                        if (contact.type == EnumMobs.GHAST || contact.type == EnumMobs.GHASTATTACKING) {
                            Render<Entity> render = this.game.getRenderManager().getEntityRenderObject(contact.entity);
                            String path = ((RenderAccessor) render).invokerGetEntityTexture(contact.entity).getPath();
                            contact.type = path.endsWith("ghast_fire.png") ? EnumMobs.GHASTATTACKING : EnumMobs.GHAST;
                        } else if (contact.type == EnumMobs.WITHER || contact.type == EnumMobs.WITHERINVULNERABLE) {
                            Render<Entity> render = this.game.getRenderManager().getEntityRenderObject(contact.entity);
                            String path = ((RenderAccessor) render).invokerGetEntityTexture(contact.entity).getPath();
                            contact.type = path.endsWith("wither_invulnerable.png") ? EnumMobs.WITHERINVULNERABLE : EnumMobs.WITHER;
                        } else if (contact.type == EnumMobs.VEX || contact.type == EnumMobs.VEXCHARGING) {
                            Render<Entity> render = this.game.getRenderManager().getEntityRenderObject(contact.entity);
                            String path = ((RenderAccessor) render).invokerGetEntityTexture(contact.entity).getPath();
                            contact.type = path.endsWith("vex_charging.png") ? EnumMobs.VEXCHARGING : EnumMobs.VEX;
                        } else if (contact.type == EnumMobs.PUFFERFISH || contact.type == EnumMobs.PUFFERFISHHALF || contact.type == EnumMobs.PUFFERFISHFULL) {
                            int size = ((EntityPufferFish) contact.entity).getPuffState();
                            switch (size) {
                                case 0:
                                    contact.type = EnumMobs.PUFFERFISH;
                                    break;
                                case 1:
                                    contact.type = EnumMobs.PUFFERFISHHALF;
                                    break;
                                case 2:
                                    contact.type = EnumMobs.PUFFERFISHFULL;
                            }
                        }

                        String resourceLocationString = contact.type.resourceLocation != null ? contact.type.resourceLocation.toString() : "";
                        String nameWithoutSize = contact.type.id + resourceLocationString;
                        contact.icons = new Sprite[]{
                                this.textureAtlas.getAtlasSprite(nameWithoutSize + "0"), this.textureAtlas.getAtlasSprite(nameWithoutSize + "1")
                        };
                        if (!this.builtInCustom[contact.type.ordinal()] && this.options.randomobs) {
                            contact.icons = this.getIconsForRandomob(contact);
                            if (this.newMobs) {
                                this.textureAtlas.stitchNew();
                            }

                            this.newMobs = false;
                        }
                    }

                    this.applyFilteringParameters();
                    GLUtils.drawPre();
                    GLUtils.setMap(contact.icons[guiScale], x, y + yOffset, (int) ((float) contact.icons[guiScale].getIconWidth() / (guiScale + 1)));
                    GLUtils.drawPost();
                    if ((
                            this.options.showHelmetsPlayers && contact.type == EnumMobs.PLAYER
                                    || this.options.showHelmetsMobs && contact.type != EnumMobs.PLAYER
                                    || contact.type == EnumMobs.SHEEP
                    )
                            && contact.armorIcons[guiScale] != null) {
                        Sprite icon = contact.armorIcons[guiScale];
                        float armorOffset = 0.0F;
                        if (contact.type == EnumMobs.ZOMBIEVILLAGER) {
                            armorOffset = -0.5F;
                        }

                        float armorScale = 1.25F;
                        float red = 1.0F;
                        float green = 1.0F;
                        float blue = 1.0F;
                        if (contact.armorColor != -1) {
                            red = (contact.armorColor >> 16 & 0xFF) / 255.0F;
                            green = (contact.armorColor >> 8 & 0xFF) / 255.0F;
                            blue = (contact.armorColor >> 0 & 0xFF) / 255.0F;
                            if (contact.type == EnumMobs.SHEEP) {
                                armorScale = 0.525F;
                                EntitySheep sheepEntity = (EntitySheep) contact.entity;
                                if (sheepEntity.hasCustomName() && "jeb_".equals(sheepEntity.getName().getUnformattedComponentText())) {
                                    int semiRandom = sheepEntity.ticksExisted / 25 + sheepEntity.getEntityId();
                                    int numDyeColors = EnumDyeColor.values().length;
                                    int colorID1 = semiRandom % numDyeColors;
                                    int colorID2 = (semiRandom + 1) % numDyeColors;
                                    float lerpVal = (sheepEntity.ticksExisted % 25 + this.game.getRenderPartialTicks()) / 25.0F;
                                    float[] sheepColors1 = EntitySheep.getDyeRgb(EnumDyeColor.byId(colorID1));
                                    float[] sheepColors2 = EntitySheep.getDyeRgb(EnumDyeColor.byId(colorID2));
                                    red = sheepColors1[0] * (1.0F - lerpVal) + sheepColors2[0] * lerpVal;
                                    green = sheepColors1[1] * (1.0F - lerpVal) + sheepColors2[1] * lerpVal;
                                    blue = sheepColors1[2] * (1.0F - lerpVal) + sheepColors2[2] * lerpVal;
                                }
                            }

                            if (wayY < 0) {
                                GLShim.glColor4f(red, green, blue, contact.brightness);
                            } else {
                                GLShim.glColor3f(red * contact.brightness, green * contact.brightness, blue * contact.brightness);
                            }
                        }

                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(icon, x, y + yOffset + armorOffset, (float) icon.getIconWidth() / (guiScale + 1) * armorScale);
                        GLUtils.drawPost();
                        if (icon == this.clothIcons[guiScale]) {
                            if (wayY < 0) {
                                GLShim.glColor4f(1.0F, 1.0F, 1.0F, contact.brightness);
                            } else {
                                GLShim.glColor3f(contact.brightness, contact.brightness, contact.brightness);
                            }

                            icon = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[2] + " " + guiScale);
                            this.applyFilteringParameters();
                            GLUtils.drawPre();
                            GLUtils.setMap(icon, x, y + yOffset + armorOffset, (int) ((float) icon.getIconWidth() / (guiScale + 1) * armorScale));
                            GLUtils.drawPost();
                            if (wayY < 0) {
                                GLShim.glColor4f(red, green, blue, contact.brightness);
                            } else {
                                GLShim.glColor3f(red * contact.brightness, green * contact.brightness, blue * contact.brightness);
                            }

                            icon = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[1] + " " + guiScale);
                            this.applyFilteringParameters();
                            GLUtils.drawPre();
                            GLUtils.setMap(icon, x, y + yOffset + armorOffset, (int) ((float) icon.getIconWidth() / (guiScale + 1) * armorScale));
                            GLUtils.drawPost();
                            GLShim.glColor3f(1.0F, 1.0F, 1.0F);
                            icon = this.textureAtlas.getAtlasSprite("armor " + this.armorNames[3] + " " + guiScale);
                            this.applyFilteringParameters();
                            GLUtils.drawPre();
                            GLUtils.setMap(icon, x, y + yOffset + armorOffset, (int) ((float) icon.getIconWidth() / (guiScale + 1) * armorScale));
                            GLUtils.drawPost();
                        }
                    } else if (contact.uuid != null && contact.uuid.equals(this.devUUID)) {
                        Sprite iconx = this.textureAtlas.getAtlasSprite("crown" + guiScale);
                        this.applyFilteringParameters();
                        GLUtils.drawPre();
                        GLUtils.setMap(iconx, x, y + yOffset, (int) ((float) iconx.getIconWidth() / (guiScale + 1)));
                        GLUtils.drawPost();
                    }

                    if (this.options.showPlayerNames && contact.type == EnumMobs.PLAYER) {
                        float scaleFactor = this.layoutVariables.scScale / this.options.fontScale;
                        GLShim.glScalef(1.0F / scaleFactor, 1.0F / scaleFactor, 1.0F);
                        int m = this.chkLen(contact.name) / 2;
                        this.write(contact.name, x * scaleFactor - m, (y + 3) * scaleFactor, 16777215);
                    }
                } catch (Exception localException) {
                    System.err.println("Error rendering mob icon! " + localException.getLocalizedMessage() + " contact type " + contact.type);
                } finally {
                    GLShim.glPopMatrix();
                }
            }
        }
    }

    private void applyFilteringParameters() {
        if (this.options.filtering) {
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLShim.glTexParameteri(3553, 10242, 10496);
            GLShim.glTexParameteri(3553, 10243, 10496);
        } else {
            GLShim.glTexParameteri(3553, 10241, 9728);
            GLShim.glTexParameteri(3553, 10240, 9728);
        }
    }

    private boolean isHostile(Entity entity) {
        if (entity instanceof EntityPigZombie) {
            return ((EntityPigZombie) entity).isAngry();
        }

        if (entity instanceof IMob) {
            return true;
        }

        if (entity instanceof EntityPolarBear) {
            for (Object object : ((EntityPolarBear) entity)
                    .world
                    .getEntitiesWithinAABB(EntityPolarBear.class, entity.getBoundingBox().expand(8.0, 4.0, 8.0))) {
                if (((EntityPolarBear) object).isChild()) {
                    return true;
                }
            }
        }

        if (entity instanceof EntityRabbit) {
            return ((EntityRabbit) entity).getRabbitType() == 99;
        } else {
            return entity instanceof EntityWolf && ((EntityWolf) entity).isAngry();
        }
    }

    private boolean isPlayer(Entity entity) {
        return entity instanceof EntityOtherPlayerMP;
    }

    private boolean isNeutral(Entity entity) {
        return entity instanceof EntityLiving && !(entity instanceof EntityPlayer) && !this.isHostile(entity);
    }
}
