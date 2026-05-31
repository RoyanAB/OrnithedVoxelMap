package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;

import java.util.ArrayList;

@SuppressWarnings("unused")
public class WorldUpdateListener implements IWorldEventListener {
	private final ArrayList<IChangeObserver> chunkProcessors = new ArrayList<>();

	public void addListener(IChangeObserver chunkProcessor) {
		this.chunkProcessors.add(chunkProcessor);
	}

	public void notifyBlockUpdate(World worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
		this.notifyObservers(pos, null);
	}

	public void notifyLightSet(BlockPos pos) {
		this.notifyObservers(pos, null);
	}

	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
		this.notifyObservers(new BlockPos(x1, y1, z1), new BlockPos(x2, y2, z2));
	}

	private void notifyObservers(BlockPos pos1, BlockPos pos2) {
		for (IChangeObserver chunkProcessor : this.chunkProcessors) {
			chunkProcessor.handleChangeInWorld(pos1, pos2);
		}
	}

	public void playSoundToAllNearExcept(EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch) {
	}

	public void playRecord(SoundEvent soundIn, BlockPos pos) {
	}

	public void spawnParticle(
		int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... parameters
	) {
	}

	public void spawnParticle(
		int id,
		boolean ignoreRange,
		boolean minimiseParticleLevel,
		double x,
		double y,
		double z,
		double xSpeed,
		double ySpeed,
		double zSpeed,
		int... parameters
	) {
	}

	public void onEntityAdded(Entity entityIn) {
	}

	public void onEntityRemoved(Entity entityIn) {
	}

	public void broadcastSound(int soundID, BlockPos pos, int data) {
	}

	public void playEvent(EntityPlayer player, int type, BlockPos blockPosIn, int data) {
	}

	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
	}
}
