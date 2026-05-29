package com.croman.singlevendorecommerce.service.thumbnail;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.croman.singlevendorecommerce.dto.storage.StoredFile;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.storage.StorageService;
import com.croman.singlevendorecommerce.utils.FileUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

@ExtendWith(MockitoExtension.class)
class ThumbnailServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private MessageService messageService;

    @InjectMocks
    private ThumbnailService thumbnailService;

    private static final String IMAGE_KEY = "categories/abc123.jpg";

    private byte[] validImageBytes;

    @BeforeEach
    void setUp() throws Exception {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", baos);
        validImageBytes = baos.toByteArray();
    }

    @Test
    void testGenerateThumbnailsUploadsBothSizes() throws Exception {
        when(storageService.download(IMAGE_KEY))
                .thenAnswer(inv -> new StoredFile(new ByteArrayInputStream(validImageBytes), validImageBytes.length, "image/jpeg", "abc123.jpg"));

        thumbnailService.generateThumbnails(IMAGE_KEY);

        verify(storageService, times(2)).download(IMAGE_KEY);
        verify(storageService).upload(
                eq(FileUtils.toSmallThumbnailKey(IMAGE_KEY)),
                any(InputStream.class),
                anyLong(),
                eq("image/jpeg")
        );
        verify(storageService).upload(
                eq(FileUtils.toMediumThumbnailKey(IMAGE_KEY)),
                any(InputStream.class),
                anyLong(),
                eq("image/jpeg")
        );
    }

    @Test
    void testGenerateThumbnailsThrowsApiExceptionWhenDownloadFails() {
        when(storageService.download(IMAGE_KEY))
                .thenThrow(new ApiServiceException(500, "Storage error"));

        assertThatThrownBy(() -> thumbnailService.generateThumbnails(IMAGE_KEY))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Storage error");
    }

    @Test
    void testGenerateThumbnailsThrowsApiExceptionWhenImageIsCorrupted() {
        byte[] corruptBytes = "not-an-image".getBytes();
        when(storageService.download(IMAGE_KEY))
                .thenAnswer(inv -> new StoredFile(new ByteArrayInputStream(corruptBytes), corruptBytes.length, "image/jpeg", "abc123.jpg"));
        when(messageService.getMessage(eq("thumbnail_generation_failed"), any(Locale.class)))
                .thenReturn("Thumbnail generation failed");

        assertThatThrownBy(() -> thumbnailService.generateThumbnails(IMAGE_KEY))
                .isInstanceOf(ApiServiceException.class)
                .hasMessageContaining("Thumbnail generation failed");
    }

    @Test
    void testGenerateThumbnailsUsesCorrectKeyNaming() throws Exception {
        when(storageService.download(IMAGE_KEY))
                .thenAnswer(inv -> new StoredFile(new ByteArrayInputStream(validImageBytes), validImageBytes.length, "image/jpeg", "abc123.jpg"));

        thumbnailService.generateThumbnails(IMAGE_KEY);

        assertThat(FileUtils.toSmallThumbnailKey(IMAGE_KEY)).isEqualTo("categories/abc123_200.jpg");
        assertThat(FileUtils.toMediumThumbnailKey(IMAGE_KEY)).isEqualTo("categories/abc123_400.jpg");
    }
}
