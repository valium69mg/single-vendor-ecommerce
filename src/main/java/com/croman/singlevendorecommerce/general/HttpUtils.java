package com.croman.singlevendorecommerce.general;

import jakarta.servlet.http.HttpServletRequest;

public final class HttpUtils {

	private HttpUtils() {
		
	}
	
	public static String getClientIp(HttpServletRequest request) {
	    String ip = request.getHeader("X-Forwarded-For");
	    if (ip != null && !ip.isEmpty()) {
	        return ip.split(",")[0].trim();
	    }
	    return request.getRemoteAddr();
	}

}
