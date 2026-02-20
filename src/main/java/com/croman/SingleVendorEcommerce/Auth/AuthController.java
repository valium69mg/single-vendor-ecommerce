package com.croman.SingleVendorEcommerce.Auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.Auth.DTO.LoginContextDTO;
import com.croman.SingleVendorEcommerce.Auth.DTO.LoginDTO;
import com.croman.SingleVendorEcommerce.Auth.DTO.LoginResponseDTO;
import com.croman.SingleVendorEcommerce.DTO.DefaultApiResponse;
import com.croman.SingleVendorEcommerce.General.HttpUtils;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/")
public class AuthController {

	private final AuthService authService;
	
	@PostMapping("login")
	@Operation(summary = "Login user", responses = {
		    @ApiResponse(
		        responseCode = "200", 
		        description = "Logged in successfully",
		        content = @Content(mediaType = "application/json",
		        schema = @Schema(implementation = LoginResponseDTO.class))
		    ),
		    @ApiResponse(
		        responseCode = "400",
		        description = "Invalid credentials",
		        content = @Content(mediaType = "application/json",
		            schema = @Schema(implementation = DefaultApiResponse.class))
		    )
		})
	public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
		String ip = HttpUtils.getClientIp(request);
		LoginContextDTO loginContextDTO = LoginContextDTO.builder().ip(ip).loginDTO(loginDTO).build();
		return ResponseEntity.status(HttpStatus.OK).body(authService.login(loginContextDTO));
	}
	
}
