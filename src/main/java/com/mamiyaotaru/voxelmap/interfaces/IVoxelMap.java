package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.RadarSettingsManager;
import com.mamiyaotaru.voxelmap.persistent.PersistentMapSettingsManager;

/**
 * The top-level facade of the VoxelMap mod.
 *
 * <p>Provides access to the map's settings managers and to the individual
 * components (minimap, radar, color manager, waypoint manager, dimension
 * manager, and persistent map) that make up the mod.
 */
public interface IVoxelMap {
	/**
	 * Returns the minimap options.
	 *
	 * @return the map settings manager
	 */
	MapSettingsManager getMapOptions();

	/**
	 * Returns the radar options.
	 *
	 * @return the radar settings manager
	 */
	RadarSettingsManager getRadarOptions();

	/**
	 * Returns the persistent map options.
	 *
	 * @return the persistent map settings manager
	 */
	PersistentMapSettingsManager getPersistentMapOptions();

	/**
	 * Returns the minimap.
	 *
	 * @return the minimap
	 */
	IMap getMap();

	/**
	 * Returns the currently active radar, if one is enabled.
	 *
	 * @return the radar, or {@code null} if no radar is active
	 */
	IRadar getRadar();

	/**
	 * Returns the color manager.
	 *
	 * @return the color manager
	 */
	IColorManager getColorManager();

	/**
	 * Returns the waypoint manager.
	 *
	 * @return the waypoint manager
	 */
	IWaypointManager getWaypointManager();

	/**
	 * Returns the dimension manager.
	 *
	 * @return the dimension manager
	 */
	IDimensionManager getDimensionManager();

	/**
	 * Returns the persistent world map.
	 *
	 * @return the persistent map
	 */
	IPersistentMap getPersistentMap();

	/**
	 * Applies the server-provided permissions to the radar and cave-map
	 * features.
	 *
	 * @param hasFullRadarPermission       whether full radar (hostiles, players, and mobs) is permitted
	 * @param hasPlayersOnRadarPermission whether showing players on the radar is permitted
	 * @param hasMobsOnRadarPermission     whether showing mobs on the radar is permitted
	 * @param hasCavemodePermission        whether cave mapping is permitted
	 */
	void setPermissions(boolean hasFullRadarPermission, boolean hasPlayersOnRadarPermission, boolean hasMobsOnRadarPermission, boolean hasCavemodePermission);

	/**
	 * Records a new subworld name received from the server or client.
	 *
	 * @param name       the new subworld name
	 * @param fromServer {@code true} if the name came from the server
	 */
	void newSubWorldName(String name, boolean fromServer);

	/**
	 * Records a new subworld hash received from the server.
	 *
	 * @param hash the subworld hash
	 */
	void newSubWorldHash(String hash);

	/**
	 * Returns the notifier that announces settings and lighting changes.
	 *
	 * @return the settings and lighting change notifier
	 */
	ISettingsAndLightingChangeNotifier getSettingsAndLightingChangeNotifier();

	/**
	 * Returns the current world's seed: the actual world seed in singleplayer,
	 * or the seed recorded by the waypoint manager on servers.
	 *
	 * @return the world seed as a string
	 */
	String getWorldSeed();

	/**
	 * Records the current world's seed. Only stored when playing on a server;
	 * ignored in singleplayer, where the real world seed is used.
	 *
	 * @param newSeed the new world seed
	 */
	void setWorldSeed(String newSeed);
}
