package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.Share;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BackgroundRenderer.class)
public class ONMixinBackgroundRenderer {
    @Redirect(
            method = "updateColorNotInWater(Lnet/minecraft/client/render/Camera;Lnet/minecraft/world/World;F)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/Vec3d;dotProduct(Lnet/minecraft/util/math/Vec3d;)D")
    )
    private double onDotProduct(Vec3d vec3d, Vec3d arg) {
        if (Share.isOldNorth()) {
            arg = new Vec3d(0.0, 0.0, -arg.x);
        }

        return vec3d.dotProduct(arg);
    }
}
