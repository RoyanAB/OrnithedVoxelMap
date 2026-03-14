package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;

import java.util.ArrayList;

public class WorldUpdateListener {
    private final ArrayList<IChangeObserver> chunkProcessors = new ArrayList<>();

    public void addListener(IChangeObserver chunkProcessor) {
        this.chunkProcessors.add(chunkProcessor);
    }

    public void notifyObservers(int chunkX, int chunkZ) {
        for (IChangeObserver chunkProcessor : this.chunkProcessors) {
            chunkProcessor.handleChangeInWorld(chunkX, chunkZ);
        }
    }
}
