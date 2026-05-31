package com.mamiyaotaru.voxelmap.interfaces;

@SuppressWarnings("unused")
public abstract class AbstractVoxelMap implements IVoxelMap {
	public static AbstractVoxelMap instance = null;

	public static AbstractVoxelMap getInstance() {
		return instance;
	}
}
