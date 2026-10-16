package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public interface IMap extends IChangeObserver {
	void forceFullRender(boolean redraw);

	void drawMinimap(Minecraft minecraft);

	float getPercentX();

	float getPercentY();

	void newWorld(World world);

	void onTick(Minecraft minecraft, boolean inGame);

	void onTickInGame(Minecraft minecraft);

	int[] getLightmapArray();

	void getFogColor();
}
