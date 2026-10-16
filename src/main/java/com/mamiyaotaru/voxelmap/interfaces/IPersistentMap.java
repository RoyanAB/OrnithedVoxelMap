package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.persistent.CachedRegion;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;
import com.mamiyaotaru.voxelmap.util.MutableBlockPos;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public interface IPersistentMap extends IChangeObserver {
	void newWorld(World world);

	void onTick(Minecraft minecraft);

	ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier();

	void setLightMapArray(int[] lightMapArray);

	void getAndStoreData(AbstractMapData mapData, World world, Chunk chunk, MutableBlockPos blockPos, boolean underground, int startX, int startZ, int imageX, int imageY);

	int getPixelColor(
		AbstractMapData mapData, World world, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, boolean underground, int startX, int startZ, int imageX, int imageY, int biomeID
	);

	CachedRegion[] getRegions(int minRegionX, int minRegionZ, int maxRegionX, int maxRegionZ);

	boolean isRegionLoaded(int regionX, int regionZ);

	boolean isGroundAt(int x, int z);

	int getHeightAt(int x, int z);

	void purgeCachedRegions();

	void saveCachedRegions();

	void renameSubworld(String oldName, String newName);

	PersistentMapSettingsManager getOptions();

	void compress();
}
