package com.croman.singlevendorecommerce.repository.translations;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.dto.translations.TranslatorPropertyType;
import com.croman.singlevendorecommerce.entity.translations.Language;
import com.croman.singlevendorecommerce.entity.translations.Translation;

public interface TranslationsRepository extends JpaRepository<Translation, Long> {

	Optional<Translation> findByRegisterIdAndLanguageAndTranslatorPropertyType(Integer registerId, 
			Language language, TranslatorPropertyType translatorPropertyType);
	
	List<Translation> findByLanguageAndTranslatorPropertyTypeAndRegisterIdIn(Language language,
			TranslatorPropertyType type, List<Integer> registerIds);

}
