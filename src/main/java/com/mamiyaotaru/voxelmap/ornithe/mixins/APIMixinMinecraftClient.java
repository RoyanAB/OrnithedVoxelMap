package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.FabricModVoxelMap;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class APIMixinMinecraftClient {
    @Inject(method = "tick()V", at = @At("RETURN"))
    private void onTick(CallbackInfo ci) {
        FabricModVoxelMap.instance.clientTick((MinecraftClient) (Object) this);
    }
}
