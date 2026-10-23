package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.textures.TextureAtlas;
import com.mamiyaotaru.voxelmap.util.BackgroundImageInfo;
import com.mamiyaotaru.voxelmap.util.Waypoint;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Manages the waypoints for the current world, including loading, saving,
 * rendering, and tracking subworlds (multi-world servers).
 */
public interface IWaypointManager {
	/**
	 * Returns all waypoints for the current world.
	 *
	 * @return the list of waypoints
	 */
	ArrayList<Waypoint> getWaypoints();

	/**
	 * Deletes the given waypoint and saves the updated set.
	 *
	 * @param point the waypoint to delete
	 */
	void deleteWaypoint(Waypoint point);

	/**
	 * Writes all waypoints to disk.
	 */
	void saveWaypoints();

	/**
	 * Adds a new waypoint and saves the updated set.
	 *
	 * @param newWaypoint the waypoint to add
	 */
	void addWaypoint(Waypoint newWaypoint);

	/**
	 * Resolves the height of legacy 2D waypoints (Y coordinate of 0 or less)
	 * near the player in the overworld, converting them to 3D waypoints as
	 * their chunks load.
	 */
	void check2dWaypoints();

	/**
	 * Handles the player's death, updating death waypoints according to the
	 * current deathpoint setting.
	 */
	void handleDeath();

	/**
	 * Called when a new world is loaded, loading the waypoints for that world.
	 *
	 * @param world the newly loaded world, or {@code null} when leaving a world
	 */
	void newWorld(World world);

	/**
	 * Records the ID of the connected Realms server.
	 *
	 * @param id the Realms server ID
	 */
	void setConnectedRealm(String id);

	/**
	 * Returns the name of the current world/server.
	 *
	 * @return the current world name
	 */
	String getCurrentWorldName();

	/**
	 * Returns the names of all known subworlds.
	 *
	 * @return the set of known subworld names
	 */
	TreeSet<String> getKnownSubworldNames();

	/**
	 * Returns whether an automatic subworld name has been received from the
	 * server.
	 *
	 * @return {@code true} if an automatic subworld name was received
	 */
	boolean receivedAutoSubworldName();

	/**
	 * Returns whether the current world uses multiple subworlds.
	 *
	 * @return {@code true} if the world is multi-world
	 */
	boolean isMultiworld();

	/**
	 * Sets the current subworld name.
	 *
	 * @param name       the subworld name
	 * @param fromServer {@code true} if the name came from the server
	 */
	void setSubworldName(String name, boolean fromServer);

	/**
	 * Sets the current subworld hash.
	 *
	 * @param hash the subworld hash
	 */
	void setSubworldHash(String hash);

	/**
	 * Renames an existing subworld and migrates its waypoints to the new name.
	 *
	 * @param oldName the current subworld name
	 * @param newName the new subworld name
	 */
	void changeSubworldName(String oldName, String newName);

	/**
	 * Deletes a subworld, clearing the subworld from its waypoints.
	 *
	 * @param name the subworld name to delete
	 */
	void deleteSubworld(String name);

	/**
	 * Sets whether "old north" (fixed north-up orientation) is used for the
	 * current subworld.
	 *
	 * @param oldNorth {@code true} to use fixed north orientation
	 */
	void setOldNorth(boolean oldNorth);

	/**
	 * Returns a descriptor for the current subworld.
	 *
	 * @param withCodes {@code true} to keep formatting codes in the descriptor
	 * @return the current subworld descriptor
	 */
	String getCurrentSubworldDescriptor(boolean withCodes);

	/**
	 * Renders all visible waypoints for the current frame.
	 *
	 * @param partialTicks the partial tick time for interpolation
	 */
	void renderWaypoints(float partialTicks);

	/**
	 * Notifies the manager that resources were reloaded so it can rebuild its
	 * waypoint icon atlas.
	 *
	 * @param resourceManager the resource manager that was reloaded
	 */
	void onResourceManagerReload(IResourceManager resourceManager);

	/**
	 * Returns the atlas holding the waypoint icons.
	 *
	 * @return the waypoint texture atlas
	 */
	TextureAtlas getTextureAtlas();

	/**
	 * Returns the atlas used by the waypoint icon chooser GUI.
	 *
	 * @return the chooser texture atlas
	 */
	TextureAtlas getTextureAtlasChooser();

	/**
	 * Sets or clears the highlighted waypoint.
	 *
	 * @param waypoint the waypoint to highlight, or {@code null} to clear
	 * @param toggle   {@code true} to toggle the highlight off if already set
	 */
	void setHighlightedWaypoint(Waypoint waypoint, boolean toggle);

	/**
	 * Returns the currently highlighted waypoint.
	 *
	 * @return the highlighted waypoint, or {@code null} if none
	 */
	Waypoint getHighlightedWaypoint();

	/**
	 * Returns the world seed recorded for the current subworld.
	 *
	 * @return the world seed, or an empty string if none has been recorded
	 */
	String getWorldSeed();

	/**
	 * Records the world seed for the current subworld and saves it.
	 *
	 * @param newSeed the new world seed
	 */
	void setWorldSeed(String newSeed);

	/**
	 * Returns the background map image information for the current world and
	 * dimension.
	 *
	 * @return the background image information, or {@code null} if none is loaded
	 */
	BackgroundImageInfo getBackgroundImageInfo();
}
