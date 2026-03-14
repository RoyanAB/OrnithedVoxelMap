package com.mamiyaotaru.voxelmap.ornithe.mixins;

import net.fabricmc.loader.api.ModContainer;
import net.ornithemc.osl.resource.loader.impl.BuiltInModResourcePack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BuiltInModResourcePack.class)
public interface BuiltInModResourcePackAccessor {
	@Accessor(remap = false)
	ModContainer getMod();
}
