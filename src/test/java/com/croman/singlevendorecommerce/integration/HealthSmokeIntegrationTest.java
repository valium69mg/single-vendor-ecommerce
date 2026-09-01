package com.croman.singlevendorecommerce.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/**
 * Smoke integration test: proves the shared context boots against the
 * PostgreSQL Testcontainer, Flyway migrations apply, the security whitelist
 * lets the public {@code /health} endpoint through, and the full MockMvc filter
 * chain returns the expected response end-to-end.
 *
 * <p>Named {@code *IntegrationTest} so Surefire's default {@code **&#47;*Test.java}
 * include runs it during {@code ./mvnw test}.
 */
class HealthSmokeIntegrationTest extends AbstractIntegrationTest {

	@Test
	void healthEndpointReturnsOk() throws Exception {
		mockMvc.perform(get("/health"))
				.andExpect(status().isOk())
				.andExpect(content().string("ok"));
	}
}
