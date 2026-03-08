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

	public static String toMediumThumbnailKey(String key) {
		if (key == null)
			return null;

		int dotIndex = key.lastIndexOf(".");
		if (dotIndex == -1) {
			return key + "_200";
		}

		return key.substring(0, dotIndex) + "_200" + key.substring(dotIndex);
	}

	public static String toSmallThumbnailKey(String key) {
		if (key == null)
			return null;

		int dotIndex = key.lastIndexOf(".");
		if (dotIndex == -1) {
			return key + "_400";
		}

		return key.substring(0, dotIndex) + "_400" + key.substring(dotIndex);
	}
}
