package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;

@SuppressWarnings("unused")
public interface IVoxelMap {
	MapSettingsManager getMapOptions();

	RadarSettingsManager getRadarOptions();

	PersistentMapSettingsManager getPersistentMapOptions();

	IMap getMap();

	IRadar getRadar();

	IColorManager getColorManager();

	IWaypointManager getWaypointManager();

	IDimensionManager getDimensionManager();

	IPersistentMap getPersistentMap();

	void setPermissions(boolean b1, boolean b2, boolean b3, boolean b4);

	void newSubWorldName(String name, boolean b);

	void newSubWorldHash(String hash);

	ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier();

	String getWorldSeed();

	void setWorldSeed(String seed);
}
