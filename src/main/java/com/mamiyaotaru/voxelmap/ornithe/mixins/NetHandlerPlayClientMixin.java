package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.SPacketChat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class NetHandlerPlayClientMixin {
	@Inject(
		method = "handleChat",
		at = @At(
			value = "HEAD"
		),
		cancellable = true
	)
	private void onChat(SPacketChat packetIn, CallbackInfo ci) {
		try {
			VoxelMapMod.tickHandler.onChat(packetIn.getChatComponent(), ci);
		} catch (Throwable ignored) {
		}
	}
}
