package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.chunk.WorldChunk;

public class MapChunk {
    private int x = 0;
    private int z = 0;
    private WorldChunk chunk;
    private boolean isChanged = false;
    private boolean isLoaded = false;

    public MapChunk(int x, int z) {
        this.x = x;
        this.z = z;
        this.chunk = MinecraftClient.getInstance().world.getChunk(x, z);
        this.isLoaded = this.chunk != null && !this.chunk.isEmpty() && MinecraftClient.getInstance().world.isChunkLoaded(x, z);
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
            this.chunk = MinecraftClient.getInstance().world.getChunk(this.x, this.z);
            if (this.chunk != null && !this.chunk.isEmpty() && MinecraftClient.getInstance().world.isChunkLoaded(this.x, this.z)) {
                this.isLoaded = true;
                hasChanged = true;
            }
        } else if (this.isLoaded && (this.chunk == null || this.chunk.isEmpty() || !MinecraftClient.getInstance().world.isChunkLoaded(this.x, this.z))) {
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
