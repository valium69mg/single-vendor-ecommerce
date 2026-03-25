package com.croman.singlevendorecommerce.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginContextDTO {

	private LoginDTO loginDTO;
	private String ip;
	
}
