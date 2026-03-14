package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;

public interface IRadar {
    void onResourceManagerReload(ResourceManager var1);

    void OnTickInGame(MinecraftClient var1, LayoutVariables var2);
}
