package com.croman.singlevendorecommerce.Translations.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Translations.DTO.TranslatorPropertyType;
import com.croman.singlevendorecommerce.Translations.Entity.Language;
import com.croman.singlevendorecommerce.Translations.Entity.Translation;

public interface TranslationsRepository extends JpaRepository<Translation, Long> {

	Optional<Translation> findByRegisterIdAndLanguageAndTranslatorPropertyType(Integer registerId, 
			Language language, TranslatorPropertyType translatorPropertyType);
	
	List<Translation> findByLanguageAndTranslatorPropertyTypeAndRegisterIdIn(Language language,
			TranslatorPropertyType type, List<Integer> registerIds);

}
