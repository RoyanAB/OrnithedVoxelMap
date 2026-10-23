package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.block.state.IBlockState;

/**
 * Stores per-column world data used to draw the map.
 *
 * <p>For each (x, z) column the map keeps track of several layers: the surface
 * (ground), the ocean floor, transparent blocks, and foliage, along with each
 * layer's height, block state, biome tint, and light value.
 */
public interface IMapData {
	/** The number of per-column data fields stored by the map data. */
	int DATABITS = 17;
	/** The number of bytes each per-column data field occupies in the backing array. */
	int BYTESPERDATUM = 4;

	/**
	 * Returns the width of the map data grid.
	 *
	 * @return the width in columns
	 */
	int getWidth();

	/**
	 * Returns the height of the map data grid.
	 *
	 * @return the height in columns
	 */
	int getHeight();

	/**
	 * Returns the surface (ground) height at the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the surface height, or -1 if no surface block was found
	 */
	int getHeight(int x, int z);

	/**
	 * Returns the block state at the surface of the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the surface block state
	 */
	IBlockState getBlockstate(int x, int z);

	/**
	 * Returns the biome tint stored for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the surface biome tint
	 */
	int getBiomeTint(int x, int z);

	/**
	 * Returns the light value stored for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the surface light value
	 */
	int getLight(int x, int z);

	/**
	 * Returns the ocean floor height at the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the ocean floor height
	 */
	int getOceanFloorHeight(int x, int z);

	/**
	 * Returns the block state at the ocean floor of the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the ocean floor block state
	 */
	IBlockState getOceanFloorBlockstate(int x, int z);

	/**
	 * Returns the ocean floor biome tint for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the ocean floor biome tint
	 */
	int getOceanFloorBiomeTint(int x, int z);

	/**
	 * Returns the ocean floor light value for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the ocean floor light value
	 */
	int getOceanFloorLight(int x, int z);

	/**
	 * Returns the height of transparent blocks at the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the transparent block height, or -1 if none
	 */
	int getTransparentHeight(int x, int z);

	/**
	 * Returns the transparent block state at the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the transparent block state
	 */
	IBlockState getTransparentBlockstate(int x, int z);

	/**
	 * Returns the transparent block biome tint for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the transparent block biome tint
	 */
	int getTransparentBiomeTint(int x, int z);

	/**
	 * Returns the transparent block light value for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the transparent block light value
	 */
	int getTransparentLight(int x, int z);

	/**
	 * Returns the foliage height at the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the foliage height, or -1 if none
	 */
	int getFoliageHeight(int x, int z);

	/**
	 * Returns the foliage block state at the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the foliage block state
	 */
	IBlockState getFoliageBlockstate(int x, int z);

	/**
	 * Returns the foliage biome tint for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the foliage biome tint
	 */
	int getFoliageBiomeTint(int x, int z);

	/**
	 * Returns the foliage light value for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the foliage light value
	 */
	int getFoliageLight(int x, int z);

	/**
	 * Returns the biome ID stored for the given column.
	 *
	 * @param x the column x coordinate
	 * @param z the column z coordinate
	 * @return the biome ID, or -1 if it could not be determined
	 */
	int getBiomeID(int x, int z);

	/**
	 * Stores the surface height for the given column.
	 *
	 * @param x      the column x coordinate
	 * @param z      the column z coordinate
	 * @param height the surface height to store
	 */
	void setHeight(int x, int z, int height);

	/**
	 * Stores the surface block state for the given column.
	 *
	 * @param x          the column x coordinate
	 * @param z          the column z coordinate
	 * @param blockState the block state to store
	 */
	void setBlockstate(int x, int z, IBlockState blockState);

	/**
	 * Stores the surface biome tint for the given column.
	 *
	 * @param x    the column x coordinate
	 * @param z    the column z coordinate
	 * @param tint the biome tint to store
	 */
	void setBiomeTint(int x, int z, int tint);

