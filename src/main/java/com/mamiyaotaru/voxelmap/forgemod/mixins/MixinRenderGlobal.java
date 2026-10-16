package com.mamiyaotaru.voxelmap.forgemod.mixins;

import com.mamiyaotaru.voxelmap.forgemod.Share;
import net.minecraft.client.renderer.RenderGlobal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {
   @Inject(method = "updateClouds()V", at = @At("HEAD"))
   public void preUpdateClouds(CallbackInfo ci) {
      Share.updateCloudsLock.lock();
   }

   @Inject(method = "updateClouds()V", at = @At("RETURN"))
   public void postUpdateClouds(CallbackInfo ci) {
      Share.updateCloudsLock.unlock();
   }
}
