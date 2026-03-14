package com.mamiyaotaru.voxelmap.ornithe.mixins;

import com.mamiyaotaru.voxelmap.ornithe.IRender;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Render.class)
public abstract class RenderMixin implements IRender {
	@Shadow
	protected abstract <T extends Entity> ResourceLocation getEntityTexture(T var1);

	@Override
	public <T extends Entity> ResourceLocation publicGetEntityTexture(T entity) {
		return this.getEntityTexture(entity);
	}
}
