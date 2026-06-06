package com.mamiyaotaru.voxelmap.persistent;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mamiyaotaru.voxelmap.VoxelConstants;
import com.mamiyaotaru.voxelmap.interfaces.AbstractMapData;
import com.mamiyaotaru.voxelmap.util.CompressionUtils;
import net.minecraft.block.state.IBlockState;

import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;


public class CompressibleMapData extends AbstractMapData {
	public static final int DATABITS = 18;

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

	private static byte[] compressedEmptyData = new byte[1179648];

	static {
		Arrays.fill(compressedEmptyData, (byte) 0);

		try {
			compressedEmptyData = CompressionUtils.compress(compressedEmptyData);
		} catch (IOException e) {
			VoxelConstants.getLogger().error(e);
		}
	}

	int count = 1;
	private byte[] data;
	private boolean isCompressed;
	private BiMap<IBlockState, Integer> stateToInt;

	public CompressibleMapData(int width, int height) {
		this.width = width;
		this.height = height;
		this.data = compressedEmptyData;
		this.isCompressed = true;
	}

	@Override
	public int getHeight(int x, int z) {
		return this.getData(x, z, HEIGHTPOS) & 0xFF;
	}

	@Override
	public IBlockState getBlockstate(int x, int z) {
		int id = (this.getData(x, z, BLOCKSTATEPOS) & 255) << 8 | this.getData(x, z, TINTPOS) & 255;
		return this.getStateFromID(id);
	}

	@Override
	public int getBiomeTint(int x, int z) {
		return 0;
	}

	@Override
	public int getLight(int x, int z) {
		return this.getData(x, z, LIGHTPOS) & 0xFF;
	}

	@Override
	public int getOceanFloorHeight(int x, int z) {
		return this.getData(x, z, OCEANFLOORHEIGHTPOS) & 0xFF;
	}

	@Override
	public IBlockState getOceanFloorBlockstate(int x, int z) {
		int id = (this.getData(x, z, OCEANFLOORBLOCKSTATEPOS) & 255) << 8 | this.getData(x, z, OCEANFLOORTINTPOS) & 255;
		return this.getStateFromID(id);
	}

	@Override
	public int getOceanFloorBiomeTint(int x, int z) {
		return 0;
	}

	@Override
	public int getOceanFloorLight(int x, int z) {
		return this.getData(x, z, OCEANFLOORLIGHTPOS) & 0xFF;
	}

	@Override
	public int getTransparentHeight(int x, int z) {
		return this.getData(x, z, TRANSPARENTHEIGHTPOS) & 0xFF;
	}

	@Override
	public IBlockState getTransparentBlockstate(int x, int z) {
		int id = (this.getData(x, z, TRANSPARENTBLOCKSTATEPOS) & 255) << 8 | this.getData(x, z, TRANSPARENTTINTPOS) & 255;
		return this.getStateFromID(id);
	}

	@Override
	public int getTransparentBiomeTint(int x, int z) {
		return 0;
	}

	@Override
	public int getTransparentLight(int x, int z) {
		return this.getData(x, z, TRANSPARENTLIGHTPOS) & 0xFF;
	}

	@Override
	public int getFoliageHeight(int x, int z) {
		return this.getData(x, z, FOLIAGEHEIGHTPOS) & 0xFF;
	}

	@Override
	public IBlockState getFoliageBlockstate(int x, int z) {
		int id = (this.getData(x, z, FOLIAGEBLOCKSTATEPOS) & 255) << 8 | this.getData(x, z, FOLIAGETINTPOS) & 255;
		return this.getStateFromID(id);
	}

	@Override
	public int getFoliageBiomeTint(int x, int z) {
		return 0;
	}

	@Override
	public int getFoliageLight(int x, int z) {
		return this.getData(x, z, FOLIAGELIGHTPOS) & 0xFF;
	}

	@Override
	public int getBiomeID(int x, int z) {
		return (this.getData(x, z, BIOMEIDPOS) & 0xFF) << 8 | this.getData(x, z, 17) & 0xFF;
	}

	private synchronized byte getData(int x, int z, int bit) {
		if (this.isCompressed) {
			this.decompress();
		}

		int index = x + z * this.width + this.width * this.height * bit;
		return this.data[index];
	}

