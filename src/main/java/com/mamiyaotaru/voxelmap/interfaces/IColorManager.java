package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.MutableBlockPos;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.resources.IResourceManager;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;

public interface IColorManager {
    void onResourceManagerReload(IResourceManager var1);

    BufferedImage getColorPicker();

    BufferedImage getBlockImage(IBlockState var1, ItemStack var2, World var3);

    boolean checkForChanges();

    int colorAdder(int var1, int var2);

    int colorMultiplier(int var1, int var2);

    int getBlockColorWithDefaultTint(MutableBlockPos var1, int var2);

    int getBlockColor(MutableBlockPos var1, int var2, int var3);

    void setSkyColor(int var1);

    int getAirColor();

    int getBiomeTint(AbstractMapData var1, World var2, IBlockState var3, int var4, MutableBlockPos var5, MutableBlockPos var6, int var7, int var8);
}
