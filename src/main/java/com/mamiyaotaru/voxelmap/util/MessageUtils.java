package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import net.minecraft.util.text.TextComponentString;

public class MessageUtils {
	private static final boolean debug = false;

	public static void chatInfo(String s) {
		VoxelConstants.getMinecraft().player.sendMessage(new TextComponentString(s));
	}

	public static void printDebug(String line) {
		if (debug) {
			System.out.println(line);
		}
	}
}
