package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.Dimension;

import java.util.ArrayList;

public interface IDimensionManager {
	ArrayList<Dimension> getDimensions();

	Dimension getDimensionByID(int var1);

	void enteredDimension(int var1);

	void populateDimensions();
}
