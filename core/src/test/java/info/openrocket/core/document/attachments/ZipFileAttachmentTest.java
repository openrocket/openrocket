package info.openrocket.core.document.attachments;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ZipFileAttachmentTest {

	@TempDir
	Path tempDirectory;

	@Test
	void readsAttachmentWithinConfiguredLimit() throws Exception {
		byte[] contents = {1, 2, 3, 4};
		Path archive = createArchive("decal.png", contents);
		ZipFileAttachment attachment = new ZipFileAttachment("decal.png", archive.toUri().toURL(), 4);

		try (InputStream stream = attachment.getBytes()) {
			assertArrayEquals(contents, stream.readAllBytes());
		}
	}

	@Test
	void rejectsInflatedAttachmentBeyondConfiguredLimit() throws Exception {
		Path archive = createArchive("decal.png", new byte[65]);
		ZipFileAttachment attachment = new ZipFileAttachment("decal.png", archive.toUri().toURL(), 64);

		IOException exception = assertThrows(IOException.class, attachment::getBytes);
		assertTrue(exception.getMessage().contains("64"));
	}

	private Path createArchive(String entryName, byte[] contents) throws IOException {
		Path archive = tempDirectory.resolve("attachments-" + Arrays.hashCode(contents) + ".zip");
		try (ZipOutputStream output = new ZipOutputStream(Files.newOutputStream(archive))) {
			output.putNextEntry(new ZipEntry(entryName));
			output.write(contents);
			output.closeEntry();
		}
		return archive;
	}
}
