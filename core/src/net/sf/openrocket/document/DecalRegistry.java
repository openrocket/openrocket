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
import net.sf.openrocket.document.attachments.FileSystemAttachment;
import net.sf.openrocket.logging.LogHelper;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.FileUtils;

public class DecalRegistry {
	private static LogHelper log = Application.getLogger();
	
	DecalRegistry() {
		
	}
	
	private Map<String, DecalImageImpl> registeredDecals = new HashMap<String, DecalImageImpl>();
	
	public DecalImage getDecalImage(Attachment attachment) {
		String decalName = attachment.getName();
		DecalImageImpl d;
		if (attachment instanceof FileSystemAttachment) {
			File location = ((FileSystemAttachment) attachment).getLocation();
			d = findDecalForFile(location);
			if (d != null) {
				return d;
			}
			
			// It's a new file, generate a name for it.
			decalName = makeUniqueName(location.getName());
			
			d = new DecalImageImpl(decalName, attachment);
			d.setFileSystemLocation(location);
			
			registeredDecals.put(decalName, d);
			return d;
			
		} else {
			d = registeredDecals.get(decalName);
			if (d != null) {
				return d;
			}
		}
		d = new DecalImageImpl(attachment);
		registeredDecals.put(decalName, d);
		return d;
	}
	
	public Collection<DecalImage> getDecalList() {
		
		Set<DecalImage> decals = new TreeSet<DecalImage>();
		
		decals.addAll(registeredDecals.values());
		
		return decals;
	}
	
	public class DecalImageImpl implements DecalImage {
		
		private final Attachment delegate;
		
		private String name;
		private File fileSystemLocation;
		
		private DecalImageImpl(String name, Attachment delegate) {
			this.name = name;
			this.delegate = delegate;
		}
		
		private DecalImageImpl(Attachment delegate) {
			this.delegate = delegate;
		}
		
		@Override
		public String getName() {
			return name != null ? name : delegate.getName();
		}
		
		@Override
		public InputStream getBytes() throws FileNotFoundException, IOException {
			return DecalRegistry.getDecal(this);
		}
		
		@Override
		public void exportImage(File file, boolean watchForChanges) throws IOException {
			DecalRegistry.exportDecal(this, file);
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
			return getName();
		}
		
		@Override
		public int compareTo(Attachment o) {
			if (!(o instanceof DecalImageImpl)) {
				return -1;
			}
			return getName().compareTo(o.getName());
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
	private static InputStream getDecal(DecalImageImpl decal) throws FileNotFoundException, IOException {
		
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
	
	private static void exportDecal(DecalImageImpl decal, File selectedFile) throws IOException {
		
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
