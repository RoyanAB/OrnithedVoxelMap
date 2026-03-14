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

    public static String prettify(String input) {
        String[] words = input.split("_");

        for (int t = 0; t < words.length; t++) {
            words[t] = words[t].substring(0, 1).toUpperCase() + words[t].substring(1).toLowerCase();
        }

        return String.join(" ", words);
    }
}
