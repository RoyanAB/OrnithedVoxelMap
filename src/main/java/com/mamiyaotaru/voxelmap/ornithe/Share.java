package com.mamiyaotaru.voxelmap.ornithe;

import com.mamiyaotaru.voxelmap.interfaces.AbstractVoxelMap;

import java.util.concurrent.locks.ReentrantLock;

public class Share {
	public static final ReentrantLock updateCloudsLock = new ReentrantLock();

	public static boolean isOldNorth() {
		return AbstractVoxelMap.getInstance().getMapOptions().oldNorth;
	}
}
