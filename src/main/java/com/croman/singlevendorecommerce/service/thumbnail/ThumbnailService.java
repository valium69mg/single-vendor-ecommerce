package com.croman.singlevendorecommerce.service.thumbnail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.croman.singlevendorecommerce.dto.storage.StoredFile;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.storage.StorageService;
import com.croman.singlevendorecommerce.utils.FileUtils;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;

@Service
@RequiredArgsConstructor
@Slf4j
public class ThumbnailService {

    private final StorageService storageService;
    private final MessageService messageService;

    public void generateThumbnails(String key) {
        generate(key, 200, FileUtils.toSmallThumbnailKey(key));
        generate(key, 400, FileUtils.toMediumThumbnailKey(key));
    }

    private void generate(String key, int size, String thumbnailKey) {
        try {
            StoredFile original = storageService.download(key);
            String extension = FileUtils.getFileExtension(key).substring(1);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Thumbnails.of(original.getInputStream())
                    .size(size, size)
                    .keepAspectRatio(true)
                    .outputFormat(extension)
                    .toOutputStream(out);

            byte[] bytes = out.toByteArray();
            storageService.upload(thumbnailKey, new ByteArrayInputStream(bytes), bytes.length, original.getContentType());
            log.debug("Generated {}px thumbnail: {}", size, thumbnailKey);
        } catch (IOException e) {
            log.error("Failed to generate {}px thumbnail for {}", size, key, e);
            throw new ApiServiceException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    messageService.getMessage("thumbnail_generation_failed", LocaleUtils.getDefaultLocale()));
        }
    }
}
