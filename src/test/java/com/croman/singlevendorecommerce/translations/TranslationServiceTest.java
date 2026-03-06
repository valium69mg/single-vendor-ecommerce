package com.croman.singlevendorecommerce.translations;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.translations.dto.TranslatorPropertyType;
import com.croman.singlevendorecommerce.translations.entity.Language;
import com.croman.singlevendorecommerce.translations.entity.Translation;
import com.croman.singlevendorecommerce.translations.repository.TranslationsRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TranslationServiceTest {

    @Mock
    private TranslationsRepository translationsRepository;

    @Mock
    private LanguageService languageService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private TranslationService translationService;

    // ─── Fixtures ────────────────────────────────────────────────────────────

    private static final Integer              REGISTER_ID   = 1;
    private static final String               SPANISH_LANG  = LocaleUtils.ES;
    private static final TranslatorPropertyType TYPE         = TranslatorPropertyType.CATEGORY;
    private static final String               TRANSLATED    = "Electrónica";

    private Language language;
    private Translation translation;

    @BeforeEach
    void setUp() {
        language = new Language();
        translation = Translation.builder()
                .translationId(1L)
                .registerId(REGISTER_ID)
                .translation(TRANSLATED)
                .language(language)
                .translatorPropertyType(TYPE)
                .build();
    }

    // ─── translate ────────────────────────────────────────────────────────────

    @Test
    void testTranslateReturnsTranslationWhenFound() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByRegisterIdAndLanguageAndTranslatorPropertyType(REGISTER_ID, language, TYPE))
                .thenReturn(Optional.of(translation));

        String result = translationService.translate(REGISTER_ID, SPANISH_LANG, TYPE);

        assertThat(result).isEqualTo(TRANSLATED);
    }

    @Test
    void testTranslateThrowsWhenTranslationNotFound() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByRegisterIdAndLanguageAndTranslatorPropertyType(REGISTER_ID, language, TYPE))
                .thenReturn(Optional.empty());
        when(messageService.getMessage(eq("translation_not_found"), any(Locale.class)))
                .thenReturn("Translation not found");

        assertThatThrownBy(() -> translationService.translate(REGISTER_ID, SPANISH_LANG, TYPE))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Translation not found");
    }

    // ─── batchTranslate ───────────────────────────────────────────────────────

    @Test
    void testBatchTranslateReturnsMapWhenTranslationsFound() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByLanguageAndTranslatorPropertyTypeAndRegisterIdIn(eq(language), eq(TYPE), anyList()))
                .thenReturn(List.of(translation));

        HashMap<Integer, String> result = translationService.batchTranslate(SPANISH_LANG, TYPE, List.of(1L));

        assertThat(result).hasSize(1);
        assertThat(result.get(REGISTER_ID)).isEqualTo(TRANSLATED);
    }

    @Test
    void testBatchTranslateThrowsWhenNoTranslationsFound() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByLanguageAndTranslatorPropertyTypeAndRegisterIdIn(eq(language), eq(TYPE), anyList()))
                .thenReturn(List.of());
        when(messageService.getMessage(eq("translation_not_found"), any(Locale.class)))
                .thenReturn("Translation not found");

        assertThatThrownBy(() -> translationService.batchTranslate(SPANISH_LANG, TYPE, List.of(1L)))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Translation not found");
    }

    @Test
    void testBatchTranslateDeduplicatesRegisterIds() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByLanguageAndTranslatorPropertyTypeAndRegisterIdIn(eq(language), eq(TYPE), anyList()))
                .thenReturn(List.of(translation));

        // passing duplicate IDs — should be deduplicated internally
        HashMap<Integer, String> result = translationService.batchTranslate(SPANISH_LANG, TYPE, List.of(1L, 1L, 1L));

        assertThat(result).hasSize(1);
        verify(translationsRepository).findByLanguageAndTranslatorPropertyTypeAndRegisterIdIn(
                eq(language), eq(TYPE), eq(List.of(1)));
    }

    // ─── createTranslation ────────────────────────────────────────────────────

    @Test
    void testCreateTranslationSavesWhenNotExists() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByRegisterIdAndLanguageAndTranslatorPropertyType(REGISTER_ID, language, TYPE))
                .thenReturn(Optional.empty());

        translationService.createTranslation(REGISTER_ID, SPANISH_LANG, TYPE, TRANSLATED);

        verify(translationsRepository).save(argThat(t ->
                t.getRegisterId().equals(REGISTER_ID) &&
                t.getTranslation().equals(TRANSLATED) &&
                t.getTranslatorPropertyType() == TYPE));
    }

    @Test
    void testCreateTranslationDoesNothingWhenAlreadyExists() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByRegisterIdAndLanguageAndTranslatorPropertyType(REGISTER_ID, language, TYPE))
                .thenReturn(Optional.of(translation));

        translationService.createTranslation(REGISTER_ID, SPANISH_LANG, TYPE, TRANSLATED);

        verify(translationsRepository, never()).save(any());
    }

    // ─── updateTranslation ────────────────────────────────────────────────────

    @Test
    void testUpdateTranslationUpdatesExistingTranslation() {
        String updatedText = "Nueva Electrónica";
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByRegisterIdAndLanguageAndTranslatorPropertyType(REGISTER_ID, language, TYPE))
                .thenReturn(Optional.of(translation));

        translationService.updateTranslation(REGISTER_ID, SPANISH_LANG, TYPE, updatedText);

        verify(translationsRepository).save(argThat(t -> t.getTranslation().equals(updatedText)));
    }

    @Test
    void testUpdateTranslationCreatesNewOneWhenNotExists() {
        String newText = "Nuevo Texto";
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByRegisterIdAndLanguageAndTranslatorPropertyType(REGISTER_ID, language, TYPE))
                .thenReturn(Optional.empty());

        translationService.updateTranslation(REGISTER_ID, SPANISH_LANG, TYPE, newText);

        verify(translationsRepository).save(argThat(t ->
                t.getRegisterId().equals(REGISTER_ID) &&
                t.getTranslation().equals(newText) &&
                t.getTranslatorPropertyType() == TYPE));
    }

    // ─── deleteTranslation ────────────────────────────────────────────────────

    @Test
    void testDeleteTranslationDeletesWhenExists() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByRegisterIdAndLanguageAndTranslatorPropertyType(REGISTER_ID, language, TYPE))
                .thenReturn(Optional.of(translation));

        translationService.deleteTranslation(REGISTER_ID, SPANISH_LANG, TYPE);

        verify(translationsRepository).delete(translation);
    }

    @Test
    void testDeleteTranslationDoesNothingWhenNotExists() {
        when(languageService.getLanguageByName(SPANISH_LANG)).thenReturn(language);
        when(translationsRepository.findByRegisterIdAndLanguageAndTranslatorPropertyType(REGISTER_ID, language, TYPE))
                .thenReturn(Optional.empty());

        translationService.deleteTranslation(REGISTER_ID, SPANISH_LANG, TYPE);

        verify(translationsRepository, never()).delete(any());
    }
}