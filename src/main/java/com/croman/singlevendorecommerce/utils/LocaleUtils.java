package com.croman.singlevendorecommerce.utils;

import java.util.Locale;

public final class LocaleUtils {

	private LocaleUtils() {
		
	}
	
	public static final String ES = "es";
	public static final String APP_DEFAULT_LANG = ES;

	public static Locale getDefaultLocale() {
		return Locale.of(APP_DEFAULT_LANG);
	}
	
	
}
