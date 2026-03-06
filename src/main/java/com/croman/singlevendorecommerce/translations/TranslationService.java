package com.croman.singlevendorecommerce.translations;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.translations.dto.TranslatorPropertyType;
import com.croman.singlevendorecommerce.translations.entity.Language;
import com.croman.singlevendorecommerce.translations.entity.Translation;
import com.croman.singlevendorecommerce.translations.repository.TranslationsRepository;

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

	public HashMap<Integer, String> batchTranslate(String languageName, TranslatorPropertyType type,
			List<Long> registerIds) {
		Language language = languageService.getLanguageByName(languageName);

		HashMap<Integer, String> translationsMap = new HashMap<>();

		List<Integer> registerIdsToInt = registerIds.stream().map(id -> id.intValue())
				.distinct().toList();
		
		translationsRepository.findByLanguageAndTranslatorPropertyTypeAndRegisterIdIn(language, type, registerIdsToInt)
				.forEach(t -> translationsMap.put(t.getRegisterId(), t.getTranslation()));

		if (translationsMap.isEmpty()) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage("translation_not_found", LocaleUtils.getDefaultLocale()));
		}

		return translationsMap;
	}
	
	public void createTranslation(Integer registerId, String languageName, TranslatorPropertyType type,
			String translation) {
		
		Language language = languageService.getLanguageByName(languageName);
		
		Optional<Translation> translationOpt = translationsRepository
				.findByRegisterIdAndLanguageAndTranslatorPropertyType(registerId, language, type);

		if (translationOpt.isPresent()) {
			return;
		}
			
		translationsRepository.save(Translation.builder().registerId(registerId).translation(translation)
				.language(language).translatorPropertyType(type).build());
	}
	
	public void updateTranslation(Integer registerId, String languageName, TranslatorPropertyType type,
			String translation) {

		Language language = languageService.getLanguageByName(languageName);

		Optional<Translation> translationOpt = translationsRepository
				.findByRegisterIdAndLanguageAndTranslatorPropertyType(registerId, language, type);

		Translation existingTranslation = null;

		if (translationOpt.isPresent()) {
			existingTranslation = translationOpt.get();
			existingTranslation.setTranslation(translation);
		} else {
			existingTranslation = Translation.builder().registerId(registerId).translation(translation)
					.language(language).translatorPropertyType(type).build();
		}

		translationsRepository.save(existingTranslation);

	}
	
	public void deleteTranslation(Integer registerId, String languageName, TranslatorPropertyType type) {
		
		Language language = languageService.getLanguageByName(languageName);

		Optional<Translation> translationOpt = translationsRepository
				.findByRegisterIdAndLanguageAndTranslatorPropertyType(registerId, language, type);
		
		if (translationOpt.isPresent()) {
			translationsRepository.delete(translationOpt.get());
		}
		
	}

}
