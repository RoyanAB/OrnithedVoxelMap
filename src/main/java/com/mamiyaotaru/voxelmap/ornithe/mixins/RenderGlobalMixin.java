package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class RenderGlobalMixin {
	@Inject(
		method = "updateClouds()V",
		at = @At(
			value = "HEAD"
		)
	)
	public void preUpdateClouds(CallbackInfo ci) {
		VoxelConstants.updateCloudsLock.lock();
	}

	@Inject(
		method = "updateClouds()V",
		at = @At(
			value = "RETURN"
		)
	)
	public void postUpdateClouds(CallbackInfo ci) {
		VoxelConstants.updateCloudsLock.unlock();
	}
}
