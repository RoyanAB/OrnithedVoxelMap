package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.interfaces.IChangeObserver;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.particles.IParticleData;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldEventListener;

import java.util.ArrayList;

public class WorldUpdateListener implements IWorldEventListener {
    private final ArrayList<IChangeObserver> chunkProcessors = new ArrayList<>();

    public void addListener(IChangeObserver chunkProcessor) {
        this.chunkProcessors.add(chunkProcessor);
    }

    public void notifyBlockUpdate(IBlockReader worldIn, BlockPos pos, IBlockState oldState, IBlockState newState, int flags) {
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

    public void playSoundToAllNearExcept(
            EntityPlayer player, SoundEvent soundIn, SoundCategory category, double x, double y, double z, float volume, float pitch
    ) {
    }

    public void playRecord(SoundEvent soundIn, BlockPos pos) {
    }

    public void addParticle(IParticleData arg0, boolean arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7) {
    }

    public void addParticle(IParticleData arg0, boolean arg1, boolean arg2, double arg3, double arg4, double arg5, double arg6, double arg7, double arg8) {
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
