package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;

public interface ISettingsManager {
	String getKeyText(EnumOptionsMinimap option);

	void setOptionFloatValue(EnumOptionsMinimap option, float value);
}
