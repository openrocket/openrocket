package net.sf.openrocket.gui.help.tours;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.sf.openrocket.util.BugException;

/**
 * Class that loads a slide set from a file.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class SlideSetLoader {
	
	private static final Pattern NEW_SLIDE_PATTERN = Pattern.compile("^\\[(.*)\\]$");
	
	private final String baseDir;
	private TextLineReader source;
	private Locale locale;
	
	


	/**
	 * Constructor.
	 * 
	 * @param baseDir	The base directory from which to load from.  It is prepended to the loaded
	 * 					file names and image file names.
	 */
	public SlideSetLoader(String baseDir) {
		this(baseDir, Locale.getDefault());
	}
	
	
	/**
	 * Constructor.
	 * 
	 * @param baseDir	The base directory from which to load from.  It is prepended to the loaded
	 * 					file names and image file names.
	 * @param locale	The locale for which the files are loaded.
	 */
	public SlideSetLoader(String baseDir, Locale locale) {
		if (baseDir.length() > 0 && !baseDir.endsWith("/")) {
			baseDir = baseDir + "/";
		}
		this.baseDir = baseDir;
		this.locale = locale;
	}
	
	
	/**
	 * Load a slide set from a file.  The base directory is prepended to the
	 * file name first.
	 * 
	 * @param filename		the file to read in the base directory.
	 * @return				the slide set
	 */
	public SlideSet load(String filename) throws IOException {
		String file = baseDir + filename;
		InputStream in = getLocalizedFile(file);
		
		try {
			InputStreamReader reader = new InputStreamReader(in, "UTF-8");
			return load(reader);
		} finally {
			in.close();
		}
	}
	
	
	private InputStream getLocalizedFile(String filename) throws IOException {
		for (String file : generateLocalizedFiles(filename)) {
			InputStream in = ClassLoader.getSystemResourceAsStream(file);
			if (in != null) {
				return in;
			}
		}
		throw new FileNotFoundException("File '" + filename + "' not found.");
	}
	
	private List<String> generateLocalizedFiles(String filename) {
		String base, ext;
		int index = filename.lastIndexOf('.');
		if (index >= 0) {
			base = filename.substring(0, index);
			ext = filename.substring(index);
		} else {
			base = filename;
			ext = "";
		}
		

		List<String> list = new ArrayList<String>();
		list.add(base + "_" + locale.getLanguage() + "_" + locale.getCountry() + "_" + locale.getVariant() + ext);
		list.add(base + "_" + locale.getLanguage() + "_" + locale.getCountry() + ext);
		list.add(base + "_" + locale.getLanguage() + ext);
		list.add(base + ext);
		return list;
	}
	
	
	/**
	 * Load slide set from a reader.
	 * 
	 * @param reader	the reader to read from.
	 * @return			the slide set.
	 */
	public SlideSet load(Reader reader) throws IOException {
		source = new TextLineReader(reader);
		
		// Read title and description
		String title = source.next();
		StringBuilder desc = new StringBuilder();
		while (!nextLineStartsSlide()) {
			if (desc.length() > 0) {
				desc.append('\n');
			}
			desc.append(source.next());
		}
		
		// Create the slide set
		SlideSet set = new SlideSet();
		set.setTitle(title);
		set.setDescription(desc.toString());
		

		// Read the slides
		while (source.hasNext()) {
			Slide s = readSlide();
			set.addSlide(s);
		}
		
		return set;
	}
	
	
	private Slide readSlide() {
		
		String imgLine = source.next();
		Matcher matcher = NEW_SLIDE_PATTERN.matcher(imgLine);
		if (!matcher.matches()) {
			throw new BugException("Line did not match new slide pattern: " + imgLine);
		}
		
		String imageFile = matcher.group(1);
		
		StringBuffer desc = new StringBuffer();
		while (source.hasNext() && !nextLineStartsSlide()) {
			if (desc.length() > 0) {
				desc.append('\n');
			}
			desc.append(source.next());
		}
		
		return new Slide(baseDir + imageFile, desc.toString());
	}
	
	

	private boolean nextLineStartsSlide() {
		return NEW_SLIDE_PATTERN.matcher(source.peek()).matches();
	}
	

}
