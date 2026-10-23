package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.persistent.CachedRegion;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;
import com.mamiyaotaru.voxelmap.util.MutableBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

/**
 * The full-screen persistent world map.
 *
 * <p>Stores already-scanned map data in cached {@link CachedRegion regions} so
 * that previously explored terrain remains visible even after the chunk is no
 * longer loaded.
 */
public interface IPersistentMap extends IChangeObserver {
	/**
	 * Called when a new world is loaded.
	 *
	 * @param world the newly loaded world, or {@code null} when leaving a world
	 */
	void newWorld(World world);

	/**
	 * Called on every client tick to update the tracked player position and
	 * react to subworld changes by purging stale cached regions.
	 *
	 * @param mc the Minecraft client instance
	 */
	void onTick(Minecraft mc);

	/**
	 * Returns the notifier that announces settings and lighting changes to the
	 * persistent map.
	 *
	 * @return the settings and lighting change notifier
	 */
	ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier();

	/**
	 * Sets the lightmap colors used to shade the persistent map.
	 *
	 * @param lightmapColors the new lightmap color array
	 */
	void setLightMapArray(int[] lightmapColors);

	/**
	 * Scans a single column of a chunk and stores its surface, ocean floor,
	 * transparent, and foliage data into the given map data.
	 *
	 * @param mapData     the map data to store into
	 * @param world       the current world
	 * @param chunk       the chunk being scanned
	 * @param blockPos    a reusable mutable position
	 * @param underground whether the region is underground (a world with no
	 *                    sky light), which changes how heights are resolved
	 * @param startX      the world X coordinate of the region's origin
	 * @param startZ      the world Z coordinate of the region's origin
	 * @param imageX      the column's x offset within the region
	 * @param imageY      the column's z offset within the region
	 */
	void getAndStoreData(AbstractMapData mapData, World world, Chunk chunk, MutableBlockPos blockPos, boolean underground, int startX, int startZ, int imageX, int imageY);

	/**
	 * Computes the color of a single pixel on the persistent map from stored
	 * map data.
	 *
	 * @param mapData      the map data to read from
	 * @param world        the current world
	 * @param blockPos     a reusable mutable position
	 * @param loopBlockPos a reusable mutable position for neighborhood lookups
	 * @param underground  whether the region is underground (a world with no
	 *                     sky light), which changes how heights are resolved
	 * @param multi        the scale factor of the map data grid, which is
	 *                     {@code 32 * multi} columns wide
	 * @param startX       the world X coordinate of the region's origin
	 * @param startZ       the world Z coordinate of the region's origin
	 * @param imageX       the pixel's x offset within the region
	 * @param imageY       the pixel's z offset within the region
	 * @return the ARGB color for the pixel, or 0 when no data is stored for it
	 */
	int getPixelColor(
		AbstractMapData mapData, World world, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, boolean underground, int multi, int startX, int startZ, int imageX, int imageY
	);

	/**
	 * Returns the cached regions covering the given inclusive range of region
	 * coordinates.
	 *
	 * @param left   the minimum region X coordinate
	 * @param right  the maximum region X coordinate
	 * @param top    the minimum region Z coordinate
	 * @param bottom the maximum region Z coordinate
	 * @return the requested cached regions
	 */
	CachedRegion[] getRegions(int left, int right, int top, int bottom);

	/**
	 * Returns whether the cached region containing the given block coordinate
	 * is loaded.
	 *
	 * @param blockX the block X coordinate
	 * @param blockZ the block Z coordinate
	 * @return {@code true} if the containing region is loaded
	 */
	boolean isRegionLoaded(int blockX, int blockZ);

	/**
	 * Returns whether the ground at the given block coordinate has been
	 * scanned and stored.
	 *
	 * @param blockX the block X coordinate
	 * @param blockZ the block Z coordinate
	 * @return {@code true} if ground data exists for the coordinate
	 */
	boolean isGroundAt(int blockX, int blockZ);

	/**
	 * Returns the stored surface height at the given block coordinate.
	 *
	 * @param blockX the block X coordinate
	 * @param blockZ the block Z coordinate
	 * @return the stored height, or 64 if no data is available
	 */
	int getHeightAt(int blockX, int blockZ);

	/**
	 * Discards all cached regions from memory.
	 */
	void purgeCachedRegions();

	/**
	 * Writes all cached regions to disk.
	 */
	void saveCachedRegions();

	/**
	 * Renames a subworld, migrating its cached region data to the new name.
	 *
	 * @param oldName the current subworld name
	 * @param newName the new subworld name
	 */
	void renameSubworld(String oldName, String newName);

	/**
	 * Returns the persistent map's options.
	 *
	 * @return the persistent map settings manager
	 */
	PersistentMapSettingsManager getOptions();

	/**
	 * Compresses all cached regions that have not changed within the last
	 * five seconds.
	 */
	void compress();
}
