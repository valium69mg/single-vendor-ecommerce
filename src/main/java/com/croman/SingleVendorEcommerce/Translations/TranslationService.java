package com.croman.SingleVendorEcommerce.Translations;

import java.util.HashMap;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;
import com.croman.SingleVendorEcommerce.Translations.Entity.Language;
import com.croman.SingleVendorEcommerce.Translations.Entity.Translation;
import com.croman.SingleVendorEcommerce.Translations.Repository.TranslationsRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TranslationService {

	private final TranslationsRepository translationsRepository;
	private final LanguageService languageService;
	private final MessageService messageService;

	public String translate(Integer registerId, String languageName, TranslatorPropertyType type) {
		Language language = languageService.getLanguageByName(languageName);

		Optional<Translation> translationOpt = translationsRepository
				.findByRegisterIdAndLanguageAndTranslatorPropertyType(registerId, language, type);

		if (translationOpt.isPresent()) {
			return translationOpt.get().getTranslation();
		}
		throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
				messageService.getMessage("translation_not_found", LocaleUtils.getDefaultLocale()));
	}

	public HashMap<Integer, String> batchTranslate(String languageName, TranslatorPropertyType type) {
		Language language = languageService.getLanguageByName(languageName);

		HashMap<Integer, String> translationsMap = new HashMap<>();

		translationsRepository.findByLanguageAndTranslatorPropertyType(language, type)
				.forEach(t -> translationsMap.put(t.getRegisterId(), t.getTranslation()));

		if (translationsMap.isEmpty()) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage("translation_not_found", LocaleUtils.getDefaultLocale()));
		}

		return translationsMap;
	}

}
