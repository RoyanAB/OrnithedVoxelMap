package com.mamiyaotaru.voxelmap.util;

import com.mamiyaotaru.voxelmap.VoxelConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public final class MessageUtils {
	private static final boolean debug = false;

	public static void chatInfo(String s) {
		Minecraft.getMinecraft().player.sendMessage(new TextComponentString(s));
	}

	public static void printDebug(String line) {
		if (debug) VoxelConstants.getLogger().warn(line);
	}
}