	@Override
	public void setHeight(int x, int z, int value) {
		this.setData(x, z, HEIGHTPOS, (byte) value);
	}

	@Override
	public void setBlockstate(int x, int z, IBlockState blockState) {
		int id = this.getIDFromState(blockState);
		this.setData(x, z, BLOCKSTATEPOS, (byte) (id >> 8));
		this.setData(x, z, TINTPOS, (byte) id);
	}

	@Override
	public void setBiomeTint(int x, int z, int value) {
	}

	@Override
	public void setLight(int x, int z, int value) {
		this.setData(x, z, LIGHTPOS, (byte) value);
	}

	@Override
	public void setOceanFloorHeight(int x, int z, int value) {
		this.setData(x, z, OCEANFLOORHEIGHTPOS, (byte) value);
	}

	@Override
	public void setOceanFloorBlockstate(int x, int z, IBlockState blockState) {
		int id = this.getIDFromState(blockState);
		this.setData(x, z, OCEANFLOORBLOCKSTATEPOS, (byte) (id >> 8));
		this.setData(x, z, OCEANFLOORTINTPOS, (byte) id);
	}

	@Override
	public void setOceanFloorBiomeTint(int x, int z, int value) {
	}

	@Override
	public void setOceanFloorLight(int x, int z, int value) {
		this.setData(x, z, OCEANFLOORLIGHTPOS, (byte) value);
	}

	@Override
	public void setTransparentHeight(int x, int z, int value) {
		this.setData(x, z, TRANSPARENTHEIGHTPOS, (byte) value);
	}

	@Override
	public void setTransparentBlockstate(int x, int z, IBlockState blockState) {
		int id = this.getIDFromState(blockState);
		this.setData(x, z, TRANSPARENTBLOCKSTATEPOS, (byte) (id >> 8));
		this.setData(x, z, TRANSPARENTTINTPOS, (byte) id);
	}

	@Override
	public void setTransparentBiomeTint(int x, int z, int value) {
	}

	@Override
	public void setTransparentLight(int x, int z, int value) {
		this.setData(x, z, TRANSPARENTLIGHTPOS, (byte) value);
	}

	@Override
	public void setFoliageHeight(int x, int z, int value) {
		this.setData(x, z, FOLIAGEHEIGHTPOS, (byte) value);
	}

	@Override
	public void setFoliageBlockstate(int x, int z, IBlockState blockState) {
		int id = this.getIDFromState(blockState);
		this.setData(x, z, FOLIAGEBLOCKSTATEPOS, (byte) (id >> 8));
		this.setData(x, z, FOLIAGETINTPOS, (byte) id);
	}

	@Override
	public void setFoliageBiomeTint(int x, int z, int value) {
	}

	@Override
	public void setFoliageLight(int x, int z, int value) {
		this.setData(x, z, FOLIAGELIGHTPOS, (byte) value);
	}

	@Override
	public void setBiomeID(int x, int z, int value) {
		this.setData(x, z, BIOMEIDPOS, (byte) (value >> 8));
		this.setData(x, z, 17, (byte) value);
	}

