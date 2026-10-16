package com.mamiyaotaru.voxelmap.forgemod;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class WorldInfoPacket implements IMessage {
   public static final String CHANNEL_NAME = "world_info";
   private String worldID;

   public WorldInfoPacket() {
   }

   public WorldInfoPacket(String worldID) {
      this.worldID = worldID;
   }

   public String getWorldID() {
      return this.worldID;
   }

   public void fromBytes(ByteBuf buf) {
      try {
         this.worldID = ByteBufUtils.readUTF8String(buf);
      } catch (Exception e) {
         System.err.println(String.format("Failed to read message: %s", e));
      }
   }

   public void toBytes(ByteBuf buf) {
      try {
         if (this.worldID != null) {
            ByteBufUtils.writeUTF8String(buf, this.worldID);
         }
      } catch (Exception e) {
         System.err.println("[toBytes]Failed to read message: " + e);
      }
   }
}
