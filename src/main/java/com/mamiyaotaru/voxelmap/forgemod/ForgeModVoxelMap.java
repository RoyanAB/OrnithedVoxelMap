package com.mamiyaotaru.voxelmap.forgemod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Logger;

@Mod(modid = "voxelmap", name = "VoxelMap", version = "1.9.25", acceptedMinecraftVersions = "[1.12.2]")
public class ForgeModVoxelMap {
   public static final String MODID = "voxelmap";
   public static final String NAME = "VoxelMap";
   public static final String VERSION = "1.9.25";
   private boolean hasFullRadarPermission = true;
   private boolean hasPlayersOnRadarPermission = true;
   private boolean hasMobsOnRadarPermission = true;
   private boolean hasCavemodePermission = true;
   private static Logger logger;
   public static SimpleNetworkWrapper WORLD_ID;
   public static SimpleNetworkWrapper WORLD_INFO;
   @Instance("VoxelMap")
   public static ForgeModVoxelMap instance;
   @SidedProxy(clientSide = "com.mamiyaotaru.voxelmap.forgemod.ClientProxy", serverSide = "com.mamiyaotaru.voxelmap.forgemod.CommonProxy", modId = "voxelmap")
   public static CommonProxy proxy;

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      logger = event.getModLog();
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      instance = this;
      WORLD_ID = NetworkRegistry.INSTANCE.newSimpleChannel("world_id");
      WORLD_INFO = NetworkRegistry.INSTANCE.newSimpleChannel("world_info");
      WORLD_ID.registerMessage(ForgeModVoxelMap.WorldIdListener.class, WorldIDPacket.class, 0, Side.CLIENT);
      WORLD_INFO.registerMessage(ForgeModVoxelMap.WorldInfoListener.class, WorldInfoPacket.class, 0, Side.CLIENT);
   }

   @EventHandler
   public void postInit(FMLPostInitializationEvent event) {
      proxy.postInit(event);
      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run() {
            ForgeModVoxelMap.proxy.onShutDown();
         }
      });
   }

   public static class WorldIdListener implements IMessageHandler<WorldIDPacket, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(WorldIDPacket message, MessageContext ctx) {
         ForgeModVoxelMap.proxy.newWorldName(message.getWorldID());
         return null;
      }
   }

   public static class WorldInfoListener implements IMessageHandler<WorldInfoPacket, IMessage> {
      @SideOnly(Side.CLIENT)
      public IMessage onMessage(WorldInfoPacket message, MessageContext ctx) {
         ForgeModVoxelMap.proxy.newWorldName(message.getWorldID());
         return null;
      }
   }
}
