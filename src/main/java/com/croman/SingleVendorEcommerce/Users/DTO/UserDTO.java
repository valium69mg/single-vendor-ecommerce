package com.croman.SingleVendorEcommerce.Users.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

	private String userId;
	private String username;
	private String lastLogin;
	private String createdAt;
	private String updatedAt;
	private boolean isValidated;
	private boolean isActive;
	
}
