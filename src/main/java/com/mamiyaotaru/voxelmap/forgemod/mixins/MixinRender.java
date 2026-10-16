package com.mamiyaotaru.voxelmap.forgemod.mixins;

import com.mamiyaotaru.voxelmap.forgemod.IRender;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Render.class)
public abstract class MixinRender implements IRender {
   @Shadow
   protected abstract <T extends Entity> ResourceLocation func_110775_a(T var1);

   @Override
   public <T extends Entity> ResourceLocation publicGetEntityTexture(T entity) {
      return this.func_110775_a(entity);
   }
}
