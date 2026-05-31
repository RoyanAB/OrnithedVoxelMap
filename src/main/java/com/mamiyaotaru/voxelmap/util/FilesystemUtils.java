package com.mamiyaotaru.voxelmap.util;

import net.minecraft.util.Util.EnumOS;

import java.io.File;

@SuppressWarnings("unused")
public class FilesystemUtils {
	public static File getAppDir(String appName, boolean createIfNotExist) {
		String userHome = System.getProperty("user.home", ".");
		File appDir;

		switch (getOs()) {
			case LINUX:
			case SOLARIS:
				appDir = new File(userHome, "." + appName + "/");
				break;
			case WINDOWS:
				String appData = System.getenv("APPDATA");
				appDir = new File(
					appData != null ? appData : userHome,
					"." + appName + "/"
				);
				break;
			case OSX:
				appDir = new File(userHome, "Library/Application Support/" + appName);
				break;
			default:
				appDir = new File(userHome, appName + "/");
		}

		if (createIfNotExist && !appDir.exists() && !appDir.mkdirs()) {
			throw new RuntimeException(
				"The working directory could not be created: " + appDir.getAbsolutePath()
			);
		}
		return appDir;
	}

	public static EnumOS getOs() {
		String osName = System.getProperty("os.name").toLowerCase();

		if (osName.contains("win")) {
			return EnumOS.WINDOWS;
		}
		if (osName.contains("mac")) {
			return EnumOS.OSX;
		}
		if (osName.contains("solaris") || osName.contains("sunos")) {
			return EnumOS.SOLARIS;
		}
		if (osName.contains("linux") || osName.contains("unix")) {
			return EnumOS.LINUX;
		}
		return EnumOS.UNKNOWN;
	}
}
