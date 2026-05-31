package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.gui.GuiAddWaypoint;
import com.mamiyaotaru.voxelmap.gui.GuiWaypoints;
import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;
import com.mamiyaotaru.voxelmap.interfaces.*;
import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import com.mamiyaotaru.voxelmap.persistent.GuiPersistentMap;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.BufferUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.SocketException;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;

import static com.mamiyaotaru.voxelmap.util.GLShim.*;

@SuppressWarnings({"unused", "unchecked"})
public class Map implements Runnable, IMap {
	private final float[] lastLightBrightnessTable = new float[16];
	private final Object coordinateLock = new Object();
	private final IVoxelMap master;
	private Minecraft game;
	private final String zmodver = "v1.9.28";
	private World world = null;
	private final int worldHeight = 256;
	private final MapSettingsManager options;
	private final LayoutVariables layoutVariables;
	private final IColorManager colorManager;
	private final IWaypointManager waypointManager;
	private final int availableProcessors = Runtime.getRuntime().availableProcessors();
	private final boolean multicore = this.availableProcessors > 1;
	private final int heightMapResetHeight = this.multicore ? 2 : 5;
	private final int heightMapResetTime = this.multicore ? 300 : 3000;
	private final boolean threading = this.multicore;
	private final FullMapData[] mapData = new FullMapData[5];
	private final MapChunkCache[] chunkCache = new MapChunkCache[5];
	private LiveGLBufferedImage[] mapImages = new LiveGLBufferedImage[5];
	private final LiveGLBufferedImage[] mapImagesFiltered = new LiveGLBufferedImage[5];
	private final LiveGLBufferedImage[] mapImagesUnfiltered = new LiveScaledGLBufferedImage[5];
	private MutableBlockPos blockPos = new MutableBlockPos(0, 0, 0);
	private final MutableBlockPos tempBlockPos = new MutableBlockPos(0, 0, 0);
	private LiveGLBufferedImage roundImage;
	private boolean imageChanged = true;
	private DynamicTexture lightmapTexture = null;
	private boolean needLightmapRefresh = true;
	private int tickCounter = 0;
	private int tickWithLightChange = 0;
	private boolean lastPaused = true;
	private float lastGamma = 0.0F;
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
	private boolean showWelcomeScreen;
	private GuiScreen lastGuiScreen = null;
	private boolean enabled = true;
	private boolean fullscreenMap = false;
	private boolean active = false;
	private int zoom = 2;
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
	private String worldName = "";
	private String subworldName = "";
	private Long newServerTime = 0L;
	private boolean checkMOTD = false;
	private ChatLine mostRecentLine = null;
	private int northRotate = 0;
	private Thread zCalc = new Thread(this, "Voxelmap LiveMap Calculation Thread");
	private int zCalcTicker = 0;
	private final FontRenderer fontRenderer;
	private int[] lightmapColors = new int[256];
	private final UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
	private double zoomScale = 1.0;
	private double zoomScaleAdjusted = 1.0;
	private boolean optifineInstalled = false;
	private double rFog;
	private double bFog;
	private double gFog;
	private int mapImageInt = -1;

	public Map(IVoxelMap master) {
		this.master = master;
		this.game = GameVariableAccessShim.getMinecraft();
		this.options = master.getMapOptions();
		this.colorManager = master.getColorManager();
		this.waypointManager = master.getWaypointManager();
		this.layoutVariables = new LayoutVariables();

		try {
			NetworkUtils.enumerateInterfaces();
		} catch (SocketException e) {
			VoxelMapMod.LOGGER.error("could not get network interface addresses");
			e.printStackTrace();
		}

		ArrayList<KeyBinding> tempBindings = new ArrayList<>();
		tempBindings.addAll(Arrays.asList(this.game.gameSettings.keyBindings));
		tempBindings.addAll(Arrays.asList(this.options.keyBindings));
		this.game.gameSettings.keyBindings = tempBindings.toArray(new KeyBinding[0]);
		java.util.Map<String, Integer> categoryOrder = (java.util.Map<String, Integer>) ReflectionUtils.getPrivateFieldValueByType(
			null, KeyBinding.class, java.util.Map.class, 1
		);
		VoxelMapMod.LOGGER.info("CATEGORY ORDER IS {}", categoryOrder.size());
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

		this.roundImage = new LiveGLBufferedImage(128, 128, 6);
		if (GLUtils.fboEnabled) {
			GLUtils.setupFBO();
		}

		this.fontRenderer = this.game.fontRenderer;
		this.zoom = this.options.zoom;
		this.setZoomScale();
		this.optifineInstalled = false;
		Field ofProfiler = null;

		try {
			ofProfiler = GameSettings.class.getDeclaredField("ofProfiler");
		} catch (SecurityException | NoSuchFieldException ignored) {
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
						} catch (InterruptedException ignored) {
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
						} catch (Exception ignored) {
						}
					}

