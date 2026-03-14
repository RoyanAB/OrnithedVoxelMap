package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.DimensionContainer;
import net.minecraft.world.dimension.Dimension;

import java.util.ArrayList;

public interface IDimensionManager {
    ArrayList<DimensionContainer> getDimensions();

    DimensionContainer getDimensionContainerByDimension(Dimension var1);

    DimensionContainer getDimensionContainerByIdentifier(String var1);

    void enteredDimension(Dimension var1);

    void populateDimensions();
}
