package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.VoxelMap;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer {
    @Inject(method = "scheduleChunkRender(IIIZ)V", at = @At("RETURN"))
    public void postScheduleChunkRender(int x, int y, int z, boolean dunno, CallbackInfo ci) {
        VoxelMap.instance.getWorldUpdateListener().notifyObservers(x, z);
    }
}
