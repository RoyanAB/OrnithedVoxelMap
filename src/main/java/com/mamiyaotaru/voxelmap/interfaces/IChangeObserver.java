package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

@SuppressWarnings("unused")
public interface IChangeObserver {
	void handleChangeInWorld(BlockPos pos1, BlockPos pos2);

	void processChunk(Chunk chunk);
}
