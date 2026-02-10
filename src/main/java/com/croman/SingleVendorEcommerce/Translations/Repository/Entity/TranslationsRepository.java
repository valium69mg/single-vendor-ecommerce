package com.croman.SingleVendorEcommerce.Translations.Repository.Entity;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Translations.Entity.Translation;

public interface TranslationsRepository extends JpaRepository<Translation, Long> {

}
