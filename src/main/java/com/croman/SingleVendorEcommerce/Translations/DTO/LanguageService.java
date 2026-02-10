package com.croman.SingleVendorEcommerce.Translations.DTO;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Translations.Entity.Language;
import com.croman.SingleVendorEcommerce.Translations.Repository.Entity.LanguageRepository;

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
		Optional<Language> languageOpt = languageRepository.findByName(LocaleUtils.ES);
		if (languageOpt.isPresent()) {
			return languageOpt.get();
		}
		throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
				messageService.getMessage("language_not_found", LocaleUtils.getDefaultLocale()));
	}
}
