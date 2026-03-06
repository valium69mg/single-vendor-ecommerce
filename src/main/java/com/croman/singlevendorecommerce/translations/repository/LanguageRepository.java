package com.croman.singlevendorecommerce.translations.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.translations.entity.Language;

public interface LanguageRepository extends JpaRepository<Language, Long>{

	Optional<Language> findByName(String name);

}
