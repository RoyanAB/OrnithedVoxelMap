package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public interface IMap extends IChangeObserver {
	void forceFullRender(boolean b);

	void drawMinimap(Minecraft mc);

	float getPercentX();

	float getPercentY();

	void newWorld(World world);

	void onTick(Minecraft mc, boolean b);

	void onTickInGame(Minecraft mc);

	int[] getLightmapArray();

	void getFogColor();
}
