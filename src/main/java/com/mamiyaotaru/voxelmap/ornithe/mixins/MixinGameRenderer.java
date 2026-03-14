package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.FabricModVoxelMap;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class MixinGameRenderer {
    @Inject(
            method = "renderCenter(FJ)V",
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand")
    )
    private void onRenderHand(float partialTicks, long timeSlice, CallbackInfo ci) {
        FabricModVoxelMap.onRenderHand(partialTicks, timeSlice);
        GlStateManager.blendFunc(770, 771);
        GlStateManager.shadeModel(7424);
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.disableFog();
        GlStateManager.disableLighting();
    }

    @Inject(
            method = "renderCenter(FJ)V",
            at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=translucent")
    )
    private void onRenderTranslucent(float partialTicks, long timeSlice, CallbackInfo ci) {
        GlStateManager.depthMask(true);
    }
}
