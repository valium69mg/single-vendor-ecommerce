package com.croman.singlevendorecommerce.translations.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.translations.dto.TranslatorPropertyType;
import com.croman.singlevendorecommerce.translations.entity.Language;
import com.croman.singlevendorecommerce.translations.entity.Translation;

public interface TranslationsRepository extends JpaRepository<Translation, Long> {

	Optional<Translation> findByRegisterIdAndLanguageAndTranslatorPropertyType(Integer registerId, 
			Language language, TranslatorPropertyType translatorPropertyType);
	
	List<Translation> findByLanguageAndTranslatorPropertyTypeAndRegisterIdIn(Language language,
			TranslatorPropertyType type, List<Integer> registerIds);

}
