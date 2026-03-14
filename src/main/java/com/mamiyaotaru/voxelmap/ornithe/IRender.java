package com.mamiyaotaru.voxelmap.ornithe;

import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public interface IRender {
	<T extends Entity> ResourceLocation publicGetEntityTexture(T var1);
}
