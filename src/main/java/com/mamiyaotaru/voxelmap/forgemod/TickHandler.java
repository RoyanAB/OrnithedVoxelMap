package com.mamiyaotaru.voxelmap.forgemod;

import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.util.CommandUtils;
import com.mamiyaotaru.voxelmap.util.ReflectionUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Timer;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;

public class TickHandler {
   private VoxelMap voxelMap;
   private Timer timer = null;

   public TickHandler(VoxelMap voxelMap) {
      this.voxelMap = voxelMap;
   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      if (this.timer == null) {
         this.timer = (Timer)ReflectionUtils.getPrivateFieldValueByType(Minecraft.getMinecraft(), Minecraft.class, Timer.class);
      }

      boolean clock = this.timer.elapsedTicks > 0;
      this.voxelMap.onTick(Minecraft.getMinecraft(), clock);
   }

   @SubscribeEvent
   public void onRenderOverlay(Post event) {
      if (event.getType() == ElementType.ALL) {
         this.voxelMap.onTickInGame(Minecraft.getMinecraft());
      }
   }

   @SubscribeEvent
   public void onRenderHand(RenderWorldLastEvent event) {
      this.voxelMap.getWaypointManager().renderWaypoints(event.getPartialTicks());
      GlStateManager.blendFunc(770, 771);
      GlStateManager.shadeModel(7424);
      GlStateManager.depthMask(true);
      GlStateManager.enableCull();
      GlStateManager.disableBlend();
      GlStateManager.disableFog();
      GlStateManager.disableLighting();
   }

   @SubscribeEvent
   public void onChat(ClientChatReceivedEvent event) {
      ITextComponent chat = event.getMessage();
      String message = chat.getFormattedText();
      if (!CommandUtils.checkForWaypoints(chat, message)) {
         event.setCanceled(true);
      }
   }

   @SubscribeEvent
   public void onSendChatMessage(ClientChatEvent event) {
      String message = event.getMessage();
      if (message.startsWith("/newWaypoint")) {
         CommandUtils.waypointClicked(message);
         event.setCanceled(true);
      } else if (message.startsWith("/ztp")) {
         CommandUtils.teleport(message);
         event.setCanceled(true);
      }
   }
}
