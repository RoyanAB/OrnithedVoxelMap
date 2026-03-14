package com.mamiyaotaru.voxelmap;

import com.google.common.collect.Lists;
import com.mamiyaotaru.voxelmap.interfaces.IVoxelMap;
import com.mamiyaotaru.voxelmap.interfaces.IWaypointManager;
import com.mamiyaotaru.voxelmap.ornithe.mixins.BuiltInModResourcePackAccessor;
import com.mamiyaotaru.voxelmap.textures.IIconCreator;
import com.mamiyaotaru.voxelmap.textures.Sprite;
import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.resources.*;
import net.minecraft.network.NetworkManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.ornithemc.osl.resource.loader.impl.BuiltInModResourcePack;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class WaypointManager implements IWaypointManager {
	private final TreeSet<String> knownSubworldNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
	private final HashSet<String> oldNorthWorldNames = new HashSet<>();
	private final HashMap<String, String> worldSeeds = new HashMap<>();
	private final Object waypointLock = new Object();
	public MapSettingsManager options = null;
	IVoxelMap master;
	TextureAtlas textureAtlas;
	TextureAtlas textureAtlasChooser;
	private Minecraft game;
	private boolean loaded = false;
	private boolean needSave = false;
	private ArrayList<Waypoint> wayPts = new ArrayList<>();
	private ArrayList<Waypoint> old2dWayPts = new ArrayList<>();
	private ArrayList<Waypoint> updatedPts;
	private Waypoint highlightedWaypoint = null;
	private String worldName = "";
	private String latestRealmsID = "";
	private String currentSubWorldName = "";
	private String currentSubWorldHash = "";
	private String currentSubworldDescriptor = "";
	private String currentSubworldDescriptorNoCodes = "";
	private boolean multiworld = false;
	private boolean gotAutoSubworldName = false;
	private float currentDimension = 0.5F;
	private BackgroundImageInfo backgroundImageInfo = null;
	private WaypointContainer waypointContainer = null;
	private File settingsFile;
	private Long lastNewWorldNameTime = 0L;

	public WaypointManager(IVoxelMap master) {
		this.master = master;
		this.options = master.getMapOptions();
		this.textureAtlas = new TextureAtlas("waypoints");
		this.textureAtlas.setBlurMipmapDirect(false, false);
		this.textureAtlasChooser = new TextureAtlas("chooser");
		this.textureAtlasChooser.setBlurMipmapDirect(false, false);
		this.waypointContainer = new WaypointContainer(this.options);
	}

	@Override
	public void onResourceManagerReload(IResourceManager resourceManager) {
		final List<ResourceLocation> images = new ArrayList<>();
		IIconCreator iconCreator = new IIconCreator() {
			@Override
			public void addIcons(TextureAtlas textureAtlas) {
				Minecraft mc = Minecraft.getMinecraft();
				List<IResourcePack> packs = Lists.newArrayList();
				Object defaultResourcePacksObj = ReflectionUtils.getPrivateFieldValueByType(mc, Minecraft.class, List.class, 1);
				if (defaultResourcePacksObj != null) {
					List<IResourcePack> defaultResourcePacks = (List<IResourcePack>) defaultResourcePacksObj;
					packs.addAll(defaultResourcePacks);
				}

				List<net.minecraft.client.resources.ResourcePackRepository.Entry> packEntries = mc.getResourcePackRepository().getRepositoryEntries();
				Iterator<net.minecraft.client.resources.ResourcePackRepository.Entry> packEntriesIterator = packEntries.iterator();

				while (packEntriesIterator.hasNext()) {
					IResourcePack pack = packEntriesIterator.next().getResourcePack();
					packs.add(pack);
				}

//				if (mc.getResourcePackRepository().getServerResourcePack() != null) {
//					packs.add(mc.getResourcePackRepository().getServerResourcePack());
//				}

				for (IResourcePack pack : packs) {
					if (pack instanceof FileResourcePack) {
						FileResourcePack filePack = (FileResourcePack) pack;
						this.addImagesFromFilePack(filePack, images);
					} else if (pack instanceof FolderResourcePack) {
						FolderResourcePack folderPack = (FolderResourcePack) pack;
						this.addImagesFromFolderPack(folderPack, images);
					} else if(pack instanceof BuiltInModResourcePack) {
						BuiltInModResourcePack builtInModResourcePack = (BuiltInModResourcePack) pack;
						this.addImagesFromBuiltInModResourcePack(builtInModResourcePack, images);
					}
				}

				Sprite markerIcon = textureAtlas.registerIconForResource(
					new ResourceLocation("voxelmap", "images/waypoints/marker.png"), Minecraft.getMinecraft().getResourceManager()
				);
				Sprite markerIconSmall = textureAtlas.registerIconForResource(
					new ResourceLocation("voxelmap", "images/waypoints/markersmall.png"), Minecraft.getMinecraft().getResourceManager()
				);

				for (ResourceLocation resourceLocation : images) {
					Sprite icon = textureAtlas.registerIconForResource(resourceLocation, Minecraft.getMinecraft().getResourceManager());
					String name = resourceLocation.toString();
					if (name.toLowerCase().contains("waypoints/waypoint") && !name.toLowerCase().contains("small")) {
						textureAtlas.registerMaskedIcon(name.replace(".png", "Small.png"), icon);
						textureAtlas.registerMaskedIcon(name.replace("waypoints/waypoint", "waypoints/marker"), markerIcon);
						textureAtlas.registerMaskedIcon(name.replace("waypoints/waypoint", "waypoints/marker").replace(".png", "Small.png"), markerIconSmall);
					} else if (name.toLowerCase().contains("waypoints/marker") && !name.toLowerCase().contains("small")) {
						textureAtlas.registerMaskedIcon(name.replace(".png", "Small.png"), icon);
					}
				}
			}

			void addImagesFromFilePack(FileResourcePack filePack, List<ResourceLocation> imagesx) {
				Object zipFileObj = ReflectionUtils.getPrivateFieldValueByType(filePack, FileResourcePack.class, ZipFile.class);
				if (zipFileObj != null) {
					ZipFile zipFile = (ZipFile) zipFileObj;
					this.addImagesFromFile(zipFile, imagesx);
				}
			}

			void addImagesFromFolderPack(FolderResourcePack folderPack, List<ResourceLocation> imagesx) {
				Object rootFolderObj = ReflectionUtils.getPrivateFieldValueByType(folderPack, AbstractResourcePack.class, File.class);
				if (rootFolderObj != null) {
					File rootFolder = (File) rootFolderObj;
					this.addImagesFromFolder(rootFolder, imagesx);
				}
			}

			void addImagesFromBuiltInModResourcePack(BuiltInModResourcePack builtInModResourcePack, List<ResourceLocation> imagesx) {
				try {
					File path = new File(((BuiltInModResourcePackAccessor) builtInModResourcePack).getMod().getOrigin().toString());
					if(path.isFile()) {
						ZipFile zipFile = new ZipFile(((BuiltInModResourcePackAccessor) builtInModResourcePack).getMod().getOrigin().toString());
						if(zipFile != null)
							this.addImagesFromFile(zipFile, imagesx);
					} else if(path.isDirectory()) {
							this.addImagesFromFolder(path, imagesx);
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			void addImagesFromFile(ZipFile zipFile, List<ResourceLocation> imagesx) {
				ZipEntry waypointsFolder = zipFile.getEntry("assets/voxelmap/images/waypoints");
				if (waypointsFolder != null) {
					Enumeration<? extends ZipEntry> entries = zipFile.entries();

					while (entries.hasMoreElements()) {
						ZipEntry zipEntry = entries.nextElement();
						String name = zipEntry.getName();
						if (name.startsWith("assets/voxelmap/images/waypoints") && name.toLowerCase().endsWith(".png")) {
							name = name.replaceFirst("assets/voxelmap/", "");
							images.add(new ResourceLocation("voxelmap", name));
						}
					}

				}
			}

			void addImagesFromFolder(File rootFolder, List<ResourceLocation> imagesx) {
				File assetsDir = new File(rootFolder, "assets/voxelmap/images/waypoints");
				if (assetsDir.isDirectory()) {
					for (File file : assetsDir.listFiles()) {
						if (file.getName().toLowerCase().endsWith(".png")) {
							String name = "images/waypoints/" + file.getName();
							images.add(new ResourceLocation("voxelmap", name));
						}
					}
				}
			}
		};
		this.textureAtlas.loadTextureAtlas(iconCreator);
		this.textureAtlasChooser.reset();
		int expectedSize = 32;

		for (ResourceLocation resourceLocation : images) {
			String name = resourceLocation.toString();
			if (name.toLowerCase().contains("waypoints/waypoint") && !name.toLowerCase().contains("small")) {
				try {
					IResource imageResource = resourceManager.getResource(resourceLocation);
					BufferedImage bufferedImage = ImageIO.read(imageResource.getInputStream());
					imageResource.close();
					float scale = (float) expectedSize / bufferedImage.getWidth();
					bufferedImage = ImageUtils.scaleImage(bufferedImage, scale);
					this.textureAtlasChooser.registerIconForBufferedImage(name, bufferedImage);
				} catch (IOException e) {
					this.textureAtlasChooser.registerIconForResource(resourceLocation, Minecraft.getMinecraft().getResourceManager());
				}
			}
		}

		this.textureAtlasChooser.stitch();
	}

	@Override
	public TextureAtlas getTextureAtlas() {
		return this.textureAtlas;
	}

	@Override
	public TextureAtlas getTextureAtlasChooser() {
		return this.textureAtlasChooser;
	}

	@Override
	public ArrayList<Waypoint> getWaypoints() {
		return this.wayPts;
	}

	@Override
	public void newWorld(World world) {
		if (world == null) {
			this.currentDimension = 0.5F;
		} else {
			this.game = Minecraft.getMinecraft();
			String mapName;
			if (this.game.isIntegratedServerRunning()) {
				mapName = this.getMapName();
			} else {
				mapName = this.getServerName();
				if (mapName != null) {
					mapName = mapName.toLowerCase();
				}
			}

			if (!this.worldName.equals(mapName) && mapName != null && !mapName.equals("")) {
				this.currentDimension = 0.5F;
				this.worldName = mapName;
				this.loadWaypoints();
				this.master.getDimensionManager().populateDimensions();
			}

			int dimensionID = DimensionManager.getDimensionIDfromProvider(world.provider);
			this.enteredDimension(dimensionID);
			this.master.getDimensionManager().enteredDimension(dimensionID);
		}
	}

	public String getMapName() {
		return this.game.getIntegratedServer().getWorldName();
	}

	public String getServerName() {
		String serverName = "";

		try {
			ServerData serverData = this.game.getCurrentServerData();
			if (serverData != null) {
				boolean isOnLAN = false;
				isOnLAN = serverData.isOnLAN();
				if (isOnLAN) {
					System.out.println("LAN server detected!");
					serverName = serverData.serverName;
				} else {
					serverName = serverData.serverIP;
				}
			} else if (!this.latestRealmsID.equals("")) {
				System.out.println("REALMS server detected!");
				serverName = this.latestRealmsID;
			} else {
				NetHandlerPlayClient netHandler = this.game.getConnection();
				NetworkManager networkManager = netHandler.getNetworkManager();
				InetSocketAddress socketAddress = (InetSocketAddress) networkManager.getRemoteAddress();
				serverName = socketAddress.getHostString() + ":" + socketAddress.getPort();
			}
		} catch (Exception e) {
			System.err.println("error getting ServerData");
			e.printStackTrace();
		}

		return serverName;
	}

	@Override
	public void setConnectedRealm(String id) {
		this.latestRealmsID = id;
	}

	@Override
	public String getCurrentWorldName() {
		return this.worldName;
	}

	@Override
	public void handleDeath() {
		HashSet<Waypoint> toDel = new HashSet<>();

		for (Waypoint pt : this.wayPts) {
			if (pt.name.equals("Latest Death")) {
				pt.name = "Previous Death";
			}

			if (pt.name.startsWith("Previous Death")) {
				if (this.options.deathpoints == 2) {
					int num = 0;

					try {
						if (pt.name.length() > 15) {
							num = Integer.parseInt(pt.name.substring(15));
						}
					} catch (Exception e) {
						num = 0;
					}

					pt.red = pt.red - (pt.red - 0.5F) / 8.0F;
					pt.green = pt.green - (pt.green - 0.5F) / 8.0F;
					pt.blue = pt.blue - (pt.blue - 0.5F) / 8.0F;
					pt.name = "Previous Death " + (num + 1);
				} else {
					toDel.add(pt);
				}
			}
		}

		if (this.options.deathpoints != 2 && toDel.size() > 0) {
			for (Waypoint pt : toDel) {
				this.deleteWaypoint(pt);
			}
		}

		if (this.options.deathpoints != 0) {
			EntityPlayerSP thePlayer = Minecraft.getMinecraft().player;
			TreeSet<Integer> dimensions = new TreeSet<>();
			dimensions.add(Minecraft.getMinecraft().player.dimension);
			this.addWaypoint(
				new Waypoint(
					"Latest Death",
					thePlayer.dimension != -1 ? GameVariableAccessShim.xCoord() : GameVariableAccessShim.xCoord() * 8,
					thePlayer.dimension != -1 ? GameVariableAccessShim.zCoord() : GameVariableAccessShim.zCoord() * 8,
					GameVariableAccessShim.yCoord() - 1,
					true,
					1.0F,
					1.0F,
					1.0F,
					"Skull",
					this.getCurrentSubworldDescriptor(false),
					dimensions
				)
			);
		}
	}

	private void enteredDimension(int dimension) {
		this.highlightedWaypoint = null;
		if (dimension == this.currentDimension) {
			this.multiworld = true;
		}

		this.currentDimension = dimension;
		synchronized (this.waypointLock) {
			this.waypointContainer = new WaypointContainer(this.options);

			for (Waypoint pt : this.wayPts) {
				pt.inDimension = pt.dimensions.size() == 0 || pt.dimensions.contains(dimension);

				this.waypointContainer.addWaypoint(pt);
			}

			this.waypointContainer.setHighlightedWaypoint(this.highlightedWaypoint);
		}

		this.loadBackgroundMapImage();
	}

	@Override
	public void setOldNorth(boolean oldNorth) {
		String oldNorthWorldName = "";
		if (this.knownSubworldNames.size() == 0) {
			oldNorthWorldName = "all";
		} else {
			oldNorthWorldName = this.getCurrentSubworldDescriptor(false);
		}

		if (oldNorth) {
			this.oldNorthWorldNames.add(oldNorthWorldName);
		} else {
			this.oldNorthWorldNames.remove(oldNorthWorldName);
		}

		this.saveWaypoints();
	}

	@Override
	public TreeSet<String> getKnownSubworldNames() {
		return this.knownSubworldNames;
	}

	@Override
	public boolean receivedAutoSubworldName() {
		return this.gotAutoSubworldName;
	}

	@Override
	public boolean isMultiworld() {
		return this.multiworld;
	}

	@Override
	public synchronized void setSubworldName(String name, boolean fromServer) {
		boolean notNull = !name.equals("");
		if (notNull || System.currentTimeMillis() - this.lastNewWorldNameTime > 2000L) {
			if (notNull) {
				if (fromServer) {
					this.gotAutoSubworldName = true;
				}

				if (!name.equals(this.currentSubWorldName)) {
					System.out.println("New world name: " + TextUtils.scrubCodes(name));
				}

				this.lastNewWorldNameTime = System.currentTimeMillis();
			}

			this.currentSubWorldName = name;
			this.setSubWorldDescriptor(this.currentSubWorldName);
		}
	}

	@Override
	public synchronized void setSubworldHash(String hash) {
		this.currentSubWorldHash = hash;
		if (this.currentSubWorldName.equals("")) {
			this.setSubWorldDescriptor(this.currentSubWorldHash);
		}
	}

	private void setSubWorldDescriptor(String descriptor) {
		boolean serverSaysOldNorth = false;
		if (descriptor.endsWith("§o§n")) {
			descriptor = descriptor.substring(0, descriptor.length() - 4);
			serverSaysOldNorth = true;
		}

		this.currentSubworldDescriptor = descriptor;
		this.currentSubworldDescriptorNoCodes = TextUtils.scrubCodes(this.currentSubworldDescriptor);
		this.newSubworldName(this.currentSubworldDescriptorNoCodes);
		String currentSubWorldDescriptorScrubbed = TextUtils.scrubName(this.currentSubworldDescriptorNoCodes);
		synchronized (this.waypointLock) {
			for (Waypoint pt : this.wayPts) {
				pt.inWorld = currentSubWorldDescriptorScrubbed == "" || pt.world == "" || currentSubWorldDescriptorScrubbed.equals(pt.world);
			}
		}

		if (serverSaysOldNorth) {
			if (this.currentSubworldDescriptorNoCodes.equals("")) {
				this.oldNorthWorldNames.add("all");
			} else {
				this.oldNorthWorldNames.add(this.currentSubworldDescriptorNoCodes);
			}
		}

		this.master.getMapOptions().oldNorth = this.oldNorthWorldNames.contains(this.currentSubworldDescriptorNoCodes);
	}

	private void newSubworldName(String name) {
		if (name != null && !name.equals("")) {
			this.multiworld = true;
			if (this.knownSubworldNames.add(name)) {
				if (this.loaded) {
					this.saveWaypoints();
				} else {
					this.needSave = true;
				}
			}
		}

		this.loadBackgroundMapImage();
	}

	@Override
	public void changeSubworldName(String oldName, String newName) {
		if (!newName.equals(oldName) && this.knownSubworldNames.remove(oldName)) {
			this.knownSubworldNames.add(newName);
			synchronized (this.waypointLock) {
				for (Waypoint pt : this.wayPts) {
					if (pt.world.equals(oldName)) {
						pt.world = newName;
					}
				}
			}

			this.master.getPersistentMap().renameSubworld(oldName, newName);
			String worldName = this.getCurrentWorldName();
			String worldNamePathPart = TextUtils.scrubNameFile(worldName);
			String subWorldNamePathPart = TextUtils.scrubNameFile(oldName) + "/";
			File oldCachedRegionFileDir = new File(
				Minecraft.getMinecraft().mcDataDir, "/mods/VoxelMods/voxelMap/cache/" + worldNamePathPart + "/" + subWorldNamePathPart
			);
			if (oldCachedRegionFileDir.exists() && oldCachedRegionFileDir.isDirectory()) {
				subWorldNamePathPart = TextUtils.scrubNameFile(newName) + "/";
				File newCachedRegionFileDir = new File(
					Minecraft.getMinecraft().mcDataDir, "/mods/VoxelMods/voxelMap/cache/" + worldNamePathPart + "/" + subWorldNamePathPart
				);
				boolean success = oldCachedRegionFileDir.renameTo(newCachedRegionFileDir);
				if (!success) {
					System.out.println("Failed renaming " + oldCachedRegionFileDir.getPath() + " to " + newCachedRegionFileDir.getPath());
				}
			}

			if (oldName.equals(this.getCurrentSubworldDescriptor(false))) {
				this.setSubworldName(newName, false);
			}

			this.saveWaypoints();
		}
	}

	@Override
	public void deleteSubworld(String name) {
		if (this.knownSubworldNames.remove(name)) {
			synchronized (this.waypointLock) {
				for (Waypoint pt : this.wayPts) {
					if (pt.world.equals(name)) {
						pt.world = "";
						pt.inWorld = true;
					}
				}
			}

			this.saveWaypoints();
			this.lastNewWorldNameTime = 0L;
			this.setSubworldName("", false);
		}
	}

	@Override
	public String getCurrentSubworldDescriptor(boolean withCodes) {
		return withCodes ? this.currentSubworldDescriptor : this.currentSubworldDescriptorNoCodes;
	}

	@Override
	public String getWorldSeed() {
		String key = "all";
		if (this.knownSubworldNames.size() > 0) {
			key = this.getCurrentSubworldDescriptor(false);
		}

		String seed = this.worldSeeds.get(key);
		if (seed == null) {
			seed = "";
		}

		return seed;
	}

	@Override
	public void setWorldSeed(String newSeed) {
		System.out.println("waypoint manager gets new world seed: " + newSeed);
		String worldName = "all";
		if (this.knownSubworldNames.size() > 0) {
			worldName = this.getCurrentSubworldDescriptor(false);
		}

		this.worldSeeds.put(worldName, newSeed);
		this.saveWaypoints();
	}

	@Override
	public void saveWaypoints() {
		String worldNameSave = this.getCurrentWorldName();
		if (worldNameSave.endsWith(":25565")) {
			int portSepLoc = worldNameSave.lastIndexOf(":");
			if (portSepLoc != -1) {
				worldNameSave = worldNameSave.substring(0, portSepLoc);
			}
		}

		worldNameSave = TextUtils.scrubNameFile(worldNameSave);
		File saveDir = new File(Minecraft.getMinecraft().mcDataDir, "/voxelmap/");
		if (!saveDir.exists()) {
			saveDir.mkdirs();
		}

		this.settingsFile = new File(saveDir, worldNameSave + ".points");

		try {
			PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.settingsFile), StandardCharsets.UTF_8));
			String knownSubworldsString = "";

			for (String subworldName : this.knownSubworldNames) {
				knownSubworldsString = knownSubworldsString + TextUtils.scrubName(subworldName) + ",";
			}

			out.println("subworlds:" + knownSubworldsString);
			String oldNorthWorldsString = "";

			for (String oldNorthWorldName : this.oldNorthWorldNames) {
				oldNorthWorldsString = oldNorthWorldsString + TextUtils.scrubName(oldNorthWorldName) + ",";
			}

			out.println("oldNorthWorlds:" + oldNorthWorldsString);
			String seedsString = "";

			for (Entry<String, String> entry : this.worldSeeds.entrySet()) {
				seedsString = seedsString + TextUtils.scrubName(entry.getKey()) + "#" + entry.getValue() + ",";
			}

			out.println("seeds:" + seedsString);

			for (Waypoint pt : this.wayPts) {
				if (!pt.name.startsWith("^")) {
					String dimensionsString = "";

					for (Integer dimension : pt.dimensions) {
						dimensionsString = dimensionsString + dimension + "#";
					}

					if (dimensionsString.equals("")) {
						dimensionsString = "-1#0#";
					}

					out.println(
						"name:"
							+ TextUtils.scrubName(pt.name)
							+ ",x:"
							+ pt.x
							+ ",z:"
							+ pt.z
							+ ",y:"
							+ pt.y
							+ ",enabled:"
							+ pt.enabled
							+ ",red:"
							+ pt.red
							+ ",green:"
							+ pt.green
							+ ",blue:"
							+ pt.blue
							+ ",suffix:"
							+ pt.imageSuffix
							+ ",world:"
							+ TextUtils.scrubName(pt.world)
							+ ",dimensions:"
							+ dimensionsString
					);
				}
			}

			out.close();
		} catch (Exception local) {
			MessageUtils.chatInfo("§EError Saving Waypoints");
		}
	}

	private void loadWaypoints() {
		this.loaded = false;
		this.multiworld = false;
		this.gotAutoSubworldName = false;
		this.currentDimension = 0.5F;
		this.knownSubworldNames.clear();
		this.oldNorthWorldNames.clear();
		this.worldSeeds.clear();
		synchronized (this.waypointLock) {
			boolean loaded = false;
			this.wayPts = new ArrayList<>();
			String worldNameStandard = this.getCurrentWorldName();
			if (worldNameStandard.endsWith(":25565")) {
				int portSepLoc = worldNameStandard.lastIndexOf(":");
				if (portSepLoc != -1) {
					worldNameStandard = worldNameStandard.substring(0, portSepLoc);
				}
			}

			worldNameStandard = TextUtils.scrubNameFile(worldNameStandard);
			String worldNameWithPort = TextUtils.scrubNameFile(this.getCurrentWorldName());
			String worldNameWithoutPort = this.getCurrentWorldName();
			int portSepLoc = worldNameWithoutPort.lastIndexOf(":");
			if (portSepLoc != -1) {
				worldNameWithoutPort = worldNameWithoutPort.substring(0, portSepLoc);
			}

			worldNameWithoutPort = TextUtils.scrubNameFile(worldNameWithoutPort);
			String worldNameWithDefaultPort = TextUtils.scrubNameFile(worldNameWithoutPort + "~colon~25565");
			loaded = this.loadWaypointsExtensible(worldNameStandard);
			if (!loaded) {
				loaded = this.loadOldWaypoints(worldNameWithoutPort, worldNameWithDefaultPort, worldNameWithPort);
			}

			if (!loaded) {
				loaded = this.findReiWaypoints(worldNameWithoutPort);
			}

			if (!loaded) {
				MessageUtils.chatInfo("§ENo waypoints exist for this world/server.");
			} else {
				this.populateOld2dWaypoints();
			}
		}

		this.setSubWorldDescriptor(this.getCurrentSubworldDescriptor(true));
		this.loaded = true;
		if (this.needSave) {
			this.needSave = false;
			this.saveWaypoints();
		}

		this.multiworld = this.multiworld || this.knownSubworldNames.size() > 0;
	}

	private boolean loadWaypointsExtensible(String worldNameStandard) {
		File settingsFileNew = new File(Minecraft.getMinecraft().mcDataDir, "/voxelmap/" + worldNameStandard + ".points");
		File settingsFileOld = new File(Minecraft.getMinecraft().mcDataDir, "/mods/mamiyaotaru/voxelmap/" + worldNameStandard + ".points");
		if (!settingsFileOld.exists() && !settingsFileNew.exists()) {
			return false;
		}

		if (!settingsFileOld.exists()) {
			this.settingsFile = settingsFileNew;
		} else if (!settingsFileNew.exists()) {
			this.settingsFile = settingsFileOld;
		} else {
			this.settingsFile = settingsFileNew;
		}

		if (this.settingsFile.exists()) {
			try {
				Properties properties = new Properties();
				FileReader fr = new FileReader(this.settingsFile);
				properties.load(fr);
				String subWorldsS = properties.getProperty("subworlds", "");
				String[] subWorlds = subWorldsS.split(",");

				for (int t = 0; t < subWorlds.length; t++) {
					if (!subWorlds[t].equals("")) {
						this.knownSubworldNames.add(TextUtils.descrubName(subWorlds[t]));
					}
				}

				String oldNorthWorldsS = properties.getProperty("oldNorthWorlds", "");
				String[] oldNorthWorlds = oldNorthWorldsS.split(",");

				for (int tx = 0; tx < oldNorthWorlds.length; tx++) {
					if (!oldNorthWorlds[tx].equals("")) {
						this.oldNorthWorldNames.add(TextUtils.descrubName(oldNorthWorlds[tx]));
					}
				}

				String worldSeedsS = properties.getProperty("seeds", "");
				String[] worldSeedPairs = worldSeedsS.split(",");

				for (int txx = 0; txx < worldSeedPairs.length; txx++) {
					String pair = worldSeedPairs[txx];
					String[] worldSeedPair = pair.split("#");
					if (worldSeedPair.length == 2) {
						this.worldSeeds.put(worldSeedPair[0], worldSeedPair[1]);
					}
				}

				fr.close();
			} catch (IOException var25) {
			}

			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(this.settingsFile), StandardCharsets.UTF_8));

				String sCurrentLine;
				while ((sCurrentLine = in.readLine()) != null) {
					String[] pairs = sCurrentLine.split(",");
					if (pairs.length > 1) {
						String name = "";
						int x = 0;
						int z = 0;
						int y = -1;
						boolean enabled = false;
						float red = 0.5F;
						float green = 0.0F;
						float blue = 0.0F;
						String suffix = "";
						String world = "";
						TreeSet<Integer> dimensions = new TreeSet<>();

						for (int txxx = 0; txxx < pairs.length; txxx++) {
							int splitIndex = pairs[txxx].indexOf(":");
							if (splitIndex != -1) {
								String key = pairs[txxx].substring(0, splitIndex).toLowerCase().trim();
								String value = pairs[txxx].substring(splitIndex + 1).trim();
								if (key.equals("name")) {
									name = TextUtils.descrubName(value);
								} else if (key.equals("x")) {
									x = Integer.parseInt(value);
								} else if (key.equals("z")) {
									z = Integer.parseInt(value);
								} else if (key.equals("y")) {
									y = Integer.parseInt(value);
								} else if (key.equals("enabled")) {
									enabled = Boolean.parseBoolean(value);
								} else if (key.equals("red")) {
									red = Float.parseFloat(value);
								} else if (key.equals("green")) {
									green = Float.parseFloat(value);
								} else if (key.equals("blue")) {
									blue = Float.parseFloat(value);
								} else if (key.equals("suffix")) {
									suffix = value;
								} else if (key.equals("world")) {
									world = TextUtils.descrubName(value);
								} else if (key.equals("dimensions")) {
									String[] dimensionStrings = value.split("#");

									for (int s = 0; s < dimensionStrings.length; s++) {
										dimensions.add(Integer.parseInt(dimensionStrings[s]));
									}

									if (dimensions.size() == 0) {
										dimensions.add(0);
										dimensions.add(-1);
									}
								}
							}
						}

						if (!name.equals("")) {
							this.loadWaypoint(name, x, z, y, enabled, red, green, blue, suffix, world, dimensions);
							if (!world.equals("")) {
								this.knownSubworldNames.add(TextUtils.descrubName(world));
							}
						}
					}
				}

				in.close();
				return true;
			} catch (Exception local) {
				MessageUtils.chatInfo("§EError Loading Waypoints");
				System.err.println("waypoint load error: " + local.getLocalizedMessage());
				return false;
			}
		} else {
			return false;
		}
	}

	private boolean loadOldWaypoints(String worldNameWithoutPort, String worldNameWithDefaultPort, String worldNameWithPort) {
		try {
			this.settingsFile = new File(FilesystemUtils.getAppDir("minecraft/mods/zan", false), worldNameWithPort + ".points");
			if (!this.settingsFile.exists()) {
				this.settingsFile = new File(FilesystemUtils.getAppDir("minecraft/mods/zan", false), worldNameWithDefaultPort + ".points");
			}

			if (!this.settingsFile.exists()) {
				this.settingsFile = new File(FilesystemUtils.getAppDir("minecraft/mods/zan", false), worldNameWithoutPort + ".points");
			}

			if (!this.settingsFile.exists()) {
				this.settingsFile = new File(FilesystemUtils.getAppDir("minecraft", false), worldNameWithoutPort + ".points");
			}

			if (!this.settingsFile.exists()) {
				return false;
			}

			TreeSet<Integer> dimensions = new TreeSet<>();
			dimensions.add(-1);
			dimensions.add(0);
			BufferedReader in = new BufferedReader(new FileReader(this.settingsFile));

			String sCurrentLine;
			while ((sCurrentLine = in.readLine()) != null) {
				String[] curLine = sCurrentLine.split(":");
				if (curLine.length == 4) {
					this.loadWaypoint(
						curLine[0],
						Integer.parseInt(curLine[1]),
						Integer.parseInt(curLine[2]),
						-1,
						Boolean.parseBoolean(curLine[3]),
						0.0F,
						1.0F,
						0.0F,
						"",
						"",
						dimensions
					);
				} else if (curLine.length == 7) {
					this.loadWaypoint(
						curLine[0],
						Integer.parseInt(curLine[1]),
						Integer.parseInt(curLine[2]),
						-1,
						Boolean.parseBoolean(curLine[3]),
						Float.parseFloat(curLine[4]),
						Float.parseFloat(curLine[5]),
						Float.parseFloat(curLine[6]),
						"",
						"",
						dimensions
					);
				} else if (curLine.length == 8) {
					if (!curLine[3].contains("true") && !curLine[3].contains("false")) {
						this.loadWaypoint(
							curLine[0],
							Integer.parseInt(curLine[1]),
							Integer.parseInt(curLine[2]),
							Integer.parseInt(curLine[3]),
							Boolean.parseBoolean(curLine[4]),
							Float.parseFloat(curLine[5]),
							Float.parseFloat(curLine[6]),
							Float.parseFloat(curLine[7]),
							"",
							"",
							dimensions
						);
					} else {
						this.loadWaypoint(
							curLine[0],
							Integer.parseInt(curLine[1]),
							Integer.parseInt(curLine[2]),
							-1,
							Boolean.parseBoolean(curLine[3]),
							Float.parseFloat(curLine[4]),
							Float.parseFloat(curLine[5]),
							Float.parseFloat(curLine[6]),
							curLine[7],
							"",
							dimensions
						);
					}
				} else if (curLine.length == 9) {
					this.loadWaypoint(
						curLine[0],
						Integer.parseInt(curLine[1]),
						Integer.parseInt(curLine[2]),
						Integer.parseInt(curLine[3]),
						Boolean.parseBoolean(curLine[4]),
						Float.parseFloat(curLine[5]),
						Float.parseFloat(curLine[6]),
						Float.parseFloat(curLine[7]),
						curLine[8],
						"",
						dimensions
					);
				}
			}

			in.close();
			return true;
		} catch (Exception local) {
			MessageUtils.chatInfo("§EError Loading Waypoints");
			System.err.println("waypoint load error: " + local.getLocalizedMessage());
			return false;
		}
	}

	private boolean findReiWaypoints(String worldNameWithoutPort) {
		boolean foundSome = false;
		this.settingsFile = new File(FilesystemUtils.getAppDir("minecraft/mods/rei_minimap", false), worldNameWithoutPort + ".points");
		if (!this.settingsFile.exists()) {
			this.settingsFile = new File(Minecraft.getMinecraft().mcDataDir, "/mods/rei_minimap/" + worldNameWithoutPort + ".points");
		}

		if (this.settingsFile.exists()) {
			this.loadReiWaypoints(this.settingsFile, 0);
			foundSome = true;
		} else {
			for (int t = -25; t < 25; t++) {
				this.settingsFile = new File(FilesystemUtils.getAppDir("minecraft/mods/rei_minimap", false), worldNameWithoutPort + ".DIM" + t + ".points");
				if (!this.settingsFile.exists()) {
					this.settingsFile = new File(Minecraft.getMinecraft().mcDataDir, "/mods/rei_minimap/" + worldNameWithoutPort + ".DIM" + t + ".points");
				}

				if (this.settingsFile.exists()) {
					foundSome = true;
					this.loadReiWaypoints(this.settingsFile, t);
				}
			}
		}

		return foundSome;
	}

	private void loadReiWaypoints(File settingsFile, int dimension) {
		try {
			if (settingsFile.exists()) {
				TreeSet<Integer> dimensions = new TreeSet<>();
				dimensions.add(dimension);
				BufferedReader in = new BufferedReader(new FileReader(settingsFile));

				String sCurrentLine;
				while ((sCurrentLine = in.readLine()) != null) {
					String[] curLine = sCurrentLine.split(":");
					if (curLine.length == 6) {
						int color = Integer.parseInt(curLine[5], 16);
						float red = (color >> 16 & 0xFF) / 255.0F;
						float green = (color >> 8 & 0xFF) / 255.0F;
						float blue = (color >> 0 & 0xFF) / 255.0F;
						int x = Integer.parseInt(curLine[1]);
						int z = Integer.parseInt(curLine[3]);
						if (dimension == -1) {
							x *= 8;
							z *= 8;
						}

						this.loadWaypoint(curLine[0], x, z, Integer.parseInt(curLine[2]), Boolean.parseBoolean(curLine[4]), red, green, blue, "", "", dimensions);
					}
				}

				in.close();
			}
		} catch (Exception e) {
			MessageUtils.chatInfo("§EError Loading Old Rei Waypoints");
			System.err.println("waypoint load error: " + e.getLocalizedMessage());
		}
	}

	private void loadWaypoint(
		String name, int x, int z, int y, boolean enabled, float red, float green, float blue, String suffix, String world, TreeSet<Integer> dimensions
	) {
		Waypoint newWaypoint = new Waypoint(name, x, z, y, enabled, red, green, blue, suffix, world, dimensions);
		if (!this.wayPts.contains(newWaypoint)) {
			this.wayPts.add(newWaypoint);
		}
	}

	private void populateOld2dWaypoints() {
		this.old2dWayPts = new ArrayList<>();

		for (Waypoint wpt : this.wayPts) {
			if (wpt.getY() <= 0) {
				this.old2dWayPts.add(wpt);
			}
		}
	}

	@Override
	public void check2dWaypoints() {
		if (Minecraft.getMinecraft().player.dimension == 0 && this.old2dWayPts.size() > 0) {
			this.updatedPts = new ArrayList<>();

			for (Waypoint pt : this.old2dWayPts) {
				BlockPos blockPos = new BlockPos(pt.getX(), 0, pt.getZ());
				Chunk chunk = Minecraft.getMinecraft().player.world.getChunkFromBlockCoords(blockPos);
				if (Math.abs(pt.getX() - GameVariableAccessShim.xCoord()) < 400
					&& Math.abs(pt.getZ() - GameVariableAccessShim.zCoord()) < 400
					&& chunk.isLoaded()) {
					pt.setY(chunk.getHeight(blockPos));
					this.updatedPts.add(pt);
					this.saveWaypoints();
				}
			}

			this.old2dWayPts.removeAll(this.updatedPts);
			System.out.println("remaining old 2d waypoints: " + this.old2dWayPts.size());
		}
	}

	@Override
	public void deleteWaypoint(Waypoint point) {
		this.old2dWayPts.remove(point);
		this.waypointContainer.removeWaypoint(point);
		this.wayPts.remove(point);
		this.saveWaypoints();
		if (point == this.highlightedWaypoint) {
			this.setHighlightedWaypoint(null, false);
		}
	}

	@Override
	public void addWaypoint(Waypoint newWaypoint) {
		this.wayPts.add(newWaypoint);
		this.waypointContainer.addWaypoint(newWaypoint);
		this.saveWaypoints();
		if (this.highlightedWaypoint != null && this.highlightedWaypoint.getX() == newWaypoint.getX() && this.highlightedWaypoint.getZ() == newWaypoint.getZ()) {
			this.setHighlightedWaypoint(newWaypoint, false);
		}
	}

	@Override
	public void setHighlightedWaypoint(Waypoint waypoint, boolean toggle) {
		if (toggle && waypoint == this.highlightedWaypoint) {
			this.highlightedWaypoint = null;
		} else {
			if (waypoint != null && !this.wayPts.contains(waypoint)) {
				waypoint.red = 2.0F;
				waypoint.blue = 0.0F;
				waypoint.green = 0.0F;
			}

			this.highlightedWaypoint = waypoint;
		}

		this.waypointContainer.setHighlightedWaypoint(this.highlightedWaypoint);
	}

	@Override
	public Waypoint getHighlightedWaypoint() {
		return this.highlightedWaypoint;
	}

	@Override
	public void renderWaypoints(float partialTicks) {
		if (this.waypointContainer != null) {
			this.waypointContainer.renderWaypoints(partialTicks);
		}
	}

	private void loadBackgroundMapImage() {
		if (this.backgroundImageInfo != null) {
			GLUtils.glah(this.backgroundImageInfo.glid);
			this.backgroundImageInfo = null;
		}

		try {
			String path = this.getCurrentWorldName();
			String subworldDescriptor = this.getCurrentSubworldDescriptor(false);
			if (subworldDescriptor != null && !subworldDescriptor.equals("")) {
				path = path + "/" + subworldDescriptor;
			}

			path = path + "/" + (int) this.currentDimension;
			InputStream is = this.game
				.getResourceManager()
				.getResource(new ResourceLocation("voxelmap", "images/backgroundmaps/" + path + "/map.png"))
				.getInputStream();
			Image image = ImageIO.read(is);
			is.close();
			BufferedImage mapImage = new BufferedImage(image.getWidth(null), image.getHeight(null), 2);
			Graphics gfx = mapImage.createGraphics();
			gfx.drawImage(image, 0, 0, null);
			gfx.dispose();
			is = this.game.getResourceManager().getResource(new ResourceLocation("voxelmap", "images/backgroundmaps/" + path + "/map.txt")).getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			Properties mapProperties = new Properties();
			mapProperties.load(isr);
			String left = mapProperties.getProperty("left");
			String right = mapProperties.getProperty("right");
			String top = mapProperties.getProperty("top");
			String bottom = mapProperties.getProperty("bottom");
			String width = mapProperties.getProperty("width");
			String height = mapProperties.getProperty("height");
			String scale = mapProperties.getProperty("scale");
			if (left != null && top != null && width != null && height != null) {
				this.backgroundImageInfo = new BackgroundImageInfo(
					mapImage, Integer.parseInt(left), Integer.parseInt(top), Integer.parseInt(width), Integer.parseInt(height)
				);
			} else if (left != null && top != null && scale != null) {
				this.backgroundImageInfo = new BackgroundImageInfo(mapImage, Integer.parseInt(left), Integer.parseInt(top), Float.parseFloat(scale));
			} else if (left != null && top != null && right != null && bottom != null) {
				int widthInt = Integer.parseInt(right) - Integer.parseInt(left);
				int heightInt = Integer.parseInt(right) - Integer.parseInt(left);
				this.backgroundImageInfo = new BackgroundImageInfo(mapImage, Integer.parseInt(left), Integer.parseInt(top), widthInt, heightInt);
			}

			isr.close();
		} catch (Exception var18) {
		}
	}

	@Override
	public BackgroundImageInfo getBackgroundImageInfo() {
		return this.backgroundImageInfo;
	}
}
