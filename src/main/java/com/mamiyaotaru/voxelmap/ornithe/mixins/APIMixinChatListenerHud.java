package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.FabricModVoxelMap;
import net.minecraft.client.gui.hud.ChatListenerHud;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChatListenerHud.class)
public class APIMixinChatListenerHud {
    @Inject(method = "onChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;)V", at = @At("HEAD"), cancellable = true)
    public void postSay(MessageType type, Text textComponent, CallbackInfo ci) {
        if (!FabricModVoxelMap.instance.onChat(textComponent)) {
            ci.cancel();
        }
    }
}
