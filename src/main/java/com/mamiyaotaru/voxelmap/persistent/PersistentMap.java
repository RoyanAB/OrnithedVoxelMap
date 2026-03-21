package com.mamiyaotaru.voxelmap.persistent;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.interfaces.*;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.BlockGlass;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PersistentMap implements IPersistentMap, IChangeObserver {
	protected final List<CachedRegion> cachedRegionsPool = Collections.synchronizedList(new ArrayList<>());
	protected final ConcurrentHashMap<String, CachedRegion> cachedRegions = new ConcurrentHashMap<>(150, 0.9F, 2);
	IVoxelMap master;
	IColorManager colorManager;
	MapSettingsManager mapOptions;
	PersistentMapSettingsManager options;
	WorldMatcher worldMatcher;
	int[] lightmapColors;
	World world;
	String subworldName = "";
	int lastLeft = 0;
	int lastRight = 0;
	int lastTop = 0;
	int lastBottom = 0;
	CachedRegion[] lastRegionsArray = new CachedRegion[0];
	Comparator<CachedRegion> ageThenDistanceSorter = (region1, region2) -> {
		long mostRecentAccess1 = region1.getMostRecentView();
		long mostRecentAccess2 = region2.getMostRecentView();
		if (mostRecentAccess1 < mostRecentAccess2) {
			return 1;
		}

		if (mostRecentAccess1 > mostRecentAccess2) {
			return -1;
		}

		double distance1sq = (region1.getX() * 256 + (double) region1.getWidth() / 2 - PersistentMap.this.options.mapX)
			* (region1.getX() * 256 + (double) region1.getWidth() / 2 - PersistentMap.this.options.mapX)
			+ (region1.getZ() * 256 + (double) region1.getWidth() / 2 - PersistentMap.this.options.mapZ)
			* (region1.getZ() * 256 + (double) region1.getWidth() / 2 - PersistentMap.this.options.mapZ);
		double distance2sq = (region2.getX() * 256 + (double) region2.getWidth() / 2 - PersistentMap.this.options.mapX)
			* (region2.getX() * 256 + (double) region2.getWidth() / 2 - PersistentMap.this.options.mapX)
			+ (region2.getZ() * 256 + (double) region2.getWidth() / 2 - PersistentMap.this.options.mapZ)
			* (region2.getZ() * 256 + (double) region2.getWidth() / 2 - PersistentMap.this.options.mapZ);
		return Double.compare(distance1sq, distance2sq);
	};
	Comparator<PersistentMap.RegionCoordinates> distanceSorter = (coordinates1, coordinates2) -> {
		double distance1sq = (coordinates1.x * 256 + 128 - PersistentMap.this.options.mapX) * (coordinates1.x * 256 + 128 - PersistentMap.this.options.mapX)
			+ (coordinates1.z * 256 + 128 - PersistentMap.this.options.mapZ) * (coordinates1.z * 256 + 128 - PersistentMap.this.options.mapZ);
		double distance2sq = (coordinates2.x * 256 + 128 - PersistentMap.this.options.mapX) * (coordinates2.x * 256 + 128 - PersistentMap.this.options.mapX)
			+ (coordinates2.z * 256 + 128 - PersistentMap.this.options.mapZ) * (coordinates2.z * 256 + 128 - PersistentMap.this.options.mapZ);
		return Double.compare(distance1sq, distance2sq);
	};

	public PersistentMap(IVoxelMap master) {
		this.master = master;
		this.colorManager = master.getColorManager();
		this.mapOptions = master.getMapOptions();
		this.options = master.getPersistentMapOptions();
		this.lightmapColors = new int[256];
		Arrays.fill(this.lightmapColors, -16777216);
	}

	@Override
	public void newWorld(World world) {
		this.purgeCachedRegions();
		this.world = world;
		if (this.worldMatcher != null) {
			this.worldMatcher.cancel();
		}

		if (world != null) {
			this.newWorldStuff();
		} else {
			Thread pauseForSubworldNamesThread = new Thread(null, null, "VoxelMap Pause for Subworld Name Thread") {
				@Override
				public void run() {
					try {
						Thread.sleep(1000L);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

					if (PersistentMap.this.world != null) {
						PersistentMap.this.newWorldStuff();
					}
				}
			};
			pauseForSubworldNamesThread.start();
		}
	}

	private void newWorldStuff() {
		String worldName = TextUtils.scrubNameFile(this.master.getWaypointManager().getCurrentWorldName());
		File oldCacheDir = new File(Minecraft.getMinecraft().gameDir, "/mods/mamiyaotaru/voxelmap/cache/" + worldName + "/");
		if (oldCacheDir.exists() && oldCacheDir.isDirectory()) {
			File newCacheDir = new File(Minecraft.getMinecraft().gameDir, "/voxelmap/cache/" + worldName + "/");
			newCacheDir.getParentFile().mkdirs();
			boolean success = oldCacheDir.renameTo(newCacheDir);
			if (!success) {
				System.out.println("Failed moving Voxelmap cache files.  Please move " + oldCacheDir.getPath() + " to " + newCacheDir.getPath());
			} else {
				System.out.println("Moved Voxelmap cache files from " + oldCacheDir.getPath() + " to " + newCacheDir.getPath());
			}
		}

		if (this.master.getWaypointManager().isMultiworld()
			&& !Minecraft.getMinecraft().isIntegratedServerRunning()
			&& !this.master.getWaypointManager().receivedAutoSubworldName()) {
			this.worldMatcher = new WorldMatcher(this.master, this, this.world);
			this.worldMatcher.findMatch();
		}
	}

	@Override
	public void onTick(Minecraft mc) {
		if (mc.currentScreen == null) {
			this.options.mapX = GameVariableAccessShim.xCoord();
			this.options.mapZ = GameVariableAccessShim.zCoord();
		}

		if (!this.master.getWaypointManager().getCurrentSubworldDescriptor(false).equals(this.subworldName)) {
			this.subworldName = this.master.getWaypointManager().getCurrentSubworldDescriptor(false);
			if (this.worldMatcher != null && !this.subworldName.isEmpty()) {
				this.worldMatcher.cancel();
			}

			this.purgeCachedRegions();
		}
	}

	@Override
	public PersistentMapSettingsManager getOptions() {
		return this.options;
	}

	@Override
	public void purgeCachedRegions() {
		synchronized (this.cachedRegionsPool) {
			for (CachedRegion cachedRegion : this.cachedRegionsPool) {
				cachedRegion.cleanup();
			}

			this.cachedRegions.clear();
			this.cachedRegionsPool.clear();
			this.getRegions(0, -1, 0, -1);
		}
	}

	@Override
	public void saveCachedRegions() {
		synchronized (this.cachedRegionsPool) {
			for (CachedRegion cachedRegion : this.cachedRegionsPool) {
				cachedRegion.save();
			}
		}
	}

	@Override
	public void renameSubworld(String oldName, String newName) {
		synchronized (this.cachedRegionsPool) {
			for (CachedRegion cachedRegion : this.cachedRegionsPool) {
				cachedRegion.renameSubworld(oldName, newName);
			}
		}
	}

	@Override
	public ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier() {
		return this.master.getSettingsAndLightingChangeNotifier();
	}

	@Override
	public void setLightMapArray(int[] lightmapColors) {
		boolean changed = false;
		int torchOffset = 0;
		int skylightMultiplier = 16;

		for (int t = 0; t < 16; t++) {
			if (lightmapColors[t * skylightMultiplier + torchOffset] != this.lightmapColors[t * skylightMultiplier + torchOffset]) {
				changed = true;
				break;
			}
		}

		this.lightmapColors = lightmapColors;
		if (changed) {
			this.getSettingsAndLightingChangeNotifier().notifyOfChanges();
		}
	}

	@Override
	public void getAndStoreData(
		AbstractMapData mapData, World world, Chunk chunk, MutableBlockPos blockPos, boolean underground, int startX, int startZ, int imageX, int imageY
	) {
		blockPos = blockPos.withXYZ(startX + imageX, 0, startZ + imageY);
		IBlockState blockState;
		int biomeID;
		if (!chunk.isEmpty()) {
			biomeID = Biome.getIdForBiome(chunk.getBiome(blockPos, world.provider.getBiomeProvider()));
		} else {
			biomeID = -1;
		}

		mapData.setBiomeID(imageX, imageY, biomeID);
		if (biomeID != -1) {
			int surfaceHeight;
			boolean solid = false;
			surfaceHeight = this.getBlockHeight(underground, chunk, blockPos, startX + imageX, startZ + imageY);
			blockState = chunk.getBlockState(blockPos.withXYZ(startX + imageX, surfaceHeight, startZ + imageY));
			if (blockState.getMaterial() != Material.SNOW) {
				blockState = chunk.getBlockState(blockPos.withXYZ(startX + imageX, surfaceHeight - 1, startZ + imageY));
			}

			blockState = blockState.getActualState(world, blockPos);
			mapData.setHeight(imageX, imageY, surfaceHeight);
			mapData.setBlockstate(imageX, imageY, blockState);
			if (surfaceHeight == -1) {
				surfaceHeight = 80;
				solid = true;
			}

			if (blockState.getMaterial() == Material.LAVA) {
				solid = false;
			}

			int light;
			if (!solid) {
				light = this.getLight(blockState, chunk, blockPos, startX + imageX, startZ + imageY, surfaceHeight, solid);
				mapData.setLight(imageX, imageY, light);
			}

			int seafloorHeight = 0;
			int seafloorLight = 0;
			int underwaterTransparentHeight = 0;
			Material material = blockState.getMaterial();
			if (material == Material.WATER || material == Material.ICE) {
				int[] underwaterHeights = this.getSeafloorHeight(chunk, blockPos, startX + imageX, startZ + imageY, surfaceHeight);
				seafloorHeight = underwaterHeights[0];
				underwaterTransparentHeight = underwaterHeights[1];
				blockPos.setXYZ(startX + imageX, seafloorHeight - 1, startZ + imageY);
				blockState = chunk.getBlockState(blockPos);
				if (blockState.getMaterial() == Material.WATER) {
					blockState = BlockRepository.air.getDefaultState();
				}
			}

			if (blockState != BlockRepository.air.getDefaultState()) {
				blockState = blockState.getActualState(world, blockPos);
				seafloorLight = this.getLight(blockState, chunk, blockPos, startX + imageX, startZ + imageY, seafloorHeight, solid);
			}

			mapData.setOceanFloorHeight(imageX, imageY, seafloorHeight);
			mapData.setOceanFloorBlockstate(imageX, imageY, blockState);
			mapData.setOceanFloorLight(imageX, imageY, seafloorLight);
			int transparentHeight;
			int transparentLight = 0;
			transparentHeight = this.getTransparentHeight(underground, chunk, blockPos, startX + imageX, startZ + imageY, surfaceHeight);
			if (transparentHeight == 0 && underwaterTransparentHeight > 0) {
				transparentHeight = underwaterTransparentHeight;
			}

			if (transparentHeight != 0) {
				blockPos.setXYZ(startX + imageX, transparentHeight - 1, startZ + imageY);
				blockState = chunk.getBlockState(blockPos);
			} else {
				blockState = BlockRepository.air.getDefaultState();
			}

			if (blockState != BlockRepository.air.getDefaultState()) {
				blockState = blockState.getActualState(world, blockPos);
				transparentLight = this.getLight(blockState, chunk, blockPos, startX + imageX, startZ + imageY, transparentHeight, solid);
			}

			mapData.setTransparentHeight(imageX, imageY, transparentHeight);
			mapData.setTransparentBlockstate(imageX, imageY, blockState);
			mapData.setTransparentLight(imageX, imageY, transparentLight);
			int foliageHeight = 0;
			int foliageLight = 0;
			IBlockState foliageBlockState = null;
			if (transparentHeight != surfaceHeight + 1 && !solid) {
				foliageHeight = surfaceHeight + 1;
				blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
				foliageBlockState = chunk.getBlockState(blockPos);
				material = foliageBlockState.getMaterial();
				if (material == Material.SNOW || material == Material.AIR || material == Material.LAVA) {
					foliageHeight = 0;
				}

				if (foliageBlockState == blockState) {
					foliageHeight = 0;
				}
			}

			if (foliageHeight == 0 && !solid && seafloorHeight > 0 && transparentHeight != seafloorHeight + 1) {
				foliageHeight = seafloorHeight + 1;
				blockPos.setXYZ(startX + imageX, foliageHeight - 1, startZ + imageY);
				foliageBlockState = chunk.getBlockState(blockPos);
				material = foliageBlockState.getMaterial();
				if (material == Material.AIR
					|| material == Material.LAVA
					|| material == Material.WATER
					|| material == Material.ICE) {
					foliageHeight = 0;
				}

				if (foliageBlockState == blockState) {
					foliageHeight = 0;
				}
			}

			if (foliageHeight != 0) {
				blockState = foliageBlockState;
			} else {
				blockState = BlockRepository.air.getDefaultState();
			}

			if (blockState != BlockRepository.air.getDefaultState()) {
				blockState = blockState.getActualState(world, blockPos);
				foliageLight = this.getLight(blockState, chunk, blockPos, startX + imageX, startZ + imageY, foliageHeight, solid);
			}

			mapData.setFoliageHeight(imageX, imageY, foliageHeight);
			mapData.setFoliageBlockstate(imageX, imageY, blockState);
			mapData.setFoliageLight(imageX, imageY, foliageLight);
		}
	}

	private int getBlockHeight(boolean underground, Chunk chunk, MutableBlockPos blockPos, int x, int z) {
		int playerHeight = 80;
		blockPos.setXYZ(x, playerHeight, z);
		int height = chunk.getHeight(blockPos);
		if (!underground) {
			int transHeight = chunk.getPrecipitationHeight(blockPos).getY();
			if (transHeight != height) {
				IBlockState blockState = chunk.getBlockState(blockPos.withXYZ(x, transHeight - 1, z));
				if (blockState.getMaterial() == Material.LAVA) {
					height = transHeight;
				}
			}

			return height;
		} else {
			int y = playerHeight;
			blockPos.setXYZ(x, y, z);
			IBlockState blockState = chunk.getBlockState(blockPos);
			if (blockState.getLightOpacity() == 0 && blockState.getMaterial() != Material.LAVA) {
				while (y > 0) {
					blockPos.setXYZ(x, --y, z);
					blockState = chunk.getBlockState(blockPos);
					if (blockState.getLightOpacity() > 0 || blockState.getMaterial() == Material.LAVA) {
						return y + 1;
					}
				}

				return y;
			} else {
				while (y <= playerHeight + 10) {
					blockPos.setXYZ(x, ++y, z);
					blockState = chunk.getBlockState(blockPos);
					if (blockState.getLightOpacity() == 0 && blockState.getMaterial() != Material.LAVA) {
						return y;
					}
				}

				return -1;
			}
		}
	}

	private int[] getSeafloorHeight(Chunk chunk, MutableBlockPos blockPos, int x, int z, int height) {
		int seafloorHeight = height;
		int underwaterTransparentHeight = -1;
		IBlockState blockState = chunk.getBlockState(blockPos.withXYZ(x, seafloorHeight - 1, z));

		while (blockState.getLightOpacity() < 5 && blockState.getMaterial() != Material.LEAVES && seafloorHeight > 1) {
			seafloorHeight--;
			blockState = chunk.getBlockState(blockPos.withXYZ(x, seafloorHeight - 1, z));
			if (blockState.getMaterial().blocksMovement() && blockState.getMaterial() != Material.ICE && underwaterTransparentHeight == -1) {
				underwaterTransparentHeight = seafloorHeight;
			}
		}

		return new int[]{seafloorHeight, underwaterTransparentHeight};
	}

	private int getTransparentHeight(boolean underground, Chunk chunk, MutableBlockPos blockPos, int x, int z, int height) {
		int transHeight;
		if (underground) {
			transHeight = 0;
		} else {
			transHeight = chunk.getPrecipitationHeight(blockPos.withXYZ(x, height, z)).getY();
			if (transHeight <= height) {
				transHeight = 0;
			}
		}

		IBlockState blockState = this.world.getBlockState(blockPos.withXYZ(x, transHeight - 1, z));
		Material material = blockState.getMaterial();
		if (transHeight == height + 1 && material == Material.SNOW) {
			transHeight = -1;
		}

		if (material == Material.BARRIER) {
			transHeight++;
			blockState = this.world.getBlockState(blockPos.withXYZ(x, transHeight - 1, z));
			material = blockState.getMaterial();
			if (material == Material.AIR) {
				transHeight = -1;
			}
		}

		return transHeight;
	}

	private int getLight(IBlockState blockState, Chunk chunk, MutableBlockPos blockPos, int x, int z, int height, boolean solid) {
		int i3 = 255;
		if (solid) {
			i3 = 0;
		} else if (blockState.getBlock() != BlockRepository.air) {
			blockPos.setXYZ(x, Math.max(Math.min(height, 255), 0), z);
			int blockLight = chunk.getLightFor(EnumSkyBlock.BLOCK, blockPos) & 15;
			int skyLight = chunk.getLightFor(EnumSkyBlock.SKY, blockPos);
			if (blockState.getMaterial() == Material.LAVA && blockLight < 14) {
				blockLight = 14;
			}

			i3 = blockLight + skyLight * 16;
		}

		return i3;
	}

	@Override
	public int getPixelColor(
		AbstractMapData mapData,
		World world,
		MutableBlockPos blockPos,
		MutableBlockPos loopBlockPos,
		boolean underground,
		int multi,
		int startX,
		int startZ,
		int imageX,
		int imageY
	) {
		int mcX = startX + imageX;
		int mcZ = startZ + imageY;
		int surfaceHeight;
		int seafloorHeight = -1;
		int transparentHeight = -1;
		int foliageHeight = -1;
		int surfaceColor;
		int seafloorColor = 0;
		int transparentColor = 0;
		int foliageColor = 0;
		blockPos = blockPos.withXYZ(mcX, 0, mcZ);
		IBlockState blockState;
		int color24;
		int biomeID = mapData.getBiomeID(imageX, imageY);
		blockState = mapData.getBlockstate(imageX, imageY);
		if (blockState == null
			|| blockState.getBlock() == BlockRepository.air && mapData.getLight(imageX, imageY) == 0 && mapData.getHeight(imageX, imageY) == 0
			|| biomeID == -1
			|| biomeID == 255) {
			return 0;
		}

		if (this.mapOptions.biomeOverlay == 1) {
			if (biomeID >= 0) {
				color24 = BiomeRepository.getBiomeColor(biomeID) | 0xFF000000;
			} else {
				color24 = 0;
			}

			return this.doSlimeAndGrid(color24, mcX, mcZ);
		} else {
			boolean solid = false;
			int blockStateID;
			surfaceHeight = mapData.getHeight(imageX, imageY);
			blockStateID = BlockRepository.getStateId(blockState);
			if (surfaceHeight == -1 || surfaceHeight == 255) {
				surfaceHeight = 80;
				solid = true;
			}

			blockPos.setXYZ(mcX, surfaceHeight - 1, mcZ);
			if (blockState.getMaterial() == Material.LAVA) {
				solid = false;
			}

			if (this.mapOptions.biomes) {
				surfaceColor = this.colorManager.getBlockColor(blockPos, blockStateID, biomeID);
				int tint;
				tint = this.colorManager.getBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
				if (tint != -1) {
					surfaceColor = this.colorManager.colorMultiplier(surfaceColor, tint);
				}
			} else {
				surfaceColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
			}

			surfaceColor = this.applyHeight(mapData, surfaceColor, underground, multi, imageX, imageY, surfaceHeight, solid, 1);
			int light = mapData.getLight(imageX, imageY);
			if (solid) {
				surfaceColor = 0;
			} else if (this.mapOptions.lightmap) {
				int lightValue = this.getLight(light);
				surfaceColor = this.colorManager.colorMultiplier(surfaceColor, lightValue);
			}

			if (this.mapOptions.waterTransparency && !solid) {
				Material material = blockState.getMaterial();
				if (material == Material.WATER || material == Material.ICE) {
					seafloorHeight = mapData.getOceanFloorHeight(imageX, imageY);
					blockPos.setXYZ(mcX, seafloorHeight - 1, mcZ);
					blockState = mapData.getOceanFloorBlockstate(imageX, imageY);
					if (blockState != null && blockState != BlockRepository.air.getDefaultState()) {
						blockStateID = BlockRepository.getStateId(blockState);
						if (this.mapOptions.biomes) {
							seafloorColor = this.colorManager.getBlockColor(blockPos, blockStateID, biomeID);
							int tint;
							tint = this.colorManager.getBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
							if (tint != -1) {
								seafloorColor = this.colorManager.colorMultiplier(seafloorColor, tint);
							}
						} else {
							seafloorColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
						}

						seafloorColor = this.applyHeight(mapData, seafloorColor, underground, multi, imageX, imageY, seafloorHeight, solid, 0);
						int seafloorLight;
						seafloorLight = mapData.getOceanFloorLight(imageX, imageY);
						if (this.mapOptions.lightmap) {
							int lightValue = this.getLight(seafloorLight);
							seafloorColor = this.colorManager.colorMultiplier(seafloorColor, lightValue);
						}
					}
				}
			}

			if (this.mapOptions.blockTransparency && !solid) {
				transparentHeight = mapData.getTransparentHeight(imageX, imageY);
				blockPos.setXYZ(mcX, transparentHeight - 1, mcZ);
				blockState = mapData.getTransparentBlockstate(imageX, imageY);
				if (blockState != null && blockState != BlockRepository.air.getDefaultState()) {
					blockStateID = BlockRepository.getStateId(blockState);
					if (this.mapOptions.biomes) {
						transparentColor = this.colorManager.getBlockColor(blockPos, blockStateID, biomeID);
						int tint;
						tint = this.colorManager.getBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
						if (tint != -1) {
							transparentColor = this.colorManager.colorMultiplier(transparentColor, tint);
						}
					} else {
						transparentColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
					}

					transparentColor = this.applyHeight(mapData, transparentColor, underground, multi, imageX, imageY, transparentHeight, solid, 3);
					int transparentLight;
					transparentLight = mapData.getTransparentLight(imageX, imageY);
					if (this.mapOptions.lightmap) {
						int lightValue = this.getLight(transparentLight);
						transparentColor = this.colorManager.colorMultiplier(transparentColor, lightValue);
					}
				}

				foliageHeight = mapData.getFoliageHeight(imageX, imageY);
				blockPos.setXYZ(mcX, foliageHeight - 1, mcZ);
				blockState = mapData.getFoliageBlockstate(imageX, imageY);
				if (blockState != null && blockState != BlockRepository.air.getDefaultState()) {
					blockStateID = BlockRepository.getStateId(blockState);
					if (this.mapOptions.biomes) {
						foliageColor = this.colorManager.getBlockColor(blockPos, blockStateID, biomeID);
						int tint;
						tint = this.colorManager.getBiomeTint(mapData, world, blockState, blockStateID, blockPos, loopBlockPos, startX, startZ);
						if (tint != -1) {
							foliageColor = this.colorManager.colorMultiplier(foliageColor, tint);
						}
					} else {
						foliageColor = this.colorManager.getBlockColorWithDefaultTint(blockPos, blockStateID);
					}

					foliageColor = this.applyHeight(mapData, foliageColor, underground, multi, imageX, imageY, foliageHeight, solid, 2);
					int foliageLight;
					foliageLight = mapData.getFoliageLight(imageX, imageY);
					if (this.mapOptions.lightmap) {
						int lightValue = this.getLight(foliageLight);
						foliageColor = this.colorManager.colorMultiplier(foliageColor, lightValue);
					}
				}
			}

			if (this.mapOptions.waterTransparency && seafloorHeight > 0) {
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

			if (this.mapOptions.biomeOverlay == 2) {
				int bc = 0;
				if (biomeID >= 0) {
					bc = BiomeRepository.getBiomeColor(biomeID);
				}

				bc = 2130706432 | bc;
				color24 = this.colorManager.colorAdder(bc, color24);
			}

			return this.doSlimeAndGrid(color24, mcX, mcZ);
		}
	}

	private int doSlimeAndGrid(int color24, int mcX, int mcZ) {
		if (this.mapOptions.slimeChunks && !this.master.getWorldSeed().isEmpty()) {
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

		if (this.mapOptions.chunkGrid) {
			if (mcX % 256 == 0 || mcZ % 256 == 0) {
				color24 = this.colorManager.colorAdder(2113863680, color24);
			} else if (mcX % 16 == 0 || mcZ % 16 == 0) {
				color24 = this.colorManager.colorAdder(2097152000, color24);
			}
		}

		return color24;
	}

	private int applyHeight(AbstractMapData mapData, int color24, boolean underground, int multi, int imageX, int imageY, int height, boolean solid, int layer) {
		if (color24 != this.colorManager.getAirColor() && color24 != 0) {
			int heightComp = -1;
			if ((this.mapOptions.heightmap || this.mapOptions.slopemap) && !solid) {
				int diff;
				double sc = 0.0;
				boolean invert = false;
				if (!this.mapOptions.slopemap) {
					diff = height - 80;
					sc = Math.log10(Math.abs(diff) / 8.0 + 1.0) / 1.8;
					if (diff < 0) {
						sc = 0.0 - sc;
					}
				} else {
					if (imageX > 0 && imageY < 32 * multi - 1) {
						if (layer == 0) {
							heightComp = mapData.getOceanFloorHeight(imageX - 1, imageY + 1);
						}

						if (layer == 1) {
							heightComp = mapData.getHeight(imageX - 1, imageY + 1);
						}

						if (layer == 2) {
							heightComp = height;
						}

						if (layer == 3) {
							heightComp = mapData.getTransparentHeight(imageX - 1, imageY + 1);
							if (heightComp == -1) {
								IBlockState transparentBlockState = mapData.getTransparentBlockstate(imageX, imageY);
								if (transparentBlockState != null && transparentBlockState != BlockRepository.air.getDefaultState()) {
									Block block = transparentBlockState.getBlock();
									if (block instanceof BlockGlass || block instanceof BlockStainedGlass) {
										heightComp = mapData.getHeight(imageX - 1, imageY + 1);
									}
								}
							}
						}
					} else if (imageX < 32 * multi - 1 && imageY > 0) {
						if (layer == 0) {
							heightComp = mapData.getOceanFloorHeight(imageX + 1, imageY - 1);
						}

						if (layer == 1) {
							heightComp = mapData.getHeight(imageX + 1, imageY - 1);
						}

						if (layer == 2) {
							heightComp = height;
						}

						if (layer == 3) {
							heightComp = mapData.getTransparentHeight(imageX + 1, imageY - 1);
							if (heightComp == -1) {
								IBlockState transparentBlockState = mapData.getTransparentBlockstate(imageX, imageY);
								if (transparentBlockState != null && transparentBlockState != BlockRepository.air.getDefaultState()) {
									Block block = transparentBlockState.getBlock();
									if (block instanceof BlockGlass || block instanceof BlockStainedGlass) {
										heightComp = mapData.getHeight(imageX + 1, imageY - 1);
									}
								}
							}
						}

						invert = true;
					} else {
						heightComp = height;
					}

					if (heightComp == -1) {
						heightComp = height;
					}

					if (!invert) {
						diff = heightComp - height;
					} else {
						diff = height - heightComp;
					}

					if (diff != 0) {
						sc = diff > 0 ? 1.0 : -1.0;
						sc /= 8.0;
					}

					if (this.mapOptions.heightmap) {
						diff = height - 80;
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

	private int getLight(int light) {
		return this.lightmapColors[light];
	}

	@Override
	public CachedRegion[] getRegions(int left, int right, int top, int bottom) {
		if (left == this.lastLeft && right == this.lastRight && top == this.lastTop && bottom == this.lastBottom) {
			return this.lastRegionsArray;
		}

		ThreadManager.emptyQueue();
		CachedRegion[] visibleCachedRegionsArray = new CachedRegion[(right - left + 1) * (bottom - top + 1)];
		String worldName = this.master.getWaypointManager().getCurrentWorldName();
		String subWorldName = this.master.getWaypointManager().getCurrentSubworldDescriptor(false);
		ArrayList<PersistentMap.RegionCoordinates> regionsToDisplay = new ArrayList<>();

		for (int t = left; t <= right; t++) {
			for (int s = top; s <= bottom; s++) {
				PersistentMap.RegionCoordinates regionCoordinates = new PersistentMap.RegionCoordinates(t, s);
				regionsToDisplay.add(regionCoordinates);
			}
		}

		regionsToDisplay.sort(this.distanceSorter);

		for (PersistentMap.RegionCoordinates regionCoordinates : regionsToDisplay) {
			int x = regionCoordinates.x;
			int z = regionCoordinates.z;
			String key = x + "," + z;
			CachedRegion cachedRegion;
			synchronized (this.cachedRegions) {
				cachedRegion = this.cachedRegions.get(key);
				if (cachedRegion == null) {
					cachedRegion = new CachedRegion(this, key, this.world, worldName, subWorldName, x, z);
					this.cachedRegions.put(key, cachedRegion);
					synchronized (this.cachedRegionsPool) {
						this.cachedRegionsPool.add(cachedRegion);
					}
				}
			}

			cachedRegion.refresh(true);
			visibleCachedRegionsArray[(z - top) * (right - left + 1) + (x - left)] = cachedRegion;
		}

		this.prunePool();
		synchronized (this.lastRegionsArray) {
			this.lastLeft = left;
			this.lastRight = right;
			this.lastTop = top;
			this.lastBottom = bottom;
			this.lastRegionsArray = visibleCachedRegionsArray;
			return visibleCachedRegionsArray;
		}
	}

	private void prunePool() {
		synchronized (this.cachedRegionsPool) {
			Iterator<CachedRegion> iterator = this.cachedRegionsPool.iterator();

			while (iterator.hasNext()) {
				CachedRegion region = iterator.next();
				if (region.isLoaded() && region.isEmpty()) {
					this.cachedRegions.put(region.getKey(), CachedRegion.emptyRegion);
					region.cleanup();
					iterator.remove();
				}
			}

			if (this.cachedRegionsPool.size() > this.options.cacheSize) {
				this.cachedRegionsPool.sort(this.ageThenDistanceSorter);
				List<CachedRegion> toRemove = this.cachedRegionsPool.subList(this.options.cacheSize, this.cachedRegionsPool.size());

				for (CachedRegion cachedRegion : toRemove) {
					this.cachedRegions.remove(cachedRegion.getKey());
					cachedRegion.cleanup();
				}

				toRemove.clear();
			}

			this.compress();
		}
	}

	@Override
	public void compress() {
		synchronized (this.cachedRegionsPool) {
			for (CachedRegion cachedRegion : this.cachedRegionsPool) {
				if (System.currentTimeMillis() - cachedRegion.getMostRecentChange() > 5000L) {
					cachedRegion.compress();
				}
			}
		}
	}

	@Override
	public void handleChangeInWorld(BlockPos pos1, BlockPos pos2) {
		Chunk chunk = this.world.getChunk(pos1);
		if (chunk != null && chunk.isLoaded() && !chunk.isEmpty()) {
			boolean isChunk = pos2 != null;
			int regionX = (int) Math.floor(pos1.getX() / 256.0);
			int regionZ = (int) Math.floor(pos1.getZ() / 256.0);
			String key = regionX + "," + regionZ;
			CachedRegion cachedRegion;
			synchronized (this.cachedRegions) {
				cachedRegion = this.cachedRegions.get(key);
				if (cachedRegion == null || cachedRegion == CachedRegion.emptyRegion) {
					String worldName = this.master.getWaypointManager().getCurrentWorldName();
					String subWorldName = this.master.getWaypointManager().getCurrentSubworldDescriptor(false);
					cachedRegion = new CachedRegion(this, key, this.world, worldName, subWorldName, regionX, regionZ);
					this.cachedRegions.put(key, cachedRegion);
					synchronized (this.cachedRegionsPool) {
						this.cachedRegionsPool.add(cachedRegion);
					}

					synchronized (this.lastRegionsArray) {
						if (regionX >= this.lastLeft && regionX <= this.lastRight && regionZ >= this.lastTop && regionZ <= this.lastBottom) {
							this.lastRegionsArray[(regionZ - this.lastTop) * (this.lastRight - this.lastLeft + 1) + (regionX - this.lastLeft)] = cachedRegion;
						}
					}
				}
			}

			if (isChunk) {
				if (Minecraft.getMinecraft().currentScreen != null && Minecraft.getMinecraft().currentScreen instanceof GuiPersistentMap) {
					cachedRegion.registerChangeAt(pos1.getX(), pos1.getZ());
					cachedRegion.refresh(false);
				} else {
					cachedRegion.loadChunk(this.world.getChunk(pos1));
				}
			} else {
				cachedRegion.registerChangeAt(pos1.getX(), pos1.getZ());
			}

			this.prunePool();
		}
	}

	@Override
	public void processChunk(Chunk chunk) {
	}

	@Override
	public boolean isRegionLoaded(int blockX, int blockZ) {
		int x = (int) Math.floor(blockX / 256.0F);
		int z = (int) Math.floor(blockZ / 256.0F);
		CachedRegion cachedRegion = this.cachedRegions.get(x + "," + z);
		return cachedRegion != null && cachedRegion.isLoaded();
	}

	@Override
	public boolean isGroundAt(int blockX, int blockZ) {
		int x = (int) Math.floor(blockX / 256.0F);
		int z = (int) Math.floor(blockZ / 256.0F);
		CachedRegion cachedRegion = this.cachedRegions.get(x + "," + z);
		return cachedRegion != null && cachedRegion.isGroundAt(blockX, blockZ);
	}

	@Override
	public int getHeightAt(int blockX, int blockZ) {
		int x = (int) Math.floor(blockX / 256.0F);
		int z = (int) Math.floor(blockZ / 256.0F);
		CachedRegion cachedRegion = this.cachedRegions.get(x + "," + z);
		return cachedRegion == null ? 64 : cachedRegion.getHeightAt(blockX, blockZ);
	}

	private class RegionCoordinates {
		int x;
		int z;

		public RegionCoordinates(int x, int z) {
			this.x = x;
			this.z = z;
		}
	}
}
