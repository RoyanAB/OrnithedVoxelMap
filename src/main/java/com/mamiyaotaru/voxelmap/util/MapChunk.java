package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
import net.minecraft.client.Minecraft;
import net.minecraft.world.chunk.Chunk;

@SuppressWarnings("unused")
public class MapChunk {
	private final int x;
	private final int z;
	private Chunk chunk;
	private boolean isChanged;
	private boolean isLoaded;

	public MapChunk(int x, int z) {
		this.x = x;
		this.z = z;
		this.chunk = Minecraft.getMinecraft().world.getChunk(x, z);
		this.isLoaded = this.chunk.isLoaded();
		this.isChanged = true;
	}

	public void calculateChunk(IChangeObserver changeObserver) {
		if (this.hasChunkLoadedOrUnloaded() || this.isChanged) {
			changeObserver.processChunk(this.chunk);
			this.isChanged = false;
		}
	}

	private boolean hasChunkLoadedOrUnloaded() {
		boolean hasChanged = false;
		if (!this.isLoaded) {
			this.chunk = Minecraft.getMinecraft().world.getChunk(this.x, this.z);
			if (this.chunk.isLoaded()) {
				this.isLoaded = true;
				hasChanged = true;
			}
		} else if (!this.chunk.isLoaded()) {
			this.isLoaded = false;
			hasChanged = true;
		}

		return hasChanged;
	}

	public int getX() {
		return this.x;
	}

	public int getZ() {
		return this.z;
	}

	public void setModified(boolean isModified) {
		this.isChanged = isModified;
	}
}
