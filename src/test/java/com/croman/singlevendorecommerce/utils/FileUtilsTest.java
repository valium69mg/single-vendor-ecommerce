package com.croman.singlevendorecommerce.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class FileUtilsTest {

    private static final String DEFAULT_EXTENSION = ".jpg";

    // ─── getFileExtension ──────────────────────────────────────────────────

    @Test
    void testGetFileExtensionReturnsExtensionWhenFilenameHasDot() {
        // Arrange
        String filename = "image.png";

        // Act
        String extension = FileUtils.getFileExtension(filename);

        // Assert
        assertThat(extension).isEqualTo(".png");
    }

    @Test
    void testGetFileExtensionReturnsDefaultWhenFilenameHasNoDot() {
        // Arrange
        String filename = "image";

        // Act
        String extension = FileUtils.getFileExtension(filename);

        // Assert
        assertThat(extension).isEqualTo(DEFAULT_EXTENSION);
    }

    @Test
    void testGetFileExtensionReturnsDefaultWhenFilenameIsNull() {
        // Act
        String extension = FileUtils.getFileExtension(null);

        // Assert
        assertThat(extension).isEqualTo(DEFAULT_EXTENSION);
    }

    @Test
    void testGetFileExtensionReturnsLastExtensionWhenFilenameHasMultipleDots() {
        // Arrange
        String filename = "archive.backup.tar.gz";

        // Act
        String extension = FileUtils.getFileExtension(filename);

        // Assert
        assertThat(extension).isEqualTo(".gz");
    }

    // ─── toMediumThumbnailKey ──────────────────────────────────────────────

    @Test
    void testToMediumThumbnailKeyReturnsNullWhenKeyIsNull() {
        // Act
        String result = FileUtils.toMediumThumbnailKey(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void testToMediumThumbnailKeyInsertsSuffixBeforeExtension() {
        // Arrange
        String key = "image.jpg";

        // Act
        String result = FileUtils.toMediumThumbnailKey(key);

        // Assert
        assertThat(result).isEqualTo("image_400.jpg");
    }

    @Test
    void testToMediumThumbnailKeyUsesLastDotWhenMultipleDotsExist() {
        // Arrange
        String key = "photo.profile.image.png";

        // Act
        String result = FileUtils.toMediumThumbnailKey(key);

        // Assert
        assertThat(result).isEqualTo("photo.profile.image_400.png");
    }

    // ─── toSmallThumbnailKey ───────────────────────────────────────────────

    @Test
    void testToSmallThumbnailKeyReturnsNullWhenKeyIsNull() {
        // Act
        String result = FileUtils.toSmallThumbnailKey(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void testToSmallThumbnailKeyInsertsSuffixBeforeExtension() {
        // Arrange
        String key = "image.jpg";

        // Act
        String result = FileUtils.toSmallThumbnailKey(key);

        // Assert
        assertThat(result).isEqualTo("image_200.jpg");
    }

    @Test
    void testToSmallThumbnailKeyUsesLastDotWhenMultipleDotsExist() {
        // Arrange
        String key = "photo.profile.image.png";

        // Act
        String result = FileUtils.toSmallThumbnailKey(key);

        // Assert
        assertThat(result).isEqualTo("photo.profile.image_200.png");
    }
}