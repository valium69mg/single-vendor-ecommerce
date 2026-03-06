package com.croman.singlevendorecommerce.general;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.croman.singlevendorecommerce.dto.DefaultApiResponse;
import com.croman.singlevendorecommerce.message.MessageService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ApiResponseService {

	private final MessageService messageService;
	
	public DefaultApiResponse getApiResponseMessage(String messagesKey, HttpStatus httpStatus) {
		return DefaultApiResponse.builder().message(messageService.getMessage(messagesKey, LocaleUtils.getDefaultLocale()))
				.status(httpStatus.value()).build();
	}
}
