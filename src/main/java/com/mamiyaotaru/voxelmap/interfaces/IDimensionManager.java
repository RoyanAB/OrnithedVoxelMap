package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.Dimension;

import java.util.ArrayList;

/**
 * Tracks the world dimensions (Overworld, Nether, End, and any modded ones)
 * known to the map.
 */
public interface IDimensionManager {
	/**
	 * Returns the list of all known dimensions.
	 *
	 * @return the known dimensions
	 */
	ArrayList<Dimension> getDimensions();

	/**
	 * Finds a dimension by its numeric ID.
	 *
	 * @param dimensionID the numeric dimension ID to look up
	 * @return the matching {@link Dimension}, or {@code null} if not known
	 */
	Dimension getDimensionByID(int dimensionID);

	/**
	 * Notifies the manager that the player entered the given dimension,
	 * registering it under a placeholder name if unknown, then resolving its
	 * display name.
	 *
	 * @param dimensionID the numeric ID of the dimension entered
	 */
	void enteredDimension(int dimensionID);

	/**
	 * Clears and repopulates the dimension list from the vanilla dimension
	 * providers and from the dimensions referenced by stored waypoints.
	 */
	void populateDimensions();
}
