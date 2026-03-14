package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.gui.GuiAddWaypoint;
import com.mamiyaotaru.voxelmap.gui.GuiWaypoints;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.interfaces.*;
import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.client.gui.screen.OutOfMemoryScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.options.AoOption;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.options.Option;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.Heightmap.Type;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

public class Map implements Runnable, IMap {
    private final int WORLD_HEIGHT = 256;
    private final float[] lastLightBrightnessTable = new float[16];
    private final Object coordinateLock = new Object();
    private final int SEAFLOORLAYER = 0;
    private final int GROUNDLAYER = 1;
    private final int FOLIAGELAYER = 2;
    private final int TRANSPARENTLAYER = 3;
    private final float SQRT2 = 1.4142F;
    LiveScaledGLBufferedImage roundImage = new LiveScaledGLBufferedImage(128, 128, 6);
    private final IVoxelMap master;
    private MinecraftClient game;
    private final String zmodver = "v1.9.28";
    private World world = null;
    private MapSettingsManager options = null;
    private LayoutVariables layoutVariables = null;
    private IColorManager colorManager = null;
    private IWaypointManager waypointManager = null;
    private final int availableProcessors = Runtime.getRuntime().availableProcessors();
    private final boolean multicore = this.availableProcessors > 1;
    private final int heightMapResetHeight = this.multicore ? 2 : 5;
    private final int heightMapResetTime = this.multicore ? 300 : 3000;
    private final boolean threading = this.multicore;
    private final FullMapData[] mapData = new FullMapData[5];
    private final MapChunkCache[] chunkCache = new MapChunkCache[5];
    private LiveGLBufferedImage[] mapImages;
    private final LiveGLBufferedImage[] mapImagesFiltered = new LiveGLBufferedImage[5];
    private final LiveGLBufferedImage[] mapImagesUnfiltered = new LiveScaledGLBufferedImage[5];
    private MutableBlockPos blockPos = new MutableBlockPos(0, 0, 0);
    private final MutableBlockPos tempBlockPos = new MutableBlockPos(0, 0, 0);
    private BlockState transparentBlockState;
    private BlockState surfaceBlockState;
    private BlockState seafloorBlockState;
    private BlockState foliageBlockState;
    private boolean imageChanged = true;
    private NativeImageBackedTexture lightmapTexture = null;
    private boolean needLightmapRefresh = true;
    private int tickWithLightChange = 0;
    private boolean lastPaused = true;
    private double lastGamma = 0.0;
    private float lastSunBrightness = 0.0F;
    private float lastLightning = 0.0F;
    private float lastPotion = 0.0F;
    private final int[] lastLightmapValues = new int[]{
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216,
            -16777216
    };
    private boolean lastBeneathRendering = false;
    private boolean needSkyColor = false;
    private boolean lastAboveHorizon = true;
    private int lastBiome = 0;
    private int lastSkyColor = 0;
    private final Random generator = new Random();
    private boolean showWelcomeScreen = true;
    private Screen lastGuiScreen = null;
    private boolean enabled = true;
    private boolean fullscreenMap = false;
    private boolean active = false;
    private int zoom = 2;
    private int mapX = 37;
    private int mapY = 37;
    private int scWidth;
    private int scHeight;
    private String error = "";
    private final String[] welcomeString = new String[8];
    private int ztimer = 0;
    private int heightMapFudge = 0;
    private int timer = 0;
    private boolean doFullRender = true;
    private boolean zoomChanged;
    private int lastX = 0;
    private int lastZ = 0;
    private int lastY = 0;
    private int lastImageX = 0;
    private int lastImageZ = 0;
    private boolean lastFullscreen = false;
    private float direction = 0.0F;
    private float percentX;
    private float percentY;
    private String subworldName = "";
    private int northRotate = 0;
    private Thread zCalc = new Thread(this, "Voxelmap LiveMap Calculation Thread");
    private int zCalcTicker = 0;
    private final TextRenderer fontRenderer;
    private final int[] lightmapColors = new int[256];
    private double zoomScale = 1.0;
    private double zoomScaleAdjusted = 1.0;
    private boolean optifineInstalled = false;
    private double rFog = 0.0;
    private double bFog = 0.0;
    private double gFog = 0.0;
    private int mapImageInt = -1;

    public Map(IVoxelMap master) {
        this.master = master;
        this.game = GameVariableAccessShim.getMinecraft();
        this.options = master.getMapOptions();
        this.colorManager = master.getColorManager();
        this.waypointManager = master.getWaypointManager();
        this.layoutVariables = new LayoutVariables();
        ArrayList<KeyBinding> tempBindings = new ArrayList<>();
        tempBindings.addAll(Arrays.asList(this.game.options.keysAll));
        tempBindings.addAll(Arrays.asList(this.options.keyBindings));
        Field f = ReflectionUtils.getFieldByType(this.game.options, GameOptions.class, KeyBinding[].class, 1);

        try {
            f.set(this.game.options, tempBindings.toArray(new KeyBinding[tempBindings.size()]));
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException ex) {
            ex.printStackTrace();
        }

        java.util.Map<String, Integer> categoryOrder = (java.util.Map<String, Integer>) ReflectionUtils.getPrivateFieldValueByType(
                null, KeyBinding.class, java.util.Map.class, 2
        );
        System.out.println("CATEGORY ORDER IS " + categoryOrder.size());
        Integer categoryPlace = categoryOrder.get("controls.minimap.title");
        if (categoryPlace == null) {
            int currentSize = categoryOrder.size();
            categoryOrder.put("controls.minimap.title", currentSize + 1);
        }

        this.showWelcomeScreen = this.options.welcome;
        this.zCalc.start();
        this.zCalc.setPriority(5);
        this.mapData[0] = new FullMapData(32, 32);
        this.mapData[1] = new FullMapData(64, 64);
        this.mapData[2] = new FullMapData(128, 128);
        this.mapData[3] = new FullMapData(256, 256);
        this.mapData[4] = new FullMapData(512, 512);
        this.chunkCache[0] = new MapChunkCache(3, 3, this);
        this.chunkCache[1] = new MapChunkCache(5, 5, this);
        this.chunkCache[2] = new MapChunkCache(9, 9, this);
        this.chunkCache[3] = new MapChunkCache(17, 17, this);
        this.chunkCache[4] = new MapChunkCache(33, 33, this);
        this.mapImagesFiltered[0] = new LiveGLBufferedImage(32, 32, 6);
        this.mapImagesFiltered[1] = new LiveGLBufferedImage(64, 64, 6);
        this.mapImagesFiltered[2] = new LiveGLBufferedImage(128, 128, 6);
        this.mapImagesFiltered[3] = new LiveGLBufferedImage(256, 256, 6);
        this.mapImagesFiltered[4] = new LiveGLBufferedImage(512, 512, 6);
        this.mapImagesUnfiltered[0] = new LiveScaledGLBufferedImage(32, 32, 6);
        this.mapImagesUnfiltered[1] = new LiveScaledGLBufferedImage(64, 64, 6);
        this.mapImagesUnfiltered[2] = new LiveScaledGLBufferedImage(128, 128, 6);
        this.mapImagesUnfiltered[3] = new LiveScaledGLBufferedImage(256, 256, 6);
        this.mapImagesUnfiltered[4] = new LiveScaledGLBufferedImage(512, 512, 6);
        if (this.options.filtering) {
            this.mapImages = this.mapImagesFiltered;
        } else {
            this.mapImages = this.mapImagesUnfiltered;
        }

        this.welcomeString[0] = "§4VoxelMap§F! " + this.zmodver + " " + I18nUtils.getString("minimap.ui.welcome1");
        this.welcomeString[1] = I18nUtils.getString("minimap.ui.welcome2");
        this.welcomeString[2] = I18nUtils.getString("minimap.ui.welcome3");
        this.welcomeString[3] = I18nUtils.getString("minimap.ui.welcome4");
        this.welcomeString[4] = "§B"
                + this.options.keyBindZoom.getLocalizedName()
                + "§F: "
                + I18nUtils.getString("minimap.ui.welcome5a")
                + ", §B"
                + this.options.keyBindMenu.getLocalizedName()
                + "§F: "
                + I18nUtils.getString("minimap.ui.welcome5b");
        this.welcomeString[5] = "§B" + this.options.keyBindFullscreen.getLocalizedName() + "§F: " + I18nUtils.getString("minimap.ui.welcome6");
        this.welcomeString[6] = "§B" + this.options.keyBindWaypoint.getLocalizedName() + "§F: " + I18nUtils.getString("minimap.ui.welcome7");
        this.welcomeString[7] = "§F" + this.options.keyBindZoom.getLocalizedName() + "§7: " + I18nUtils.getString("minimap.ui.welcome8");
        if (GLUtils.fboEnabled) {
            GLUtils.setupFrameBuffer();
        }

        this.fontRenderer = this.game.textRenderer;
        this.zoom = this.options.zoom;
        this.setZoomScale();
        this.optifineInstalled = false;
        Field ofProfiler = null;

        try {
            ofProfiler = GameOptions.class.getDeclaredField("ofProfiler");
        } catch (SecurityException var14) {
        } catch (NoSuchFieldException var15) {
        } finally {
            if (ofProfiler != null) {
                this.optifineInstalled = true;
            }
        }
    }

    @Override
    public void forceFullRender(boolean forceFullRender) {
        this.doFullRender = forceFullRender;
        this.master.getSettingsAndLightingChangeNotifier().notifyOfChanges();
    }

    @Override
    public float getPercentX() {
        return this.percentX;
    }

    @Override
    public float getPercentY() {
        return this.percentY;
    }

    @Override
    public void run() {
        if (this.game != null) {
            while (true) {
                while (!this.threading) {
                    synchronized (this.zCalc) {
                        try {
                            this.zCalc.wait(0L);
                        } catch (InterruptedException var6) {
                        }
                    }
                }

                for (this.active = true; this.game.player != null && this.world != null && this.active; this.active = false) {
                    if (!this.options.hide) {
                        try {
                            this.mapCalc(this.doFullRender);
                            if (!this.doFullRender) {
                                this.chunkCache[this.zoom].centerChunks(this.blockPos.withXYZ(this.lastX, 0, this.lastZ));
                                this.chunkCache[this.zoom].calculateChunks();
                            }
                        } catch (Exception var5) {
                        }
                    }

                    this.doFullRender = this.zoomChanged;
                    this.zoomChanged = false;
                }

                this.zCalcTicker = 0;
                synchronized (this.zCalc) {
                    try {
                        this.zCalc.wait(0L);
                    } catch (InterruptedException var8) {
                    }
                }
            }
        }
    }

    @Override
    public void newWorld(World world) {
        this.world = world;
        this.lightmapTexture = this.getLightmapTexture();
        this.mapData[this.zoom].blank();
        this.mapImages[this.zoom].blank();
        this.doFullRender = true;
        this.master.getSettingsAndLightingChangeNotifier().notifyOfChanges();
    }

    @Override
    public void newWorldName() {
        this.subworldName = this.waypointManager.getCurrentSubworldDescriptor(true);
        StringBuilder subworldNameBuilder = new StringBuilder("§r").append(I18nUtils.getString("worldmap.multiworld.newworld")).append(":").append(" ");
        if (this.subworldName.equals("") && this.waypointManager.isMultiworld()) {
            subworldNameBuilder.append("???");
        } else if (!this.subworldName.equals("")) {
            subworldNameBuilder.append(this.subworldName);
        }

        this.error = subworldNameBuilder.toString();
    }

