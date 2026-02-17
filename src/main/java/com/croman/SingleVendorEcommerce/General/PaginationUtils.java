package com.croman.SingleVendorEcommerce.General;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {
	
	private static final int MAX_PAGINATION_SIZE = 100;

	public static int safeSize(int size) {
		return Math.min(Math.max(size, 1), MAX_PAGINATION_SIZE);
	}
	
	public static int safePage(int page) {
		return Math.max(0, page);
	}
	
	public static Pageable getPageable(int page, int size, String tablePrimaryKeyName) {
		int safePage = PaginationUtils.safePage(page);
		int safeSize = PaginationUtils.safeSize(size);
		return PageRequest.of(safePage, safeSize, Sort.by(tablePrimaryKeyName).descending());
	}
}
