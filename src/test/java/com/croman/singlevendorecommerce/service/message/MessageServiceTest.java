package com.croman.singlevendorecommerce.service.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;

import com.croman.singlevendorecommerce.service.message.MessageService;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    // -------------------------------------------------------------------------
    // Constants (no magic values)
    // -------------------------------------------------------------------------

    private static final String MESSAGE_KEY = "test.message.key";
    private static final String MESSAGE_VALUE = "resolved message";
    private static final Locale LOCALE_EN = Locale.ENGLISH;

    // -------------------------------------------------------------------------
    // Mocks
    // -------------------------------------------------------------------------

    @Mock
    private MessageSource messageSource;

    // -------------------------------------------------------------------------
    // System under test
    // -------------------------------------------------------------------------

    @InjectMocks
    private MessageService messageService;

    // -------------------------------------------------------------------------
    // Tests
    // -------------------------------------------------------------------------

    @Test
    void testGetMessageDelegatesToMessageSourceAndReturnsResolvedMessage() {
        // Arrange
        when(messageSource.getMessage(MESSAGE_KEY, null, LOCALE_EN))
            .thenReturn(MESSAGE_VALUE);

        // Act
        String result = messageService.getMessage(MESSAGE_KEY, LOCALE_EN);

        // Assert
        assertEquals(MESSAGE_VALUE, result);
        verify(messageSource).getMessage(MESSAGE_KEY, null, LOCALE_EN);
    }
}
