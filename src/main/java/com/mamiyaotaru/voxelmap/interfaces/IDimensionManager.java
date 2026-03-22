package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.Dimension;

import java.util.ArrayList;

@SuppressWarnings("unused")
public interface IDimensionManager {
	ArrayList<Dimension> getDimensions();

	Dimension getDimensionByID(int id);

	void enteredDimension(int id);

	void populateDimensions();
}
