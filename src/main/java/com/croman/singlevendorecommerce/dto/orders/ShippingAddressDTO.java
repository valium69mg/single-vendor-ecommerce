package com.croman.singlevendorecommerce.dto.orders;

import jakarta.validation.constraints.NotBlank;

public record ShippingAddressDTO(
		@NotBlank String recipient,
		@NotBlank String line1,
		String line2,
		@NotBlank String city,
		@NotBlank String state,
		@NotBlank String postalCode,
		@NotBlank String country,
		@NotBlank String phone) {
}
