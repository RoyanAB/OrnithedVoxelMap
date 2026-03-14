package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public interface IChangeObserver {
	void handleChangeInWorld(BlockPos var1, BlockPos var2);

	void processChunk(Chunk var1);
}
