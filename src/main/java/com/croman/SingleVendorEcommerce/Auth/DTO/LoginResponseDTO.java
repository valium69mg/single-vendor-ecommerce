package com.croman.SingleVendorEcommerce.Auth.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDTO {

	private String userId;
	private String email;
	private String token;
	private String role;
}
