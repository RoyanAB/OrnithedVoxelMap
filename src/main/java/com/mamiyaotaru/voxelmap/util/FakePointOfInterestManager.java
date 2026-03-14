package com.mamiyaotaru.voxelmap.util;

import com.mojang.datafixers.DataFixer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.poi.PointOfInterestStorage;

import java.io.File;
import java.util.function.BooleanSupplier;

public class FakePointOfInterestManager extends PointOfInterestStorage {
    public FakePointOfInterestManager(File file, DataFixer dataFixer) {
        super(file, dataFixer);
    }

    public void initForPalette(ChunkPos chunkPos, ChunkSection chunkSection) {
    }

    public void tick(BooleanSupplier booleanSupplier) {
    }

    public void method_20436(ChunkPos chunkPos) {
    }
}
