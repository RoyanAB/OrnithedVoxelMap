package com.mamiyaotaru.voxelmap.interfaces;

import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

public interface IMap extends IChangeObserver {
    void forceFullRender(boolean var1);

    void drawMinimap(MinecraftClient var1);

    float getPercentX();

    float getPercentY();

    void newWorld(World var1);

    void onTickInGame(MinecraftClient var1);

    int[] getLightmapArray();

    void getFogColor();

    void newWorldName();
}
