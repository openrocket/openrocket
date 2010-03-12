package net.sf.openrocket.gui.main;

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * A FileFilter similar to FileNameExtensionFilter except that
 * it allows multipart extensions (.ork.gz).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SimpleFileFilter extends FileFilter {

	private final String description;
	private final String[] extensions;
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param description	the description of this file filter.
	 * @param extensions	an array of extensions that match this filter.
	 */
	public SimpleFileFilter(String description, String ... extensions) {
		this.description = description;
		this.extensions = new String[extensions.length];
		for (int i=0; i<extensions.length; i++) {
			String ext = extensions[i].toLowerCase();
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
			return true;
		
		String filename = file.getName();
		filename = filename.toLowerCase();
		for (String ext: extensions) {
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
