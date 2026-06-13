package com.mamiyaotaru.voxelmap.gui.overridden;

import net.minecraft.client.gui.GuiButton;

public class GuiOptionButtonMinimap extends GuiButton {
	private final EnumOptionsMinimap enumOptions;

	public GuiOptionButtonMinimap(int buttonId, int x, int y, int width, int height, String buttonText) {
		super(buttonId, x, y, width, height, buttonText);
		this.enumOptions = null;
	}

	public GuiOptionButtonMinimap(int buttonId, int x, int y, EnumOptionsMinimap optionsMinimap, String buttonText) {
		super(buttonId, x, y, 150, 20, buttonText);
		this.enumOptions = optionsMinimap;
	}

	public EnumOptionsMinimap returnEnumOptions() {
		return this.enumOptions;
	}
}