					this.doFullRender = this.zoomChanged;
					this.zoomChanged = false;
				}

				this.zCalcTicker = 0;
				synchronized (this.zCalc) {
					try {
						this.zCalc.wait(0L);
					} catch (InterruptedException ignored) {
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
		this.subworldName = "???";
	}

	@Override
	public void onTick(Minecraft mc, boolean clock) {
		if (clock) {
			this.tickCounter = this.tickCounter > 500000 ? 0 : this.tickCounter + 1;
		}
	}

	@Override
	public void onTickInGame(Minecraft mc) {
		this.northRotate = this.options.oldNorth ? 90 : 0;
		if (this.game == null) {
			this.game = mc;
		}

		if (this.lightmapTexture == null) {
			this.lightmapTexture = this.getLightmapTexture();
		}

		if (this.game.currentScreen == null && this.options.keyBindMenu.isPressed()) {
			this.showWelcomeScreen = false;
			if (this.options.welcome) {
				this.options.welcome = false;
				this.options.saveAll();
			}

			this.game.displayGuiScreen(new GuiPersistentMap(null, this.master));
		}

		if (this.game.currentScreen == null && this.options.keyBindWaypointMenu.isPressed()) {
			this.showWelcomeScreen = false;
			if (this.options.welcome) {
				this.options.welcome = false;
				this.options.saveAll();
			}

			this.game.displayGuiScreen(new GuiWaypoints(null, this.master));
		}

		if (this.game.currentScreen == null && this.options.keyBindWaypoint.isPressed()) {
			this.showWelcomeScreen = false;
			if (this.options.welcome) {
				this.options.welcome = false;
				this.options.saveAll();
			}

			float r;
			float g;
			float b;
			if (this.waypointManager.getWaypoints().isEmpty()) {
				r = 0.0F;
				g = 1.0F;
				b = 0.0F;
			} else {
				r = this.generator.nextFloat();
				g = this.generator.nextFloat();
				b = this.generator.nextFloat();
			}

			TreeSet<Integer> dimensions = new TreeSet<>();
			dimensions.add(this.game.player.dimension);
			Waypoint newWaypoint = new Waypoint(
				"",
				this.game.player.dimension != -1 ? GameVariableAccessShim.xCoord() : GameVariableAccessShim.xCoord() * 8,
				this.game.player.dimension != -1 ? GameVariableAccessShim.zCoord() : GameVariableAccessShim.zCoord() * 8,
				GameVariableAccessShim.yCoord(),
				true,
				r,
				g,
				b,
				"",
				this.master.getWaypointManager().getCurrentSubworldDescriptor(false),
				dimensions
			);
			this.game.displayGuiScreen(new GuiAddWaypoint(null, this.master, newWaypoint, false));
		}

		if (this.game.currentScreen == null && this.options.keyBindMobToggle.isPressed()) {
			this.master.getRadarOptions().setOptionValue(EnumOptionsMinimap.SHOWRADAR, 0);
			this.options.saveAll();
		}

		if (this.game.currentScreen == null && this.options.keyBindWaypointToggle.isPressed()) {
			this.options.toggleIngameWaypoints();
		}

		if (this.game.currentScreen == null && this.options.keyBindZoom.isPressed()) {
			this.showWelcomeScreen = false;
			if (this.options.welcome) {
				this.options.welcome = false;
				this.options.saveAll();
			} else {
				this.cycleZoomLevel();
			}
		}

		if (this.game.currentScreen == null && this.options.keyBindFullscreen.isPressed()) {
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
		if (this.game.currentScreen instanceof GuiGameOver && !(this.lastGuiScreen instanceof GuiGameOver)) {
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

			if (!(this.game.currentScreen instanceof GuiGameOver) && !(this.game.currentScreen instanceof GuiMemoryErrorScreen)) {
				this.zCalcTicker++;
				if (this.zCalcTicker > 200) {
					this.zCalcTicker = 0;
					this.zCalc.interrupt();
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

		this.enabled = (!mc.gameSettings.hideGUI || this.game.currentScreen != null)
			&& (this.options.showUnderMenus || this.game.currentScreen == null || this.game.currentScreen instanceof GuiChat)
			&& !this.game.gameSettings.showDebugInfo;

		this.direction = GameVariableAccessShim.rotationYaw() + 180.0F;

		while (this.direction >= 360.0F) {
			this.direction -= 360.0F;
		}

		while (this.direction < 0.0F) {
			this.direction += 360.0F;
		}

		if (!this.error.isEmpty() && this.ztimer == 0) {
			this.ztimer = 500;
		}

		if (this.ztimer > 0) {
			this.ztimer--;
		}

		if (this.ztimer == 0 && !this.error.isEmpty()) {
			this.error = "";
		}

		if (this.enabled) {
			this.drawMinimap(mc);
		}

		this.timer = this.timer > 5000 ? 0 : this.timer + 1;
		if (this.timer == 5000 && this.game.player.dimension == 0) {
			this.waypointManager.check2dWaypoints();
		}
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
			if (this.multicore && this.game.gameSettings.getOptionFloatValue(Options.RENDER_DISTANCE) > 8.0F) {
				this.options.zoom = 4;
				this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.25x)";
			} else {
				this.options.zoom = 3;
				this.error = I18nUtils.getString("minimap.ui.zoomlevel") + " (0.5x)";
			}
		}

		this.options.saveAll();
		this.mapImages[this.options.zoom].blank();
		this.zoomChanged = true;
		this.zoom = this.options.zoom;
		this.setZoomScale();
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

	private DynamicTexture getLightmapTexture() {
		Object lightmapTextureObj = ReflectionUtils.getPrivateFieldValueByType(this.game.entityRenderer, EntityRenderer.class, DynamicTexture.class);
		return lightmapTextureObj == null ? null : (DynamicTexture) lightmapTextureObj;
	}

	public void calculateCurrentLightAndSkyColor() {
		if (this.world != null) {
			if (this.needLightmapRefresh && this.tickCounter != this.tickWithLightChange && !this.game.isGamePaused() || this.options.realTimeTorches) {
				this.lightmapColors = this.lightmapTexture.getTextureData().clone();
				if (this.lightmapColors[255] != 0) {
					this.needLightmapRefresh = false;
				}
			}

			boolean lightChanged = false;
			if (this.game.gameSettings.gammaSetting != this.lastGamma) {
				lightChanged = true;
				this.lastGamma = this.game.gameSettings.gammaSetting;
			}

			float[] providerLightBrightnessTable = this.world.provider.getLightBrightnessTable();

			for (int t = 0; t < 16; t++) {
				if (providerLightBrightnessTable[t] != this.lastLightBrightnessTable[t]) {
					lightChanged = true;
					this.lastLightBrightnessTable[t] = providerLightBrightnessTable[t];
				}
			}

			float sunBrightness = this.world.getSunBrightness(1.0F);
			if (Math.abs(this.lastSunBrightness - sunBrightness) > 0.01
				|| sunBrightness == 1.0 && sunBrightness != this.lastSunBrightness
				|| sunBrightness == 0.0 && sunBrightness != this.lastSunBrightness) {
				lightChanged = true;
				this.needSkyColor = true;
				this.lastSunBrightness = sunBrightness;
			}

			float potionEffect = 0.0F;
			if (this.game.player.isPotionActive(MobEffects.NIGHT_VISION)) {
				int duration = this.game.player.getActivePotionEffect(MobEffects.NIGHT_VISION).getDuration();
				potionEffect = duration > 200 ? 1.0F : 0.7F + MathHelper.sin((duration - 1.0F) * (float) Math.PI * 0.2F) * 0.3F;
			}

			if (this.lastPotion != potionEffect) {
				this.lastPotion = potionEffect;
				lightChanged = true;
			}

			int lastLightningBolt = this.world.getLastLightningBolt();
			if (this.lastLightning != lastLightningBolt) {
				this.lastLightning = lastLightningBolt;
				lightChanged = true;
			}

			if (this.lastPaused != this.game.isGamePaused()) {
				this.lastPaused = !this.lastPaused;
				lightChanged = true;
			}

			boolean scheduledUpdate = (this.timer - 50)
				% (this.lastLightBrightnessTable[0] == 0.0F ? 250 : (this.game.player.dimension != -1 ? 500 : 5000))
				== 0;
			if (lightChanged || scheduledUpdate) {
				this.tickWithLightChange = this.tickCounter;
				this.needLightmapRefresh = true;
			}

			boolean aboveHorizon = this.game.player.getPositionEyes(0.0F).y + this.game.player.getEyeHeight() >= this.world.getHorizon();
			if (aboveHorizon != this.lastAboveHorizon) {
				this.needSkyColor = true;
				this.lastAboveHorizon = aboveHorizon;
			}

			int biomeID = Biome.getIdForBiome(
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
		if (this.needLightmapRefresh && this.tickCounter != this.tickWithLightChange && !this.game.isGamePaused()) {
			float[] fogColors = new float[16];
			FloatBuffer temp = BufferUtils.createFloatBuffer(16);
			GLShim.glGetFloat(GL_COLOR_CLEAR_VALUE, temp);
			temp.get(fogColors);
			this.rFog = fogColors[0];
			this.gFog = fogColors[1];
			this.bFog = fogColors[2];
		}
	}

	private int getSkyColor() {
		this.needSkyColor = false;
		boolean aboveHorizon = this.lastAboveHorizon;
		double rFog = this.rFog;
		double gFog = this.gFog;
		double bFog = this.bFog;
		int fogColor;
		if (!aboveHorizon && this.game.gameSettings.renderDistanceChunks >= 4) {
			fogColor = 167772160 + (int) (rFog * 255.0) * 65536 + (int) (gFog * 255.0) * 256 + (int) (bFog * 255.0);
		} else {
			fogColor = -16777216 + (int) (rFog * 255.0) * 65536 + (int) (gFog * 255.0) * 256 + (int) (bFog * 255.0);
		}

		if (this.game.world.provider.isSurfaceWorld()
			&& this.game.gameSettings.getOptionFloatValue(Options.RENDER_DISTANCE) >= 4.0F
			&& (!this.game.gameSettings.getOptionOrdinalValue(Options.USE_VBO) || this.optifineInstalled)) {
			double rSky;
			double bSky;
			double gSky;
			if (!aboveHorizon) {
				bSky = 0.0;
				gSky = 0.0;
				rSky = 0.0;
			} else {
				Vec3d skyColorVec = this.world.getSkyColor(this.game.getRenderViewEntity(), 0.0F);
				rSky = skyColorVec.x;
				gSky = skyColorVec.y;
				bSky = skyColorVec.z;
				if (this.world.provider.isSkyColored()) {
					rSky = rSky * 0.2F + 0.04F;
					gSky = gSky * 0.2F + 0.04F;
					bSky = bSky * 0.6F + 0.1F;
				}
			}

			boolean showLocalFog = this.world.provider.doesXZShowFog(GameVariableAccessShim.xCoord(), GameVariableAccessShim.zCoord());
			float farPlaneDistance = this.game.gameSettings.getOptionFloatValue(Options.RENDER_DISTANCE) * 16.0F;
			float fogStart = 0.0F;
			float fogEnd;
			if (showLocalFog) {
				fogStart = farPlaneDistance * 0.05F;
				fogEnd = Math.min(farPlaneDistance, 192.0F) * 0.5F;
			} else {
				fogEnd = farPlaneDistance * 0.8F;
			}

			float fogDensity = Math.max(
				0.0F, Math.min(1.0F, (fogEnd - (GameVariableAccessShim.yCoord() - (float) this.game.world.getHorizon())) / (fogEnd - fogStart))
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
	public void drawMinimap(Minecraft mc) {
		int scScale = 1;

		while (this.game.displayWidth / (scScale + 1) >= 320 && this.game.displayHeight / (scScale + 1) >= 240) {
			scScale++;
		}

		scScale += this.fullscreenMap ? 0 : this.options.sizeModifier;
		double scaledWidthD = (double) this.game.displayWidth / scScale;
		double scaledHeightD = (double) this.game.displayHeight / scScale;
		this.scWidth = MathHelper.ceil(scaledWidthD);
		this.scHeight = MathHelper.ceil(scaledHeightD);
		GLShim.glMatrixMode(GL_PROJECTION);
		GLShim.glPushMatrix();
		GLShim.glLoadIdentity();
		GLShim.glOrtho(0.0, scaledWidthD, scaledHeightD, 0.0, 1000.0, 3000.0);
		GLShim.glMatrixMode(GL_MODELVIEW);
		GLShim.glPushMatrix();
		GLShim.glLoadIdentity();
		GLShim.glTranslatef(0.0F, 0.0F, -2000.0F);
		int mapX;
		if (this.options.mapCorner != 0 && this.options.mapCorner != 3) {
			mapX = this.scWidth - 37;
		} else {
			mapX = 37;
		}

		int mapY;
		if (this.options.mapCorner != 0 && this.options.mapCorner != 1) {
			mapY = this.scHeight - 37;
		} else {
			mapY = 37;
		}

		if (this.options.mapCorner == 1 && !this.game.player.getActivePotionEffects().isEmpty()) {
			ScaledResolution scSize = new ScaledResolution(this.game);
			int scHeight = scSize.getScaledHeight();
			float resFactor = (float) this.scHeight / scHeight;
			float statusIconOffset = 0.0F;

			for (PotionEffect statusEffectInstance : this.game.player.getActivePotionEffects()) {
				if (statusEffectInstance.getPotion().hasStatusIcon() && statusEffectInstance.doesShowParticles()) {
					if (statusEffectInstance.getPotion().isBeneficial()) {
						statusIconOffset = Math.max(statusIconOffset, 24.0F);
					} else {
						statusIconOffset = Math.max(statusIconOffset, 50.0F);
					}
				}
			}

			mapY += (int) (resFactor * statusIconOffset);
		}

		GLShim.glEnable(GL_BLEND);
		GLShim.glEnable(GL_TEXTURE_2D);
		GLShim.glBlendFunc(GL_SRC_ALPHA, 0);
		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (!this.options.hide) {
			GLShim.glEnable(GL_DEPTH_TEST);
			if (this.fullscreenMap) {
				this.renderMapFull(this.scWidth, this.scHeight);
			} else {
				this.renderMap(mapX, mapY, scScale);
			}

			GLShim.glDisable(GL_DEPTH_TEST);
			if (this.master.getRadar() != null && !this.fullscreenMap) {
				this.layoutVariables.updateVars(scScale, mapX, mapY, this.zoomScale, this.zoomScaleAdjusted);
				this.master.getRadar().OnTickInGame(mc, this.layoutVariables);
			}

			if (!this.fullscreenMap) {
				this.drawDirections(mapX, mapY);
			}

			if (this.fullscreenMap) {
				this.drawArrow(this.scWidth / 2, this.scHeight / 2);
			} else {
				this.drawArrow(mapX, mapY);
			}
		}

		if (this.options.coords) {
			this.showCoords(mapX, mapY);
		}

		if (this.showWelcomeScreen) {
			this.drawWelcomeScreen(this.scWidth, this.scHeight);
		}

		GLShim.glDepthMask(true);
		GLShim.glEnable(GL_DEPTH_TEST);
		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GLShim.glMatrixMode(GL_PROJECTION);
		GLShim.glPopMatrix();
		GLShim.glMatrixMode(GL_MODELVIEW);
		GLShim.glPopMatrix();
		GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	}

	private void checkForChanges() {
		boolean changed = false;
		if (this.checkMOTD) {
			this.checkPermissionMessages();
		}

		if (!this.worldName.equals(this.master.getWaypointManager().getCurrentWorldName())) {
			this.worldName = this.master.getWaypointManager().getCurrentWorldName();
			this.master.getRadarOptions().radarAllowed = true;
			this.master.getRadarOptions().radarPlayersAllowed = this.master.getRadarOptions().radarAllowed;
			this.master.getRadarOptions().radarMobsAllowed = this.master.getRadarOptions().radarAllowed;
			this.options.cavesAllowed = true;
			if (!this.game.isIntegratedServerRunning()) {
				this.newServerTime = System.currentTimeMillis();
				this.checkMOTD = true;
			}
		}

		if (!this.subworldName.equals(this.waypointManager.getCurrentSubworldDescriptor(true))) {
			this.subworldName = this.waypointManager.getCurrentSubworldDescriptor(true);
			StringBuilder subworldNameBuilder = new StringBuilder("§r").append(I18nUtils.getString("worldmap.multiworld.newworld")).append(":").append(" ");
			if (this.subworldName.isEmpty() && this.waypointManager.isMultiworld()) {
				subworldNameBuilder.append("???");
			} else if (!this.subworldName.isEmpty()) {
				subworldNameBuilder.append(this.subworldName);
			}

			this.error = subworldNameBuilder.toString();
		}

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

	private void checkPermissionMessages() {
		if (GameVariableAccessShim.getWorld() != null
			&& this.game.player != null
			&& this.game.ingameGUI != null
			&& System.currentTimeMillis() - this.newServerTime < 5000L) {
			UUID playerUUID = this.game.player.getUniqueID();
			Object guiNewChat = this.game.ingameGUI.getChatGUI();
			if (guiNewChat == null) {
				VoxelMapMod.LOGGER.info("failed to get guiNewChat");
			} else {
				Object chatListObj = ReflectionUtils.getPrivateFieldValueByType(guiNewChat, GuiNewChat.class, List.class, 1);
				if (chatListObj == null) {
					VoxelMapMod.LOGGER.info("could not get chatlist");
				} else {
					List<ChatLine> chatList = (List<ChatLine>) chatListObj;
					boolean killRadar = false;
					boolean killCaves = false;

					for (ChatLine checkMe : chatList) {
						if (checkMe.equals(this.mostRecentLine)) {
							break;
						}

						String msg = checkMe.getChatComponent().getFormattedText();
						msg = msg.replaceAll("§r", "");
						if (msg.contains("§3 §6 §3 §6 §3 §6 §d")) {
							killCaves = true;
							this.error = "Server disabled cavemapping";
						}

						if (msg.contains("§3 §6 §3 §6 §3 §6 §e")) {
							killRadar = true;
							this.error = "Server disabled radar";
						}
					}

					this.master.getRadarOptions().radarAllowed = this.master.getRadarOptions().radarAllowed && (!killRadar || this.devUUID.equals(playerUUID));
					this.master.getRadarOptions().radarPlayersAllowed = this.master.getRadarOptions().radarAllowed;
					this.master.getRadarOptions().radarMobsAllowed = this.master.getRadarOptions().radarAllowed;
					this.options.cavesAllowed = this.options.cavesAllowed && (!killCaves || this.devUUID.equals(playerUUID));
					this.mostRecentLine = !chatList.isEmpty() ? chatList.get(0) : null;
				}
			}
		} else {
			this.checkMOTD = false;
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
		this.blockPos.setXYZ(this.lastX, Math.max(Math.min(GameVariableAccessShim.yCoord(), this.worldHeight - 1), 0), this.lastZ);
		Chunk playerChunk = this.world.getChunk(this.blockPos);
		if (this.game.player.dimension == -1) {
			netherPlayerInOpen = playerChunk.getHeight(this.blockPos) <= currentY;
			nether = currentY < 126;
			if (this.options.cavesAllowed && this.options.showCaves && currentY >= 126 && !netherPlayerInOpen) {
				caves = true;
			}
		} else if (this.game.player.dimension == 1) {
			boolean endPlayerInOpen = playerChunk.getHeight(this.blockPos) <= currentY;
			if (this.options.cavesAllowed && this.options.showCaves && !endPlayerInOpen) {
				caves = true;
			}
		} else if (this.options.cavesAllowed && this.options.showCaves && playerChunk.getLightFor(EnumSkyBlock.SKY, this.blockPos) <= 0) {
			caves = true;
		}

		if (this.lastBeneathRendering != (caves || nether && (currentY <= 125 || !netherPlayerInOpen && this.options.showCaves))) {
			this.lastBeneathRendering = caves || nether && (currentY <= 125 || !netherPlayerInOpen && this.options.showCaves);
			full = true;
		}

		needHeightAndID = needHeightMap && (nether || caves);
		int color24;
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
	public void handleChangeInWorld(BlockPos pos1, BlockPos pos2) {
		this.chunkCache[this.zoom].registerChangeAt(pos1);
	}

	@Override
	public void processChunk(Chunk chunk) {
		this.rectangleCalc(chunk.x * 16, chunk.z * 16, chunk.x * 16 + 15, chunk.z * 16 + 15);
	}

	private void rectangleCalc(int left, int top, int right, int bottom) {
		boolean nether = false;
		boolean caves = false;
		boolean netherPlayerInOpen;
		this.blockPos.setXYZ(this.lastX, Math.max(Math.min(GameVariableAccessShim.yCoord(), this.worldHeight - 1), 0), this.lastZ);
		Chunk playerChunk = this.world.getChunk(this.blockPos);
		if (this.game.player.dimension == -1) {
			int currentY = GameVariableAccessShim.yCoord();
			netherPlayerInOpen = playerChunk.getHeight(this.blockPos) <= currentY;
			nether = currentY < 126;
			if (this.options.cavesAllowed && this.options.showCaves && currentY >= 126 && !netherPlayerInOpen) {
				caves = true;
			}
		} else if (this.options.cavesAllowed && this.options.showCaves && playerChunk.getLightFor(EnumSkyBlock.SKY, this.blockPos) <= 0) {
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
		int color24;

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
		int surfaceHeight;
		int seafloorHeight = -1;
		int underwaterTransparentHeight = -1;
		int transparentHeight = -1;
		int foliageHeight = -1;
		int surfaceColor;
		int seafloorColor = 0;
		int transparentColor = 0;
		int foliageColor = 0;
		this.blockPos = this.blockPos.withXYZ(startX + imageX, 0, startZ + imageY);
		IBlockState blockState;
		int color24;
		int biomeID;
		if (needBiome) {
			if (world.getChunk(this.blockPos).isLoaded()) {
				biomeID = Biome.getIdForBiome(world.getBiome(this.blockPos));
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

		} else {
			boolean blockChangeForcedTint = false;
			boolean solid = false;
			int blockStateID;
			if (needHeightAndID) {
				surfaceHeight = this.getBlockHeight(nether, caves, world, startX + imageX, startZ + imageY);
				blockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, surfaceHeight, startZ + imageY));
				if (blockState.getMaterial() != Material.SNOW) {
					blockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
				}

				blockState = blockState.getActualState(world, this.blockPos);
				blockStateID = BlockRepository.getStateId(blockState);
				if (this.options.biomes && blockState != this.mapData[this.zoom].getBlockstate(imageX, imageY)) {
					blockChangeForcedTint = true;
				}

				this.mapData[this.zoom].setHeight(imageX, imageY, surfaceHeight);
				this.mapData[this.zoom].setBlockstateID(imageX, imageY, blockStateID);
			} else {
				surfaceHeight = this.mapData[this.zoom].getHeight(imageX, imageY);
				blockStateID = this.mapData[this.zoom].getBlockstateID(imageX, imageY);
				blockState = BlockRepository.getStateById(blockStateID);
			}

			if (surfaceHeight == -1) {
				surfaceHeight = this.lastY + 1;
				solid = true;
			}

			if (blockState.getMaterial() == Material.LAVA) {
				solid = false;
			}

			if (this.options.biomes) {
				surfaceColor = this.colorManager.getBlockColor(this.blockPos, blockStateID, biomeID);
				int tint;
				if (!needTint && !blockChangeForcedTint) {
					tint = this.mapData[this.zoom].getBiomeTint(imageX, imageY);
				} else {
					tint = this.colorManager
						.getBiomeTint(this.mapData[this.zoom], world, blockState, blockStateID, this.blockPos, this.tempBlockPos, startX, startZ);
					this.mapData[this.zoom].setBiomeTint(imageX, imageY, tint);
				}

				if (tint != -1) {
					surfaceColor = this.colorManager.colorMultiplier(surfaceColor, tint);
				}
			} else {
				surfaceColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, blockStateID);
			}

			surfaceColor = this.applyHeight(surfaceColor, nether, caves, world, multi, startX, startZ, imageX, imageY, surfaceHeight, solid, 1);
			int light;
			if (needLight) {
				light = this.getLight(surfaceColor, blockState, world, startX + imageX, startZ + imageY, surfaceHeight, solid);
				this.mapData[this.zoom].setLight(imageX, imageY, light);
			} else {
				light = this.mapData[this.zoom].getLight(imageX, imageY);
			}

			if (light == 0) {
				surfaceColor = 0;
			} else if (light != 255) {
				surfaceColor = this.colorManager.colorMultiplier(surfaceColor, light);
			}

			if (this.options.waterTransparency) {
				Material material = blockState.getMaterial();
				if (material == Material.WATER || material == Material.ICE) {
					if (needHeightAndID) {
						int[] underwaterHeights = this.getSeafloorHeight(world, startX + imageX, startZ + imageY, surfaceHeight);
						seafloorHeight = underwaterHeights[0];
						underwaterTransparentHeight = underwaterHeights[1];
						this.blockPos.setXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY);
						blockState = world.getBlockState(this.blockPos);
						blockState = blockState.getActualState(world, this.blockPos);
						if (blockState.getMaterial() == Material.WATER) {
							blockState = BlockRepository.air.getDefaultState();
						}

						blockStateID = BlockRepository.getStateId(blockState);
						if (this.options.biomes && blockState != this.mapData[this.zoom].getOceanFloorBlockstate(imageX, imageY)) {
							blockChangeForcedTint = true;
						}

						this.mapData[this.zoom].setOceanFloorHeight(imageX, imageY, seafloorHeight);
						this.mapData[this.zoom].setOceanFloorBlockstateID(imageX, imageY, blockStateID);
					} else {
						seafloorHeight = this.mapData[this.zoom].getOceanFloorHeight(imageX, imageY);
						blockStateID = this.mapData[this.zoom].getOceanFloorBlockstateID(imageX, imageY);
						blockState = BlockRepository.getStateById(blockStateID);
					}

					if (!this.options.biomes) {
						seafloorColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, blockStateID);
					} else {
						seafloorColor = this.colorManager.getBlockColor(this.blockPos, blockStateID, biomeID);
						int tintx;
						if (!needTint && !blockChangeForcedTint) {
							tintx = this.mapData[this.zoom].getOceanFloorBiomeTint(imageX, imageY);
						} else {
							tintx = this.colorManager
								.getBiomeTint(this.mapData[this.zoom], world, blockState, blockStateID, this.blockPos, this.tempBlockPos, startX, startZ);
							this.mapData[this.zoom].setOceanFloorBiomeTint(imageX, imageY, tintx);
						}

						if (tintx != -1) {
							seafloorColor = this.colorManager.colorMultiplier(seafloorColor, tintx);
						}
					}

					seafloorColor = this.applyHeight(seafloorColor, nether, caves, world, multi, startX, startZ, imageX, imageY, seafloorHeight, solid, 0);
					int seafloorLight;
					if (needLight) {
						seafloorLight = this.getLight(seafloorColor, blockState, world, startX + imageX, startZ + imageY, seafloorHeight, solid);
						this.blockPos.setXYZ(startX + imageX, seafloorHeight, startZ + imageY);
						blockState = world.getBlockState(this.blockPos);
						Material materialAbove = blockState.getMaterial();
						if (this.options.lightmap && materialAbove == Material.ICE) {
							int multiplier = 255;
							if (this.game.gameSettings.ambientOcclusion == 1) {
								multiplier = 200;
							} else if (this.game.gameSettings.ambientOcclusion == 2) {
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
			}

			if (this.options.blockTransparency) {
				if (needHeightAndID) {
					transparentHeight = this.getTransparentHeight(nether, caves, world, startX + imageX, startZ + imageY, surfaceHeight);
					if (transparentHeight == -1 && this.options.waterTransparency && underwaterTransparentHeight > 0) {
						transparentHeight = underwaterTransparentHeight;
					}

					if (transparentHeight != -1) {
						this.blockPos.setXYZ(startX + imageX, transparentHeight - 1, startZ + imageY);
						blockState = world.getBlockState(this.blockPos);
						blockState = blockState.getActualState(world, this.blockPos);
					} else {
						blockState = BlockRepository.air.getDefaultState();
					}

					blockStateID = BlockRepository.getStateId(blockState);
					if (this.options.biomes && blockState != this.mapData[this.zoom].getTransparentBlockstate(imageX, imageY)) {
						blockChangeForcedTint = true;
					}

					this.mapData[this.zoom].setTransparentHeight(imageX, imageY, transparentHeight);
					this.mapData[this.zoom].setTransparentBlockstateID(imageX, imageY, blockStateID);
				} else {
					transparentHeight = this.mapData[this.zoom].getTransparentHeight(imageX, imageY);
					blockStateID = this.mapData[this.zoom].getTransparentBlockstateID(imageX, imageY);
					blockState = BlockRepository.getStateById(blockStateID);
				}

				if (blockState != null && blockState != BlockRepository.air.getDefaultState()) {
					if (this.options.biomes) {
						transparentColor = this.colorManager.getBlockColor(this.blockPos, blockStateID, biomeID);
						int tintxx;
						if (!needTint && !blockChangeForcedTint) {
							tintxx = this.mapData[this.zoom].getTransparentBiomeTint(imageX, imageY);
						} else {
							tintxx = this.colorManager
								.getBiomeTint(this.mapData[this.zoom], world, blockState, blockStateID, this.blockPos, this.tempBlockPos, startX, startZ);
							this.mapData[this.zoom].setTransparentBiomeTint(imageX, imageY, tintxx);
						}

						if (tintxx != -1) {
							transparentColor = this.colorManager.colorMultiplier(transparentColor, tintxx);
						}
					} else {
						transparentColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, blockStateID);
					}

					transparentColor = this.applyHeight(transparentColor, nether, caves, world, multi, startX, startZ, imageX, imageY, transparentHeight, solid, 3);
					int transparentLight;
					if (needLight) {
						transparentLight = this.getLight(transparentColor, blockState, world, startX + imageX, startZ + imageY, transparentHeight, solid);
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

				if (!needHeightAndID) {
					foliageHeight = this.mapData[this.zoom].getFoliageHeight(imageX, imageY);
					blockStateID = this.mapData[this.zoom].getFoliageBlockstateID(imageX, imageY);
					blockState = BlockRepository.getStateById(blockStateID);
				} else {
					IBlockState foliageBlockState = null;
					if (transparentHeight != surfaceHeight + 1) {
						foliageHeight = surfaceHeight + 1;
						this.blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
						IBlockState var63 = world.getBlockState(this.blockPos);
						foliageBlockState = var63.getActualState(world, this.blockPos);
						Material material = foliageBlockState.getMaterial();
						if (material == Material.SNOW || material == Material.AIR || material == Material.LAVA) {
							foliageHeight = -1;
						}

						if (foliageBlockState == blockState) {
							foliageHeight = -1;
						}
					}

					if (foliageHeight == -1 && this.options.waterTransparency && seafloorHeight > 0 && transparentHeight != seafloorHeight) {
						foliageHeight = seafloorHeight + 1;
						this.blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
						IBlockState var64 = world.getBlockState(this.blockPos);
						foliageBlockState = var64.getActualState(world, this.blockPos);
						Material materialx = foliageBlockState.getMaterial();
						if (materialx == Material.AIR
							|| materialx == Material.LAVA
							|| materialx == Material.WATER
							|| materialx == Material.ICE) {
							foliageHeight = -1;
						}

						if (foliageBlockState == blockState) {
							foliageHeight = -1;
						}
					}

					if (foliageHeight != -1) {
						blockState = foliageBlockState;
					} else {
						blockState = BlockRepository.air.getDefaultState();
					}

					blockStateID = BlockRepository.getStateId(blockState);
					if (this.options.biomes && blockState != this.mapData[this.zoom].getFoliageBlockstate(imageX, imageY)) {
						blockChangeForcedTint = true;
					}

					this.mapData[this.zoom].setFoliageHeight(imageX, imageY, foliageHeight);
					this.mapData[this.zoom].setFoliageBlockstateID(imageX, imageY, blockStateID);
				}

				if (blockState != null && blockState != BlockRepository.air.getDefaultState()) {
					if (!this.options.biomes) {
						foliageColor = this.colorManager.getBlockColorWithDefaultTint(this.blockPos, blockStateID);
					} else {
						foliageColor = this.colorManager.getBlockColor(this.blockPos, blockStateID, biomeID);
						int tintxxx;
						if (!needTint && !blockChangeForcedTint) {
							tintxxx = this.mapData[this.zoom].getFoliageBiomeTint(imageX, imageY);
						} else {
							tintxxx = this.colorManager
								.getBiomeTint(this.mapData[this.zoom], world, blockState, blockStateID, this.blockPos, this.tempBlockPos, startX, startZ);
							this.mapData[this.zoom].setFoliageBiomeTint(imageX, imageY, tintxxx);
						}

						if (tintxxx != -1) {
							foliageColor = this.colorManager.colorMultiplier(foliageColor, tintxxx);
						}
					}

					foliageColor = this.applyHeight(foliageColor, nether, caves, world, multi, startX, startZ, imageX, imageY, foliageHeight, solid, 2);
					int foliageLight;
					if (needLight) {
						foliageLight = this.getLight(foliageColor, blockState, world, startX + imageX, startZ + imageY, foliageHeight, solid);
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

			if (seafloorHeight > 0) {
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

		}
		return this.doSlimeAndGrid(color24, startX + imageX, startZ + imageY);
	}

	private int doSlimeAndGrid(int color24, int mcX, int mcZ) {
		if (this.options.slimeChunks && !this.master.getWorldSeed().isEmpty()) {
			int xPosition = mcX >> 4;
			int zPosition = mcZ >> 4;
			String seedString = this.master.getWorldSeed();
			long seed;

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
			if (mcX % 256 == 0 || mcZ % 256 == 0) {
				color24 = this.colorManager.colorAdder(2113863680, color24);
			} else if (mcX % 16 == 0 || mcZ % 16 == 0) {
				color24 = this.colorManager.colorAdder(2097152000, color24);
			}
		}

		return color24;
	}

	private int getBlockHeight(boolean nether, boolean caves, World world, int x, int z) {
		int playerHeight = GameVariableAccessShim.yCoord();
		this.blockPos.setXYZ(x, playerHeight, z);
		Chunk chunk = world.getChunk(this.blockPos);
		int height = chunk.getHeight(this.blockPos);
		if ((nether || caves) && height > playerHeight) {
			int y = this.lastY;
			this.blockPos.setXYZ(x, y, z);
			IBlockState blockState = world.getBlockState(this.blockPos);
			if (blockState.getLightOpacity() == 0 && blockState.getMaterial() != Material.LAVA) {
				while (y > 0) {
					this.blockPos.setXYZ(x, --y, z);
					blockState = world.getBlockState(this.blockPos);
					if (blockState.getLightOpacity() > 0 || blockState.getMaterial() == Material.LAVA) {
						return y + 1;
					}
				}

				return y;
			} else {
				while (y <= this.lastY + 10 && y < (nether ? 127 : this.worldHeight)) {
					this.blockPos.setXYZ(x, ++y, z);
					blockState = world.getBlockState(this.blockPos);
					if (blockState.getLightOpacity() == 0 && blockState.getMaterial() != Material.LAVA) {
						return y;
					}
				}

				return -1;
			}
		} else {
			return height;
		}
	}

	private int[] getSeafloorHeight(World world, int x, int z, int height) {
		int seafloorHeight = height;
		int underwaterTransparentHeight = -1;
		IBlockState blockState = world.getBlockState(this.blockPos.withXYZ(x, seafloorHeight - 1, z));

		while (blockState.getLightOpacity() < 5 && blockState.getMaterial() != Material.LEAVES && seafloorHeight > 1) {
			seafloorHeight--;
			blockState = world.getBlockState(this.blockPos.withXYZ(x, seafloorHeight - 1, z));
			if (blockState.getMaterial().blocksMovement() && blockState.getMaterial() != Material.ICE && underwaterTransparentHeight == -1) {
				underwaterTransparentHeight = seafloorHeight;
			}
		}

		return new int[]{seafloorHeight, underwaterTransparentHeight};
	}

	private int getTransparentHeight(boolean nether, boolean caves, World world, int x, int z, int height) {
		int transHeight;
		if (!caves && !nether) {
			transHeight = world.getPrecipitationHeight(this.blockPos.withXYZ(x, height, z)).getY();
			if (transHeight <= height) {
				transHeight = -1;
			}
		} else {
			transHeight = -1;
		}

		IBlockState blockState = world.getBlockState(this.blockPos.withXYZ(x, transHeight - 1, z));
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
		if (color24 != this.colorManager.getAirColor() && color24 != 0) {
			int heightComp = -1;
			if ((this.options.heightmap || this.options.slopemap) && !solid) {
				int diff;
				double sc = 0.0;
				if (!this.options.slopemap) {
					diff = height - this.lastY;
					sc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 1.8;
					if (diff < 0) {
						sc = 0.0 - sc;
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
								if (block instanceof BlockGlass || block instanceof BlockStainedGlass) {
									heightComp = this.mapData[this.zoom].getHeight(imageX - 1, imageY + 1);
								}
							}
						}
					} else {
						if (layer == 0) {
							int baseHeight = this.getBlockHeight(nether, caves, world, startX + imageX - 1, startZ + imageY + 1);
							heightComp = this.getSeafloorHeight(world, startX + imageX - 1, startZ + imageY + 1, baseHeight)[0];
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
								IBlockState blockState = world.getBlockState(this.blockPos.withXYZ(startX + imageX, height - 1, startZ + imageY));
								Block block = blockState.getBlock();
								if (block instanceof BlockGlass || block instanceof BlockStainedGlass) {
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
						sc = diff > 0 ? 1.0 : -1.0;
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
				int b = color24 & 0xFF;
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
		}

		return color24;
	}

	private int getLight(int color24, IBlockState blockState, World world, int x, int z, int height, boolean solid) {
		int i3 = 255;
		if (solid) {
			i3 = 0;
		} else if (color24 != this.colorManager.getAirColor() && color24 != 0 && this.options.lightmap) {
			this.blockPos.setXYZ(x, Math.max(Math.min(height, this.worldHeight - 1), 0), z);
			Chunk chunk = world.getChunk(this.blockPos);
			int blockLight = chunk.getLightFor(EnumSkyBlock.BLOCK, this.blockPos);
			int skyLight = chunk.getLightFor(EnumSkyBlock.SKY, this.blockPos);
			if (blockState.getMaterial() == Material.LAVA && blockLight < 14) {
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
			GLShim.glColorMask(false, false, false, true);
			GLShim.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
			GLShim.glClear(GL_COLOR_BUFFER_BIT);
			GLShim.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			GLShim.glColorMask(true, true, true, true);
			GLUtils.img(new ResourceLocation("voxelmap", this.options.squareMap ? "images/square.png" : "images/circle.png"));
			GLUtils.drawPre();
			GLUtils.setMap(x, y, 128);
			GLUtils.drawPost();
			GLShim.glBlendFunc(GL_DST_ALPHA, GL_ONE_MINUS_DST_ALPHA);
			synchronized (this.coordinateLock) {
				if (this.imageChanged) {
					this.imageChanged = false;
					this.mapImages[this.zoom].write();
					this.lastImageX = this.lastX;
					this.lastImageZ = this.lastZ;
				}
			}

			float multi = (float) (1.0 / this.zoomScaleAdjusted);
			this.percentX = (float) (GameVariableAccessShim.xCoordDouble() - this.lastImageX);
			this.percentY = (float) (GameVariableAccessShim.zCoordDouble() - this.lastImageZ);
			this.percentX *= multi;
			this.percentY *= multi;
			GLUtils.disp(this.mapImages[this.zoom].getIndex());
			GLShim.glPushMatrix();
			GLShim.glTranslatef(x, y, 0.0F);
			GLShim.glRotatef(!this.options.rotates ? this.northRotate : -this.direction + this.northRotate, 0.0F, 0.0F, 1.0F);
			GLShim.glTranslatef(-x, -y, 0.0F);
			GLShim.glTranslatef(-this.percentX, -this.percentY, 0.0F);
			if (GLUtils.openGL14Enabled) {
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			} else {
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			}

			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		} else if (GLUtils.fboEnabled) {
			GLShim.glBindTexture(GL_TEXTURE_2D, 0);
			GLShim.glPushAttrib(GL_TRANSFORM_BIT);
			GLShim.glViewport(0, 0, GLUtils.fboSize, GLUtils.fboSize);
			GLShim.glMatrixMode(GL_PROJECTION);
			GLShim.glPushMatrix();
			GLShim.glLoadIdentity();
			GLShim.glOrtho(0.0, GLUtils.fboSize, GLUtils.fboSize, 0.0, 1000.0, 3000.0);
			GLShim.glMatrixMode(GL_MODELVIEW);
			GLShim.glPushMatrix();
			GLShim.glLoadIdentity();
			GLShim.glTranslatef(0.0F, 0.0F, -2000.0F);
			GLUtils.bindFrameBuffer();
			GLShim.glDepthMask(false);
			GLShim.glDisable(GL_DEPTH_TEST);
			GLShim.glClearColor(0.0F, 0.0F, 0.0F, 0.0F);
			GLShim.glClear(GL_COLOR_BUFFER_BIT);
			GLShim.glBlendFunc(GL_SRC_ALPHA, 0);
			GLUtils.img(new ResourceLocation("voxelmap", this.options.squareMap ? "images/square.png" : "images/circle.png"));
			GLUtils.drawPre();
			GLUtils.ldrawthree(GLUtils.fboRad - GLUtils.fboRad / scale, GLUtils.fboRad + GLUtils.fboRad / scale, 1.0, 0.0, 0.0);
			GLUtils.ldrawthree(GLUtils.fboRad + GLUtils.fboRad / scale, GLUtils.fboRad + GLUtils.fboRad / scale, 1.0, 1.0, 0.0);
			GLUtils.ldrawthree(GLUtils.fboRad + GLUtils.fboRad / scale, GLUtils.fboRad - GLUtils.fboRad / scale, 1.0, 1.0, 1.0);
			GLUtils.ldrawthree(GLUtils.fboRad - GLUtils.fboRad / scale, GLUtils.fboRad - GLUtils.fboRad / scale, 1.0, 0.0, 1.0);
			GLUtils.drawPost();
			GLShim.glBlendFuncSeparate(1, 0, GL_DST_COLOR, 0);
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
			if (GLUtils.openGL14Enabled) {
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			} else {
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			}

			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			GLShim.glTranslatef(GLUtils.fboRad, GLUtils.fboRad, 0.0F);
			if (!this.options.rotates) {
				GLShim.glRotatef(-this.northRotate, 0.0F, 0.0F, 1.0F);
			} else {
				GLShim.glRotatef(this.direction - this.northRotate, 0.0F, 0.0F, 1.0F);
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
			GLShim.glEnable(GL_DEPTH_TEST);
			GLUtils.unbindFrameBuffer();
			GLShim.glMatrixMode(GL_PROJECTION);
			GLShim.glPopMatrix();
			GLShim.glMatrixMode(GL_MODELVIEW);
			GLShim.glPopMatrix();
			GLShim.glPopAttrib();
			GLShim.glViewport(0, 0, this.game.displayWidth, this.game.displayHeight);
			GLShim.glPushMatrix();
			GLShim.glBlendFunc(GL_SRC_ALPHA, 0);
			GLShim.glEnable(GL_ALPHA_TEST);
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

					this.roundImage = new LiveGLBufferedImage(diameter, diameter, 6);
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

			float multix = (float) (1.0 / this.zoomScaleAdjusted);
			this.percentX = (float) (GameVariableAccessShim.xCoordDouble() - this.lastImageX);
			this.percentY = (float) (GameVariableAccessShim.zCoordDouble() - this.lastImageZ);
			this.percentX *= multix;
			this.percentY *= multix;
			GLShim.glBlendFunc(GL_SRC_ALPHA, 0);
			GLUtils.disp(this.options.squareMap ? this.mapImages[this.zoom].getIndex() : this.roundImage.getIndex());
			if (GLUtils.openGL14Enabled) {
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
			} else {
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			}

			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			GLShim.glPushMatrix();
			GLShim.glTranslatef(x, y, 0.0F);
			GLShim.glRotatef(!this.options.rotates ? this.northRotate : -this.direction + this.northRotate, 0.0F, 0.0F, 1.0F);
			GLShim.glTranslatef(-x, -y, 0.0F);
			GLShim.glTranslatef(-this.percentX, -this.percentY, 0.0F);
		}

		double guiScale = (double) this.game.displayWidth / this.scWidth;
		GLShim.glEnable(GL_SCISSOR_TEST);
		GLShim.glScissor((int) (guiScale * (x - 32)), (int) (guiScale * (this.scHeight - y - 32.0)), (int) (guiScale * 64.0), (int) (guiScale * 63.0));
		GLUtils.drawPre();
		GLUtils.setMapWithScale(x, y, scale);
		GLUtils.drawPost();
		GLShim.glDisable(GL_SCISSOR_TEST);
		GLShim.glPopMatrix();
		GLShim.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
		GLShim.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		if (this.options.squareMap) {
			this.drawSquareMapFrame(x, y);
		} else {
			this.drawRoundMapFrame(x, y);
		}

		double lastXDouble = GameVariableAccessShim.xCoordDouble();
		double lastZDouble = GameVariableAccessShim.zCoordDouble();
		TextureAtlas textureAtlas = this.master.getWaypointManager().getTextureAtlas();
		GLUtils.disp(textureAtlas.getGlTextureId());
		Waypoint highlightedPoint = this.waypointManager.getHighlightedWaypoint();

		for (Waypoint pt : this.waypointManager.getWaypoints()) {
			if (pt.isActive() || pt == highlightedPoint) {
				double distanceSq = pt.getDistanceSqToEntity(Objects.requireNonNull(this.game.getRenderViewEntity()));
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
		boolean far;
		if (this.options.rotates) {
			locate += this.direction;
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
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				GLShim.glTranslatef(x, y, 0.0F);
				GLShim.glRotatef(-locate + this.northRotate, 0.0F, 0.0F, 1.0F);
				if (uprightIcon) {
					GLShim.glTranslated(0.0, -hypot, 0.0);
					GLShim.glRotatef(locate - this.northRotate, 0.0F, 0.0F, 1.0F);
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
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
				GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
				GLShim.glRotatef(-locate + this.northRotate, 0.0F, 0.0F, 1.0F);
				GLShim.glTranslated(0.0, -hypot, 0.0);
				GLShim.glRotatef(-(-locate + this.northRotate), 0.0F, 0.0F, 1.0F);
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
			GLShim.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
			GLShim.glPushMatrix();
			GLUtils.img(new ResourceLocation("voxelmap", "images/mmarrow.png"));
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			GLShim.glTranslatef(x, y, 0.0F);
			GLShim.glRotatef(this.options.rotates && !this.fullscreenMap ? 0.0F : this.direction, 0.0F, 0.0F, 1.0F);
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
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		} else {
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		}

		GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
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
			GLShim.glDisable(GL_DEPTH_TEST);

			for (AbstractMapData.BiomeLabel label : labels) {
				if (label.segmentSize > minimumSize) {
					String name = Objects.requireNonNull(Biome.getBiome(label.biomeID)).getBiomeName();
					int nameWidth = this.chkLen(name);
					float x = (float) (label.x * factor);
					float z = (float) (label.z * factor);
					if (this.options.oldNorth) {
						this.write(name, left + 256 - z - (float) nameWidth / 2, top + x - 3.0F, 16777215);
					} else {
						this.write(name, left + x - (float) nameWidth / 2, top + z - 3.0F, 16777215);
					}
				}
			}

			GLShim.glEnable(GL_DEPTH_TEST);
		}
	}

	private void drawSquareMapFrame(int x, int y) {
		try {
			GLUtils.disp(this.mapImageInt);
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
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
			InputStream is = this.game.getResourceManager().getResource(new ResourceLocation("voxelmap", "images/squaremap.png")).getInputStream();
			BufferedImage mapImage = ImageIO.read(is);
			is.close();
			this.mapImageInt = GLUtils.tex(mapImage);
		} catch (Exception e) {
			try {
				InputStream isx = this.game.getResourceManager().getResource(new ResourceLocation("textures/map/map_background.png")).getInputStream();
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
				VoxelMapMod.LOGGER.error("Error loading texture pack's map image: {}", f.getLocalizedMessage());
			}
		}
	}

	private void drawRoundMapFrame(int x, int y) {
		try {
			GLUtils.img(new ResourceLocation("voxelmap", "images/roundmap.png"));
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
			GLShim.glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
			GLUtils.drawPre();
			GLUtils.setMap(x, y, 128);
			GLUtils.drawPost();
		} catch (Exception localException) {
			this.error = "Error: minimap overlay not found!";
		}
	}

	private void drawDirections(int x, int y) {
		boolean unicode = this.fontRenderer.getUnicodeFlag();
		float scale = unicode ? 1.0F : 0.5F;
		float rotate;
		if (this.options.rotates) {
			rotate = -this.direction - 90.0F;
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
			boolean unicode = this.fontRenderer.getUnicodeFlag();
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
			String stats = "("
				+ this.dCoord(GameVariableAccessShim.xCoord())
				+ ", "
				+ GameVariableAccessShim.yCoord()
				+ ", "
				+ this.dCoord(GameVariableAccessShim.zCoord())
				+ ") "
				+ (int) this.direction
				+ "'";
			int m = this.chkLen(stats) / 2;
			this.write(stats, (float) this.scWidth / 2 - m, 5.0F, 16777215);
			if (this.ztimer > 0) {
				m = this.chkLen(this.error) / 2;
				this.write(this.error, (float) this.scWidth / 2 - m, 15.0F, 16777215);
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
		this.fontRenderer.drawStringWithShadow(text, x, y, color);
	}

	private void write(String text, float x, float y, int color, boolean unicode) {
		boolean unicodeOriginal = this.fontRenderer.getUnicodeFlag();
		this.fontRenderer.setUnicodeFlag(unicode);
		this.fontRenderer.drawStringWithShadow(text, x, y, color);
		this.fontRenderer.setUnicodeFlag(unicodeOriginal);
	}

	private void drawWelcomeScreen(int scWidth, int scHeight) {
		if (this.welcomeString[1] == null || this.welcomeString[1].equals("minimap.ui.welcome2")) {
			this.welcomeString[0] = "§4VoxelMap§F! " + this.zmodver + " " + I18nUtils.getString("minimap.ui.welcome1");
			this.welcomeString[1] = I18nUtils.getString("minimap.ui.welcome2");
			this.welcomeString[2] = I18nUtils.getString("minimap.ui.welcome3");
			this.welcomeString[3] = I18nUtils.getString("minimap.ui.welcome4");
			this.welcomeString[4] = "§B"
				+ this.options.getKeyDisplayString(this.options.keyBindZoom.getKeyCode())
				+ "§F: "
				+ I18nUtils.getString("minimap.ui.welcome5a")
				+ ", §B"
				+ this.options.getKeyDisplayString(this.options.keyBindMenu.getKeyCode())
				+ "§F: "
				+ I18nUtils.getString("minimap.ui.welcome5b");
			this.welcomeString[5] = "§B"
				+ this.options.getKeyDisplayString(this.options.keyBindFullscreen.getKeyCode())
				+ "§F: "
				+ I18nUtils.getString("minimap.ui.welcome6");
			this.welcomeString[6] = "§B"
				+ this.options.getKeyDisplayString(this.options.keyBindWaypoint.getKeyCode())
				+ "§F: "
				+ I18nUtils.getString("minimap.ui.welcome7");
			this.welcomeString[7] = "§F"
				+ this.options.getKeyDisplayString(this.options.keyBindZoom.getKeyCode())
				+ "§7: "
				+ I18nUtils.getString("minimap.ui.welcome8");
		}

		GLShim.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
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
		GLShim.glDisable(GL_TEXTURE_2D);
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
		GLShim.glEnable(GL_TEXTURE_2D);
		this.write(head, centerX - (float) title / 2, centerY - (float) ((height - 1) * 10) / 2 - 19, 16777215);

		for (int n = 1; n < height; n++) {
			this.write(this.welcomeString[n], centerX - (float) maxSize / 2, centerY - (float) ((height - 1) * 10) / 2 + n * 10 - 9, 16777215);
		}

		this.write(hide, centerX - (float) footer / 2, (float) (scHeight + 5) / 2 + (float) ((height - 1) * 10) / 2 + 11, 16777215);
	}

	private void drawBox(double leftX, double rightX, double topY, double botY) {
		GLUtils.drawPre(DefaultVertexFormats.POSITION);
		GLUtils.ldrawtwo(leftX, botY, 0.0);
		GLUtils.ldrawtwo(rightX, botY, 0.0);
		GLUtils.ldrawtwo(rightX, topY, 0.0);
		GLUtils.ldrawtwo(leftX, topY, 0.0);
		GLUtils.drawPost();
	}
}
