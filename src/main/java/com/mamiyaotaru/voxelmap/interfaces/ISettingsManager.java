package com.mamiyaotaru.voxelmap.interfaces;

import com.mamiyaotaru.voxelmap.gui.overridden.EnumOptionsMinimap;

/**
 * Provides access to a set of minimap options that are exposed through the
 * options GUI.
 */
public interface ISettingsManager {
	/**
	 * Returns the display string for the given option, combining its localized
	 * label with its current value.
	 *
	 * @param option the option to look up
	 * @return the option's display string
	 */
	String getKeyText(EnumOptionsMinimap option);

	/**
	 * Sets the floating-point value of the given option.
	 *
	 * @param option the option to change
	 * @param value  the new value for the option
	 */
	void setOptionFloatValue(EnumOptionsMinimap option, float value);
}
