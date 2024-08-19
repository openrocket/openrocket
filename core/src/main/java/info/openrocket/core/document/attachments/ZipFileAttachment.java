package info.openrocket.core.document.attachments;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import info.openrocket.core.document.Attachment;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.FileUtils;

public class ZipFileAttachment extends Attachment {

	private final URL zipFileLocation;

	public ZipFileAttachment(String name, URL zipFileLocation) {
		super(name);
		this.zipFileLocation = zipFileLocation;
	}

	@Override
	public InputStream getBytes() throws DecalNotFoundException, IOException {
		String name = getName();

		try (ZipInputStream zis = new ZipInputStream(zipFileLocation.openStream())) {
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				if (entry.getName().equals(name)) {
					byte[] bytes = FileUtils.readBytes(zis);
					return new ByteArrayInputStream(bytes);
				}
				entry = zis.getNextEntry();
			}
			throw new DecalNotFoundException(name, null);
		}

	}

}
