package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
	@Inject(
		method = "runTick",
		at = @At(
			value = "HEAD"
		)
	)
	private void startTick(CallbackInfo ci) {
		VoxelMapMod.tickHandler.onTick();
	}

	@Inject(
		method = "startGame",
		at = @At(
			value = "TAIL"
		)
	)
	private void ready(CallbackInfo ci) {
		VoxelMapMod.postInit();
	}
}
