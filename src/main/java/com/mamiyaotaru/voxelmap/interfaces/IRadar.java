package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.util.LayoutVariables;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IResourceManager;

public interface IRadar {
	void onResourceManagerReload(IResourceManager iResourceManager);

	void OnTickInGame(Minecraft mc, LayoutVariables layoutVariables);
}
