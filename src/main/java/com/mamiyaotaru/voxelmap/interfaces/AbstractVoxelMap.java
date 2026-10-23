package com.mamiyaotaru.voxelmap.interfaces;

/**
 * Abstract base for the mod's top-level {@link IVoxelMap} implementation.
 *
 * <p>Holds the singleton {@link #instance} and exposes it through
 * {@link #getInstance()}, so the rest of the mod can reach the active
 * VoxelMap without passing references around.
 */
public abstract class AbstractVoxelMap implements IVoxelMap {
	/**
	 * The currently active VoxelMap instance.
	 */
	public static AbstractVoxelMap instance = null;

	/**
	 * Returns the currently active VoxelMap instance.
	 *
	 * @return the active instance, or {@code null} if it has not been
	 *         initialized yet
	 */
	public static AbstractVoxelMap getInstance() {
		return instance;
	}
}
