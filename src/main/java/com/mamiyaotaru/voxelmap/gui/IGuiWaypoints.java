package com.mamiyaotaru.voxelmap.gui;

import net.minecraft.client.gui.GuiYesNoCallback;

@SuppressWarnings("unused")
public interface IGuiWaypoints extends GuiYesNoCallback {
	boolean isEditing();
}
