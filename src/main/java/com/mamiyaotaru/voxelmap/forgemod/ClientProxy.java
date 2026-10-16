package com.mamiyaotaru.voxelmap.forgemod;

import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.persistent.ThreadManager;
import com.mamiyaotaru.voxelmap.util.BiomeRepository;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

public class ClientProxy extends CommonProxy {
   VoxelMap voxelMap;

   @Override
   public void postInit(FMLPostInitializationEvent event) {
      this.voxelMap = new VoxelMap();
      this.voxelMap.lateInit(false, false);
      MinecraftForge.EVENT_BUS.register(new TickHandler(this.voxelMap));
   }

   @Override
   public void newWorldName(String worldName) {
      this.voxelMap.newSubWorldName(worldName, true);
   }

   @Override
   public void onSetupCameraTransform() {
      this.voxelMap.onSetupCameraTransform();
   }

   @Override
   public void onShutDown() {
      System.out.print("Saving all world maps");
      this.voxelMap.getPersistentMap().saveCachedRegions();
      this.voxelMap.getMapOptions().saveAll();
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
}
