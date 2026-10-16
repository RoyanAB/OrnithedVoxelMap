package com.mamiyaotaru.voxelmap.forgemod.mixins;

import com.mamiyaotaru.voxelmap.forgemod.ForgeModVoxelMap;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
   @Inject(
      method = "renderWorldPass(IFJ)V",
      at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=translucent")
   )
   private void onRenderTranslucent(int pass, float partialTicks, long timeSlice, CallbackInfo ci) {
      GlStateManager.depthMask(true);
   }

   @Inject(
      method = "renderWorldPass(IFJ)V",
      at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V", args = "ldc=frustum")
   )
   private void onSetupCameraTransform(int pass, float partialTicks, long timeSlice, CallbackInfo ci) {
      ForgeModVoxelMap.proxy.onSetupCameraTransform();
   }
}
