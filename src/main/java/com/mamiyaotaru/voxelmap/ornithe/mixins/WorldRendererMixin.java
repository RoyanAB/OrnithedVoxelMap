package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.Share;
import net.minecraft.client.renderer.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
    @Inject(
            method = "Lnet/minecraft/client/renderer/WorldRenderer;tick()V",
            at = @At(
                    value = "HEAD"
            )
    )
    public void preUpdateClouds(CallbackInfo ci) {
        Share.updateCloudsLock.lock();
    }

    @Inject(
            method = "Lnet/minecraft/client/renderer/WorldRenderer;tick()V",
            at = @At(
                    value = "RETURN"
            )
    )
    public void postUpdateClouds(CallbackInfo ci) {
        Share.updateCloudsLock.unlock();
    }
}
