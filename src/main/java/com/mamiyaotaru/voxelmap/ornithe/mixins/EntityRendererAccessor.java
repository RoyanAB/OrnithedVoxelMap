package com.mamiyaotaru.voxelmap.ornithe.mixins;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(EntityRenderer.class)
public interface EntityRendererAccessor<T extends Entity> {
    @Invoker("getTexture")
    Identifier invokerGetTexture(T entity);
}
