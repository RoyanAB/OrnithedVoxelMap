package com.mamiyaotaru.voxelmap.ornithe;

import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.persistent.ThreadManager;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.ornithemc.osl.entrypoints.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VoxelMapMod implements ModInitializer {
    public static final String MOD_ID = "voxelmap";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);
    public static String MOD_VERSION = "unknown";
    public static String MOD_NAME = "unknown";
    public static VoxelMap voxelMap;
    public static TickHandler tickHandler;

    public static void postInit() {
        voxelMap = new VoxelMap();
        tickHandler = new TickHandler(voxelMap);
        voxelMap.lateInit(false, false);
        Runtime.getRuntime().addShutdownHook(new Thread(VoxelMapMod::onShutDown));
    }

    public static void onSetupCameraTransform() {
        voxelMap.onSetupCameraTransform();
    }

    public static void onShutDown() {
        System.out.print("Saving all world maps");
        voxelMap.getPersistentMap().saveCachedRegions();
        voxelMap.getMapOptions().saveAll();
        BiomeRepository.saveBiomeColors();
        long shutdownTime = System.currentTimeMillis();

        while (
                ThreadManager.executorService.getQueue().size() + ThreadManager.executorService.getActiveCount() > 0
                        && System.currentTimeMillis() - shutdownTime < 10000L
        ) {
            System.out.print(".");

            try {
                Thread.sleep(200L);
            } catch (InterruptedException var4) {
            }
        }

        System.out.println();
    }

    @Override
    public void init() {
        ModMetadata metadata = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow(RuntimeException::new).getMetadata();
        MOD_NAME = metadata.getName();
        MOD_VERSION = metadata.getVersion().getFriendlyString();
    }

    public void newWorldName(String worldName) {
        voxelMap.newSubWorldName(worldName, true);
    }
}
