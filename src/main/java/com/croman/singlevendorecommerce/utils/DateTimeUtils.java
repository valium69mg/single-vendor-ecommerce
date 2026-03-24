package com.croman.singlevendorecommerce.utils;

import java.time.format.DateTimeFormatter;

import lombok.Getter;

public final class DateTimeUtils {

	private DateTimeUtils() {
		
	}
	
	@Getter
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
	@Getter
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
}
