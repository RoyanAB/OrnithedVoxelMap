package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

public interface IMap extends IChangeObserver {
	void forceFullRender(boolean var1);

	void drawMinimap(Minecraft var1);

	float getPercentX();

	float getPercentY();

	void newWorld(World var1);

	void onTick(Minecraft var1, boolean var2);

	void onTickInGame(Minecraft var1);

	int[] getLightmapArray();

	void getFogColor();
}
