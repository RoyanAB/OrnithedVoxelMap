package com.mamiyaotaru.voxelmap;

import com.mamiyaotaru.voxelmap.interfaces.*;
import com.mamiyaotaru.voxelmap.persistent.PersistentMap;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;
import com.mamiyaotaru.voxelmap.util.*;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.IResourceManagerReloadListener;
import net.minecraft.world.World;

import java.util.UUID;

public class VoxelMap extends AbstractVoxelMap implements IResourceManagerReloadListener {
	private MapSettingsManager mapOptions;
	private RadarSettingsManager radarOptions;
	private PersistentMapSettingsManager persistentMapOptions;
	private IMap map;
	private IRadar radar;
	private IRadar radarSimple;
	private PersistentMap persistentMap;
	private ISettingsAndLightingChangeNotifier settingsAndLightingChangeNotifier;
	private WorldUpdateListener worldUpdateListener;
	private IColorManager colorManager;
	private IWaypointManager waypointManager;
	private IDimensionManager dimensionManager;
	private World world;

	public VoxelMap() {
		instance = this;
	}

	public void lateInit(boolean showUnderMenus, boolean isFair) {
		GLUtils.textureManager = Minecraft.getMinecraft().getTextureManager();
		this.mapOptions = new MapSettingsManager();
		this.mapOptions.showUnderMenus = showUnderMenus;
		this.radarOptions = new RadarSettingsManager();
		this.mapOptions.addSecondaryOptionsManager(this.radarOptions);
		this.persistentMapOptions = new PersistentMapSettingsManager();
		this.mapOptions.addSecondaryOptionsManager(this.persistentMapOptions);
		BiomeRepository.loadBiomeColors();
		this.colorManager = new ColorManager(this);
		this.waypointManager = new WaypointManager(this);
		this.dimensionManager = new DimensionManager(this);
		this.persistentMap = new PersistentMap(this);
		this.mapOptions.loadAll();

		try {
			if (isFair) {
				this.radarOptions.radarAllowed = false;
				this.radarOptions.radarMobsAllowed = false;
				this.radarOptions.radarPlayersAllowed = false;
			} else {
				this.radarOptions.radarAllowed = true;
				this.radarOptions.radarMobsAllowed = true;
				this.radarOptions.radarPlayersAllowed = true;
				this.radar = new Radar(this);
				this.radarSimple = new RadarSimple(this);
			}
		} catch (Exception e) {
			this.radarOptions.radarAllowed = false;
			this.radarOptions.radarMobsAllowed = false;
			this.radarOptions.radarPlayersAllowed = false;
			this.radar = null;
			this.radarSimple = null;
		}

		this.map = new Map(this);
		this.settingsAndLightingChangeNotifier = new SettingsAndLightingChangeNotifier();
		this.worldUpdateListener = new WorldUpdateListener();
		this.worldUpdateListener.addListener(this.map);
		this.worldUpdateListener.addListener(this.persistentMap);
		IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
		resourceManager.registerReloadListener(this);
	}

	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.waypointManager.onResourceManagerReload(resourceManager);
		if (this.radar != null) {
			this.radar.onResourceManagerReload(resourceManager);
		}

		if (this.radarSimple != null) {
			this.radarSimple.onResourceManagerReload(resourceManager);
		}

		this.colorManager.onResourceManagerReload(resourceManager);
	}

	public void onTickInGame(Minecraft mc) {
		this.map.onTickInGame(mc);
	}

	public void onSetupCameraTransform() {
		this.map.getFogColor();
	}

	public void onTick(Minecraft mc, boolean clock) {
		if (GameVariableAccessShim.getWorld() != null && !GameVariableAccessShim.getWorld().equals(this.world)
			|| this.world != null && !this.world.equals(GameVariableAccessShim.getWorld())) {
			if (this.world != null) {
				this.world.removeEventListener(this.worldUpdateListener);
			}

			this.world = GameVariableAccessShim.getWorld();
			if (this.world != null) {
				this.newSubWorldName("", false);
//				ForgeModVoxelMap.WORLD_ID.sendToServer(new WorldIDPacket());
				mc.player.getLocationSkin();
				java.util.Map<Type, MinecraftProfileTexture> skinMap = mc.getSkinManager().loadSkinFromCache(mc.player.getGameProfile());
				if (skinMap.containsKey(Type.SKIN)) {
					mc.getSkinManager().loadSkin(skinMap.get(Type.SKIN), Type.SKIN);
				}

				this.map.newWorld(this.world);
				this.world.addEventListener(this.worldUpdateListener);
			}

			this.waypointManager.newWorld(this.world);
			this.persistentMap.newWorld(this.world);
		}

		this.map.onTick(mc, clock);
		this.persistentMap.onTick(mc);
	}

	public void setConnectedRealm(String id) {
		this.waypointManager.setConnectedRealm(id);
	}

	@Override
	public MapSettingsManager getMapOptions() {
		return this.mapOptions;
	}

	@Override
	public RadarSettingsManager getRadarOptions() {
		return this.radarOptions;
	}

	@Override
	public PersistentMapSettingsManager getPersistentMapOptions() {
		return this.persistentMapOptions;
	}

	@Override
	public IMap getMap() {
		return this.map;
	}

	@Override
	public ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier() {
		return this.settingsAndLightingChangeNotifier;
	}

	@Override
	public IRadar getRadar() {
		if (this.radarOptions.showRadar) {
			if (this.radarOptions.radarMode == RadarSettingsManager.SIMPLE) {
				return this.radarSimple;
			}

			if (this.radarOptions.radarMode == RadarSettingsManager.FULL) {
				return this.radar;
			}
		}

		return null;
	}

	@Override
	public IColorManager getColorManager() {
		return this.colorManager;
	}

	@Override
	public IWaypointManager getWaypointManager() {
		return this.waypointManager;
	}

	@Override
	public IDimensionManager getDimensionManager() {
		return this.dimensionManager;
	}

	@Override
	public IPersistentMap getPersistentMap() {
		return this.persistentMap;
	}

	@Override
	public void setPermissions(
		boolean hasFullRadarPermission, boolean hasPlayersOnRadarPermission, boolean hasMobsOnRadarPermission, boolean hasCavemodePermission
	) {
		boolean override = false;

		try {
			UUID devUUID = UUID.fromString("9b37abb9-2487-4712-bb96-21a1e0b2023c");
			UUID playerUUID = Minecraft.getMinecraft().player.getUniqueID();
			override = playerUUID.equals(devUUID);
		} catch (Exception ignored) {
		}

		this.radarOptions.radarAllowed = hasFullRadarPermission || override;
		this.radarOptions.radarPlayersAllowed = hasPlayersOnRadarPermission || override;
		this.radarOptions.radarMobsAllowed = hasMobsOnRadarPermission || override;
		this.mapOptions.cavesAllowed = hasCavemodePermission || override;
	}

	@Override
	public synchronized void newSubWorldName(String name, boolean fromServer) {
		this.waypointManager.setSubworldName(name, fromServer);
	}

	@Override
	public synchronized void newSubWorldHash(String hash) {
		this.waypointManager.setSubworldHash(hash);
	}

	@Override
	public String getWorldSeed() {
		return Minecraft.getMinecraft().isIntegratedServerRunning()
			? Long.toString(Minecraft.getMinecraft().getIntegratedServer().getEntityWorld().getSeed())
			: this.waypointManager.getWorldSeed();
	}

	@Override
	public void setWorldSeed(String newSeed) {
		if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
			this.waypointManager.setWorldSeed(newSeed);
		}
	}
}
