package com.mamiyaotaru.voxelmap.persistent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mamiyaotaru.voxelmap.interfaces.IPersistentMap;
import com.mamiyaotaru.voxelmap.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Scanner;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ComparisonCachedRegion {
	private final IPersistentMap persistentMap;
	private final String key;
	private final World world;
	private final String worldName;
	private final String subworldName;
	private final String worldNamePathPart;
	private final String dimensionNamePathPart;
	private final boolean underground;
	private final int x;
	private final int z;
	private final CompressibleMapData data;
	MutableBlockPos blockPos = new MutableBlockPos(0, 0, 0);
	private String subworldNamePathPart;
	private int loadedChunks = 0;
	private boolean loaded = false;
	private boolean empty = true;

	public ComparisonCachedRegion(IPersistentMap persistentMap, String key, World world, String worldName, String subworldName, int x, int z) {
		this.data = new CompressibleMapData(256, 256);
		this.persistentMap = persistentMap;
		this.key = key;
		this.world = world;
		this.worldName = worldName;
		this.subworldName = subworldName;
		this.worldNamePathPart = TextUtils.scrubNameFile(worldName);
		if (subworldName != "") {
			this.subworldNamePathPart = TextUtils.scrubNameFile(subworldName) + "/";
		}

		String dimensionName = world.provider.getDimensionType().getName();
		int dimensionID = DimensionManager.getDimensionIDfromProvider(world.provider);
		this.dimensionNamePathPart = TextUtils.scrubNameFile(dimensionName + " (dimension " + dimensionID + ")");
		boolean knownUnderground = false;
		this.underground = !world.provider.isSurfaceWorld() &&
//			!world.provider.hasSkyLight() &&
			dimensionID != 1 || knownUnderground;
		this.x = x;
		this.z = z;
	}

	public void loadCurrent() {
		this.loadedChunks = 0;

		for (int t = 0; t < 16; t++) {
			for (int s = 0; s < 16; s++) {
				Chunk chunk = this.world.getChunkFromChunkCoords(this.x * 16 + t, this.z * 16 + s);
				if (chunk != null && chunk.isLoaded() && !chunk.isEmpty() && chunk.isPopulated()) {
					this.loadChunkData(chunk, t, s);
					this.loadedChunks++;
				}
			}
		}
	}

	private void loadChunkData(Chunk chunk, int chunkX, int chunkZ) {
		for (int t = 0; t < 16; t++) {
			for (int s = 0; s < 16; s++) {
				this.persistentMap
					.getAndStoreData(this.data, this.world, chunk, this.blockPos, this.underground, this.x * 256, this.z * 256, chunkX * 16 + t, chunkZ * 16 + s);
			}
		}
	}

	public void loadStored() {
		try {
			File cachedRegionFileDir = new File(
				Minecraft.getMinecraft().mcDataDir, "/voxelmap/cache/" + this.worldNamePathPart + "/" + this.subworldNamePathPart + this.dimensionNamePathPart
			);
			cachedRegionFileDir.mkdirs();
			File cachedRegionFile = new File(cachedRegionFileDir, "/" + this.key + ".zip");
			if (cachedRegionFile.exists()) {
				FileInputStream fis = new FileInputStream(cachedRegionFile);
				ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
				Scanner sc = new Scanner(zis);
				BiMap<IBlockState, Integer> stateToInt = null;
				int version = 1;
				int total = 0;

				ZipEntry ze;
				byte[] decompressedByteData;
				for (decompressedByteData = new byte[this.data.getWidth() * this.data.getHeight() * 17 * 4]; (ze = zis.getNextEntry()) != null; zis.closeEntry()) {
					if (ze.getName().equals("data")) {
						byte[] data = new byte[2048];

						int count;
						while ((count = zis.read(data, 0, 2048)) != -1 && count + total <= this.data.getWidth() * this.data.getHeight() * 17 * 4) {
							System.arraycopy(data, 0, decompressedByteData, total, count);
							total += count;
						}
					}

					if (ze.getName().equals("key")) {
						stateToInt = HashBiMap.create();

						while (sc.hasNextLine()) {
							BlockStateParser.parseLine(sc.nextLine(), stateToInt);
						}
					}

					if (ze.getName().equals("control")) {
						Properties properties = new Properties();
						properties.load(zis);
						String versionString = properties.getProperty("version", "1");

						try {
							version = Integer.parseInt(versionString);
						} catch (NumberFormatException ex) {
							version = 1;
						}
					}
				}

				if (total == this.data.getWidth() * this.data.getHeight() * 18 && stateToInt != null) {
					byte[] byteData = new byte[this.data.getWidth() * this.data.getHeight() * 18];
					System.arraycopy(decompressedByteData, 0, byteData, 0, byteData.length);
					this.data.setData(byteData, stateToInt, version);
					this.empty = false;
					this.loaded = true;
				} else {
					System.out.println("failed to load data from " + cachedRegionFile.getPath());
				}

				sc.close();
				zis.close();
				fis.close();
			}
		} catch (IOException e) {
			System.err
				.println(
					"Failed to load region file for "
						+ this.x
						+ ","
						+ this.z
						+ " in "
						+ this.worldNamePathPart
						+ "/"
						+ this.subworldNamePathPart
						+ this.dimensionNamePathPart
				);
			e.printStackTrace();
		}
	}

	public String getSubworldName() {
		return this.subworldName;
	}

	public String getKey() {
		return this.key;
	}

	public CompressibleMapData getMapData() {
		return this.data;
	}

	public boolean isLoaded() {
		return this.loaded;
	}

	public boolean isEmpty() {
		return this.empty;
	}

	public int getLoadedChunks() {
		return this.loadedChunks;
	}

	public boolean isGroundAt(int blockX, int blockZ) {
		return this.isLoaded() && this.getHeightAt(blockX, blockZ) > 0;
	}

	public int getHeightAt(int blockX, int blockZ) {
		int x = blockX - this.x * 256;
		int z = blockZ - this.z * 256;
		int y = this.data.getHeight(x, z);
		if (this.underground && y == 255) {
			y = CommandUtils.getSafeHeight(blockX, 64, blockZ, this.world);
		}

		return y;
	}

	public int getSimilarityTo(ComparisonCachedRegion candidate) {
		int compared = 0;
		int matched = 0;
		CompressibleMapData candidateData = candidate.getMapData();

		for (int t = 0; t < 16; t++) {
			for (int s = 0; s < 16; s++) {
				int nonZeroHeights = 0;
				int nonZeroHeightsInCandidate = 0;
				int matchesInChunk = 0;

				for (int i = 0; i < 16; i++) {
					for (int j = 0; j < 16; j++) {
						int x = t * 16 + i;
						int z = s * 16 + j;
						if (this.data.getHeight(x, z) == candidateData.getHeight(x, z)
							&& this.data.getBlockstate(x, z) == candidateData.getBlockstate(x, z)
							&& (
							this.data.getOceanFloorHeight(x, z) == 0
								|| this.data.getOceanFloorHeight(x, z) == candidateData.getOceanFloorHeight(x, z)
								&& this.data.getOceanFloorBlockstate(x, z) == candidateData.getOceanFloorBlockstate(x, z)
						)) {
							matchesInChunk++;
						}

						if (this.data.getHeight(x, z) != 0) {
							nonZeroHeights++;
						}

						if (candidateData.getHeight(x, z) != 0) {
							nonZeroHeightsInCandidate++;
						}
					}
				}

				if (nonZeroHeights != 0 && nonZeroHeightsInCandidate != 0) {
					compared += 256;
					matched += matchesInChunk;
				}
			}
		}

		MessageUtils.printDebug("compared: " + compared + ", matched: " + matched);
		return compared >= 256 ? matched * 100 / compared : 0;
	}
}
