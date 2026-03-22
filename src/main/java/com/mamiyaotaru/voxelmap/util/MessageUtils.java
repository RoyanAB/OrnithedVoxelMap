package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.ornithe.VoxelMapMod;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class MessageUtils {
	private static final boolean debug = false;

	public static void chatInfo(String s) {
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString(s));
	}

	public static void printDebug(String line) {
		if (debug) {
			VoxelMapMod.LOGGER.info(line);
		}
	}
}
