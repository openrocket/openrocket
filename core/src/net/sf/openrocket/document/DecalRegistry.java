package net.sf.openrocket.document;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.appearance.DecalImage;
import net.sf.openrocket.document.BaseAttachmentFactory.BaseAttachment;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.FileUtils;

public class DecalRegistry implements AttachmentFactory<DecalImage> {
	private static LogHelper log = Application.getLogger();
	
	private final BaseAttachmentFactory baseFactory;
	
	public DecalRegistry(BaseAttachmentFactory baseFactory) {
		this.baseFactory = baseFactory;
	}
	
	private Map<String, DecalImageImpl> registeredDecals = new HashMap<String, DecalImageImpl>();
	
	public DecalImage getAttachment(String decalName) {
		DecalImageImpl d = registeredDecals.get(decalName);
		if (d == null) {
			BaseAttachment attachment = baseFactory.getAttachment(decalName);
			d = new DecalImageImpl(attachment);
			registeredDecals.put(decalName, d);
		}
		return d;
	}
	
	public DecalImage getAttachment(File file) {
		
		// See if this file is being used already
		DecalImageImpl decal = findDecalForFile(file);
		
		if (decal != null) {
			return decal;
		}
		
		// It's a new file, generate a name for it.
		String decalName = makeUniqueName(file.getName());
		
		BaseAttachment attachment = baseFactory.getAttachment(decalName);
		decal = new DecalImageImpl(attachment);
		decal.setFileSystemLocation(file);
		
		registeredDecals.put(decalName, decal);
		return decal;
		
	}
	
	public Collection<DecalImage> getDecalList() {
		
		Set<DecalImage> decals = new TreeSet<DecalImage>();
		
		decals.addAll(registeredDecals.values());
		
		return decals;
	}
	
	public class DecalImageImpl implements DecalImage, Comparable {
		
		private final BaseAttachment delegate;
		
		private File fileSystemLocation;
		
		private DecalImageImpl(BaseAttachment delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public String getName() {
			return delegate.getName();
		}
		
		@Override
		public InputStream getBytes() throws FileNotFoundException, IOException {
			return DecalRegistry.this.getDecal(this);
		}
		
		@Override
		public void exportImage(File file, boolean watchForChanges) throws IOException {
			DecalRegistry.this.exportDecal(this, file);
			this.fileSystemLocation = file;
		}
		
		File getFileSystemLocation() {
			return fileSystemLocation;
		}
		
		void setFileSystemLocation(File fileSystemLocation) {
			this.fileSystemLocation = fileSystemLocation;
		}
		
		@Override
		public String toString() {
			return delegate.toString();
		}
		
		@Override
		public int compareTo(Object o) {
			if (!(o instanceof DecalImageImpl)) {
				return -1;
			}
			return delegate.compareTo(((DecalImageImpl) o).delegate);
		}
		
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
	private InputStream getDecal(DecalImageImpl decal) throws FileNotFoundException, IOException {
		
		// First check if the decal is located on the file system
		File exportedFile = decal.getFileSystemLocation();
		if (exportedFile != null) {
			InputStream rawIs = new FileInputStream(exportedFile);
			try {
				byte[] bytes = FileUtils.readBytes(rawIs);
				return new ByteArrayInputStream(bytes);
			} finally {
				rawIs.close();
			}
			
		}
		
		return decal.delegate.getBytes();
		
	}
	
	private void exportDecal(DecalImageImpl decal, File selectedFile) throws IOException {
		
		try {
			InputStream is = decal.getBytes();
			OutputStream os = new BufferedOutputStream(new FileOutputStream(selectedFile));
			
			FileUtils.copy(is, os);
			
			is.close();
			os.close();
			
		} catch (IOException iex) {
			throw new BugException(iex);
		}
		
	}
	
	private DecalImageImpl findDecalForFile(File file) {
		
		for (DecalImageImpl d : registeredDecals.values()) {
			if (file.equals(d.getFileSystemLocation())) {
				return d;
			}
		}
		return null;
	}
	
	/**
	 * Regular expression for parsing file names with numerical identifiers.
	 * For examples:
	 * 
	 * decals/an image (3).png
	 * 
	 * group(0) = "decals/an image (3).png"
	 * group(1) = "decals/an image"
	 * group(2) = " (3)"
	 * group(3) = "3"
	 * group(4) = "png"
	 * 
	 * decals/an image.png
	 * 
	 * group(0) = "decals/an image.png"
	 * group(1) = "decals/an image"
	 * group(2) = "null"
	 * group(3) = "null"
	 * group(4) = "png"
	 */
	private static final Pattern fileNamePattern = Pattern.compile("(.*?)( \\((\\d+)\\)\\)?+)?\\.(\\w*)");
	private static final int BASE_NAME_INDEX = 1;
	private static final int NUMBER_INDEX = 3;
	private static final int EXTENSION_INDEX = 4;
	
	private String makeUniqueName(String name) {
		
		String newName = "decals/" + name;
		String basename = "";
		String extension = "";
		Matcher nameMatcher = fileNamePattern.matcher(newName);
		if (nameMatcher.matches()) {
			basename = nameMatcher.group(BASE_NAME_INDEX);
			extension = nameMatcher.group(EXTENSION_INDEX);
		}
		
		Set<Integer> counts = new TreeSet<Integer>();
		
		boolean needsRewrite = false;
		
		for (DecalImageImpl d : registeredDecals.values()) {
			Matcher m = fileNamePattern.matcher(d.getName());
			if (m.matches()) {
				if (basename.equals(m.group(BASE_NAME_INDEX)) && extension.equals(m.group(EXTENSION_INDEX))) {
					String intString = m.group(NUMBER_INDEX);
					if (intString != null) {
						Integer i = Integer.parseInt(intString);
						counts.add(i);
					}
					needsRewrite = true;
				}
			} else if (newName.equals(d.getName())) {
				needsRewrite = true;
			}
		}
		
		if (!needsRewrite) {
			return newName;
		}
		
		// find a missing integer;
		Integer newIndex = 1;
		while (counts.contains(newIndex)) {
			newIndex++;
		}
		
		return MessageFormat.format("{0} ({1}).{2}", basename, newIndex, extension);
	}
	
}
