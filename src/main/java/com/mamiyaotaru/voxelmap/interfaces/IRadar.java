package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;

public interface IRadar {
	void onResourceManagerReload(IResourceManager resourceManager);

	void OnTickInGame(Minecraft minecraft, LayoutVariables layoutVariables);
}
