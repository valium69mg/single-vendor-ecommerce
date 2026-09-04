package com.croman.singlevendorecommerce.dto.users;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VerifyEmailDTO {

	@NotBlank(message = "Email is required")
	@Email(message = "Email must be valid")
	private String email;

	@NotBlank(message = "Code is required")
	@Pattern(regexp = "\\d{6}", message = "Code must be 6 digits")
	private String code;

}
