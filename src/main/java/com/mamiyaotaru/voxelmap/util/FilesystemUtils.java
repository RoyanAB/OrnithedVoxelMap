package com.mamiyaotaru.voxelmap.util;

import net.minecraft.util.Util;

import java.io.File;

public class FilesystemUtils {
	public static File getAppDir(String appName, boolean createIfNotExist) {
		String userHome = System.getProperty("user.home", ".");
		File appDir;
		switch (getOs()) {
			case LINUX:
			case SOLARIS:
				appDir = new File(userHome, '.' + appName + '/');
				break;
			case WINDOWS:
				String appDataPath = System.getenv("APPDATA");
				if (appDataPath != null) {
					appDir = new File(appDataPath, "." + appName + '/');
				} else {
					appDir = new File(userHome, '.' + appName + '/');
				}
				break;
			case OSX:
				appDir = new File(userHome, "Library/Application Support/" + appName);
				break;
			default:
				appDir = new File(userHome, appName + '/');
		}

		if (createIfNotExist && !appDir.exists() && !appDir.mkdirs()) {
			throw new RuntimeException("The working directory could not be created: " + appDir);
		} else {
			return appDir;
		}
	}

	public static Util.EnumOS getOs() {
		String osName = System.getProperty("os.name").toLowerCase();
		return osName.contains("win")
			? Util.EnumOS.WINDOWS
			: (
			osName.contains("mac")
				? Util.EnumOS.OSX
				: (
				osName.contains("solaris")
					? Util.EnumOS.SOLARIS
					: (
					osName.contains("sunos")
						? Util.EnumOS.SOLARIS
						: (osName.contains("linux") ? Util.EnumOS.LINUX : (osName.contains("unix") ? Util.EnumOS.LINUX : Util.EnumOS.UNKNOWN))
				)
			)
		);
	}
}
