package com.mamiyaotaru.voxelmap.util;

public class TextUtils {
	public static String scrubCodes(String string) {
		return string.replaceAll("(§.)", "");
	}

	public static String scrubName(String input) {
		input = input.replace(",", "~comma~");
		return input.replace(":", "~colon~");
	}

	public static String scrubNameRegex(String input) {
		input = input.replace(",", "﹐");
		input = input.replace("[", "⟦");
		return input.replace("]", "⟧");
	}

	public static String scrubNameFile(String input) {
		input = input.replace("<", "~less~");
		input = input.replace(">", "~greater~");
		input = input.replace(":", "~colon~");
		input = input.replace("\"", "~quote~");
		input = input.replace("/", "~slash~");
		input = input.replace("\\", "~backslash~");
		input = input.replace("|", "~pipe~");
		input = input.replace("?", "~question~");
		return input.replace("*", "~star~");
	}

	public static String descrubName(String input) {
		input = input.replace("~less~", "<");
		input = input.replace("~greater~", ">");
		input = input.replace("~colon~", ":");
		input = input.replace("~quote~", "\"");
		input = input.replace("~slash~", "/");
		input = input.replace("~backslash~", "\\");
		input = input.replace("~pipe~", "|");
		input = input.replace("~question~", "?");
		input = input.replace("~star~", "*");
		input = input.replace("~comma~", ",");
		input = input.replace("~colon~", ":");
		input = input.replace("﹐", ",");
		input = input.replace("⟦", "[");
		return input.replace("⟧", "]");
	}

	public static String titleize(String input) {
		StringBuilder output = new StringBuilder(input.length());
		boolean lastCharacterWasWhitespace = true;

		for (int i = 0; i < input.length(); i++) {
			char currentCharacter = input.charAt(i);
			if (lastCharacterWasWhitespace && Character.isLowerCase(currentCharacter)) {
				currentCharacter = Character.toTitleCase(currentCharacter);
			}

			output.append(currentCharacter);
			lastCharacterWasWhitespace = Character.isWhitespace(currentCharacter);
		}

		return output.toString();
	}
}
