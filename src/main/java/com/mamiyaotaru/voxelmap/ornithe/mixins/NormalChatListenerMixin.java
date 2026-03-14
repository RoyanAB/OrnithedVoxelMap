package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.TickHandler;
import net.minecraft.client.gui.chat.NormalChatListener;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.ITextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NormalChatListener.class)
public class NormalChatListenerMixin {
    @Inject(
            method = "say(Lnet/minecraft/util/text/ChatType;Lnet/minecraft/util/text/ITextComponent;)V",
            at = @At(
                    "HEAD"
            ),
            cancellable = true
    )
    public void postSay(ChatType chatTypeIn, ITextComponent message, CallbackInfo ci) {
        TickHandler.onChat(message, ci);
    }
}
