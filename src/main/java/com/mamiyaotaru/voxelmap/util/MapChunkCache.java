package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class MapChunkCache {
    private final int width;
    private final int height;
    private Chunk lastCenterChunk = null;
    private final MapChunk[] mapChunks;
    private int left = 0;
    private int right = 0;
    private int top = 0;
    private int bottom = 0;
    private boolean loaded = false;
    private final IChangeObserver changeObserver;

    public MapChunkCache(int width, int height, IChangeObserver changeObserver) {
        this.width = width;
        this.height = height;
        this.mapChunks = new MapChunk[width * height];
        this.changeObserver = changeObserver;
    }

    public void centerChunks(BlockPos blockPos) {
        Chunk currentChunk = Minecraft.getInstance().world.getChunk(blockPos);
        if (currentChunk != this.lastCenterChunk) {
            if (this.lastCenterChunk == null) {
                this.fillAllChunks(blockPos);
                this.lastCenterChunk = currentChunk;
                return;
            }

            int middleX = this.width / 2;
            int middleZ = this.height / 2;
            int movedX = currentChunk.x - this.lastCenterChunk.x;
            int movedZ = currentChunk.z - this.lastCenterChunk.z;
            if (Math.abs(movedX) < this.width && Math.abs(movedZ) < this.height && currentChunk.getWorld().equals(this.lastCenterChunk.getWorld())) {
                this.moveX(movedX);
                this.moveZ(movedZ);

                for (int z = movedZ > 0 ? this.height - movedZ : 0; z < (movedZ > 0 ? this.height : -movedZ); z++) {
                    for (int x = 0; x < this.width; x++) {
                        this.mapChunks[x + z * this.width] = new MapChunk(currentChunk.x - (middleX - x), currentChunk.z - (middleZ - z));
                    }
                }

                for (int z = 0; z < this.height; z++) {
                    for (int x = movedX > 0 ? this.width - movedX : 0; x < (movedX > 0 ? this.width : -movedX); x++) {
                        this.mapChunks[x + z * this.width] = new MapChunk(currentChunk.x - (middleX - x), currentChunk.z - (middleZ - z));
                    }
                }
            } else {
                this.fillAllChunks(blockPos);
            }

            this.left = this.mapChunks[0].getX();
            this.top = this.mapChunks[0].getZ();
            this.right = this.mapChunks[this.mapChunks.length - 1].getX();
            this.bottom = this.mapChunks[this.mapChunks.length - 1].getZ();
            this.lastCenterChunk = currentChunk;
        }
    }

    private void fillAllChunks(BlockPos blockPos) {
        Chunk currentChunk = Minecraft.getInstance().world.getChunk(blockPos);
        int middleX = this.width / 2;
        int middleZ = this.height / 2;

        for (int z = 0; z < this.height; z++) {
            for (int x = 0; x < this.width; x++) {
                this.mapChunks[x + z * this.width] = new MapChunk(currentChunk.x - (middleX - x), currentChunk.z - (middleZ - z));
            }
        }

        this.left = this.mapChunks[0].getX();
        this.top = this.mapChunks[0].getZ();
        this.right = this.mapChunks[this.mapChunks.length - 1].getX();
        this.bottom = this.mapChunks[this.mapChunks.length - 1].getZ();
        this.loaded = true;
    }

    private void moveX(int offset) {
        if (offset > 0) {
            System.arraycopy(this.mapChunks, offset, this.mapChunks, 0, this.mapChunks.length - offset);
        } else if (offset < 0) {
            System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset, this.mapChunks.length + offset);
        }
    }

    private void moveZ(int offset) {
        if (offset > 0) {
            System.arraycopy(this.mapChunks, offset * this.width, this.mapChunks, 0, this.mapChunks.length - offset * this.width);
        } else if (offset < 0) {
            System.arraycopy(this.mapChunks, 0, this.mapChunks, -offset * this.width, this.mapChunks.length + offset * this.width);
        }
    }

    public void calculateChunks() {
        if (this.loaded) {
            for (int z = this.height - 1; z >= 0; z--) {
                for (int x = 0; x < this.width; x++) {
                    this.mapChunks[x + z * this.width].calculateChunk(this.changeObserver);
                }
            }
        }
    }

    public void registerChangeAt(BlockPos pos) {
        if (this.lastCenterChunk != null) {
            int chunkX = (int) Math.floor(pos.getX() / 16.0F);
            int chunkZ = (int) Math.floor(pos.getZ() / 16.0F);
            if (chunkX >= this.left && chunkX <= this.right && chunkZ >= this.top && chunkZ <= this.bottom) {
                int arrayX = chunkX - this.left;
                int arrayZ = chunkZ - this.top;
                MapChunk mapChunk = this.mapChunks[arrayX + arrayZ * this.width];
                mapChunk.setModified(true);
            }
        }
    }
}
