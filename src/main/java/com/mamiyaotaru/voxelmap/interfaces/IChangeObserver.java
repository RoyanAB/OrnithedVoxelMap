package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

/**
 * Receives notifications about changes that occur in the world so that map
 * data can be recomputed for the affected areas.
 *
 * <p>Implemented by the live minimap ({@link IMap}) and the persistent world
 * map ({@link IPersistentMap}), both of which are registered as world event
 * listeners.
 */
public interface IChangeObserver {
	/**
	 * Called when one or more blocks in the world change.
	 *
	 * <p>If {@code pos2} is {@code null} then only the single block at
	 * {@code pos1} changed; otherwise the two positions describe the corners
	 * of a rectangular area of blocks that changed.
	 *
	 * @param pos1 the position of the changed block, or the first corner of the changed area
	 * @param pos2 the second corner of the changed area, or {@code null} for a single-block change
	 */
	void handleChangeInWorld(BlockPos pos1, BlockPos pos2);

	/**
	 * Called when a whole chunk should be (re)processed.
	 *
	 * @param chunk the chunk to process
	 */
	void processChunk(Chunk chunk);
}