    @Override
    public void onTickInGame(MinecraftClient mc) {
        this.northRotate = this.options.oldNorth ? 90 : 0;
        if (this.game == null) {
            this.game = mc;
        }

        if (this.lightmapTexture == null) {
            this.lightmapTexture = this.getLightmapTexture();
        }

        if (this.game.currentScreen == null && this.options.keyBindMenu.wasPressed()) {
            this.showWelcomeScreen = false;
            if (this.options.welcome) {
                this.options.welcome = false;
                this.options.saveAll();
            }

            this.game.openScreen(new GuiPersistentMap(null, this.master));
        }

        if (this.game.currentScreen == null && this.options.keyBindWaypointMenu.wasPressed()) {
            this.showWelcomeScreen = false;
            if (this.options.welcome) {
                this.options.welcome = false;
                this.options.saveAll();
            }

            this.game.openScreen(new GuiWaypoints(null, this.master));
        }

        if (this.game.currentScreen == null && this.options.keyBindWaypoint.wasPressed()) {
            this.showWelcomeScreen = false;
            if (this.options.welcome) {
                this.options.welcome = false;
                this.options.saveAll();
            }

            float r;
            float g;
            float b;
            if (this.waypointManager.getWaypoints().size() == 0) {
                r = 0.0F;
                g = 1.0F;
                b = 0.0F;
            } else {
                r = this.generator.nextFloat();
                g = this.generator.nextFloat();
                b = this.generator.nextFloat();
            }

            TreeSet<DimensionContainer> dimensions = new TreeSet<>();
            dimensions.add(AbstractVoxelMap.getInstance().getDimensionManager().getDimensionContainerByDimension(this.game.world.dimension));
            Waypoint newWaypoint = new Waypoint(
                    "",
                    this.game.player.dimension != DimensionType.THE_NETHER ? GameVariableAccessShim.xCoord() : GameVariableAccessShim.xCoord() * 8,
                    this.game.player.dimension != DimensionType.THE_NETHER ? GameVariableAccessShim.zCoord() : GameVariableAccessShim.zCoord() * 8,
                    GameVariableAccessShim.yCoord(),
                    true,
                    r,
                    g,
                    b,
                    "",
                    this.master.getWaypointManager().getCurrentSubworldDescriptor(false),
                    dimensions
            );
            this.game.openScreen(new GuiAddWaypoint(null, this.master, newWaypoint, false));
        }

        if (this.game.currentScreen == null && this.options.keyBindMobToggle.wasPressed()) {
            this.master.getRadarOptions().setOptionValue(EnumOptionsMinimap.SHOWRADAR, 0);
            this.options.saveAll();
        }

        if (this.game.currentScreen == null && this.options.keyBindWaypointToggle.wasPressed()) {
            this.options.toggleIngameWaypoints();
        }

        if (this.game.currentScreen == null && this.options.keyBindZoom.wasPressed()) {
            this.showWelcomeScreen = false;
            if (this.options.welcome) {
                this.options.welcome = false;
                this.options.saveAll();
            } else {
                this.cycleZoomLevel();
            }
        }

        if (this.game.currentScreen == null && this.options.keyBindFullscreen.wasPressed()) {
            this.fullscreenMap = !this.fullscreenMap;
            if (this.zoom == 4) {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.25x)";
            } else if (this.zoom == 3) {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.5x)";
            } else if (this.zoom == 2) {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (1.0x)";
            } else if (this.zoom == 1) {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (2.0x)";
            } else {
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (4.0x)";
            }
        }

        this.checkForChanges();
        if (this.game.currentScreen instanceof DeathScreen && !(this.lastGuiScreen instanceof DeathScreen)) {
            this.waypointManager.handleDeath();
        }

        this.lastGuiScreen = this.game.currentScreen;
        this.calculateCurrentLightAndSkyColor();
        if (this.threading) {
            if (!this.zCalc.isAlive() && this.threading) {
                this.zCalc = new Thread(this, "Voxelmap LiveMap Calculation Thread");
                this.zCalc.setPriority(5);
                this.zCalc.start();
            }

            if (!(this.game.currentScreen instanceof DeathScreen) && !(this.game.currentScreen instanceof OutOfMemoryScreen)) {
                this.zCalcTicker++;
                if (this.zCalcTicker > 200) {
                    this.zCalcTicker = 0;
                    this.zCalc.stop();
                } else {
                    synchronized (this.zCalc) {
                        this.zCalc.notify();
                    }
                }
            }
        } else if (!this.threading) {
            if (!this.options.hide && this.world != null) {
                this.mapCalc(this.doFullRender);
                if (!this.doFullRender) {
                    this.chunkCache[this.zoom].centerChunks(this.blockPos.withXYZ(this.lastX, 0, this.lastZ));
                    this.chunkCache[this.zoom].calculateChunks();
                }
            }

            this.doFullRender = false;
        }

        this.enabled = !mc.options.hudHidden && (this.options.showUnderMenus || this.game.currentScreen == null) && !this.game.options.debugEnabled;

        this.direction = GameVariableAccessShim.rotationYaw() + 180.0F;

        while (this.direction >= 360.0F) {
            this.direction -= 360.0F;
        }

        while (this.direction < 0.0F) {
            this.direction += 360.0F;
        }

        if (!this.error.equals("") && this.ztimer == 0) {
            this.ztimer = 500;
        }

        if (this.ztimer > 0) {
            this.ztimer--;
        }

        if (this.ztimer == 0 && !this.error.equals("")) {
            this.error = "";
        }

        if (this.enabled) {
            this.drawMinimap(mc);
        }

        this.timer = this.timer > 5000 ? 0 : this.timer + 1;
    }

    private void cycleZoomLevel() {
        if (this.options.zoom == 4) {
            this.options.zoom = 3;
            this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.5x)";
        } else if (this.options.zoom == 3) {
            this.options.zoom = 2;
            this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (1.0x)";
        } else if (this.options.zoom == 2) {
            this.options.zoom = 1;
            this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (2.0x)";
        } else if (this.options.zoom == 1) {
            this.options.zoom = 0;
            this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (4.0x)";
        } else if (this.options.zoom == 0) {
            if (this.multicore && Option.RENDER_DISTANCE.get(this.game.options) > 8.0) {
                this.options.zoom = 4;
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.25x)";
            } else {
                this.options.zoom = 3;
                this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.5x)";
            }
        }

        this.options.saveAll();
        this.zoomChanged = true;
        this.zoom = this.options.zoom;
        this.setZoomScale();
        this.mapImages[this.zoom].blank();
        this.doFullRender = true;
    }

    private void setZoomScale() {
        this.zoomScale = Math.pow(2.0, this.zoom) / 2.0;
        if (this.options.squareMap && this.options.rotates) {
            this.zoomScaleAdjusted = this.zoomScale / 1.4142F;
        } else {
            this.zoomScaleAdjusted = this.zoomScale;
        }
    }

    private NativeImageBackedTexture getLightmapTexture() {
        Object lightTextureObj = ReflectionUtils.getPrivateFieldValueByType(this.game.gameRenderer, GameRenderer.class, LightmapTextureManager.class);
        if (lightTextureObj == null) {
            return null;
        }

        LightmapTextureManager lightTexture = (LightmapTextureManager) lightTextureObj;
        Object lightmapTextureObj = ReflectionUtils.getPrivateFieldValueByType(lightTexture, LightmapTextureManager.class, NativeImageBackedTexture.class);
        return lightmapTextureObj == null ? null : (NativeImageBackedTexture) lightmapTextureObj;
    }

    public void calculateCurrentLightAndSkyColor() {
        if (this.world != null) {
            if (this.needLightmapRefresh && TickCounter.tickCounter != this.tickWithLightChange && !this.game.isPaused() || this.options.realTimeTorches) {
                GLUtils.disp(this.lightmapTexture.getGlId());
                ByteBuffer byteBuffer = ByteBuffer.allocateDirect(1024).order(ByteOrder.nativeOrder());
                GLShim.glGetTexImage(3553, 0, 6408, 5121, byteBuffer);

                for (int i = 0; i < this.lightmapColors.length; i++) {
                    int index = i * 4;
                    this.lightmapColors[i] = (byteBuffer.get(index + 3) << 24)
                            + (byteBuffer.get(index) << 16)
                            + (byteBuffer.get(index + 1) << 8)
                            + (byteBuffer.get(index + 2) << 0);
                }

                if (this.lightmapColors[255] != 0) {
                    this.needLightmapRefresh = false;
                }
            }

            boolean lightChanged = false;
            if (this.game.options.gamma != this.lastGamma) {
                lightChanged = true;
                this.lastGamma = this.game.options.gamma;
            }

            float[] providerLightBrightnessTable = this.world.dimension.getLightLevelToBrightness();

            for (int t = 0; t < 16; t++) {
                if (providerLightBrightnessTable[t] != this.lastLightBrightnessTable[t]) {
                    lightChanged = true;
                    this.lastLightBrightnessTable[t] = providerLightBrightnessTable[t];
                }
            }

            float sunBrightness = this.world.getAmbientLight(1.0F);
            if (Math.abs(this.lastSunBrightness - sunBrightness) > 0.01
                    || sunBrightness == 1.0 && sunBrightness != this.lastSunBrightness
                    || sunBrightness == 0.0 && sunBrightness != this.lastSunBrightness) {
                lightChanged = true;
                this.needSkyColor = true;
                this.lastSunBrightness = sunBrightness;
            }

            float potionEffect = 0.0F;
            if (this.game.player.hasStatusEffect(StatusEffects.NIGHT_VISION)) {
                int duration = this.game.player.getStatusEffect(StatusEffects.NIGHT_VISION).getDuration();
                potionEffect = duration > 200 ? 1.0F : 0.7F + MathHelper.sin((duration - 1.0F) * (float) Math.PI * 0.2F) * 0.3F;
            }

            if (this.lastPotion != potionEffect) {
                this.lastPotion = potionEffect;
                lightChanged = true;
            }

            int lastLightningBolt = this.world.getTicksSinceLightning();
            if (this.lastLightning != lastLightningBolt) {
                this.lastLightning = lastLightningBolt;
                lightChanged = true;
            }

            if (this.lastPaused != this.game.isPaused()) {
                this.lastPaused = !this.lastPaused;
                lightChanged = true;
            }

            boolean scheduledUpdate = (this.timer - 50)
                    % (this.lastLightBrightnessTable[0] == 0.0F ? 250 : (this.game.player.dimension != DimensionType.THE_NETHER ? 500 : 5000))
                    == 0;
            if (lightChanged || scheduledUpdate) {
                this.tickWithLightChange = TickCounter.tickCounter;
                lightChanged = false;
                this.needLightmapRefresh = true;
            }

            boolean aboveHorizon = this.game.player.getCameraPosVec(0.0F).y + this.game.player.getEyeHeight(this.game.player.getPose())
                    >= this.world.getHorizonHeight();
            if (DimensionType.getId(this.world.dimension.getType()).toString().toLowerCase().contains("ether")) {
                aboveHorizon = true;
            }

            if (aboveHorizon != this.lastAboveHorizon) {
                this.needSkyColor = true;
                this.lastAboveHorizon = aboveHorizon;
            }

            int biomeID = Registry.BIOME
                    .getRawId(
                            this.world.getBiome(this.blockPos.withXYZ(GameVariableAccessShim.xCoord(), GameVariableAccessShim.yCoord(), GameVariableAccessShim.zCoord()))
                    );
            if (biomeID != this.lastBiome) {
                this.needSkyColor = true;
                this.lastBiome = biomeID;
            }

            if (this.needSkyColor || scheduledUpdate) {
                this.colorManager.setSkyColor(this.getSkyColor());
            }
        }
    }

    @Override
    public void getFogColor() {
        if (this.needLightmapRefresh && TickCounter.tickCounter != this.tickWithLightChange && !this.game.isPaused()) {
            float[] fogColors = new float[16];
            FloatBuffer temp = BufferUtils.createFloatBuffer(16);
            GLShim.glGetFloat(3106, temp);
            temp.get(fogColors);
            this.rFog = fogColors[0];
            this.gFog = fogColors[1];
            this.bFog = fogColors[2];
        }
    }

    private int getSkyColor() {
        this.needSkyColor = false;
        boolean aboveHorizon = this.lastAboveHorizon;
        float[] fogColors = new float[16];
        FloatBuffer temp = BufferUtils.createFloatBuffer(16);
        GLShim.glGetFloat(3106, temp);
        temp.get(fogColors);
        double rFog = fogColors[0];
        double gFog = fogColors[1];
        double bFog = fogColors[2];
        int fogColor;
        if (!aboveHorizon && this.game.options.viewDistance >= 4) {
            fogColor = 167772160 + (int) (rFog * 255.0) * 65536 + (int) (gFog * 255.0) * 256 + (int) (bFog * 255.0);
        } else {
            fogColor = -16777216 + (int) (rFog * 255.0) * 65536 + (int) (gFog * 255.0) * 256 + (int) (bFog * 255.0);
        }

        if (this.game.world.dimension.hasVisibleSky() && this.game.options.viewDistance >= 4) {
            double rSky;
            double bSky;
            double gSky;
            if (!aboveHorizon) {
                bSky = 0.0;
                gSky = 0.0;
                rSky = 0.0;
            } else {
                Vec3d skyColorVec = this.world.getSkyColor(this.game.getCameraEntity().getBlockPos(), 0.0F);
                rSky = skyColorVec.x;
                gSky = skyColorVec.y;
                bSky = skyColorVec.z;
                if (this.world.dimension.method_12449()) {
                    rSky = rSky * 0.2F + 0.04F;
                    gSky = gSky * 0.2F + 0.04F;
                    bSky = bSky * 0.6F + 0.1F;
                }
            }

            boolean showLocalFog = this.world.dimension.isFogThick(GameVariableAccessShim.xCoord(), GameVariableAccessShim.zCoord());
            float farPlaneDistance = this.game.options.viewDistance * 16.0F;
            float fogStart = 0.0F;
            float fogEnd = 0.0F;
            if (showLocalFog) {
                fogStart = farPlaneDistance * 0.05F;
                fogEnd = Math.min(farPlaneDistance, 192.0F) * 0.5F;
            } else {
                fogEnd = farPlaneDistance * 0.8F;
            }

            float fogDensity = Math.max(
                    0.0F, Math.min(1.0F, (fogEnd - (GameVariableAccessShim.yCoord() - (float) this.game.world.getHorizonHeight())) / (fogEnd - fogStart))
            );
            int skyColor = (int) (fogDensity * 255.0F) * 16777216 + (int) (rSky * 255.0) * 65536 + (int) (gSky * 255.0) * 256 + (int) (bSky * 255.0);
            return this.colorManager.colorAdder(skyColor, fogColor);
        } else {
            return fogColor;
        }
    }

    @Override
    public int[] getLightmapArray() {
        return this.lightmapColors;
    }

    @Override
    public void drawMinimap(MinecraftClient mc) {
        int scScale = 1;

        while (this.game.window.getFramebufferWidth() / (scScale + 1) >= 320 && this.game.window.getFramebufferHeight() / (scScale + 1) >= 240) {
            scScale++;
        }

        scScale += this.fullscreenMap ? 0 : this.options.sizeModifier;
        double scaledWidthD = (double) this.game.window.getFramebufferWidth() / scScale;
        double scaledHeightD = (double) this.game.window.getFramebufferHeight() / scScale;
        this.scWidth = MathHelper.ceil(scaledWidthD);
        this.scHeight = MathHelper.ceil(scaledHeightD);
        GLShim.glMatrixMode(5889);
        GLShim.glPushMatrix();
        GLShim.glLoadIdentity();
        GLShim.glOrtho(0.0, scaledWidthD, scaledHeightD, 0.0, 1000.0, 3000.0);
        GLShim.glMatrixMode(5888);
        GLShim.glPushMatrix();
        GLShim.glLoadIdentity();
        GLShim.glTranslatef(0.0F, 0.0F, -2000.0F);
        if (this.options.mapCorner != 0 && this.options.mapCorner != 3) {
            this.mapX = this.scWidth - 37;
        } else {
            this.mapX = 37;
        }

        if (this.options.mapCorner != 0 && this.options.mapCorner != 1) {
            this.mapY = this.scHeight - 37;
        } else {
            this.mapY = 37;
        }

        if (this.options.mapCorner == 1 && this.game.player.getStatusEffects().size() > 0) {
            float statusIconOffset = 0.0F;

            for (StatusEffectInstance statusEffectInstance : this.game.player.getStatusEffects()) {
                if (statusEffectInstance.shouldShowIcon()) {
                    if (statusEffectInstance.getEffectType().method_5573()) {
                        statusIconOffset = Math.max(statusIconOffset, 24.0F);
                    } else {
                        statusIconOffset = Math.max(statusIconOffset, 50.0F);
                    }
                }
            }

            int scHeight = this.game.window.getScaledHeight();
            float resFactor = (float) this.scHeight / scHeight;
            this.mapY += (int) (statusIconOffset * resFactor);
        }

        GLShim.glEnable(3042);
        GLShim.glEnable(3553);
        GLShim.glBlendFunc(770, 0);
        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (!this.options.hide) {
            GLShim.glEnable(2929);
            if (this.fullscreenMap) {
                this.renderMapFull(this.scWidth, this.scHeight);
            } else {
                this.renderMap(this.mapX, this.mapY, scScale);
            }

            GLShim.glDisable(2929);
            if (this.master.getRadar() != null && !this.fullscreenMap) {
                this.layoutVariables.updateVars(scScale, this.mapX, this.mapY, this.zoomScale, this.zoomScaleAdjusted);
                this.master.getRadar().OnTickInGame(mc, this.layoutVariables);
            }

            if (!this.fullscreenMap) {
                this.drawDirections(this.mapX, this.mapY);
            }

            if (this.fullscreenMap) {
                this.drawArrow(this.scWidth / 2, this.scHeight / 2);
            } else {
                this.drawArrow(this.mapX, this.mapY);
            }
        }

        if (this.options.coords) {
            this.showCoords(this.mapX, this.mapY);
        }

        if (this.showWelcomeScreen) {
            this.drawWelcomeScreen(this.scWidth, this.scHeight);
        }

        GLShim.glDepthMask(true);
        GLShim.glEnable(2929);
        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GLShim.glMatrixMode(5889);
        GLShim.glPopMatrix();
        GLShim.glMatrixMode(5888);
        GLShim.glPopMatrix();
        GLShim.glTexParameteri(3553, 10241, 9728);
        GLShim.glTexParameteri(3553, 10240, 9728);
    }

    private void checkForChanges() {
        boolean changed = false;
        if (this.colorManager.checkForChanges()) {
            this.loadMapImage();
            changed = true;
        }

        if (this.options.isChanged()) {
            if (this.options.filtering) {
                this.mapImages = this.mapImagesFiltered;
            } else {
                this.mapImages = this.mapImagesUnfiltered;
            }

            changed = true;
            this.setZoomScale();
        }

        if (changed) {
            this.doFullRender = true;
            this.master.getSettingsAndLightingChangeNotifier().notifyOfChanges();
        }
    }

    private void mapCalc(boolean full) {
        int currentX = GameVariableAccessShim.xCoord();
        int currentZ = GameVariableAccessShim.zCoord();
        int currentY = GameVariableAccessShim.yCoord();
        int offsetX = currentX - this.lastX;
        int offsetZ = currentZ - this.lastZ;
        int offsetY = currentY - this.lastY;
        int multi = (int) Math.pow(2.0, this.zoom);
        boolean needHeightAndID = false;
        boolean needHeightMap = false;
        boolean needLight = false;
        boolean skyColorChanged = false;
        int skyColor = this.colorManager.getAirColor();
        if (this.lastSkyColor != skyColor) {
            skyColorChanged = true;
            this.lastSkyColor = skyColor;
        }

        if (this.options.lightmap) {
            int torchOffset = this.options.realTimeTorches ? 8 : 0;
            int skylightMultiplier = 16;

            for (int t = 0; t < 16; t++) {
                if (this.lastLightmapValues[t] != this.lightmapColors[t * skylightMultiplier + torchOffset]) {
                    needLight = true;
                    this.lastLightmapValues[t] = this.lightmapColors[t * skylightMultiplier + torchOffset];
                }
            }
        }

        if (offsetY != 0) {
            this.heightMapFudge++;
        } else if (this.heightMapFudge != 0) {
            this.heightMapFudge++;
        }

        if (full || Math.abs(offsetY) >= this.heightMapResetHeight || this.heightMapFudge > this.heightMapResetTime) {
            if (this.lastY != currentY) {
                needHeightMap = true;
            }

            this.lastY = currentY;
            this.heightMapFudge = 0;
        }

        if (Math.abs(offsetX) > 32 * multi || Math.abs(offsetZ) > 32 * multi) {
            full = true;
        }

        boolean nether = false;
        boolean caves = false;
        boolean netherPlayerInOpen = false;
        this.blockPos.setXYZ(this.lastX, Math.max(Math.min(GameVariableAccessShim.yCoord(), 256 - 1), 0), this.lastZ);
        if (this.game.player.dimension == DimensionType.THE_NETHER) {
            netherPlayerInOpen = this.world.getChunk(this.blockPos).sampleHeightmap(Type.MOTION_BLOCKING, this.blockPos.getX() & 15, this.blockPos.getZ() & 15)
                    <= currentY;
            nether = currentY < 126;
            if (this.options.cavesAllowed && this.options.showCaves && currentY >= 126 && !netherPlayerInOpen) {
                caves = true;
            }
        } else if (this.game.player.dimension == DimensionType.THE_END) {
            boolean endPlayerInOpen = this.world
                    .getChunk(this.blockPos)
                    .sampleHeightmap(Type.MOTION_BLOCKING, this.blockPos.getX() & 15, this.blockPos.getZ() & 15)
                    <= currentY;
            if (this.options.cavesAllowed && this.options.showCaves && !endPlayerInOpen) {
                caves = true;
            }
        } else if (this.options.cavesAllowed && this.options.showCaves && this.world.getLightLevel(LightType.SKY, this.blockPos) <= 0) {
            caves = true;
        }

        if (this.lastBeneathRendering != (caves || nether && (currentY <= 125 || !netherPlayerInOpen && this.options.showCaves))) {
            this.lastBeneathRendering = caves || nether && (currentY <= 125 || !netherPlayerInOpen && this.options.showCaves);
            full = true;
        }

        needHeightAndID = needHeightMap && (nether || caves);
        int color24 = -1;
        synchronized (this.coordinateLock) {
            if (!full) {
                this.mapImages[this.zoom].moveY(offsetZ);
                this.mapImages[this.zoom].moveX(offsetX);
            }

            this.lastX = currentX;
            this.lastZ = currentZ;
        }

        int startX = currentX - 16 * multi;
        int startZ = currentZ - 16 * multi;
        if (!full) {
            this.mapData[this.zoom].moveZ(offsetZ);
            this.mapData[this.zoom].moveX(offsetX);

            for (int imageY = offsetZ > 0 ? 32 * multi - 1 : -offsetZ - 1; imageY >= (offsetZ > 0 ? 32 * multi - offsetZ : 0); imageY--) {
                for (int imageX = 0; imageX < 32 * multi; imageX++) {
                    color24 = this.getPixelColor(true, true, true, true, nether, caves, this.world, multi, startX, startZ, imageX, imageY);
                    this.mapImages[this.zoom].setRGB(imageX, imageY, color24);
                }
            }

            for (int imageY = 32 * multi - 1; imageY >= 0; imageY--) {
                for (int imageX = offsetX > 0 ? 32 * multi - offsetX : 0; imageX < (offsetX > 0 ? 32 * multi : -offsetX); imageX++) {
                    color24 = this.getPixelColor(true, true, true, true, nether, caves, this.world, multi, startX, startZ, imageX, imageY);
                    this.mapImages[this.zoom].setRGB(imageX, imageY, color24);
                }
            }
        }

        if (full || this.options.heightmap && needHeightMap || needHeightAndID || this.options.lightmap && needLight || skyColorChanged) {
            for (int imageY = 32 * multi - 1; imageY >= 0; imageY--) {
                for (int imageX = 0; imageX < 32 * multi; imageX++) {
                    color24 = this.getPixelColor(
                            full, full || needHeightAndID, full, full || needLight || needHeightAndID, nether, caves, this.world, multi, startX, startZ, imageX, imageY
                    );
                    this.mapImages[this.zoom].setRGB(imageX, imageY, color24);
                }
            }
        }

        if ((full || offsetX != 0 || offsetZ != 0 || !this.lastFullscreen) && this.fullscreenMap && this.options.biomeOverlay != 0) {
            this.mapData[this.zoom].segmentBiomes();
            this.mapData[this.zoom].findCenterOfSegments(!this.options.oldNorth);
        }

        this.lastFullscreen = this.fullscreenMap;
        if (full || offsetX != 0 || offsetZ != 0 || needHeightMap || needLight || skyColorChanged) {
            this.imageChanged = true;
        }

        if (needLight || skyColorChanged) {
            this.master.getSettingsAndLightingChangeNotifier().notifyOfChanges();
        }
    }

    @Override
    public void handleChangeInWorld(int chunkX, int chunkZ) {
        this.chunkCache[this.zoom].registerChangeAt(chunkX, chunkZ);
    }

    @Override
    public void processChunk(WorldChunk chunk) {
        this.rectangleCalc(chunk.getPos().x * 16, chunk.getPos().z * 16, chunk.getPos().x * 16 + 15, chunk.getPos().z * 16 + 15);
    }

    private void rectangleCalc(int left, int top, int right, int bottom) {
        boolean nether = false;
        boolean caves = false;
        boolean netherPlayerInOpen = false;
        this.blockPos.setXYZ(this.lastX, Math.max(Math.min(GameVariableAccessShim.yCoord(), 256 - 1), 0), this.lastZ);
        if (this.game.player.dimension == DimensionType.THE_NETHER) {
            int currentY = GameVariableAccessShim.yCoord();
            netherPlayerInOpen = this.world.isSkyVisible(this.blockPos);
            nether = currentY < 126;
            if (this.options.cavesAllowed && this.options.showCaves && currentY >= 126 && !netherPlayerInOpen) {
                caves = true;
            }
        } else if (this.options.cavesAllowed && this.options.showCaves && this.world.getLightLevel(LightType.SKY, this.blockPos) <= 0) {
            caves = true;
        }

        int startX = this.lastX;
        int startZ = this.lastZ;
        int multi = (int) Math.pow(2.0, this.zoom);
        startX -= 16 * multi;
        startZ -= 16 * multi;
        left = left - startX - 1;
        right = right - startX + 1;
        top = top - startZ - 1;
        bottom = bottom - startZ + 1;
        left = Math.max(0, left);
        right = Math.min(32 * multi - 1, right);
        top = Math.max(0, top);
        bottom = Math.min(32 * multi - 1, bottom);
        int color24 = 0;

        for (int imageY = bottom; imageY >= top; imageY--) {
            for (int imageX = left; imageX <= right; imageX++) {
                color24 = this.getPixelColor(true, true, true, true, nether, caves, this.world, multi, startX, startZ, imageX, imageY);
                this.mapImages[this.zoom].setRGB(imageX, imageY, color24);
            }
        }

        this.imageChanged = true;
    }

    private int getPixelColor(
            boolean needBiome,
            boolean needHeightAndID,
            boolean needTint,
            boolean needLight,
            boolean nether,
            boolean caves,
            World world,
            int multi,
            int startX,
            int startZ,
            int imageX,
            int imageY
    ) {
        int surfaceHeight = 0;
        int seafloorHeight = -1;
        int transparentHeight = -1;
        int foliageHeight = -1;
        int surfaceColor = 0;
        int seafloorColor = 0;
        int transparentColor = 0;
        int foliageColor = 0;
        this.surfaceBlockState = null;
        this.transparentBlockState = BlockRepository.air.getDefaultState();
        this.foliageBlockState = BlockRepository.air.getDefaultState();
        this.seafloorBlockState = BlockRepository.air.getDefaultState();
        boolean surfaceBlockChangeForcedTint = false;
        boolean transparentBlockChangeForcedTint = false;
        boolean foliageBlockChangeForcedTint = false;
        boolean seafloorBlockChangeForcedTint = false;
        int surfaceBlockStateID = 0;
        int transparentBlockStateID = 0;
        int foliageBlockStateID = 0;
        int seafloorBlockStateID = 0;
        this.blockPos = this.blockPos.withXYZ(startX + imageX, 0, startZ + imageY);
        int color24 = 0;
        int biomeID = 0;
        if (needBiome) {
            if (world.isBlockLoaded(this.blockPos)) {
                biomeID = Registry.BIOME.getRawId(world.getBiome(this.blockPos));
            } else {
                biomeID = -1;
            }

            this.mapData[this.zoom].setBiomeID(imageX, imageY, biomeID);
        } else {
            biomeID = this.mapData[this.zoom].getBiomeID(imageX, imageY);
        }

        if (this.options.biomeOverlay == 1) {
            if (biomeID >= 0) {
                color24 = BiomeRepository.getBiomeColor(biomeID) | 0xFF000000;
            } else {
                color24 = 0;
            }

            return this.doSlimeAndGrid(color24, startX + imageX, startZ + imageY);
        } else {
            boolean solid = false;
            if (needHeightAndID) {
                if (!nether && !caves) {
                    WorldChunk chunk = (WorldChunk) world.getChunk(this.blockPos);
                    transparentHeight = chunk.sampleHeightmap(Type.MOTION_BLOCKING, this.blockPos.getX() & 15, this.blockPos.getZ() & 15) + 1;
                    this.transparentBlockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, transparentHeight - 1, startZ + imageY));
                    FluidState fluidState = this.transparentBlockState.getFluidState();
                    if (fluidState != Fluids.EMPTY.getDefaultState()) {
                        this.transparentBlockState = fluidState.getBlockState();
                    }

                    surfaceHeight = transparentHeight;
                    this.surfaceBlockState = this.transparentBlockState;
                    VoxelShape voxelShape = null;
                    boolean hasOpacity = this.surfaceBlockState.getOpacity(world, this.blockPos) > 0;
                    if (!hasOpacity && this.surfaceBlockState.isOpaque() && this.surfaceBlockState.hasSidedTransparency()) {
                        voxelShape = this.surfaceBlockState.getCullingFace(world, this.blockPos, Direction.DOWN);
                        hasOpacity = VoxelShapes.method_20713(voxelShape, VoxelShapes.empty());
                        voxelShape = this.surfaceBlockState.getCullingFace(world, this.blockPos, Direction.UP);
                        hasOpacity = hasOpacity || VoxelShapes.method_20713(VoxelShapes.empty(), voxelShape);
                    }

                    while (!hasOpacity && surfaceHeight > 0) {
                        this.foliageBlockState = this.surfaceBlockState;
                        surfaceHeight--;
                        this.surfaceBlockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                        fluidState = this.surfaceBlockState.getFluidState();
                        if (fluidState != Fluids.EMPTY.getDefaultState()) {
                            this.surfaceBlockState = fluidState.getBlockState();
                        }

                        hasOpacity = this.surfaceBlockState.getOpacity(world, this.blockPos) > 0;
                        if (!hasOpacity && this.surfaceBlockState.isOpaque() && this.surfaceBlockState.hasSidedTransparency()) {
                            voxelShape = this.surfaceBlockState.getCullingFace(world, this.blockPos, Direction.DOWN);
                            hasOpacity = VoxelShapes.method_20713(voxelShape, VoxelShapes.empty());
                            voxelShape = this.surfaceBlockState.getCullingFace(world, this.blockPos, Direction.UP);
                            hasOpacity = hasOpacity || VoxelShapes.method_20713(VoxelShapes.empty(), voxelShape);
                        }
                    }

                    if (surfaceHeight == transparentHeight) {
                        transparentHeight = -1;
                        this.transparentBlockState = BlockRepository.air.getDefaultState();
                        this.foliageBlockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, surfaceHeight, startZ + imageY));
                    }

                    if (this.foliageBlockState.getMaterial() == Material.SNOW) {
                        this.surfaceBlockState = this.foliageBlockState;
                        this.foliageBlockState = BlockRepository.air.getDefaultState();
                    }

                    if (this.foliageBlockState == this.transparentBlockState) {
                        this.foliageBlockState = BlockRepository.air.getDefaultState();
                    }

                    if (this.foliageBlockState != null && this.foliageBlockState.getMaterial() != Material.AIR) {
                        foliageHeight = surfaceHeight + 1;
                    } else {
                        foliageHeight = -1;
                    }

                    Material material = this.surfaceBlockState.getMaterial();
                    if (material == Material.WATER || material == Material.ICE) {
                        seafloorHeight = surfaceHeight;

                        for (this.seafloorBlockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY));
                             this.seafloorBlockState.getOpacity(world, this.blockPos) < 5
                                     && this.seafloorBlockState.getMaterial() != Material.LEAVES
                                     && seafloorHeight > 1;
                             this.seafloorBlockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY))
                        ) {
                            material = this.seafloorBlockState.getMaterial();
                            if (transparentHeight == -1 && material != Material.ICE && material != Material.WATER && material.blocksMovement()) {
                                transparentHeight = seafloorHeight;
                                this.transparentBlockState = this.seafloorBlockState;
                            }

                            if (foliageHeight == -1
                                    && seafloorHeight != transparentHeight
                                    && this.transparentBlockState != this.seafloorBlockState
                                    && material != Material.ICE
                                    && material != Material.WATER
                                    && material != Material.AIR
                                    && material != Material.BUBBLE_COLUMN) {
                                foliageHeight = seafloorHeight;
                                this.foliageBlockState = this.seafloorBlockState;
                            }

                            seafloorHeight--;
                        }

                        if (this.seafloorBlockState.getMaterial() == Material.WATER) {
                            this.seafloorBlockState = BlockRepository.air.getDefaultState();
                        }
                    }
                } else {
                    surfaceHeight = this.getNetherHeight(nether, startX + imageX, startZ + imageY);
                    this.surfaceBlockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
                    surfaceBlockStateID = BlockRepository.getStateId(this.surfaceBlockState);
                    foliageHeight = surfaceHeight + 1;
                    this.blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
                    this.foliageBlockState = world.getBlockState(this.blockPos);
                    Material material = this.foliageBlockState.getMaterial();
                    if (material != Material.SNOW && material != Material.AIR && material != Material.LAVA && material != Material.WATER) {
                        foliageBlockStateID = BlockRepository.getStateId(this.foliageBlockState);
                    } else {
                        foliageHeight = -1;
                    }
                }

                surfaceBlockStateID = BlockRepository.getStateId(this.surfaceBlockState);
                if (this.options.biomes && this.surfaceBlockState != this.mapData[this.zoom].getBlockstate(imageX, imageY)) {
                    surfaceBlockChangeForcedTint = true;
                }

                this.mapData[this.zoom].setHeight(imageX, imageY, surfaceHeight);
                this.mapData[this.zoom].setBlockstateID(imageX, imageY, surfaceBlockStateID);
                if (this.options.biomes && this.transparentBlockState != this.mapData[this.zoom].getTransparentBlockstate(imageX, imageY)) {
                    transparentBlockChangeForcedTint = true;
                }

                this.mapData[this.zoom].setTransparentHeight(imageX, imageY, transparentHeight);
                transparentBlockStateID = BlockRepository.getStateId(this.transparentBlockState);
                this.mapData[this.zoom].setTransparentBlockstateID(imageX, imageY, transparentBlockStateID);
                if (this.options.biomes && this.foliageBlockState != this.mapData[this.zoom].getFoliageBlockstate(imageX, imageY)) {
                    foliageBlockChangeForcedTint = true;
                }

                this.mapData[this.zoom].setFoliageHeight(imageX, imageY, foliageHeight);
                foliageBlockStateID = BlockRepository.getStateId(this.foliageBlockState);
                this.mapData[this.zoom].setFoliageBlockstateID(imageX, imageY, foliageBlockStateID);
                if (this.options.biomes && this.seafloorBlockState != this.mapData[this.zoom].getOceanFloorBlockstate(imageX, imageY)) {
                    seafloorBlockChangeForcedTint = true;
                }

                this.mapData[this.zoom].setOceanFloorHeight(imageX, imageY, seafloorHeight);
                seafloorBlockStateID = BlockRepository.getStateId(this.seafloorBlockState);
                this.mapData[this.zoom].setOceanFloorBlockstateID(imageX, imageY, seafloorBlockStateID);
            } else {
                surfaceHeight = this.mapData[this.zoom].getHeight(imageX, imageY);
                surfaceBlockStateID = this.mapData[this.zoom].getBlockstateID(imageX, imageY);
                this.surfaceBlockState = BlockRepository.getStateById(surfaceBlockStateID);
                transparentHeight = this.mapData[this.zoom].getTransparentHeight(imageX, imageY);
                transparentBlockStateID = this.mapData[this.zoom].getTransparentBlockstateID(imageX, imageY);
                this.transparentBlockState = BlockRepository.getStateById(transparentBlockStateID);
                foliageHeight = this.mapData[this.zoom].getFoliageHeight(imageX, imageY);
                foliageBlockStateID = this.mapData[this.zoom].getFoliageBlockstateID(imageX, imageY);
                this.foliageBlockState = BlockRepository.getStateById(foliageBlockStateID);
                seafloorHeight = this.mapData[this.zoom].getOceanFloorHeight(imageX, imageY);
                seafloorBlockStateID = this.mapData[this.zoom].getOceanFloorBlockstateID(imageX, imageY);
                this.seafloorBlockState = BlockRepository.getStateById(seafloorBlockStateID);
            }

            if (surfaceHeight == -1) {
                surfaceHeight = this.lastY + 1;
                solid = true;
            }

            if (this.surfaceBlockState.getMaterial() == Material.LAVA) {
                solid = false;
            }

            if (this.options.biomes) {
                surfaceColor = this.colorManager.getBlockColor(this.blockPos, surfaceBlockStateID, biomeID);
                int tint = -1;
                if (!needTint && !surfaceBlockChangeForcedTint) {
                    tint = this.mapData[this.zoom].getBiomeTint(imageX, imageY);
                } else {
                    tint = this.colorManager
                            .getBiomeTint(
                                    this.mapData[this.zoom],
                                    world,
                                    this.surfaceBlockState,
                                    surfaceBlockStateID,
                                    this.blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY),
                                    this.tempBlockPos,
                                    startX,
                                    startZ
                            );
                    this.mapData[this.zoom].setBiomeTint(imageX, imageY, tint);
                }

                if (tint != -1) {
                    surfaceColor = this.colorManager.colorMultiplier(surfaceColor, tint);
                }
            } else {
                surfaceColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, surfaceBlockStateID);
            }

            surfaceColor = this.applyHeight(surfaceColor, nether, caves, world, multi, startX, startZ, imageX, imageY, surfaceHeight, solid, 1);
            int light = solid ? 0 : 255;
            if (needLight) {
                light = this.getLight(surfaceColor, this.surfaceBlockState, world, startX + imageX, startZ + imageY, surfaceHeight, solid);
                this.mapData[this.zoom].setLight(imageX, imageY, light);
            } else {
                light = this.mapData[this.zoom].getLight(imageX, imageY);
            }

            if (light == 0) {
                surfaceColor = 0;
            } else if (light != 255) {
                surfaceColor = this.colorManager.colorMultiplier(surfaceColor, light);
            }

            if (this.options.waterTransparency && seafloorHeight != -1) {
                if (!this.options.biomes) {
                    seafloorColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, seafloorBlockStateID);
                } else {
                    seafloorColor = this.colorManager.getBlockColor(this.blockPos, seafloorBlockStateID, biomeID);
                    int tintx = -1;
                    if (!needTint && !seafloorBlockChangeForcedTint) {
                        tintx = this.mapData[this.zoom].getOceanFloorBiomeTint(imageX, imageY);
                    } else {
                        tintx = this.colorManager
                                .getBiomeTint(
                                        this.mapData[this.zoom],
                                        world,
                                        this.seafloorBlockState,
                                        seafloorBlockStateID,
                                        this.blockPos.withXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY),
                                        this.tempBlockPos,
                                        startX,
                                        startZ
                                );
                        this.mapData[this.zoom].setOceanFloorBiomeTint(imageX, imageY, tintx);
                    }

                    if (tintx != -1) {
                        seafloorColor = this.colorManager.colorMultiplier(seafloorColor, tintx);
                    }
                }

                seafloorColor = this.applyHeight(seafloorColor, nether, caves, world, multi, startX, startZ, imageX, imageY, seafloorHeight, solid, 0);
                int seafloorLight = 255;
                if (needLight) {
                    seafloorLight = this.getLight(seafloorColor, this.seafloorBlockState, world, startX + imageX, startZ + imageY, seafloorHeight, solid);
                    this.blockPos.setXYZ(startX + imageX, seafloorHeight, startZ + imageY);
                    BlockState blockStateAbove = world.getBlockState(this.blockPos);
                    Material materialAbove = blockStateAbove.getMaterial();
                    if (this.options.lightmap && materialAbove == Material.ICE) {
                        int multiplier = 255;
                        if (this.game.options.ao == AoOption.MIN) {
                            multiplier = 200;
                        } else if (this.game.options.ao == AoOption.MAX) {
                            multiplier = 120;
                        }

                        seafloorLight = this.colorManager.colorMultiplier(seafloorLight, 0xFF000000 | multiplier << 16 | multiplier << 8 | multiplier);
                    }

                    this.mapData[this.zoom].setOceanFloorLight(imageX, imageY, seafloorLight);
                } else {
                    seafloorLight = this.mapData[this.zoom].getOceanFloorLight(imageX, imageY);
                }

                if (seafloorLight == 0) {
                    seafloorColor = 0;
                } else if (seafloorLight != 255) {
                    seafloorColor = this.colorManager.colorMultiplier(seafloorColor, seafloorLight);
                }
            }

            if (this.options.blockTransparency) {
                if (transparentHeight != -1 && this.transparentBlockState != null && this.transparentBlockState != BlockRepository.air.getDefaultState()) {
                    if (this.options.biomes) {
                        transparentColor = this.colorManager.getBlockColor(this.blockPos, transparentBlockStateID, biomeID);
                        int tintxx = -1;
                        if (!needTint && !transparentBlockChangeForcedTint) {
                            tintxx = this.mapData[this.zoom].getTransparentBiomeTint(imageX, imageY);
                        } else {
                            tintxx = this.colorManager
                                    .getBiomeTint(
                                            this.mapData[this.zoom],
                                            world,
                                            this.transparentBlockState,
                                            transparentBlockStateID,
                                            this.blockPos.withXYZ(startX + imageX, transparentHeight - 1, startZ + imageY),
                                            this.tempBlockPos,
                                            startX,
                                            startZ
                                    );
                            this.mapData[this.zoom].setTransparentBiomeTint(imageX, imageY, tintxx);
                        }

                        if (tintxx != -1) {
                            transparentColor = this.colorManager.colorMultiplier(transparentColor, tintxx);
                        }
                    } else {
                        transparentColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, transparentBlockStateID);
                    }

                    transparentColor = this.applyHeight(transparentColor, nether, caves, world, multi, startX, startZ, imageX, imageY, transparentHeight, solid, 3);
                    int transparentLight = 255;
                    if (needLight) {
                        transparentLight = this.getLight(
                                transparentColor, this.transparentBlockState, world, startX + imageX, startZ + imageY, transparentHeight, solid
                        );
                        this.mapData[this.zoom].setTransparentLight(imageX, imageY, transparentLight);
                    } else {
                        transparentLight = this.mapData[this.zoom].getTransparentLight(imageX, imageY);
                    }

                    if (transparentLight == 0) {
                        transparentColor = 0;
                    } else if (transparentLight != 255) {
                        transparentColor = this.colorManager.colorMultiplier(transparentColor, transparentLight);
                    }
                }

                if (foliageHeight != -1 && this.foliageBlockState != null && this.foliageBlockState != BlockRepository.air.getDefaultState()) {
                    if (!this.options.biomes) {
                        foliageColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, foliageBlockStateID);
                    } else {
                        foliageColor = this.colorManager.getBlockColor(this.blockPos, foliageBlockStateID, biomeID);
                        int tintxxx = -1;
                        if (!needTint && !foliageBlockChangeForcedTint) {
                            tintxxx = this.mapData[this.zoom].getFoliageBiomeTint(imageX, imageY);
                        } else {
                            tintxxx = this.colorManager
                                    .getBiomeTint(
                                            this.mapData[this.zoom],
                                            world,
                                            this.foliageBlockState,
                                            foliageBlockStateID,
                                            this.blockPos.withXYZ(startX + imageX, foliageHeight - 1, startZ + imageY),
                                            this.tempBlockPos,
                                            startX,
                                            startZ
                                    );
                            this.mapData[this.zoom].setFoliageBiomeTint(imageX, imageY, tintxxx);
                        }

                        if (tintxxx != -1) {
                            foliageColor = this.colorManager.colorMultiplier(foliageColor, tintxxx);
                        }
                    }

                    foliageColor = this.applyHeight(foliageColor, nether, caves, world, multi, startX, startZ, imageX, imageY, foliageHeight, solid, 2);
                    int foliageLight = 255;
                    if (needLight) {
                        foliageLight = this.getLight(foliageColor, this.foliageBlockState, world, startX + imageX, startZ + imageY, foliageHeight, solid);
                        this.mapData[this.zoom].setFoliageLight(imageX, imageY, foliageLight);
                    } else {
                        foliageLight = this.mapData[this.zoom].getFoliageLight(imageX, imageY);
                    }

                    if (foliageLight == 0) {
                        foliageColor = 0;
                    } else if (foliageLight != 255) {
                        foliageColor = this.colorManager.colorMultiplier(foliageColor, foliageLight);
                    }
                }
            }

            if (seafloorColor != 0 && seafloorHeight > 0) {
                color24 = seafloorColor;
                if (foliageColor != 0 && foliageHeight <= surfaceHeight) {
                    color24 = this.colorManager.colorAdder(foliageColor, color24);
                }

                if (transparentColor != 0 && transparentHeight <= surfaceHeight) {
                    color24 = this.colorManager.colorAdder(transparentColor, color24);
                }

                color24 = this.colorManager.colorAdder(surfaceColor, color24);
            } else {
                color24 = surfaceColor;
            }

            if (foliageColor != 0 && foliageHeight > surfaceHeight) {
                color24 = this.colorManager.colorAdder(foliageColor, color24);
            }

            if (transparentColor != 0 && transparentHeight > surfaceHeight) {
                color24 = this.colorManager.colorAdder(transparentColor, color24);
            }

            if (this.options.biomeOverlay == 2) {
                int bc = 0;
                if (biomeID >= 0) {
                    bc = BiomeRepository.getBiomeColor(biomeID);
                }

                bc = 2130706432 | bc;
                color24 = this.colorManager.colorAdder(bc, color24);
            }

            return this.doSlimeAndGrid(color24, startX + imageX, startZ + imageY);
        }
    }

    private int doSlimeAndGrid(int color24, int mcX, int mcZ) {
        if (this.options.slimeChunks && !this.master.getWorldSeed().equals("")) {
            int xPosition = mcX >> 4;
            int zPosition = mcZ >> 4;
            String seedString = this.master.getWorldSeed();
            long seed = 0L;

            try {
                seed = Long.parseLong(seedString);
            } catch (NumberFormatException e) {
                seed = seedString.hashCode();
            }

            Random random = new Random(
                    seed + (long) xPosition * xPosition * 4987142 + xPosition * 5947611L + zPosition * zPosition * 4392871L + zPosition * 389711L ^ 987234911L
            );
            if (random.nextInt(10) == 0) {
                color24 = this.colorManager.colorAdder(2097217280, color24);
            }
        }

        if (this.options.chunkGrid) {
            if (mcX % 512 == 0 || mcZ % 512 == 0) {
                color24 = this.colorManager.colorAdder(2113863680, color24);
            } else if (mcX % 16 == 0 || mcZ % 16 == 0) {
                color24 = this.colorManager.colorAdder(2097152000, color24);
            }
        }

        return color24;
    }

    private final int getBlockHeight(boolean nether, boolean caves, World world, int x, int z) {
        int playerHeight = GameVariableAccessShim.yCoord();
        this.blockPos.setXYZ(x, playerHeight, z);
        WorldChunk chunk = (WorldChunk) world.getChunk(this.blockPos);
        int height = chunk.sampleHeightmap(Type.MOTION_BLOCKING, this.blockPos.getX() & 15, this.blockPos.getZ() & 15) + 1;
        BlockState blockState = world.getBlockState(this.blockPos.withXYZ(x, height - 1, z));
        FluidState fluidState = this.transparentBlockState.getFluidState();
        if (fluidState != Fluids.EMPTY.getDefaultState()) {
            blockState = fluidState.getBlockState();
        }

        while (blockState.getOpacity(world, this.blockPos) == 0 && height > 0) {
            height--;
            blockState = world.getBlockState(this.blockPos.withXYZ(x, height - 1, z));
            fluidState = this.surfaceBlockState.getFluidState();
            if (fluidState != Fluids.EMPTY.getDefaultState()) {
                blockState = fluidState.getBlockState();
            }
        }

        return (nether || caves) && height > playerHeight ? this.getNetherHeight(nether, x, z) : height;
    }

    private int getNetherHeight(boolean nether, int x, int z) {
        int y = this.lastY;
        this.blockPos.setXYZ(x, y, z);
        BlockState blockState = this.world.getBlockState(this.blockPos);
        if (blockState.getOpacity(this.world, this.blockPos) == 0 && blockState.getMaterial() != Material.LAVA) {
            while (y > 0) {
                this.blockPos.setXYZ(x, --y, z);
                blockState = this.world.getBlockState(this.blockPos);
                if (blockState.getOpacity(this.world, this.blockPos) > 0 || blockState.getMaterial() == Material.LAVA) {
                    return y + 1;
                }
            }

            return y;
        } else {
            while (y <= this.lastY + 10 && y < (nether ? 127 : 256)) {
                this.blockPos.setXYZ(x, ++y, z);
                blockState = this.world.getBlockState(this.blockPos);
                if (blockState.getOpacity(this.world, this.blockPos) == 0 && blockState.getMaterial() != Material.LAVA) {
                    return y;
                }
            }

            return -1;
        }
    }

    private final int getSeafloorHeight(World world, int x, int z, int height) {
        WorldChunk chunk = (WorldChunk) world.getChunk(this.blockPos);

        for (BlockState blockState = world.getBlockState(this.blockPos.withXYZ(x, height - 1, z));
             blockState.getOpacity(world, this.blockPos) < 5 && blockState.getMaterial() != Material.LEAVES && height > 1;
             blockState = world.getBlockState(this.blockPos.withXYZ(x, height - 1, z))
        ) {
            height--;
        }

        return height;
    }

    private final int getTransparentHeight(boolean nether, boolean caves, World world, int x, int z, int height) {
        int transHeight = -1;
        if (!caves && !nether) {
            transHeight = world.getTopPosition(Type.MOTION_BLOCKING, this.blockPos.withXYZ(x, height, z)).getY();
            if (transHeight <= height) {
                transHeight = -1;
            }
        } else {
            transHeight = -1;
        }

        BlockState blockState = world.getBlockState(this.blockPos.withXYZ(x, transHeight - 1, z));
        Material material = blockState.getMaterial();
        if (transHeight == height + 1 && material == Material.SNOW) {
            transHeight = -1;
        }

        if (material == Material.BARRIER) {
            transHeight++;
            blockState = world.getBlockState(this.blockPos.withXYZ(x, transHeight - 1, z));
            material = blockState.getMaterial();
            if (material == Material.AIR) {
                transHeight = -1;
            }
        }

        return transHeight;
    }

    private int applyHeight(
            int color24, boolean nether, boolean caves, World world, int multi, int startX, int startZ, int imageX, int imageY, int height, boolean solid, int layer
    ) {
        if (color24 != this.colorManager.getAirColor() && color24 != 0 && (this.options.heightmap || this.options.slopemap) && !solid) {
            int heightComp = -1;
            int diff = 0;
            double sc = 0.0;
            if (!this.options.slopemap) {
                if (this.options.heightmap) {
                    diff = height - this.lastY;
                    sc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 1.8;
                    if (diff < 0) {
                        sc = 0.0 - sc;
                    }
                }
            } else {
                if (imageX > 0 && imageY < 32 * multi - 1) {
                    if (layer == 0) {
                        heightComp = this.mapData[this.zoom].getOceanFloorHeight(imageX - 1, imageY + 1);
                    }

                    if (layer == 1) {
                        heightComp = this.mapData[this.zoom].getHeight(imageX - 1, imageY + 1);
                    }

                    if (layer == 2) {
                        heightComp = height;
                    }

                    if (layer == 3) {
                        heightComp = this.mapData[this.zoom].getTransparentHeight(imageX - 1, imageY + 1);
                        if (heightComp == -1) {
                            Block block = BlockRepository.getStateById(this.mapData[this.zoom].getTransparentBlockstateID(imageX, imageY)).getBlock();
                            if (block instanceof GlassBlock || block instanceof StainedGlassBlock) {
                                heightComp = this.mapData[this.zoom].getHeight(imageX - 1, imageY + 1);
                            }
                        }
                    }
                } else {
                    if (layer == 0) {
                        int baseHeight = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                        heightComp = this.getSeafloorHeight(world, startX + imageX - 1, startZ + imageY + 1, baseHeight);
                    }

                    if (layer == 1) {
                        heightComp = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                    }

                    if (layer == 2) {
                        heightComp = height;
                    }

                    if (layer == 3) {
                        int baseHeight = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
                        heightComp = this.getTransparentHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1, baseHeight);
                        if (heightComp == -1) {
                            BlockState blockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, height - 1, startZ + imageY));
                            Block block = blockState.getBlock();
                            if (block instanceof GlassBlock || block instanceof StainedGlassBlock) {
                                heightComp = baseHeight;
                            }
                        }
                    }
                }

                if (heightComp == -1) {
                    heightComp = height;
                }

                diff = heightComp - height;
                if (diff != 0) {
                    sc = diff > 0 ? 1.0 : (diff < 0 ? -1.0 : 0.0);
                    sc /= 8.0;
                }

                if (this.options.heightmap) {
                    diff = height - this.lastY;
                    double heightsc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 3.0;
                    sc = diff > 0 ? sc + heightsc : sc - heightsc;
                }
            }

            int alpha = color24 >> 24 & 0xFF;
            int r = color24 >> 16 & 0xFF;
            int g = color24 >> 8 & 0xFF;
            int b = color24 >> 0 & 0xFF;
            if (sc > 0.0) {
                r += (int) (sc * (255 - r));
                g += (int) (sc * (255 - g));
                b += (int) (sc * (255 - b));
            } else if (sc < 0.0) {
                sc = Math.abs(sc);
                r -= (int) (sc * r);
                g -= (int) (sc * g);
                b -= (int) (sc * b);
            }

            color24 = alpha * 16777216 + r * 65536 + g * 256 + b;
        }

        return color24;
    }

    private int getLight(int color24, BlockState blockState, World world, int x, int z, int height, boolean solid) {
        int i3 = 255;
        if (solid) {
            i3 = 0;
        } else if (color24 != this.colorManager.getAirColor() && color24 != 0 && this.options.lightmap) {
            this.blockPos.setXYZ(x, Math.max(Math.min(height, 256 - 1), 0), z);
            int blockLight = world.getLightLevel(LightType.BLOCK, this.blockPos);
            int skyLight = world.getLightLevel(LightType.SKY, this.blockPos);
            if (blockState.getMaterial() == Material.LAVA || blockState.getBlock() == Blocks.MAGMA_BLOCK) {
                blockLight = 14;
            }

            i3 = this.lightmapColors[blockLight + skyLight * 16];
        }

        return i3;
    }

    private void renderMap(int x, int y, int scScale) {
        float scale = 1.0F;
        if (this.options.squareMap && this.options.rotates) {
            scale = 1.4142F;
        }

        if (GLUtils.hasAlphaBits) {
        }

        if (GLUtils.fboEnabled) {
            GLShim.glBindTexture(3553, 0);
            GLShim.glPushAttrib(4096);
            GLShim.glViewport(0, 0, GLUtils.fboSize, GLUtils.fboSize);
            GLShim.glMatrixMode(5889);
            GLShim.glPushMatrix();
            GLShim.glLoadIdentity();
            GLShim.glOrtho(0.0, GLUtils.fboSize, GLUtils.fboSize, 0.0, 1000.0, 3000.0);
            GLShim.glMatrixMode(5888);
            GLShim.glPushMatrix();
            GLShim.glLoadIdentity();
            GLShim.glTranslatef(0.0F, 0.0F, -2000.0F);
            GLUtils.bindFrameBuffer();
            GLShim.glDepthMask(false);
            GLShim.glDisable(2929);
            GLShim.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
            GLShim.glClear(16384);
            GLShim.glBlendFunc(770, 0);
            GLUtils.img(new Identifier("voxelmap", this.options.squareMap ? "images/square.png" : "images/circle.png"));
            GLUtils.drawPre();
            GLUtils.ldrawthree(GLUtils.fboRad - GLUtils.fboRad / scale, GLUtils.fboRad + GLUtils.fboRad / scale, 1.0, 0.0, 0.0);
            GLUtils.ldrawthree(GLUtils.fboRad + GLUtils.fboRad / scale, GLUtils.fboRad + GLUtils.fboRad / scale, 1.0, 1.0, 0.0);
            GLUtils.ldrawthree(GLUtils.fboRad + GLUtils.fboRad / scale, GLUtils.fboRad - GLUtils.fboRad / scale, 1.0, 1.0, 1.0);
            GLUtils.ldrawthree(GLUtils.fboRad - GLUtils.fboRad / scale, GLUtils.fboRad - GLUtils.fboRad / scale, 1.0, 0.0, 1.0);
            GLUtils.drawPost();
            GLShim.glBlendFuncSeparate(1, 0, 774, 0);
            synchronized (this.coordinateLock) {
                if (this.imageChanged) {
                    this.imageChanged = false;
                    this.mapImages[this.zoom].write();
                    this.lastImageX = this.lastX;
                    this.lastImageZ = this.lastZ;
                }
            }

            float multi = (float) (1.0 / this.zoomScale);
            this.percentX = (float) (GameVariableAccessShim.xCoordDouble() - this.lastImageX);
            this.percentY = (float) (GameVariableAccessShim.zCoordDouble() - this.lastImageZ);
            this.percentX *= multi;
            this.percentY *= multi;
            GLUtils.disp(this.mapImages[this.zoom].getIndex());
            GLShim.glTexParameteri(3553, 10241, 9987);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLShim.glTranslatef(GLUtils.fboRad, GLUtils.fboRad, 0.0F);
            if (!this.options.rotates) {
                GLShim.glRotatef(-this.northRotate, 0.0F, 0.0F, 1.0F);
            } else {
                GLShim.glRotatef(this.direction, 0.0F, 0.0F, 1.0F);
            }

            GLShim.glTranslatef(-GLUtils.fboRad, -GLUtils.fboRad, 0.0F);
            GLShim.glTranslatef(-this.percentX * GLUtils.fboSize / 64.0F, this.percentY * GLUtils.fboSize / 64.0F, 0.0F);
            GLUtils.drawPre();
            GLUtils.ldrawthree(0.0, GLUtils.fboSize, 1.0, 0.0, 0.0);
            GLUtils.ldrawthree(GLUtils.fboSize, GLUtils.fboSize, 1.0, 1.0, 0.0);
            GLUtils.ldrawthree(GLUtils.fboSize, 0.0, 1.0, 1.0, 1.0);
            GLUtils.ldrawthree(0.0, 0.0, 1.0, 0.0, 1.0);
            GLUtils.drawPost();
            GLShim.glDepthMask(true);
            GLShim.glEnable(2929);
            GLUtils.unbindFrameBuffer();
            GLShim.glMatrixMode(5889);
            GLShim.glPopMatrix();
            GLShim.glMatrixMode(5888);
            GLShim.glPopMatrix();
            GLShim.glPopAttrib();
            GLShim.glViewport(0, 0, this.game.window.getFramebufferWidth(), this.game.window.getFramebufferHeight());
            GLShim.glPushMatrix();
            GLShim.glBlendFunc(770, 0);
            GLShim.glEnable(3008);
            GLUtils.disp(GLUtils.fboTextureID);
        } else {
            if (this.imageChanged) {
                this.imageChanged = false;
                if (this.options.squareMap) {
                    synchronized (this.coordinateLock) {
                        this.mapImages[this.zoom].write();
                        this.lastImageX = this.lastX;
                        this.lastImageZ = this.lastZ;
                    }
                } else {
                    int diameter = this.mapImages[this.zoom].getWidth();
                    if (this.roundImage != null) {
                        this.roundImage.baleet();
                    }

                    this.roundImage = new LiveScaledGLBufferedImage(diameter, diameter, 6);
                    Double ellipse = new Double(
                            Math.pow(2.0, this.zoom) / 2.0 + 1.0,
                            Math.pow(2.0, this.zoom) / 2.0 + 1.0,
                            diameter - Math.pow(2.0, this.zoom) - 1.0,
                            diameter - Math.pow(2.0, this.zoom) - 1.0
                    );
                    Graphics2D gfx = this.roundImage.createGraphics();
                    gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    gfx.setClip(ellipse);
                    gfx.setColor(new Color(0.1F, 0.0F, 0.0F, 0.1F));
                    gfx.fillRect(0, 0, diameter, diameter);
                    synchronized (this.coordinateLock) {
                        gfx.drawImage(this.mapImages[this.zoom], 0, 0, null);
                        this.lastImageX = this.lastX;
                        this.lastImageZ = this.lastZ;
                    }

                    gfx.dispose();
                    this.roundImage.write();
                }
            }

            float multi = (float) (1.0 / this.zoomScaleAdjusted);
            this.percentX = (float) (GameVariableAccessShim.xCoordDouble() - this.lastImageX);
            this.percentY = (float) (GameVariableAccessShim.zCoordDouble() - this.lastImageZ);
            this.percentX *= multi;
            this.percentY *= multi;
            GLShim.glBlendFunc(770, 0);
            GLUtils.disp(this.options.squareMap ? this.mapImages[this.zoom].getIndex() : this.roundImage.getIndex());
            if (GLUtils.openGL14Enabled) {
                GLShim.glTexParameteri(3553, 10241, 9987);
            } else {
                GLShim.glTexParameteri(3553, 10241, 9729);
            }

            GLShim.glTexParameteri(3553, 10240, 9729);
            GLShim.glPushMatrix();
            GLShim.glTranslatef(x, y, 0.0F);
            GLShim.glRotatef(!this.options.rotates ? this.northRotate : -this.direction, 0.0F, 0.0F, 1.0F);
            GLShim.glTranslatef(-x, -y, 0.0F);
            GLShim.glTranslatef(-this.percentX, -this.percentY, 0.0F);
        }

        double guiScale = (double) this.game.window.getFramebufferWidth() / this.scWidth;
        GLShim.glEnable(3089);
        GLShim.glScissor((int) (guiScale * (x - 32)), (int) (guiScale * (this.scHeight - y - 32.0)), (int) (guiScale * 64.0), (int) (guiScale * 63.0));
        GLUtils.drawPre();
        GLUtils.setMapWithScale(x, y, scale);
        GLUtils.drawPost();
        GLShim.glDisable(3089);
        GLShim.glPopMatrix();
        GLShim.glBlendFunc(770, 771);
        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if (this.options.squareMap) {
            this.drawSquareMapFrame(x, y);
        } else {
            this.drawRoundMapFrame(x, y);
        }

        double lastXDouble = GameVariableAccessShim.xCoordDouble();
        double lastZDouble = GameVariableAccessShim.zCoordDouble();
        TextureAtlas textureAtlas = this.master.getWaypointManager().getTextureAtlas();
        GLUtils.disp(textureAtlas.getGlId());
        Waypoint highlightedPoint = this.waypointManager.getHighlightedWaypoint();

        for (Waypoint pt : this.waypointManager.getWaypoints()) {
            if (pt.isActive() || pt == highlightedPoint) {
                double distanceSq = pt.getDistanceSqToEntity(this.game.getCameraEntity());
                if (distanceSq < this.options.maxWaypointDisplayDistance * this.options.maxWaypointDisplayDistance
                        || this.options.maxWaypointDisplayDistance < 0
                        || pt == highlightedPoint) {
                    this.drawWaypoint(pt, textureAtlas, x, y, scScale, lastXDouble, lastZDouble, null, null, null, null);
                }
            }
        }

        if (highlightedPoint != null) {
            this.drawWaypoint(
                    highlightedPoint,
                    textureAtlas,
                    x,
                    y,
                    scScale,
                    lastXDouble,
                    lastZDouble,
                    textureAtlas.getAtlasSprite("voxelmap:images/waypoints/target.png"),
                    1.0F,
                    0.0F,
                    0.0F
            );
        }

        GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void drawWaypoint(
            Waypoint pt, TextureAtlas textureAtlas, int x, int y, int scScale, double lastXDouble, double lastZDouble, Sprite icon, Float r, Float g, Float b
    ) {
        boolean uprightIcon = icon != null;
        if (r == null) {
            r = pt.red;
        }

        if (g == null) {
            g = pt.green;
        }

        if (b == null) {
            b = pt.blue;
        }

        double wayX = lastXDouble - pt.getX() - 0.5;
        double wayY = lastZDouble - pt.getZ() - 0.5;
        float locate = (float) Math.toDegrees(Math.atan2(wayX, wayY));
        double hypot = Math.sqrt(wayX * wayX + wayY * wayY);
        boolean far = false;
        if (this.options.rotates) {
            locate += this.direction;
        } else {
            locate -= this.northRotate;
        }

        hypot /= this.zoomScaleAdjusted;
        if (this.options.squareMap) {
            double radLocate = Math.toRadians(locate);
            double dispX = hypot * Math.cos(radLocate);
            double dispY = hypot * Math.sin(radLocate);
            far = Math.abs(dispX) > 28.5 || Math.abs(dispY) > 28.5;
            if (far) {
                hypot = hypot / Math.max(Math.abs(dispX), Math.abs(dispY)) * 30.0;
            }
        } else {
            far = hypot >= 31.0;
            if (far) {
                hypot = 34.0;
            }
        }

        boolean target = false;
        if (far) {
            try {
                if (icon == null) {
                    if (scScale >= 3) {
                        icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/marker" + pt.imageSuffix + ".png");
                    } else {
                        icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/marker" + pt.imageSuffix + "Small.png");
                    }

                    if (icon == textureAtlas.getMissingImage()) {
                        if (scScale >= 3) {
                            icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/marker.png");
                        } else {
                            icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/markerSmall.png");
                        }
                    }
                } else {
                    target = true;
                }

                GLShim.glPushMatrix();
                GLShim.glColor4f(r, g, b, !pt.enabled && !target ? 0.3F : 1.0F);
                GLShim.glTexParameteri(3553, 10241, 9729);
                GLShim.glTexParameteri(3553, 10240, 9729);
                GLShim.glTranslatef(x, y, 0.0F);
                GLShim.glRotatef(-locate, 0.0F, 0.0F, 1.0F);
                if (uprightIcon) {
                    GLShim.glTranslated(0.0, -hypot, 0.0);
                    GLShim.glRotatef(locate, 0.0F, 0.0F, 1.0F);
                    GLShim.glTranslatef(-x, -y, 0.0F);
                } else {
                    GLShim.glTranslatef(-x, -y, 0.0F);
                    GLShim.glTranslated(0.0, -hypot, 0.0);
                }

                GLUtils.drawPre();
                GLUtils.setMap(icon, x, y, 16.0F);
                GLUtils.drawPost();
            } catch (Exception localException) {
                this.error = "Error: marker overlay not found!";
            } finally {
                GLShim.glPopMatrix();
            }
        } else {
            try {
                if (icon == null) {
                    if (scScale >= 3) {
                        icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/waypoint" + pt.imageSuffix + ".png");
                    } else {
                        icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/waypoint" + pt.imageSuffix + "Small.png");
                    }

                    if (icon == textureAtlas.getMissingImage()) {
                        if (scScale >= 3) {
                            icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/waypoint.png");
                        } else {
                            icon = textureAtlas.getAtlasSprite("voxelmap:images/waypoints/waypointSmall.png");
                        }
                    }
                } else {
                    target = true;
                }

                GLShim.glPushMatrix();
                GLShim.glColor4f(r, g, b, !pt.enabled && !target ? 0.3F : 1.0F);
                GLShim.glTexParameteri(3553, 10241, 9729);
                GLShim.glTexParameteri(3553, 10240, 9729);
                GLShim.glRotatef(-locate, 0.0F, 0.0F, 1.0F);
                GLShim.glTranslated(0.0, -hypot, 0.0);
                GLShim.glRotatef(-(-locate), 0.0F, 0.0F, 1.0F);
                GLUtils.drawPre();
                GLUtils.setMap(icon, x, y, 16.0F);
                GLUtils.drawPost();
            } catch (Exception localException) {
                this.error = "Error: waypoint overlay not found!";
            } finally {
                GLShim.glPopMatrix();
            }
        }
    }

    private void drawArrow(int x, int y) {
        try {
            GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            GLShim.glBlendFunc(770, 771);
            GLShim.glPushMatrix();
            GLUtils.img(new Identifier("voxelmap", "images/mmarrow.png"));
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLShim.glTranslatef(x, y, 0.0F);
            GLShim.glRotatef(this.options.rotates && !this.fullscreenMap ? 0.0F : this.direction + this.northRotate, 0.0F, 0.0F, 1.0F);
            GLShim.glTranslatef(-x, -y, 0.0F);
            GLUtils.drawPre();
            GLUtils.setMap(x, y, 16);
            GLUtils.drawPost();
        } catch (Exception localException) {
            this.error = "Error: minimap arrow not found!";
        } finally {
            GLShim.glPopMatrix();
        }
    }

    private void renderMapFull(int scWidth, int scHeight) {
        synchronized (this.coordinateLock) {
            if (this.imageChanged) {
                this.imageChanged = false;
                this.mapImages[this.zoom].write();
                this.lastImageX = this.lastX;
                this.lastImageZ = this.lastZ;
            }
        }

        GLUtils.disp(this.mapImages[this.zoom].getIndex());
        if (GLUtils.openGL14Enabled) {
            GLShim.glTexParameteri(3553, 10241, 9987);
        } else {
            GLShim.glTexParameteri(3553, 10241, 9729);
        }

        GLShim.glTexParameteri(3553, 10240, 9729);
        GLShim.glPushMatrix();
        GLShim.glTranslatef(scWidth / 2.0F, scHeight / 2.0F, 0.0F);
        GLShim.glRotatef(this.northRotate, 0.0F, 0.0F, 1.0F);
        GLShim.glTranslatef(-(scWidth / 2.0F), -(scHeight / 2.0F), 0.0F);
        GLUtils.drawPre();
        int left = scWidth / 2 - 128;
        int top = scHeight / 2 - 128;
        GLUtils.ldrawone(left, top + 256, 160.0, 0.0, 1.0);
        GLUtils.ldrawone(left + 256, top + 256, 160.0, 1.0, 1.0);
        GLUtils.ldrawone(left + 256, top, 160.0, 1.0, 0.0);
        GLUtils.ldrawone(left, top, 160.0, 0.0, 0.0);
        GLUtils.drawPost();
        GLShim.glPopMatrix();
        if (this.options.biomeOverlay != 0) {
            double factor = Math.pow(2.0, 3 - this.zoom);
            int minimumSize = (int) Math.pow(2.0, this.zoom);
            minimumSize *= minimumSize;
            ArrayList<AbstractMapData.BiomeLabel> labels = this.mapData[this.zoom].getBiomeLabels();
            GLShim.glDisable(2929);

            for (int t = 0; t < labels.size(); t++) {
                AbstractMapData.BiomeLabel label = labels.get(t);
                if (label.segmentSize > minimumSize) {
                    String name = label.name;
                    int nameWidth = this.chkLen(name);
                    float x = (float) (label.x * factor);
                    float z = (float) (label.z * factor);
                    if (this.options.oldNorth) {
                        this.write(name, left + 256 - z - nameWidth / 2, top + x - 3.0F, 16777215);
                    } else {
                        this.write(name, left + x - nameWidth / 2, top + z - 3.0F, 16777215);
                    }
                }
            }

            GLShim.glEnable(2929);
        }
    }

    private void drawSquareMapFrame(int x, int y) {
        try {
            GLUtils.disp(this.mapImageInt);
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLShim.glTexParameteri(3553, 10242, 10496);
            GLShim.glTexParameteri(3553, 10243, 10496);
            GLUtils.drawPre();
            GLUtils.setMap(x, y, 128);
            GLUtils.drawPost();
        } catch (Exception localException) {
            this.error = "error: minimap overlay not found!";
        }
    }

    private void loadMapImage() {
        if (this.mapImageInt != -1) {
            GLUtils.glah(this.mapImageInt);
        }

        try {
            InputStream is = this.game.getResourceManager().getResource(new Identifier("voxelmap", "images/squaremap.png")).getInputStream();
            BufferedImage mapImage = ImageIO.read(is);
            is.close();
            this.mapImageInt = GLUtils.tex(mapImage);
        } catch (Exception e) {
            try {
                InputStream isx = this.game.getResourceManager().getResource(new Identifier("textures/map/map_background.png")).getInputStream();
                Image tpMap = ImageIO.read(isx);
                isx.close();
                BufferedImage mapImagex = new BufferedImage(tpMap.getWidth(null), tpMap.getHeight(null), 2);
                Graphics2D gfx = mapImagex.createGraphics();
                if (!GLUtils.fboEnabled && !GLUtils.hasAlphaBits) {
                    gfx.setColor(Color.DARK_GRAY);
                    gfx.fillRect(0, 0, mapImagex.getWidth(), mapImagex.getHeight());
                }

                gfx.drawImage(tpMap, 0, 0, null);
                int border = mapImagex.getWidth() * 8 / 128;
                gfx.setComposite(AlphaComposite.Clear);
                gfx.fillRect(border, border, mapImagex.getWidth() - border * 2, mapImagex.getHeight() - border * 2);
                gfx.dispose();
                this.mapImageInt = GLUtils.tex(mapImagex);
            } catch (Exception f) {
                System.err.println("Error loading texture pack's map image: " + f.getLocalizedMessage());
            }
        }
    }

    private void drawRoundMapFrame(int x, int y) {
        try {
            GLUtils.img(new Identifier("voxelmap", "images/roundmap.png"));
            GLShim.glTexParameteri(3553, 10241, 9729);
            GLShim.glTexParameteri(3553, 10240, 9729);
            GLUtils.drawPre();
            GLUtils.setMap(x, y, 128);
            GLUtils.drawPost();
        } catch (Exception localException) {
            this.error = "Error: minimap overlay not found!";
        }
    }

    private void drawDirections(int x, int y) {
        boolean unicode = false;
        float scale = unicode ? 1.0F : 0.5F;
        float rotate;
        if (this.options.rotates) {
            rotate = -this.direction - 90.0F - this.northRotate;
        } else {
            rotate = -90.0F;
        }

        float distance;
        if (this.options.squareMap) {
            if (this.options.rotates) {
                float tempdir = this.direction % 90.0F;
                tempdir = 45.0F - Math.abs(45.0F - tempdir);
                distance = (float) (33.5 / scale / Math.cos(Math.toRadians(tempdir)));
            } else {
                distance = 33.5F / scale;
            }
        } else {
            distance = 32.0F / scale;
        }

        GLShim.glPushMatrix();
        GLShim.glScalef(scale, scale, 1.0F);
        GLShim.glTranslated(distance * Math.sin(Math.toRadians(-(rotate - 90.0))), distance * Math.cos(Math.toRadians(-(rotate - 90.0))), 0.0);
        this.write("N", x / scale - 2.0F, y / scale - 4.0F, 16777215);
        GLShim.glPopMatrix();
        GLShim.glPushMatrix();
        GLShim.glScalef(scale, scale, 1.0F);
        GLShim.glTranslated(distance * Math.sin(Math.toRadians(-rotate)), distance * Math.cos(Math.toRadians(-rotate)), 0.0);
        this.write("E", x / scale - 2.0F, y / scale - 4.0F, 16777215);
        GLShim.glPopMatrix();
        GLShim.glPushMatrix();
        GLShim.glScalef(scale, scale, 1.0F);
        GLShim.glTranslated(distance * Math.sin(Math.toRadians(-(rotate + 90.0))), distance * Math.cos(Math.toRadians(-(rotate + 90.0))), 0.0);
        this.write("S", x / scale - 2.0F, y / scale - 4.0F, 16777215);
        GLShim.glPopMatrix();
        GLShim.glPushMatrix();
        GLShim.glScalef(scale, scale, 1.0F);
        GLShim.glTranslated(distance * Math.sin(Math.toRadians(-(rotate + 180.0))), distance * Math.cos(Math.toRadians(-(rotate + 180.0))), 0.0);
        this.write("W", x / scale - 2.0F, y / scale - 4.0F, 16777215);
        GLShim.glPopMatrix();
    }

    private void showCoords(int x, int y) {
        int textStart;
        if (y > this.scHeight - 37 - 32 - 4 - 15) {
            textStart = y - 32 - 4 - 9;
        } else {
            textStart = y + 32 + 4;
        }

        if (!this.options.hide && !this.fullscreenMap) {
            boolean unicode = false;
            float scale = unicode ? 1.0F : 0.5F;
            GLShim.glPushMatrix();
            GLShim.glScalef(scale, scale, 1.0F);
            String xy = this.dCoord(GameVariableAccessShim.xCoord()) + ", " + this.dCoord(GameVariableAccessShim.zCoord());
            int m = this.chkLen(xy) / 2;
            this.write(xy, x / scale - m, textStart / scale, 16777215);
            xy = Integer.toString(GameVariableAccessShim.yCoord());
            m = this.chkLen(xy) / 2;
            this.write(xy, x / scale - m, textStart / scale + 10.0F, 16777215);
            if (this.ztimer > 0) {
                m = this.chkLen(this.error) / 2;
                this.write(this.error, x / scale - m, textStart / scale + 19.0F, 16777215);
            }

            GLShim.glPopMatrix();
        } else {
            int heading = (int) (this.direction + this.northRotate);
            if (heading > 360) {
                heading -= 360;
            }

            String stats = "("
                    + this.dCoord(GameVariableAccessShim.xCoord())
                    + ", "
                    + GameVariableAccessShim.yCoord()
                    + ", "
                    + this.dCoord(GameVariableAccessShim.zCoord())
                    + ") "
                    + heading
                    + "'";
            int m = this.chkLen(stats) / 2;
            this.write(stats, this.scWidth / 2 - m, 5.0F, 16777215);
            if (this.ztimer > 0) {
                m = this.chkLen(this.error) / 2;
                this.write(this.error, this.scWidth / 2 - m, 15.0F, 16777215);
            }
        }
    }

    private String dCoord(int paramInt1) {
        if (paramInt1 < 0) {
            return "-" + Math.abs(paramInt1);
        } else {
            return paramInt1 > 0 ? "+" + paramInt1 : " " + paramInt1;
        }
    }

    private int chkLen(String paramStr) {
        return this.fontRenderer.getStringWidth(paramStr);
    }

    private void write(String text, float x, float y, int color) {
        this.fontRenderer.drawWithShadow(text, x, y, color);
    }

    private void drawWelcomeScreen(int scWidth, int scHeight) {
        GLShim.glBlendFunc(770, 771);
        int maxSize = 0;
        int border = 2;
        String head = this.welcomeString[0];

        int height;
        for (height = 1; height < this.welcomeString.length - 1; height++) {
            if (this.chkLen(this.welcomeString[height]) > maxSize) {
                maxSize = this.chkLen(this.welcomeString[height]);
            }
        }

        int title = this.chkLen(head);
        int centerX = (int) ((scWidth + 5) / 2.0);
        int centerY = (int) ((scHeight + 5) / 2.0);
        String hide = this.welcomeString[this.welcomeString.length - 1];
        int footer = this.chkLen(hide);
        GLShim.glDisable(3553);
        GLShim.glColor4f(0.0F, 0.0F, 0.0F, 0.7F);
        double leftX = centerX - title / 2.0 - border;
        double rightX = centerX + title / 2.0 + border;
        double topY = centerY - (height - 1) / 2.0 * 10.0 - border - 20.0;
        double botY = centerY - (height - 1) / 2.0 * 10.0 + border - 10.0;
        this.drawBox(leftX, rightX, topY, botY);
        leftX = centerX - maxSize / 2.0 - border;
        rightX = centerX + maxSize / 2.0 + border;
        topY = centerY - (height - 1) / 2.0 * 10.0 - border;
        botY = centerY + (height - 1) / 2.0 * 10.0 + border;
        this.drawBox(leftX, rightX, topY, botY);
        leftX = centerX - footer / 2.0 - border;
        rightX = centerX + footer / 2.0 + border;
        topY = centerY + (height - 1) / 2.0 * 10.0 - border + 10.0;
        botY = centerY + (height - 1) / 2.0 * 10.0 + border + 20.0;
        this.drawBox(leftX, rightX, topY, botY);
        GLShim.glEnable(3553);
        this.write(head, centerX - title / 2, centerY - (height - 1) * 10 / 2 - 19, 16777215);

        for (int n = 1; n < height; n++) {
            this.write(this.welcomeString[n], centerX - maxSize / 2, centerY - (height - 1) * 10 / 2 + n * 10 - 9, 16777215);
        }

        this.write(hide, centerX - footer / 2, (scHeight + 5) / 2 + (height - 1) * 10 / 2 + 11, 16777215);
    }

    private void drawBox(double leftX, double rightX, double topY, double botY) {
        GLUtils.drawPre(VertexFormats.POSITION);
        GLUtils.ldrawtwo(leftX, botY, 0.0);
        GLUtils.ldrawtwo(rightX, botY, 0.0);
        GLUtils.ldrawtwo(rightX, topY, 0.0);
        GLUtils.ldrawtwo(leftX, topY, 0.0);
        GLUtils.drawPost();
    }
}
