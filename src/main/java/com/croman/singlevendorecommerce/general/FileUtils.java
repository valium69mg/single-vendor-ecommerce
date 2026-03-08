package com.croman.singlevendorecommerce.general;

public final class FileUtils {

	private FileUtils() {
	}

	public static String getFileExtension(String originalFilename) {
		String extension;
		if (originalFilename != null && originalFilename.contains(".")) {
			extension = originalFilename.substring(originalFilename.lastIndexOf("."));
		} else {
			extension = ".jpg";
		}
		return extension;
	}
}
