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
	static final int MAX_ATTACHMENT_BYTES = 32 * 1024 * 1024;

	private final URL zipFileLocation;
	private final int maxAttachmentBytes;

	public ZipFileAttachment(String name, URL zipFileLocation) {
		this(name, zipFileLocation, MAX_ATTACHMENT_BYTES);
	}

	ZipFileAttachment(String name, URL zipFileLocation, int maxAttachmentBytes) {
		super(name);
		this.zipFileLocation = zipFileLocation;
		this.maxAttachmentBytes = maxAttachmentBytes;
	}

	@Override
	public InputStream getBytes() throws DecalNotFoundException, IOException {
		String name = getName();

		try (ZipInputStream zis = new ZipInputStream(zipFileLocation.openStream())) {
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				if (entry.getName().equals(name)) {
					if (entry.getSize() > maxAttachmentBytes) {
						throw new IOException("Attachment '" + name + "' exceeds the maximum size of "
								+ maxAttachmentBytes + " bytes");
					}
					byte[] bytes = FileUtils.readBytes(zis, maxAttachmentBytes);
					return new ByteArrayInputStream(bytes);
				}
				entry = zis.getNextEntry();
			}
			throw new DecalNotFoundException(name, null);
		}

	}

}
