package com.croman.SingleVendorEcommerce.Translations.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;
import com.croman.SingleVendorEcommerce.Translations.Entity.Language;
import com.croman.SingleVendorEcommerce.Translations.Entity.Translation;

public interface TranslationsRepository extends JpaRepository<Translation, Long> {

	Optional<Translation> findByRegisterIdAndLanguageAndTranslatorPropertyType(Integer registerId, 
			Language language, TranslatorPropertyType translatorPropertyType);
	
	List<Translation> findByLanguageAndTranslatorPropertyType(Language language, TranslatorPropertyType type);

}
