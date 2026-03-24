package com.croman.singlevendorecommerce.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HttpUtilsTest {

    private static final String REMOTE_IP = "192.168.1.10";
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";
    
    @Mock
    private HttpServletRequest request;

    // ─── X-Forwarded-For present ────────────────────────────────────────────

    @Test
    void testGetClientIpReturnsFirstIpWhenXForwardedForContainsMultipleIps() {
        // Arrange
        when(request.getHeader(X_FORWARDED_FOR))
                .thenReturn("10.0.0.1, 10.0.0.2, 10.0.0.3");

        // Act
        String result = HttpUtils.getClientIp(request);

        // Assert
        assertThat(result).isEqualTo("10.0.0.1");
    }

    @Test
    void testGetClientIpReturnsIpWhenXForwardedForContainsSingleIp() {
        // Arrange
        when(request.getHeader(X_FORWARDED_FOR))
                .thenReturn("10.0.0.5");

        // Act
        String result = HttpUtils.getClientIp(request);

        // Assert
        assertThat(result).isEqualTo("10.0.0.5");
    }

    // ─── X-Forwarded-For absent or empty ────────────────────────────────────

    @Test
    void testGetClientIpFallsBackToRemoteAddrWhenXForwardedForIsNull() {
        // Arrange
        when(request.getHeader(X_FORWARDED_FOR)).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn(REMOTE_IP);

        // Act
        String result = HttpUtils.getClientIp(request);

        // Assert
        assertThat(result).isEqualTo(REMOTE_IP);
    }

    @Test
    void testGetClientIpFallsBackToRemoteAddrWhenXForwardedForIsEmpty() {
        // Arrange
        when(request.getHeader(X_FORWARDED_FOR)).thenReturn("");
        when(request.getRemoteAddr()).thenReturn(REMOTE_IP);

        // Act
        String result = HttpUtils.getClientIp(request);

        // Assert
        assertThat(result).isEqualTo(REMOTE_IP);
    }
}