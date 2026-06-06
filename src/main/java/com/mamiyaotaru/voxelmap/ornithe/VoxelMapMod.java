package com.mamiyaotaru.voxelmap.ornithe;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import com.mamiyaotaru.voxelmap.persistent.ThreadManager;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;

public class VoxelMapMod {
	public static TickHandler tickHandler;

	public static void postInit() {
		tickHandler = new TickHandler(VoxelConstants.getVoxelMapInstance());
		VoxelConstants.getVoxelMapInstance().lateInit(false, false);
		Runtime.getRuntime().addShutdownHook(new Thread(VoxelMapMod::onShutDown));
	}

	public static void onSetupCameraTransform() {
		VoxelConstants.getVoxelMapInstance().onSetupCameraTransform();
	}

	public static void onShutDown() {
		VoxelConstants.getLogger().info("Saving all world maps");
		VoxelConstants.getVoxelMapInstance().getPersistentMap().saveCachedRegions();
		VoxelConstants.getVoxelMapInstance().getMapOptions().saveAll();
		BiomeRepository.saveBiomeColors();
		long shutdownTime = System.currentTimeMillis();

		while (ThreadManager.executorService.getQueue().size() + ThreadManager.executorService.getActiveCount() > 0 && System.currentTimeMillis() - shutdownTime < 10000L) {
			try {
				Thread.sleep(200L);
			} catch (InterruptedException ignored) {
			}
		}
	}
}
