package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.FabricModVoxelMap;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class APINetHandlerPlayClient {
    @Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
    private void onHandleCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
        if (FabricModVoxelMap.instance.handleCustomPayload(packet)) {
            ci.cancel();
        }
    }
}
