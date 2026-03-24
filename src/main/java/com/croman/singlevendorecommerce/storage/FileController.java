package com.croman.singlevendorecommerce.storage;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.croman.singlevendorecommerce.storage.dto.StoredFile;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/file")
public class FileController {

	private final StorageService storageService;
	
	@GetMapping
	@Operation(
	    summary = "Download file",
	    responses = {
	        @ApiResponse(
	            responseCode = "200",
	            description = "File downloaded successfully"
	        ),
	        @ApiResponse(
	            responseCode = "404",
	            description = "File not found"
	        )
	    }
	)
	public ResponseEntity<InputStreamResource> download(
	        @RequestParam String key
	) {

	    StoredFile file = storageService.download(key);

	    return ResponseEntity.ok()
	            .contentType(MediaType.parseMediaType(file.getContentType()))
	            .contentLength(file.getSize())
	            .body(new InputStreamResource(file.getInputStream()));
	}
}
