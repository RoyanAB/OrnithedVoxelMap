package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;
import net.minecraft.block.state.IBlockState;

import java.util.Arrays;


public class FullMapData extends AbstractMapData {
	public static final int DATABITS = 17;

	private static final int HEIGHTPOS = 0;
	private static final int BLOCKSTATEPOS = 1;
	private static final int TINTPOS = 2;
	private static final int LIGHTPOS = 3;
	private static final int OCEANFLOORHEIGHTPOS = 4;
	private static final int OCEANFLOORBLOCKSTATEPOS = 5;
	private static final int OCEANFLOORTINTPOS = 6;
	private static final int OCEANFLOORLIGHTPOS = 7;
	private static final int TRANSPARENTHEIGHTPOS = 8;
	private static final int TRANSPARENTBLOCKSTATEPOS = 9;
	private static final int TRANSPARENTTINTPOS = 10;
	private static final int TRANSPARENTLIGHTPOS = 11;
	private static final int FOLIAGEHEIGHTPOS = 12;
	private static final int FOLIAGEBLOCKSTATEPOS = 13;
	private static final int FOLIAGETINTPOS = 14;
	private static final int FOLIAGELIGHTPOS = 15;
	private static final int BIOMEIDPOS = 16;

	private int[] data;

	public FullMapData(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = new int[width * height * DATABITS];
		Arrays.fill(this.data, 0);
	}

	public void blank() {
		Arrays.fill(this.data, 0);
	}

	@Override
	public int getHeight(int x, int z) {
		return this.getData(x, z, HEIGHTPOS);
	}

	public int getBlockstateID(int x, int z) {
		return this.getData(x, z, BLOCKSTATEPOS);
	}

	@Override
	public IBlockState getBlockstate(int x, int z) {
		return this.getStateFromID(this.getData(x, z, BLOCKSTATEPOS));
	}

	@Override
	public int getBiomeTint(int x, int z) {
		return this.getData(x, z, TINTPOS);
	}

	@Override
	public int getLight(int x, int z) {
		return this.getData(x, z, LIGHTPOS);
	}

	@Override
	public int getOceanFloorHeight(int x, int z) {
		return this.getData(x, z, OCEANFLOORHEIGHTPOS);
	}

	public int getOceanFloorBlockstateID(int x, int z) {
		return this.getData(x, z, OCEANFLOORBLOCKSTATEPOS);
	}

	@Override
	public IBlockState getOceanFloorBlockstate(int x, int z) {
		return this.getStateFromID(this.getData(x, z, OCEANFLOORBLOCKSTATEPOS));
	}

	@Override
	public int getOceanFloorBiomeTint(int x, int z) {
		return this.getData(x, z, OCEANFLOORTINTPOS);
	}

	@Override
	public int getOceanFloorLight(int x, int z) {
		return this.getData(x, z, OCEANFLOORLIGHTPOS);
	}

	@Override
	public int getTransparentHeight(int x, int z) {
		return this.getData(x, z, TRANSPARENTHEIGHTPOS);
	}

	public int getTransparentBlockstateID(int x, int z) {
		return this.getData(x, z, TRANSPARENTBLOCKSTATEPOS);
	}

	@Override
	public IBlockState getTransparentBlockstate(int x, int z) {
		return this.getStateFromID(this.getData(x, z, TRANSPARENTBLOCKSTATEPOS));
	}

	@Override
	public int getTransparentBiomeTint(int x, int z) {
		return this.getData(x, z, TRANSPARENTTINTPOS);
	}

	@Override
	public int getTransparentLight(int x, int z) {
		return this.getData(x, z, TRANSPARENTLIGHTPOS);
	}

	@Override
	public int getFoliageHeight(int x, int z) {
		return this.getData(x, z, FOLIAGEHEIGHTPOS);
	}

	public int getFoliageBlockstateID(int x, int z) {
		return this.getData(x, z, FOLIAGEBLOCKSTATEPOS);
	}

	@Override
	public IBlockState getFoliageBlockstate(int x, int z) {
		return this.getStateFromID(this.getData(x, z, FOLIAGEBLOCKSTATEPOS));
	}

	@Override
	public int getFoliageBiomeTint(int x, int z) {
		return this.getData(x, z, FOLIAGETINTPOS);
	}

	@Override
	public int getFoliageLight(int x, int z) {
		return this.getData(x, z, FOLIAGELIGHTPOS);
	}

	@Override
	public int getBiomeID(int x, int z) {
		return this.getData(x, z, BIOMEIDPOS);
	}

	private int getData(int x, int z, int bit) {
		int index = (x + z * this.width) * DATABITS + bit;
		return this.data[index];
	}