	/**
	 * Stores the surface light value for the given column.
	 *
	 * @param x     the column x coordinate
	 * @param z     the column z coordinate
	 * @param light the light value to store
	 */
	void setLight(int x, int z, int light);

	/**
	 * Stores the ocean floor height for the given column.
	 *
	 * @param x      the column x coordinate
	 * @param z      the column z coordinate
	 * @param height the ocean floor height to store
	 */
	void setOceanFloorHeight(int x, int z, int height);

	/**
	 * Stores the ocean floor block state for the given column.
	 *
	 * @param x          the column x coordinate
	 * @param z          the column z coordinate
	 * @param blockState the block state to store
	 */
	void setOceanFloorBlockstate(int x, int z, IBlockState blockState);

	/**
	 * Stores the ocean floor biome tint for the given column.
	 *
	 * @param x    the column x coordinate
	 * @param z    the column z coordinate
	 * @param tint the biome tint to store
	 */
	void setOceanFloorBiomeTint(int x, int z, int tint);

	/**
	 * Stores the ocean floor light value for the given column.
	 *
	 * @param x     the column x coordinate
	 * @param z     the column z coordinate
	 * @param light the light value to store
	 */
	void setOceanFloorLight(int x, int z, int light);

	/**
	 * Stores the transparent block height for the given column.
	 *
	 * @param x      the column x coordinate
	 * @param z      the column z coordinate
	 * @param height the transparent block height to store
	 */
	void setTransparentHeight(int x, int z, int height);

	/**
	 * Stores the transparent block state for the given column.
	 *
	 * @param x          the column x coordinate
	 * @param z          the column z coordinate
	 * @param blockState the block state to store
	 */
	void setTransparentBlockstate(int x, int z, IBlockState blockState);

	/**
	 * Stores the transparent block biome tint for the given column.
	 *
	 * @param x    the column x coordinate
	 * @param z    the column z coordinate
	 * @param tint the biome tint to store
	 */
	void setTransparentBiomeTint(int x, int z, int tint);

	/**
	 * Stores the transparent block light value for the given column.
	 *
	 * @param x     the column x coordinate
	 * @param z     the column z coordinate
	 * @param light the light value to store
	 */
	void setTransparentLight(int x, int z, int light);

	/**
	 * Stores the foliage height for the given column.
	 *
	 * @param x      the column x coordinate
	 * @param z      the column z coordinate
	 * @param height the foliage height to store
	 */
	void setFoliageHeight(int x, int z, int height);

	/**
	 * Stores the foliage block state for the given column.
	 *
	 * @param x          the column x coordinate
	 * @param z          the column z coordinate
	 * @param blockState the block state to store
	 */
	void setFoliageBlockstate(int x, int z, IBlockState blockState);

	/**
	 * Stores the foliage biome tint for the given column.
	 *
	 * @param x    the column x coordinate
	 * @param z    the column z coordinate
	 * @param tint the biome tint to store
	 */
	void setFoliageBiomeTint(int x, int z, int tint);

	/**
	 * Stores the foliage light value for the given column.
	 *
	 * @param x     the column x coordinate
	 * @param z     the column z coordinate
	 * @param light the light value to store
	 */
	void setFoliageLight(int x, int z, int light);

	/**
	 * Stores the biome ID for the given column.
	 *
	 * @param x       the column x coordinate
	 * @param z       the column z coordinate
	 * @param biomeID the biome ID to store
	 */
	void setBiomeID(int x, int z, int biomeID);

	/**
	 * Shifts the entire grid horizontally by the given number of columns.
	 * A positive offset shifts content toward lower x.
	 *
	 * @param offset the number of columns to shift
	 */
	void moveX(int offset);

	/**
	 * Shifts the entire grid vertically by the given number of columns.
	 * A positive offset shifts content toward lower z.
	 *
	 * @param offset the number of columns to shift
	 */
	void moveZ(int offset);
}
