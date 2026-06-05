package com.mamiyaotaru.voxelmap.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public class TextUtils {
	@NotNull
	@Contract(pure = true)
	public static String scrubCodes(String string) {
		return string.replaceAll("(§.)", "");
	}

	@NotNull
	public static String scrubName(String input) {
		input = input.replace(",", "~comma~");
		return input.replace(":", "~colon~");
	}

	@NotNull
	public static String scrubNameRegex(String input) {
		input = input.replace(",", "﹐");
		input = input.replace("[", "⟦");
		return input.replace("]", "⟧");
	}

	@NotNull
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

	@NotNull
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

	@NotNull
	public static String titleize(@NotNull String input) {
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
