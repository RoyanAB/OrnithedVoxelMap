package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.TextComponentString;

public class MessageUtils {
	private static final boolean debug = false;

	public static void chatInfo(String s) {
		Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(new TextComponentString(s));
	}

	public static void printDebug(String line) {
		if (debug) {
			System.out.println(line);
		}
	}
}
