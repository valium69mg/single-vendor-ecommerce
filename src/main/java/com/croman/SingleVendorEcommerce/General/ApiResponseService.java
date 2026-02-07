package com.croman.SingleVendorEcommerce.General;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.croman.SingleVendorEcommerce.DTO.ApiResponse;
import com.croman.SingleVendorEcommerce.Message.MessageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiResponseService {

	private final MessageService messageService;
	
	public ApiResponse getApiResponseMessage(String messagesKey, HttpStatus httpStatus) {
		return ApiResponse.builder().message(messageService.getMessage(messagesKey, LocaleUtils.getDefaultLocale()))
				.status(httpStatus.value()).build();
	}
}
