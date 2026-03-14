package com.mamiyaotaru.voxelmap.util;

import net.minecraft.util.Util.EnumOS;

import java.io.File;

public class FilesystemUtils {
	// $VF: Unable to simplify switch-on-enum, as the enum class was not able to be found.
	// Please report this to the Vineflower issue tracker, at https://github.com/Vineflower/vineflower/issues with a copy of the class file (if you have the rights to distribute it!)
	public static File getAppDir(String par0Str, boolean createIfNotExist) {
		String var1x = System.getProperty("user.home", ".");
		File var2;
		switch (getOs().ordinal()) {
			case 0:
			case 1:
				var2 = new File(var1x, '.' + par0Str + '/');
				break;
			case 2:
				String var3 = System.getenv("APPDATA");
				if (var3 != null) {
					var2 = new File(var3, "." + par0Str + '/');
				} else {
					var2 = new File(var1x, '.' + par0Str + '/');
				}
				break;
			case 3:
				var2 = new File(var1x, "Library/Application Support/" + par0Str);
				break;
			default:
				var2 = new File(var1x, par0Str + '/');
		}

		if (createIfNotExist && !var2.exists() && !var2.mkdirs()) {
			throw new RuntimeException("The working directory could not be created: " + var2);
		} else {
			return var2;
		}
	}

	public static EnumOS getOs() {
		String var0 = System.getProperty("os.name").toLowerCase();
		return var0.contains("win")
			? EnumOS.WINDOWS
			: (
			var0.contains("mac")
				? EnumOS.OSX
				: (
				var0.contains("solaris")
					? EnumOS.SOLARIS
					: (
					var0.contains("sunos")
						? EnumOS.SOLARIS
						: (var0.contains("linux") ? EnumOS.LINUX : (var0.contains("unix") ? EnumOS.LINUX : EnumOS.UNKNOWN))
				)
			)
		);
	}
}
