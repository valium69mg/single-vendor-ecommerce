package com.croman.singlevendorecommerce.service.storage;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

import com.croman.singlevendorecommerce.dto.storage.StoredFile;

public interface StorageService {

    void upload(String key, InputStream data, long contentLength, String contentType);

    StoredFile download(String key);

    boolean exists(String key);

    void delete(String key);

    long size(String key);

    Optional<Map<String, String>> metadata(String key);

}