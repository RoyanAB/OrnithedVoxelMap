package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.FabricModVoxelMap;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class APIMixinClientPlayerEntity extends AbstractClientPlayerEntity {
    public APIMixinClientPlayerEntity() {
        super(null, null);
    }

    @Inject(method = "sendChatMessage(Ljava/lang/String;)V", at = @At("HEAD"), cancellable = true)
    public void onSendChatMessage(String message, CallbackInfo ci) {
        if (!FabricModVoxelMap.instance.onSendChatMessage(message)) {
            ci.cancel();
        }
    }
}
