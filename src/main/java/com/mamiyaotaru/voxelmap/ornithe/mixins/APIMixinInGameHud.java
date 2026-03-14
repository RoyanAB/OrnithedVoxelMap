package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.FabricModVoxelMap;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class APIMixinInGameHud {
    @Inject(method = "render(F)V", at = @At("RETURN"))
    private void onRenderGameOverlay(float partialTicks, CallbackInfo ci) {
        FabricModVoxelMap.instance.renderOverlay();
    }
}
