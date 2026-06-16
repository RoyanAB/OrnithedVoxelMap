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

	void setPermissions(boolean hasFullRadarPermission, boolean hasPlayersOnRadarPermission, boolean hasMobsOnRadarPermission, boolean hasCavemodePermission);

	void newSubWorldName(String name, boolean fromServer);

	void newSubWorldHash(String hash);

	ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier();

	String getWorldSeed();

	void setWorldSeed(String seed);
}
