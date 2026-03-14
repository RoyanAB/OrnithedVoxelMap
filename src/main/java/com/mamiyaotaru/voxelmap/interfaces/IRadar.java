package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;

public interface IRadar {
    void onResourceManagerReload(IResourceManager var1);

    void OnTickInGame(Minecraft var1, LayoutVariables var2);
}
