package com.croman.singlevendorecommerce.repository.translations;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.translations.Language;

public interface LanguageRepository extends JpaRepository<Language, Long>{

	Optional<Language> findByName(String name);

}
