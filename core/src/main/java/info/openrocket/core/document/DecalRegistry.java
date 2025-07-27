package info.openrocket.core.document;

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

import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.document.attachments.FileSystemAttachment;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.util.ChangeSource;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.FileUtils;
import info.openrocket.core.util.StateChangeListener;

/**
 * 
 * Class that handles decal usage registration
 *
 */
public class DecalRegistry {

	/**
	 * default constructor, does nothing
	 */
	DecalRegistry() {
	}

	/** the decal usage map */
	private final Map<String, DecalImageImpl> registeredDecals = new HashMap<>();

	/**
	 * returns a new decal with the same image but with unique names
	 * supports only classes and subclasses of DecalImageImpl
	 * 
	 * @param original the decal to be made unique
	 * @return
	 */
	public DecalImage makeUniqueImage(DecalImage original) {

		if (!(original instanceof DecalImageImpl)) {
			return original;
		}

		DecalImageImpl o = (DecalImageImpl) original;

		DecalImageImpl newDecal = o.clone();

		String newName = makeUniqueName(o.getName());

		// Return the old decal if a new one isn't required.
		if (newName.equals(o.getName())) {
			return original;
		}

		newDecal.name = newName;

		registeredDecals.put(newName, newDecal);

		return newDecal;

	}

	/**
	 * get the image from an attachment
	 * 
	 * @param attachment
	 * @return
	 */
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
			d.setDecalFile(location);

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

		Set<DecalImage> decals = new TreeSet<>();

		decals.addAll(registeredDecals.values());

		return decals;
	}

	public class DecalImageImpl implements DecalImage, Cloneable, Comparable<DecalImage>, ChangeSource {

		private final Attachment delegate;

		private String name;
		private File decalFile;
		private final Translator trans = Application.getTranslator();
		// Flag to check whether this DecalImage should be ignored for saving
		private boolean ignored = false;

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
		public void fireChangeEvent(Object source) {
			delegate.fireChangeEvent(source);
		}

		/**
		 * This function returns an InputStream backed by a byte[] containing the decal
		 * pixels.
		 * If it reads in the bytes from an actual file, the underlying file is closed.
		 * 
		 * @return InputStream containing byte[] of the image
		 * @throws FileNotFoundException
		 * @throws IOException
		 */
		@Override
		public InputStream getBytes() throws FileNotFoundException, IOException, DecalNotFoundException {
			// First check if the decal is located on the file system
			File exportedFile = getDecalFile();
			if (exportedFile != null) {
				if (!exportedFile.exists()) {
					throw new DecalNotFoundException(exportedFile.getAbsolutePath(), this);
				}
				try (InputStream rawIs = new FileInputStream(exportedFile)) {
					byte[] bytes = FileUtils.readBytes(rawIs);
					return new ByteArrayInputStream(bytes);
				}

			}
			try {
				return delegate.getBytes();
			} catch (DecalNotFoundException decex) {
				throw new DecalNotFoundException(delegate.getName(), this);
			}
		}

		@Override
		public void exportImage(File file) throws IOException, DecalNotFoundException {
			InputStream is;
			is = getBytes();
			OutputStream os = new BufferedOutputStream(new FileOutputStream(file));

			FileUtils.copy(is, os);

			is.close();
			os.close();
		}

		/**
		 * 
		 * @return
		 */
		public File getDecalFile() {
			return decalFile;
		}

		public void setDecalFile(File file) {
			this.decalFile = file;
		}

		@Override
		public boolean isIgnored() {
			return this.ignored;
		}

		@Override
		public void setIgnored(boolean ignored) {
			this.ignored = ignored;
		}

		@Override
		public String toString() {
			return getName();
		}

		@Override
		public int compareTo(DecalImage o) {
			return getName().compareTo(o.getName());
		}

		@Override
		protected DecalImageImpl clone() {
			DecalImageImpl clone = new DecalImageImpl(this.delegate);
			clone.decalFile = this.decalFile;

			return clone;
		}

		@Override
		public void addChangeListener(StateChangeListener listener) {
			delegate.addChangeListener(listener);
		}

		@Override
		public void removeChangeListener(StateChangeListener listener) {
			delegate.removeChangeListener(listener);
		}

	}

	/**
	 * Find decal that has file {file} as source
	 * 
	 * @param file decal source file
	 * @return
	 */
	private DecalImageImpl findDecalForFile(File file) {

		for (DecalImageImpl d : registeredDecals.values()) {
			if (file.equals(d.getDecalFile())) {
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
	private static final Pattern fileNamePattern = Pattern.compile("(.*?)( \\((\\d+)\\)+)?\\.(\\w*)");
	private static final int BASE_NAME_INDEX = 1;
	private static final int NUMBER_INDEX = 3;
	private static final int EXTENSION_INDEX = 4;

	/**
	 * Makes a unique name for saving decal files in case the name already exists
	 * 
	 * @param name the name of the decal
	 * @return the name formatted and unique
	 */
	private String makeUniqueName(String name) {

		String newName = checkPathConsistency(name);
		String basename = getGroup(BASE_NAME_INDEX, fileNamePattern.matcher(newName));
		String extension = getGroup(EXTENSION_INDEX, fileNamePattern.matcher(newName));

		Set<Integer> counts = new TreeSet<>();

		boolean needsRewrite = false;

		for (DecalImageImpl d : registeredDecals.values()) {
			Matcher m = fileNamePattern.matcher(d.getName());
			if (m.matches()) {
				if (isofSameBaseAndExtension(m, basename, extension)) {
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

		return MessageFormat.format("{0} ({1}).{2}", basename, findMissingInteger(counts), extension);
	}

	/**
	 * Searches the count for a new Integer
	 * 
	 * @param counts the count set
	 * @return a unique integer in the count
	 */
	private Integer findMissingInteger(Set<Integer> counts) {
		Integer newIndex = 1;
		while (counts.contains(newIndex)) {
			newIndex++;
		}
		return newIndex;
	}

	/**
	 * Tests if a matcher has the same basename and extension
	 * 
	 * @param m         the matcher being tested
	 * @param basename  the basename
	 * @param extension the extension
	 * @return
	 */
	private boolean isofSameBaseAndExtension(Matcher m, String basename, String extension) {
		return basename.equals(m.group(BASE_NAME_INDEX)) && extension.equals(m.group(EXTENSION_INDEX));
	}

	/**
	 * gets the String group from a matcher
	 * 
	 * @param index   the index of the group to
	 * @param matcher the matcher for the search
	 * @return the String according with the group, empty if there's no match
	 */
	private String getGroup(int index, Matcher matcher) {
		if (matcher.matches())
			return matcher.group(index);
		return "";
	}

	/**
	 * checks if the name starts with "decals/"
	 * 
	 * @param name the name being checked
	 * @return the name complete with the starting folder
	 */
	private String checkPathConsistency(String name) {
		if (name.startsWith("decals/"))
			return name;
		return "decals/" + name;
	}

}
