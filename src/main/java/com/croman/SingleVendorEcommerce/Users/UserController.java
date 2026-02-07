package com.croman.SingleVendorEcommerce.Users;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.DTO.ApiResponse;
import com.croman.SingleVendorEcommerce.General.ApiResponseService;
import com.croman.SingleVendorEcommerce.Users.DTO.CreateUserDTO;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;
	private final ApiResponseService apiResponseService;

	@PostMapping("/v1/register")
	private ResponseEntity<Object> createUser(@RequestBody CreateUserDTO dto) {
		userService.register(dto);
		ApiResponse response = apiResponseService.getApiResponseMessage("user_created", HttpStatus.CREATED);
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}
	
	@DeleteMapping("v1/{email}")
	private ResponseEntity<Object> deleteUser(@PathVariable String email) {
		userService.deleteUserByEmail(email);
		ApiResponse response = apiResponseService.getApiResponseMessage("user_deleted", HttpStatus.ACCEPTED);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
	}

}
