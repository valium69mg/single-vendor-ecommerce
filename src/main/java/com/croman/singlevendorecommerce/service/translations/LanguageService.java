package com.croman.singlevendorecommerce.service.translations;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.singlevendorecommerce.entity.translations.Language;
import com.croman.singlevendorecommerce.repository.translations.LanguageRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LanguageService {

	private final LanguageRepository languageRepository;
	private final MessageService messageService;

	public Language getLanguageByName(String name) {
		Optional<Language> languageOpt = languageRepository.findByName(name);
		if (languageOpt.isPresent()) {
			return languageOpt.get();
		}
		throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
				messageService.getMessage("language_not_found", LocaleUtils.getDefaultLocale()));
	}

	public Language getDefaultLanguage() {
		Optional<Language> languageOpt = languageRepository.findByName(LocaleUtils.APP_DEFAULT_LANG);
		if (languageOpt.isPresent()) {
			return languageOpt.get();
		}
		throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
				messageService.getMessage("language_not_found", LocaleUtils.getDefaultLocale()));
	}
}
