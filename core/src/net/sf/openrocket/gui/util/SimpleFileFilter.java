package net.sf.openrocket.gui.util;

import java.io.File;
import java.util.Locale;

import javax.swing.filechooser.FileFilter;

/**
 * A FileFilter similar to FileNameExtensionFilter except that
 * it allows multipart extensions (.ork.gz), and also implements
 * the java.io.FileFilter interface.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimpleFileFilter extends FileFilter implements java.io.FileFilter {
	
	private final String description;
	private final boolean acceptDir;
	private final String[] extensions;
	
	
	/**
	 * Create filter that accepts files with the provided extensions that
	 * accepts directories as well.
	 * 
	 * @param description	the description of this file filter.
	 * @param extensions	an array of extensions that match this filter.
	 */
	public SimpleFileFilter(String description, String... extensions) {
		this(description, true, extensions);
	}
	
	public String[] getExtensions() {
		return extensions;
	}
	
	/**
	 * Create filter that accepts files with the provided extensions.
	 * 
	 * @param description	the description of this file filter.
	 * @param acceptDir		whether to accept directories
	 * @param extensions	an array of extensions that match this filter.
	 */
	public SimpleFileFilter(String description, boolean acceptDir, String... extensions) {
		this.description = description;
		this.acceptDir = acceptDir;
		this.extensions = new String[extensions.length];
		for (int i = 0; i < extensions.length; i++) {
			String ext = extensions[i].toLowerCase(Locale.ENGLISH);
			if (ext.charAt(0) == '.') {
				this.extensions[i] = ext;
			} else {
				this.extensions[i] = '.' + ext;
			}
		}
	}
	
	
	@Override
	public boolean accept(File file) {
		if (file == null)
			return false;
		if (file.isDirectory())
			return acceptDir;
		
		String filename = file.getName();
		filename = filename.toLowerCase(Locale.ENGLISH);
		for (String ext : extensions) {
			if (filename.endsWith(ext))
				return true;
		}
		
		return false;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
	
}
