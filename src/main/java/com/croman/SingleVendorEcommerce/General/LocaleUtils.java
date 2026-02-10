package com.croman.SingleVendorEcommerce.General;

import java.util.Locale;

public class LocaleUtils {

	public static final String ES = "es";
	public static final String EN = "en";
	public static final String APP_DEFAULT_LANG = ES;
	public static final String DATABASE_DEFAULT_LANG = EN;

	public static Locale getDefaultLocale() {
		return Locale.of(APP_DEFAULT_LANG);
	}
	
	
}
