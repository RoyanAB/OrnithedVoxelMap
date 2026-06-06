package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import net.minecraft.client.entity.EntityPlayerSP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityPlayerSP.class)
public class EntityPlayerSPMixin {
	@Inject(
		method = "sendChatMessage",
		at = @At(
			value = "HEAD"
		),
		cancellable = true
	)
	private void onSendChatMessage(String message, CallbackInfo ci) {
		try {
			VoxelMapMod.tickHandler.onSendChatMessage(message, ci);
		} catch (Throwable ignored) {
		}
	}
}
