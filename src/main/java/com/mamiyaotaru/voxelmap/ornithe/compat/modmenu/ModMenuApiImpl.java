package com.mamiyaotaru.voxelmap.ornithe.compat.modmenu;

import com.mamiyaotaru.voxelmap.gui.GuiMinimapOptions;
import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.GuiScreen;

public class ModMenuApiImpl implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (currentScreen) -> {
			GuiScreen screen = new GuiMinimapOptions(currentScreen, VoxelMapMod.voxelMap);
			return screen;
		};
	}
}
