package com.croman.singlevendorecommerce.translations;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.translations.entity.Language;
import com.croman.singlevendorecommerce.translations.repository.LanguageRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LanguageServiceTest {

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private LanguageService languageService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final String SPANISH_LANG = LocaleUtils.ES;
    private static final String DEFAULT_LANG = LocaleUtils.APP_DEFAULT_LANG;

    private Language spanishLanguage;
    private Language defaultLanguage;

    @BeforeEach
    void setUp() {
        spanishLanguage = new Language(1L, SPANISH_LANG);
        defaultLanguage = new Language(2L, DEFAULT_LANG);
    }

    // ─── getLanguageByName ────────────────────────────────────────────────────

    @Test
    void testGetLanguageByNameReturnsLanguageWhenFound() {
        when(languageRepository.findByName(SPANISH_LANG)).thenReturn(Optional.of(spanishLanguage));

        Language result = languageService.getLanguageByName(SPANISH_LANG);

        assertThat(result.getName()).isEqualTo(SPANISH_LANG);
        assertThat(result.getLanguageId()).isEqualTo(1L);
    }

    @Test
    void testGetLanguageByNameThrowsWhenLanguageNotFound() {
        when(languageRepository.findByName(SPANISH_LANG)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("language_not_found"), any(Locale.class)))
                .thenReturn("Language not found");

        assertThatThrownBy(() -> languageService.getLanguageByName(SPANISH_LANG))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Language not found");
    }

    // ─── getDefaultLanguage ───────────────────────────────────────────────────

    @Test
    void testGetDefaultLanguageReturnsDefaultLanguageWhenFound() {
        when(languageRepository.findByName(DEFAULT_LANG)).thenReturn(Optional.of(defaultLanguage));

        Language result = languageService.getDefaultLanguage();

        assertThat(result.getName()).isEqualTo(DEFAULT_LANG);
        assertThat(result.getLanguageId()).isEqualTo(2L);
    }

    @Test
    void testGetDefaultLanguageThrowsWhenDefaultLanguageNotFound() {
        when(languageRepository.findByName(DEFAULT_LANG)).thenReturn(Optional.empty());
        when(messageService.getMessage(eq("language_not_found"), any(Locale.class)))
                .thenReturn("Language not found");

        assertThatThrownBy(() -> languageService.getDefaultLanguage())
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Language not found");
    }
}