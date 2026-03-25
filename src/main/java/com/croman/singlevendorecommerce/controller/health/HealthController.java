package com.croman.singlevendorecommerce.controller.health;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("health")
public class HealthController {

	
	@GetMapping
	@Operation(
	    summary = "Health check",
	    responses = {
	        @ApiResponse(
	            responseCode = "200",
	            description = "Content successfully returned",
	            content = @Content(
	                mediaType = "application/json",
	                schema = @Schema(implementation = String.class)
	            )
	        )
	    }
	)
	public ResponseEntity<String> healthCheck() {
		return ResponseEntity.ok("ok");
	}
	
}
