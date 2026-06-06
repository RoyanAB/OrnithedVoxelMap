package com.mamiyaotaru.voxelmap.ornithe.compat.modmenu;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import com.mamiyaotaru.voxelmap.gui.GuiMinimapOptions;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.GuiScreen;

public class ModMenuApiImpl implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return (currentScreen) -> (GuiScreen) new GuiMinimapOptions(currentScreen, VoxelConstants.getVoxelMapInstance());
	}
}