	@Override
	public void setHeight(int x, int z, int value) {
		this.setData(x, z, HEIGHTPOS, value);
	}

	public void setBlockstateID(int x, int z, int id) {
		this.setData(x, z, BLOCKSTATEPOS, id);
	}

	@Override
	public void setBlockstate(int x, int z, IBlockState blockState) {
		this.setData(x, z, BLOCKSTATEPOS, this.getIDFromState(blockState));
	}

	@Override
	public void setBiomeTint(int x, int z, int value) {
		this.setData(x, z, TINTPOS, value);
	}

	@Override
	public void setLight(int x, int z, int value) {
		this.setData(x, z, LIGHTPOS, value);
	}

	@Override
	public void setOceanFloorHeight(int x, int z, int value) {
		this.setData(x, z, OCEANFLOORHEIGHTPOS, value);
	}

	public void setOceanFloorBlockstateID(int x, int z, int id) {
		this.setData(x, z, OCEANFLOORBLOCKSTATEPOS, id);
	}

	@Override
	public void setOceanFloorBlockstate(int x, int z, IBlockState blockState) {
		this.setData(x, z, OCEANFLOORBLOCKSTATEPOS, this.getIDFromState(blockState));
	}

	@Override
	public void setOceanFloorBiomeTint(int x, int z, int value) {
		this.setData(x, z, OCEANFLOORTINTPOS, value);
	}

	@Override
	public void setOceanFloorLight(int x, int z, int value) {
		this.setData(x, z, OCEANFLOORLIGHTPOS, value);
	}

	@Override
	public void setTransparentHeight(int x, int z, int value) {
		this.setData(x, z, TRANSPARENTHEIGHTPOS, value);
	}

	public void setTransparentBlockstateID(int x, int z, int id) {
		this.setData(x, z, TRANSPARENTBLOCKSTATEPOS, id);
	}

	@Override
	public void setTransparentBlockstate(int x, int z, IBlockState blockState) {
		this.setData(x, z, TRANSPARENTBLOCKSTATEPOS, this.getIDFromState(blockState));
	}

	@Override
	public void setTransparentBiomeTint(int x, int z, int value) {
		this.setData(x, z, TRANSPARENTTINTPOS, value);
	}

	@Override
	public void setTransparentLight(int x, int z, int value) {
		this.setData(x, z, TRANSPARENTLIGHTPOS, value);
	}

	@Override
	public void setFoliageHeight(int x, int z, int value) {
		this.setData(x, z, FOLIAGEHEIGHTPOS, value);
	}

	public void setFoliageBlockstateID(int x, int z, int id) {
		this.setData(x, z, FOLIAGEBLOCKSTATEPOS, id);
	}

	@Override
	public void setFoliageBlockstate(int x, int z, IBlockState blockState) {
		this.setData(x, z, FOLIAGEBLOCKSTATEPOS, this.getIDFromState(blockState));
	}

	@Override
	public void setFoliageBiomeTint(int x, int z, int value) {
		this.setData(x, z, FOLIAGETINTPOS, value);
	}

	@Override
	public void setFoliageLight(int x, int z, int value) {
		this.setData(x, z, FOLIAGELIGHTPOS, value);
	}

	@Override
	public void setBiomeID(int x, int z, int value) {
		this.setData(x, z, BIOMEIDPOS, value);
	}

	private void setData(int x, int z, int bit, int value) {
		int index = (x + z * this.width) * DATABITS + bit;
		this.data[index] = value;
	}

	@Override
	public void moveX(int offset) {
		synchronized (this.dataLock) {
			if (offset > 0) {
				System.arraycopy(this.data, offset * DATABITS, this.data, 0, this.data.length - offset * DATABITS);
			} else if (offset < 0) {
				System.arraycopy(this.data, 0, this.data, -offset * DATABITS, this.data.length + offset * DATABITS);
			}
		}
	}

	@Override
	public void moveZ(int offset) {
		synchronized (this.dataLock) {
			if (offset > 0) {
				System.arraycopy(this.data, offset * this.width * DATABITS, this.data, 0, this.data.length - offset * this.width * DATABITS);
			} else if (offset < 0) {
				System.arraycopy(this.data, 0, this.data, -offset * this.width * DATABITS, this.data.length + offset * this.width * DATABITS);
			}
		}
	}

	public int[] getData() {
		return this.data;
	}

	public void setData(int[] is) {
		this.data = is;
	}

	private int getIDFromState(IBlockState blockState) {
		return BlockRepository.getStateId(blockState);
	}

	private IBlockState getStateFromID(int id) {
		return BlockRepository.getStateById(id);
	}
}
