package com.mamiyaotaru.voxelmap.interfaces;

import java.io.File;
import java.io.PrintWriter;

/**
 * A settings manager that can persist its options to and from disk, in
 * addition to the option access provided by {@link ISettingsManager}.
 */
public interface ISubSettingsManager extends ISettingsManager {
	/**
	 * Loads the options from the given settings file.
	 *
	 * @param settingsFile the file to load settings from
	 */
	void loadSettings(File settingsFile);

	/**
	 * Writes the current options to the given writer.
	 *
	 * @param writer the writer to save settings to
	 */
	void saveAll(PrintWriter writer);
}
