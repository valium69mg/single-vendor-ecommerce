package com.croman.singlevendorecommerce.General;

import java.time.format.DateTimeFormatter;

import lombok.Getter;

public class DateTimeUtils {

	@Getter
	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
	@Getter
	private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ISO_DATE;
}
