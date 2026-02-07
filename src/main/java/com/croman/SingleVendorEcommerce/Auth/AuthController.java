package com.croman.SingleVendorEcommerce.Auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.Auth.DTO.LoginDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

	private final AuthService authService;
	
	@PostMapping("/v1/login")
	public ResponseEntity<Object> login(@Valid @RequestBody LoginDTO loginDTO) {
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.login(loginDTO));
	}
	
}