	private synchronized void setData(int x, int z, int bit, byte value) {
		if (this.isCompressed) {
			this.decompress();
		}

		int index = x + z * this.width + this.width * this.height * bit;
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

	public synchronized void setData(byte[] is, BiMap<IBlockState, Integer> newStateToInt, int version) {
		this.data = is;
		this.isCompressed = false;
		if (version < 2) {
			this.convertData();
		}

		this.stateToInt = newStateToInt;
		this.count = this.stateToInt.size();
	}

	private synchronized void convertData() {
		if (this.isCompressed) {
			this.decompress();
		}

		byte[] newData = new byte[this.data.length];

		for (int x = 0; x < this.width; x++) {
			for (int z = 0; z < this.height; z++) {
				for (int bit = 0; bit < DATABITS; bit++) {
					int oldIndex = (x + z * this.width) * DATABITS + bit;
					int newIndex = x + z * this.width + this.width * this.height * bit;
					newData[newIndex] = this.data[oldIndex];
				}
			}
		}

		this.data = newData;
	}

	public synchronized byte[] getData() {
		if (this.isCompressed) {
			this.decompress();
		}

		return this.data;
	}

	public synchronized void compress() {
		if (!this.isCompressed) {
			try {
				this.isCompressed = true;
				this.data = CompressionUtils.compress(this.data);
			} catch (IOException ignored) {
			}
		}
	}

	private synchronized void decompress() {
		if (this.stateToInt == null) {
			this.stateToInt = HashBiMap.create();
		}

		if (this.isCompressed) {
			try {
				this.data = CompressionUtils.decompress(this.data);
				this.isCompressed = false;
			} catch (IOException | DataFormatException ignored) {
			}
		}
	}

	public synchronized boolean isCompressed() {
		return this.isCompressed;
	}

	private synchronized int getIDFromState(IBlockState blockState) {
		Integer id = this.stateToInt.get(blockState);
		if (id == null && blockState != null) {
			while (this.stateToInt.inverse().containsKey(this.count)) {
				this.count++;
			}

			id = this.count;
			this.stateToInt.put(blockState, id);
		}

		return id;
	}

	private IBlockState getStateFromID(int id) {
		return this.stateToInt.inverse().get(id);
	}

	public BiMap<IBlockState, Integer> getStateToInt() {
		this.stateToInt = this.createKeyFromCurrentBlocks(this.stateToInt);
		return this.stateToInt;
	}

	private BiMap<IBlockState, Integer> createKeyFromCurrentBlocks(BiMap<IBlockState, Integer> oldMap) {
		this.count = 1;
		BiMap<IBlockState, Integer> newMap = HashBiMap.create();

		for (int x = 0; x < this.width; x++) {
			for (int z = 0; z < this.height; z++) {
				int oldID = (this.getData(x, z, BLOCKSTATEPOS) & 255) << 8 | this.getData(x, z, TINTPOS) & 255;
				if (oldID != 0) {
					IBlockState blockState = oldMap.inverse().get(oldID);
					Integer id = newMap.get(blockState);
					if (id == null && blockState != null) {
						while (newMap.inverse().containsKey(this.count)) {
							this.count++;
						}

						id = this.count;
						newMap.put(blockState, id);
					}

					this.setData(x, z, BLOCKSTATEPOS, (byte) (id >> 8));
					this.setData(x, z, TINTPOS, (byte) id.intValue());
				}

				oldID = (this.getData(x, z, OCEANFLOORBLOCKSTATEPOS) & 255) << 8 | this.getData(x, z, OCEANFLOORTINTPOS) & 255;
				if (oldID != 0) {
					IBlockState blockState = oldMap.inverse().get(oldID);
					Integer id = newMap.get(blockState);
					if (id == null && blockState != null) {
						while (newMap.inverse().containsKey(this.count)) {
							this.count++;
						}

						id = this.count;
						newMap.put(blockState, id);
					}

					this.setData(x, z, OCEANFLOORBLOCKSTATEPOS, (byte) (id >> 8));
					this.setData(x, z, OCEANFLOORTINTPOS, (byte) id.intValue());
				}

				oldID = (this.getData(x, z, TRANSPARENTBLOCKSTATEPOS) & 255) << 8 | this.getData(x, z, TRANSPARENTTINTPOS) & 255;
				if (oldID != 0) {
					IBlockState blockState = oldMap.inverse().get(oldID);
					Integer id = newMap.get(blockState);
					if (id == null && blockState != null) {
						while (newMap.inverse().containsKey(this.count)) {
							this.count++;
						}

						id = this.count;
						newMap.put(blockState, id);
					}

					this.setData(x, z, TRANSPARENTBLOCKSTATEPOS, (byte) (id >> 8));
					this.setData(x, z, TRANSPARENTTINTPOS, (byte) id.intValue());
				}

				oldID = (this.getData(x, z, FOLIAGEBLOCKSTATEPOS) & 255) << 8 | this.getData(x, z, FOLIAGETINTPOS) & 255;
				if (oldID != 0) {
					IBlockState blockState = oldMap.inverse().get(oldID);
					Integer id = newMap.get(blockState);
					if (id == null && blockState != null) {
						while (newMap.inverse().containsKey(this.count)) {
							this.count++;
						}

						id = this.count;
						newMap.put(blockState, id);
					}

					this.setData(x, z, FOLIAGEBLOCKSTATEPOS, (byte) (id >> 8));
					this.setData(x, z, FOLIAGETINTPOS, (byte) id.intValue());
				}
			}
		}

		return newMap;
	}
}
