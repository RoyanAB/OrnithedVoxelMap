package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.MutableBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;

public interface IColorManager {
	void onResourceManagerReload(IResourceManager resourceManager);

	BufferedImage getColorPicker();

	BufferedImage getBlockImage(IBlockState blockState, ItemStack stack, World world);

	boolean checkForChanges();

	int colorAdder(int color1, int color2);

	int colorMultiplier(int color1, int color2);

	int getBlockColorWithDefaultTint(MutableBlockPos blockPos, int blockStateID);

	int getBlockColor(MutableBlockPos blockPos, int blockStateID, int biomeID);

	void setSkyColor(int skyColor);

	int getAirColor();

	int getBiomeTint(AbstractMapData mapData, World world, IBlockState blockState, int blockStateID, MutableBlockPos blockPos, MutableBlockPos loopBlockPos, int startX, int startZ);
}
