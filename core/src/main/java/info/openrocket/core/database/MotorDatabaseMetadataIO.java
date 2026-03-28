package info.openrocket.core.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class MotorDatabaseMetadataIO {
	private MotorDatabaseMetadataIO() {
	}

	public static MotorDatabaseMetadata readFile(File metadataFile) throws IOException {
		try (InputStream is = new FileInputStream(metadataFile)) {
			return MotorDatabaseMetadata.parse(is);
		}
	}

	public static long readDatabaseVersion(File metadataFile) throws IOException {
		return readFile(metadataFile).getDatabaseVersion();
	}

	public static MotorDatabaseMetadata readResource(Class<?> clazz, String resourcePath) throws IOException {
		try (InputStream is = clazz.getResourceAsStream(resourcePath)) {
			if (is == null) {
				throw new IOException("Could not find resource: " + resourcePath);
			}
			return MotorDatabaseMetadata.parse(is);
		}
	}

	public static void writeRawJson(File metadataFile, String rawJson) throws IOException {
		String content = rawJson == null ? "" : rawJson.trim();
		content = content + "\n";
		try (FileOutputStream out = new FileOutputStream(metadataFile)) {
			out.write(content.getBytes(StandardCharsets.UTF_8));
		}
	}
}

