package com.mamiyaotaru.voxelmap.gui.overridden;

import com.mamiyaotaru.voxelmap.MapSettingsManager;
import com.mamiyaotaru.voxelmap.VoxelMap;
import com.mamiyaotaru.voxelmap.util.GLShim;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;

public class GuiScreenMinimap extends GuiScreen {
	public void drawMap() {
		if (!VoxelMap.instance.getMapOptions().showUnderMenus) {
			VoxelMap.instance.getMap().drawMinimap(this.mc);
			GLShim.glClear(256);
		}
	}

	public void onGuiClosed() {
		MapSettingsManager.instance.saveAll();
	}

	public Minecraft getMinecraft() {
		return this.mc;
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public List<GuiButton> getButtonList() {
		return this.buttonList;
	}

	public FontRenderer getFontRenderer() {
		return this.fontRendererObj;
	}
}
