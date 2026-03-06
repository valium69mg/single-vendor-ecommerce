package com.croman.singlevendorecommerce.Translations.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Translations.Entity.Language;

public interface LanguageRepository extends JpaRepository<Language, Long>{

	Optional<Language> findByName(String name);

}
