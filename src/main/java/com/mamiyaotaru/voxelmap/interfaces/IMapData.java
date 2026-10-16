package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.block.state.IBlockState;

public interface IMapData {
	int DATABITS = 17;
	int BYTESPERDATUM = 4;

	int getWidth();

	int getHeight();

	int getHeight(int x, int z);

	IBlockState getBlockstate(int x, int z);

	int getBiomeTint(int x, int z);

	int getLight(int x, int z);

	int getOceanFloorHeight(int x, int z);

	IBlockState getOceanFloorBlockstate(int x, int z);

	int getOceanFloorBiomeTint(int x, int z);

	int getOceanFloorLight(int x, int z);

	int getTransparentHeight(int x, int z);

	IBlockState getTransparentBlockstate(int x, int z);

	int getTransparentBiomeTint(int x, int z);

	int getTransparentLight(int x, int z);

	int getFoliageHeight(int x, int z);

	IBlockState getFoliageBlockstate(int x, int z);

	int getFoliageBiomeTint(int x, int z);

	int getFoliageLight(int x, int z);

	int getBiomeID(int x, int z);

	void setHeight(int x, int z, int height);

	void setBlockstate(int x, int z, IBlockState blockState);

	void setBiomeTint(int x, int z, int tint);

	void setLight(int x, int z, int light);

	void setOceanFloorHeight(int x, int z, int height);

	void setOceanFloorBlockstate(int x, int z, IBlockState blockState);

	void setOceanFloorBiomeTint(int x, int z, int tint);

	void setOceanFloorLight(int x, int z, int light);

	void setTransparentHeight(int x, int z, int height);

	void setTransparentBlockstate(int x, int z, IBlockState blockState);

	void setTransparentBiomeTint(int x, int z, int tint);

	void setTransparentLight(int x, int z, int light);

	void setFoliageHeight(int x, int z, int height);

	void setFoliageBlockstate(int x, int z, IBlockState blockState);

	void setFoliageBiomeTint(int x, int z, int tint);

	void setFoliageLight(int x, int z, int light);

	void setBiomeID(int x, int z, int biomeID);

	void moveX(int amount);

	void moveZ(int amount);
}
