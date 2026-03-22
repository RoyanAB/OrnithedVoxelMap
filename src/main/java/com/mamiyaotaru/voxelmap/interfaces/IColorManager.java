package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.MutableBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;

@SuppressWarnings("unused")
public interface IColorManager {
	void onResourceManagerReload(IResourceManager iResourceManager);

	BufferedImage getColorPicker();

	BufferedImage getBlockImage(IBlockState iBlockState, ItemStack itemStack, World world);

	boolean checkForChanges();

	int colorAdder(int color1, int color2);

	int colorMultiplier(int color, int tint);

	int getBlockColorWithDefaultTint(MutableBlockPos pos, int blockStateID);

	int getBlockColor(MutableBlockPos pos, int blockStateID, int biomeID);

	void setSkyColor(int color);

	int getAirColor();

	int getBiomeTint(AbstractMapData abstractMapData, World world, IBlockState iBlockState, int blockStateID, MutableBlockPos pos1, MutableBlockPos pos2, int startX, int startZ);
}
