package com.croman.singlevendorecommerce.integration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Shared base for backend integration tests. Boots the full Spring context once
 * per JVM against a single disposable PostgreSQL 16 Testcontainer, with real
 * Flyway migrations applied on startup.
 *
 * <p>The container is a plain {@code static} singleton started in a static
 * initializer — <em>not</em> managed by {@code @Testcontainers}/{@code @Container}
 * — so it starts exactly once and is shared by every subclass for the whole
 * {@code ./mvnw test} run. JVM shutdown (plus Testcontainers' Ryuk reaper) stops
 * it; subclasses never touch container or profile wiring.
 *
 * <p>{@code webEnvironment = MOCK} + {@link AutoConfigureMockMvc} runs the full
 * filter chain (JWT filter, Spring Security whitelist) through {@link MockMvc}
 * with no servlet container or socket. Subclasses add fixtures / {@code @Sql}
 * only.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

	@ServiceConnection
	protected static final PostgreSQLContainer<?> POSTGRES =
			new PostgreSQLContainer<>("postgres:16");

	static {
		POSTGRES.start();
	}

	@Autowired
	protected MockMvc mockMvc;
}
