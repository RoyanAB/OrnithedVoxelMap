package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.block.state.IBlockState;

@SuppressWarnings("unused")
public interface IMapData {
	int getWidth();

	int getHeight();

	int getHeight(int imageX, int imageY);

	IBlockState getBlockstate(int imageX, int imageY);

	int getBiomeTint(int imageX, int imageY);

	int getLight(int imageX, int imageY);

	int getOceanFloorHeight(int imageX, int imageY);

	IBlockState getOceanFloorBlockstate(int imageX, int imageY);

	int getOceanFloorBiomeTint(int imageX, int imageY);

	int getOceanFloorLight(int imageX, int imageY);

	int getTransparentHeight(int imageX, int imageY);

	IBlockState getTransparentBlockstate(int imageX, int imageY2);

	int getTransparentBiomeTint(int imageX, int imageY);

	int getTransparentLight(int imageX, int imageY);

	int getFoliageHeight(int imageX, int imageY);

	IBlockState getFoliageBlockstate(int imageX, int imageY);

	int getFoliageBiomeTint(int imageX, int imageY);

	int getFoliageLight(int imageX, int imageY);

	int getBiomeID(int imageX, int imageY);

	void setHeight(int imageX, int imageY, int surfaceHeight);

	void setBlockstate(int imageX, int imageY, IBlockState blockState);

	void setBiomeTint(int imageX, int imageY, int tint);

	void setLight(int imageX, int imageY, int light);

	void setOceanFloorHeight(int imageX, int imageY, int seafloorHeight);

	void setOceanFloorBlockstate(int imageX, int imageY, IBlockState blockState);

	void setOceanFloorBiomeTint(int imageX, int imageY, int tint);

	void setOceanFloorLight(int imageX, int imageY, int seafloorLight);

	void setTransparentHeight(int imageX, int imageY, int transparentHeight);

	void setTransparentBlockstate(int imageX, int imageY, IBlockState blockState);

	void setTransparentBiomeTint(int imageX, int imageY, int tint);

	void setTransparentLight(int imageX, int imageY, int transparentLight);

	void setFoliageHeight(int imageX, int imageY, int foliageHeight);

	void setFoliageBlockstate(int imageX, int imageY, IBlockState blockState);

	void setFoliageBiomeTint(int imageX, int imageY, int tint);

	void setFoliageLight(int imageX, int imageY, int foliageLight);

	void setBiomeID(int imageX, int imageY, int biomeId);

	void moveX(int offsetX);

	void moveZ(int offsetZ);
}
