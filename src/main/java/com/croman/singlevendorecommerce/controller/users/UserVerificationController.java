package com.croman.singlevendorecommerce.controller.users;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.singlevendorecommerce.dto.DefaultApiResponse;
import com.croman.singlevendorecommerce.dto.users.ResendCodeDTO;
import com.croman.singlevendorecommerce.dto.users.VerifyEmailDTO;
import com.croman.singlevendorecommerce.service.users.VerificationService;
import com.croman.singlevendorecommerce.utils.ApiResponseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users/")
public class UserVerificationController {

	private final VerificationService verificationService;
	private final ApiResponseService apiResponseService;

	@PostMapping("verify")
	@Operation(summary = "Verify email with a 6-digit code", responses = {
			@ApiResponse(
					responseCode = "200",
					description = "Account verified successfully",
					content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = DefaultApiResponse.class))
			),
			@ApiResponse(
					responseCode = "400",
					description = "Invalid or exhausted code",
					content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = DefaultApiResponse.class))
			),
			@ApiResponse(
					responseCode = "410",
					description = "Expired code",
					content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = DefaultApiResponse.class))
			)
	})
	public ResponseEntity<DefaultApiResponse> verify(@Valid @RequestBody VerifyEmailDTO dto) {
		verificationService.verify(dto.getEmail(), dto.getCode());
		DefaultApiResponse response = apiResponseService.getApiResponseMessage("account_verified", HttpStatus.OK);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

	@PostMapping("verify/resend")
	@Operation(summary = "Resend a verification code", responses = {
			@ApiResponse(
					responseCode = "200",
					description = "Code resent (or no-op for unknown email)",
					content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = DefaultApiResponse.class))
			),
			@ApiResponse(
					responseCode = "429",
					description = "Too many codes requested",
					content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = DefaultApiResponse.class))
			)
	})
	public ResponseEntity<DefaultApiResponse> resend(@Valid @RequestBody ResendCodeDTO dto) {
		verificationService.resend(dto.getEmail());
		DefaultApiResponse response = apiResponseService.getApiResponseMessage("verification_code_resent",
				HttpStatus.OK);
		return ResponseEntity.status(HttpStatus.OK).body(response);
	}

}
