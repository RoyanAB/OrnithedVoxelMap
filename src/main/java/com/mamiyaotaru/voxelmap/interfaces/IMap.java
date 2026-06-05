package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;

@SuppressWarnings("unused")
public interface IMap extends IChangeObserver {
	void forceFullRender(boolean forceFullRender);

	void drawMinimap(Minecraft mc);

	float getPercentX();

	float getPercentY();

	void newWorld(World world);

	void onTick(Minecraft mc, boolean clock);

	void onTickInGame(Minecraft mc);

	int[] getLightmapArray();

	void getFogColor();
}
