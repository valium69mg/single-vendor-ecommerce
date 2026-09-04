package com.croman.singlevendorecommerce.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.croman.singlevendorecommerce.web.interceptor.VerifiedAccountInterceptor;

import lombok.RequiredArgsConstructor;

/**
 * Registers {@link VerifiedAccountInterceptor} against the cart-mutation
 * surface only. {@code GET /api/v1/cart} and every route outside this
 * explicit pattern list are unaffected.
 */
@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

	private final VerifiedAccountInterceptor verifiedAccountInterceptor;

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(verifiedAccountInterceptor).addPathPatterns("/api/v1/cart/items",
				"/api/v1/cart/items/**");
	}
}
