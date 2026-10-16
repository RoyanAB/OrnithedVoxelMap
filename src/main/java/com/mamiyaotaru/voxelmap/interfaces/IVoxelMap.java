package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;

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

	void setPermissions(boolean caveMode, boolean waypointSharing, boolean radarPlayers, boolean radarMobs);

	void newSubWorldName(String subworldName, boolean vanilla);

	void newSubWorldHash(String subworldHash);

	ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier();

	String getWorldSeed();

	void setWorldSeed(String seed);
}
