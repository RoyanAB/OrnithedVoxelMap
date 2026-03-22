package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;

@SuppressWarnings("unused")
public interface ISettingsManager {
	String getKeyText(EnumOptionsMinimap enumOptionsMinimap);

	void setOptionFloatValue(EnumOptionsMinimap enumOptionsMinimap, float value);
}
