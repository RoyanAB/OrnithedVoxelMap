package com.mamiyaotaru.voxelmap.ornithe.mixins;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Render.class)
public interface RenderAccessor<T extends Entity> {
    @Invoker("getEntityTexture")
    ResourceLocation invokerGetEntityTexture(T entity);
}
