package com.croman.SingleVendorEcommerce.Users;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.DTO.DefaultApiResponse;
import com.croman.SingleVendorEcommerce.General.ApiResponseService;
import com.croman.SingleVendorEcommerce.Users.DTO.CreateUserDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/")
public class UserController {

	private final UserService userService;
	private final ApiResponseService apiResponseService;

	@PostMapping("register")
	@Operation(summary = "Register user", responses = {
		    @ApiResponse(
		        responseCode = "201", 
		        description = "User created succesfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	private ResponseEntity<DefaultApiResponse> createUser(@Valid @RequestBody CreateUserDTO dto) {
		userService.register(dto);
		DefaultApiResponse response = apiResponseService.getApiResponseMessage("user_created", HttpStatus.CREATED);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@DeleteMapping("{email}")
	@Operation(summary = "Hard delete user by email (Only dev mode)", responses = {
		    @ApiResponse(
		        responseCode = "204", 
		        description = "User deleted successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	private ResponseEntity<DefaultApiResponse> deleteUser(@Valid @Email(message = "Must be an email") @PathVariable String email) {
		userService.deleteUserByEmail(email);
		DefaultApiResponse response = apiResponseService.getApiResponseMessage("user_deleted", HttpStatus.NO_CONTENT);
		return ResponseEntity.status(HttpStatus.NO_CONTENT).body(response);
	}
	
	@PostMapping("register/admin")
	@Operation(summary = "Register the admin of the app (One time only)", responses = {
		    @ApiResponse(
		        responseCode = "201", 
		        description = "User admin created successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = DefaultApiResponse.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Bad request",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	private ResponseEntity<DefaultApiResponse> createSiteAdmin(@Valid @RequestBody CreateUserDTO dto) {
		userService.createSiteAdmin(dto);
		DefaultApiResponse response = apiResponseService.getApiResponseMessage("user_created", HttpStatus.CREATED);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
