package com.croman.SingleVendorEcommerce.Translations.Repository.Entity;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Translations.Entity.Language;

public interface LanguageRepository extends JpaRepository<Language, Long>{

	Optional<Language> findByName(String name);

}
