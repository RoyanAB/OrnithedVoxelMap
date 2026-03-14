package com.mamiyaotaru.voxelmap.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

import java.text.Collator;
import java.util.Locale;

public class I18nUtils {
	public static String getString(String translateMe, Object... args) {
		return I18n.format(translateMe, args);
	}

	public static Collator getLocaleAwareCollator() {
		String mcLocale = "en_US";

		try {
			mcLocale = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
		} catch (NullPointerException var3) {
		}

		String[] bits = mcLocale.split("_");
		Locale locale = new Locale(bits[0], bits.length > 1 ? bits[1] : "");
		return Collator.getInstance(locale);
	}
}
