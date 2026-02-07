package com.croman.SingleVendorEcommerce.Users;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.SingleVendorEcommerce.DTO.ApiResponse;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Users.DTO.CreateUserDTO;
import com.croman.SingleVendorEcommerce.Users.Utils.LocaleUtils;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

	private final MessageService messageService;
	private final UserService userService;

	@PostMapping("/v1/register")
	private ResponseEntity<Object> createUser(@RequestBody CreateUserDTO dto) {
		userService.register(dto);
		ApiResponse response = ApiResponse.builder().message(messageService.getMessage("user_created", LocaleUtils.getDefaultLocale()))
				.build();
		return ResponseEntity.status(HttpStatus.CREATED).body(response);

	}

}
