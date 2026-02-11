package com.croman.SingleVendorEcommerce.Auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.Auth.DTO.LoginContextDTO;
import com.croman.SingleVendorEcommerce.Auth.DTO.LoginDTO;
import com.croman.SingleVendorEcommerce.General.HttpUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/")
public class AuthController {

	private final AuthService authService;
	
	@PostMapping("login")
	public ResponseEntity<Object> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
		String ip = HttpUtils.getClientIp(request);
		LoginContextDTO loginContextDTO = LoginContextDTO.builder().ip(ip).loginDTO(loginDTO).build();
		return ResponseEntity.status(HttpStatus.CREATED).body(authService.login(loginContextDTO));
	}
	
}
