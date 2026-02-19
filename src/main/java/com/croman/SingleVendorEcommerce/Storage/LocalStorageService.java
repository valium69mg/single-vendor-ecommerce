package com.croman.SingleVendorEcommerce.Storage;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.io.*;
import java.nio.file.*;
import java.util.*;

@Service
@Slf4j
public class LocalStorageService implements StorageService {

    @Getter
    private Path basePath;

    @Value("${storage.local.base-path}")
    private String basePathValue;

    @PostConstruct
    private void init() {
        this.basePath = Paths.get(basePathValue).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
            log.info("Storage initialized at: {}", basePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to initialize storage folder: " + basePath, e);
        }
    }

    @Override
    public void upload(String key, InputStream data, long contentLength, String contentType) {
        Path target = basePath.resolve(key).normalize();
        try (OutputStream os = Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = data.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            log.info("File uploaded: {}", key);
        } catch (IOException e) {
            log.error("Error uploading file: {}", key, e);
            throw new RuntimeException("Error uploading file with key: " + key, e);
        }
    }

    @Override
    public InputStream download(String key) {
        Path target = basePath.resolve(key).normalize();
        if (!Files.exists(target)) {
            log.warn("File not found: {}", key);
            throw new NoSuchElementException("File not found: " + key);
        }
        try {
            return Files.newInputStream(target, StandardOpenOption.READ);
        } catch (IOException e) {
            log.error("Error downloading file: {}", key, e);
            throw new RuntimeException("Error downloading file with key: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        Path target = basePath.resolve(key).normalize();
        return Files.exists(target);
    }

    @Override
    public void delete(String key) {
        Path target = basePath.resolve(key).normalize();
        try {
            Files.deleteIfExists(target);
            log.info("File deleted: {}", key);
        } catch (IOException e) {
            log.error("Error deleting file: {}", key, e);
            throw new RuntimeException("Error deleting file with key: " + key, e);
        }
    }

    @Override
    public long size(String key) {
        Path target = basePath.resolve(key).normalize();
        try {
            return Files.size(target);
        } catch (IOException e) {
            log.error("Error getting file size: {}", key, e);
            throw new RuntimeException("Error getting size of file with key: " + key, e);
        }
    }

    @Override
    public Optional<Map<String, String>> metadata(String key) {
        Path target = basePath.resolve(key).normalize();
        if (!Files.exists(target)) {
            return Optional.empty();
        }
        try {
            Map<String, String> meta = new HashMap<>();
            meta.put("size", String.valueOf(Files.size(target)));
            meta.put("lastModified", String.valueOf(Files.getLastModifiedTime(target).toMillis()));
            meta.put("path", target.toString());
            return Optional.of(meta);
        } catch (IOException e) {
            log.error("Error getting metadata for file: {}", key, e);
            throw new RuntimeException("Error getting metadata for file with key: " + key, e);
        }
    }
}