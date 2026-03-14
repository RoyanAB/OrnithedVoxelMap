package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.GlStateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private Minecraft mc;

    @Inject(
            method = "updateCameraAndRender(FJ)V",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    args = "ldc=translucent"
            )
    )
    private void onRenderTranslucent(float partialTicks, long nanoTime, CallbackInfo ci) {
        GlStateManager.depthMask(true);
    }

    @Inject(
            method = "updateCameraAndRender(FJ)V",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    args = "ldc=frustum"
            )
    )
    private void onSetupCameraTransform(float partialTicks, long nanoTime, CallbackInfo ci) {
        VoxelMapMod.onSetupCameraTransform();
    }

    @Inject(
            method = "updateCameraAndRender(FJ)V",
            at = @At(
                    value = "INVOKE_STRING",
                    target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V",
                    args = "ldc=hand"
            )
    )
    private void onRenderHand(float partialTicks, long nanoTime, CallbackInfo ci) {
        VoxelMapMod.tickHandler.onRenderHand(partialTicks);
    }

    @Inject(method = "updateCameraAndRender(FJZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/GuiIngame;renderGameOverlay(F)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onRenderGameOverlayPost(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo ci) {
        if (this.mc.world != null && this.mc.player != null) {
            VoxelMapMod.tickHandler.onRenderOverlay();
        }
    }
}
