package net.sf.openrocket.document.attachments;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.openrocket.document.Attachment;
import net.sf.openrocket.util.FileUtils;

public class ZipFileAttachment extends Attachment {
	
	private final URL zipFileLocation;
	
	public ZipFileAttachment(String name, URL zipFileLocation) {
		super(name);
		this.zipFileLocation = zipFileLocation;
	}
	
	@Override
	public InputStream getBytes() throws FileNotFoundException, IOException {
		String name = getName();
		
		ZipInputStream zis = new ZipInputStream(zipFileLocation.openStream());
		
		try {
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				if (entry.getName().equals(name)) {
					byte[] bytes = FileUtils.readBytes(zis);
					return new ByteArrayInputStream(bytes);
				}
				entry = zis.getNextEntry();
			}
			throw new FileNotFoundException("Unable to locate decal for name " + name);
		} finally {
			zis.close();
		}
		
	}
	
}
