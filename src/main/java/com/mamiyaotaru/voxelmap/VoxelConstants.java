package com.mamiyaotaru.voxelmap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.locks.ReentrantLock;

@SuppressWarnings("unused")
public final class VoxelConstants {
	public static final String MOD_ID = "voxelmap";
	public static String MOD_VERSION = "v1.9.28";
	public static String MOD_NAME = "VoxelMap";

	private static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	private static final VoxelMap VOXELMAP_INSTANCE = new VoxelMap();

	public static final ReentrantLock updateCloudsLock = new ReentrantLock();

	@Contract(pure = true)
	private VoxelConstants() {
	}

	@NotNull
	@Contract(pure = true)
	public static Logger getLogger() {
		return LOGGER;
	}

	@NotNull
	@Contract(pure = true)
	public static VoxelMap getVoxelMapInstance() {
		return VOXELMAP_INSTANCE;
	}

	public static void newWorldName(String worldName) {
		VOXELMAP_INSTANCE.newSubWorldName(worldName, true);
	}
}
