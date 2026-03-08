package com.croman.singlevendorecommerce.storage.dto;

import java.io.InputStream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StoredFile {

    private InputStream inputStream;
    private long size;
    private String contentType;
    private String filename;

}