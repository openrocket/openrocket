package net.sf.openrocket.document;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.sf.openrocket.file.FileInfo;
import net.sf.openrocket.util.FileUtils;

public class BaseAttachmentFactory implements AttachmentFactory<BaseAttachmentFactory.BaseAttachment> {
	
	private FileInfo fileInfo;
	private boolean isZipFile = false;
	
	public void setBaseFile(FileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}
	
	public void setIsZipFile(boolean isZipFile) {
		this.isZipFile = isZipFile;
	}
	
	public class BaseAttachment implements Attachment, Comparable {
		
		protected String name;
		
		BaseAttachment(String name) {
			this.name = name;
		}
		
		@Override
		public String getName() {
			return name;
		}
		
		@Override
		public InputStream getBytes() throws FileNotFoundException, IOException {
			return BaseAttachmentFactory.this.getBytes(this);
		}
		
		@Override
		public int compareTo(Object o) {
			if (!(o instanceof BaseAttachment)) {
				return -1;
			}
			return this.name.compareTo(((BaseAttachment) o).name);
		}
		
		@Override
		public String toString() {
			return getName();
		}
		
	}
	
	@Override
	public BaseAttachment getAttachment(String name) {
		return new BaseAttachment(name);
	}
	
	/**
	 * This function returns an InputStream backed by a byte[] containing the decal pixels.
	 * If it reads in the bytes from an actual file, the underlying file is closed.
	 * 
	 * @param name
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private InputStream getBytes(BaseAttachment attachment) throws FileNotFoundException, IOException {
		
		// This is the InputStream to be returned.
		InputStream rawIs = null;
		
		
		String name = attachment.getName();
		
		if (rawIs == null && isZipFile) {
			rawIs = findInZipContainer(name);
		}
		
		// Try relative to the model file directory.  This is so we can support unzipped container format.
		if (rawIs == null) {
			if (fileInfo != null && fileInfo.getDirectory() != null) {
				File decalFile = new File(fileInfo.getDirectory(), name);
				rawIs = new FileInputStream(decalFile);
			}
		}
		
		if (rawIs == null) {
			throw new FileNotFoundException("Unable to locate decal for name " + name);
		}
		
		try {
			byte[] bytes = FileUtils.readBytes(rawIs);
			return new ByteArrayInputStream(bytes);
		} finally {
			rawIs.close();
		}
		
	}
	
	private ZipInputStream findInZipContainer(String name) {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(fileInfo.getFileURL().openStream());
		} catch (IOException ex) {
			return null;
		}
		try {
			ZipEntry entry = zis.getNextEntry();
			while (entry != null) {
				if (entry.getName().equals(name)) {
					return zis;
				}
				entry = zis.getNextEntry();
			}
			zis.close();
			return null;
		} catch (IOException ioex) {
			try {
				zis.close();
			} catch (IOException ex) {
				// why does close throw?  it's maddening
			}
			return null;
		}
	}
	
}
