package com.croman.SingleVendorEcommerce.General;

import jakarta.servlet.http.HttpServletRequest;

public class HttpUtils {

	public static String getClientIp(HttpServletRequest request) {
	    String ip = request.getHeader("X-Forwarded-For");
	    if (ip != null && !ip.isEmpty()) {
	        return ip.split(",")[0].trim();
	    }
	    return request.getRemoteAddr();
	}

}
