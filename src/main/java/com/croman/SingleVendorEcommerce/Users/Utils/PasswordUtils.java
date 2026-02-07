package com.croman.SingleVendorEcommerce.Users.Utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtils {

	private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	public static String hashPassword(String rawPassword) {
		return passwordEncoder.encode(rawPassword);
	}

	public static boolean matches(String rawPassword, String hashedPassword) {
		return passwordEncoder.matches(rawPassword, hashedPassword);
	}
}
