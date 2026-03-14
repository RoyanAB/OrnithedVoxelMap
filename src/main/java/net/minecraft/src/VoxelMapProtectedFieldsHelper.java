package net.minecraft.src;

import com.mamiyaotaru.voxelmap.ornithe.IRender;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class VoxelMapProtectedFieldsHelper {
	static boolean getRendersResourceLocationDirect = true;

	public static ResourceLocation getRendersResourceLocation(Render<? extends Entity> render, Entity entity) {
		return ((IRender) render).publicGetEntityTexture(entity);
	}
}
