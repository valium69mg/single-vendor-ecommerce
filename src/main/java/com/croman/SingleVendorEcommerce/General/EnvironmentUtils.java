package com.croman.SingleVendorEcommerce.General;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EnvironmentUtils {
	@Value("${spring.profiles.active:default}")
	private String activeProfile;

	public boolean isDev() {
		return "dev".equalsIgnoreCase(activeProfile);
	}

	public boolean isProd() {
		return "prod".equalsIgnoreCase(activeProfile);
	}

	public String getActiveProfile() {
		return activeProfile;
	}
}