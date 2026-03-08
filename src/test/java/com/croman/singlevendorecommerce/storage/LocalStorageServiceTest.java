package com.croman.singlevendorecommerce.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

import com.croman.singlevendorecommerce.storage.dto.StoredFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    @TempDir
    Path tempDir;

    private LocalStorageService localStorageService;

    private static final String FILE_KEY     = "test-file.txt";
    private static final String FILE_CONTENT = "Hello, storage!";

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        ReflectionTestUtils.setField(localStorageService, "basePath", tempDir);
    }

    // ─── helpers ─────────────────────────────────────────────────────────────

    private InputStream contentAsStream() {
        return new ByteArrayInputStream(FILE_CONTENT.getBytes(StandardCharsets.UTF_8));
    }

    private void uploadFile(String key) {
        localStorageService.upload(key, contentAsStream(), FILE_CONTENT.length(), "text/plain");
    }

    // ─── upload ───────────────────────────────────────────────────────────────

    @Test
    void testUploadCreatesFileWithCorrectContent() throws Exception {
        uploadFile(FILE_KEY);

        Path uploaded = tempDir.resolve(FILE_KEY);
        assertThat(uploaded).exists();
        assertThat(Files.readString(uploaded)).isEqualTo(FILE_CONTENT);
    }

    @Test
    void testUploadOverwritesExistingFile() throws Exception {
        uploadFile(FILE_KEY);

        String newContent = "Updated content";
        localStorageService.upload(FILE_KEY,
                new ByteArrayInputStream(newContent.getBytes(StandardCharsets.UTF_8)),
                newContent.length(), "text/plain");

        assertThat(Files.readString(tempDir.resolve(FILE_KEY))).isEqualTo(newContent);
    }

    // ─── download ─────────────────────────────────────────────────────────────

    @Test
    void testDownloadReturnsCorrectContent() throws Exception {
        uploadFile(FILE_KEY);

        StoredFile result = localStorageService.download(FILE_KEY);

        assertThat(new String(result.getInputStream().readAllBytes(), StandardCharsets.UTF_8)).isEqualTo(FILE_CONTENT);
    }

    @Test
    void testDownloadThrowsWhenFileNotFound() {
        assertThatThrownBy(() -> localStorageService.download("nonexistent.txt"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("nonexistent.txt");
    }

    // ─── exists ───────────────────────────────────────────────────────────────

    @Test
    void testExistsReturnsTrueWhenFileExists() {
        uploadFile(FILE_KEY);

        assertThat(localStorageService.exists(FILE_KEY)).isTrue();
    }

    @Test
    void testExistsReturnsFalseWhenFileDoesNotExist() {
        assertThat(localStorageService.exists("ghost.txt")).isFalse();
    }

    // ─── delete ───────────────────────────────────────────────────────────────

    @Test
    void testDeleteRemovesExistingFile() {
        uploadFile(FILE_KEY);

        localStorageService.delete(FILE_KEY);

        assertThat(tempDir.resolve(FILE_KEY)).doesNotExist();
    }

    @Test
    void testDeleteDoesNotThrowWhenFileDoesNotExist() {
        // deleteIfExists — should be silent
        localStorageService.delete("nonexistent.txt");
    }

    // ─── size ─────────────────────────────────────────────────────────────────

    @Test
    void testSizeReturnsCorrectFileSize() {
        uploadFile(FILE_KEY);

        long size = localStorageService.size(FILE_KEY);

        assertThat(size).isEqualTo(FILE_CONTENT.getBytes(StandardCharsets.UTF_8).length);
    }

    @Test
    void testSizeThrowsWhenFileDoesNotExist() {
        assertThatThrownBy(() -> localStorageService.size("nonexistent.txt"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("nonexistent.txt");
    }

    // ─── metadata ─────────────────────────────────────────────────────────────

    @Test
    void testMetadataReturnsCorrectValuesWhenFileExists() {
        uploadFile(FILE_KEY);

        Optional<Map<String, String>> result = localStorageService.metadata(FILE_KEY);

        assertThat(result).isPresent();
        assertThat(result.get()).containsKey("size");
        assertThat(result.get()).containsKey("lastModified");
        assertThat(result.get()).containsKey("path");
        assertThat(result.get().get("size"))
                .isEqualTo(String.valueOf(FILE_CONTENT.getBytes(StandardCharsets.UTF_8).length));
    }

    @Test
    void testMetadataReturnsEmptyWhenFileDoesNotExist() {
        Optional<Map<String, String>> result = localStorageService.metadata("nonexistent.txt");

        assertThat(result).isEmpty();
    }
}