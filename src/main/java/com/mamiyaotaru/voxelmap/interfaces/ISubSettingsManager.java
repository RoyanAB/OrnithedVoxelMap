package com.mamiyaotaru.voxelmap.interfaces;

import java.io.File;
import java.io.PrintWriter;

public interface ISubSettingsManager extends ISettingsManager {
	void loadSettings(File file);

	void saveAll(PrintWriter printWriter);
}
