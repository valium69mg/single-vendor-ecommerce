package com.croman.singlevendorecommerce.translations;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.translations.entity.Language;
import com.croman.singlevendorecommerce.translations.repository.LanguageRepository;

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
