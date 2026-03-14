package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.Share;
import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.render.WorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class ONMixinWorldRenderer {
    @Redirect(method = "renderSky(F)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/GlStateManager;rotatef(FFFF)V"))
    private void onRotate(float angle, float x, float y, float z) {
        if (Share.isOldNorth()) {
            if (angle == 90.0F && x == 0.0F && y == 0.0F && z == 1.0F) {
                return;
            }

            if (angle == -90.0F && x == 0.0F && y == 1.0F && z == 0.0F) {
                return;
            }
        }

        GlStateManager.rotatef(angle, x, y, z);
    }
}
