package com.croman.SingleVendorEcommerce.Storage;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public interface StorageService {

    void upload(String key, InputStream data, long contentLength, String contentType);

    InputStream download(String key);

    boolean exists(String key);

    void delete(String key);

    long size(String key);

    Optional<Map<String, String>> metadata(String key);
    
}
