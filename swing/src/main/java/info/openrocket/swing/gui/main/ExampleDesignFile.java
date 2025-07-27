package info.openrocket.swing.gui.main;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import info.openrocket.core.util.BugException;
import info.openrocket.core.util.JarUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExampleDesignFile implements Comparable<ExampleDesignFile> {
	
	private final static Logger logger = LoggerFactory.getLogger(ExampleDesignFile.class);
	
	private final URL url;
	private final String name;
	
	private ExampleDesignFile(URL url, String name) {
		this.url = url;
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public URL getURL() {
		return url;
	}
	
	@Override
	public int compareTo(ExampleDesignFile o) {
		return this.name.compareTo(o.name);
	}
	
	public static ExampleDesignFile[] getExampleDesigns() {
		
		ExampleDesignFile[] designs = getJarFileNames();
		if (designs == null || designs.length == 0) {
			logger.debug("Cannot find jar file, trying to load from directory");
			designs = getDirFileNames();
		}

		if (designs == null ){
			return new ExampleDesignFile[0];
		}

		if( 0 < designs.length ) {
			Arrays.sort(designs);
		}
		
		return designs;
	}
	
	private static final String DIRECTORY = "datafiles/examples/";
	private static final String PATTERN = ".*\\.[oO][rR][kK]$";
	private static final FilenameFilter FILTER = new FilenameFilter() {
		@Override
		public boolean accept(File dir, String name) {
			return name.matches(PATTERN);
		}
	};

	private static ExampleDesignFile[] getDirFileNames() {
		
		// Try to find directory as a system resource
		File dir;
		URL url = ClassLoader.getSystemResource(DIRECTORY);
		
		logger.debug("Loading example from {} ", url);
		if (url.getProtocol().equals("jar")) {
			List<ExampleDesignFile> designFiles = new ArrayList<>();
			try {
				// Loaded from inside a JAR
				String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
				JarFile jar = new JarFile(jarPath);
				Enumeration<JarEntry> entries = jar.entries();
				while (entries.hasMoreElements()) {
					JarEntry entry = entries.nextElement();
					String name = entry.getName();
					if (name.startsWith(DIRECTORY) && name.endsWith(".ork")) {
						URL entryUrl = new URL("jar:file:" + jarPath + "!/" + name);
						designFiles.add(new ExampleDesignFile(entryUrl, name.substring(name.lastIndexOf('/') + 1, name.length() - 4)));
					}
				}
				jar.close();
				return designFiles.toArray(new ExampleDesignFile[0]);
			} catch (IOException e) {
				logger.error("IOException when processing jarFile", e);
				return null;
			}
		}

		try {
			dir = JarUtil.urlToFile(url);
		} catch (Exception e1) {
			dir = new File(DIRECTORY);
		}
		logger.debug("Directory to search is: {}", dir);
		// Get the list of files
		File[] files = dir.listFiles(FILTER);
		if (files == null) {
			logger.debug("No files found in directory");
			return null;
		}
		
		ExampleDesignFile[] designs = new ExampleDesignFile[files.length];
		
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			try {
				designs[i] = new ExampleDesignFile(files[i].toURI().toURL(),
						name.substring(0, name.length() - 4));
			} catch (MalformedURLException e) {
				throw new BugException(e);
			}
		}
		return designs;
	}
	
	
	
	private static ExampleDesignFile[] getJarFileNames() {
		
		ArrayList<ExampleDesignFile> list = new ArrayList<>();
		int dirLength = DIRECTORY.length();
		
		// Find and open the jar file this class is contained in
		File file = JarUtil.getCurrentJarFile();
		logger.debug("Current jar file is: {}", file);
		if (file == null)
			return null;
		
		
		// Generate URL pointing to JAR file
		URL fileUrl;
		try {
			fileUrl = file.toURI().toURL();
		} catch (MalformedURLException e1) {
			logger.error("Unable to transform file name {} to URL", file, e1);
			throw new BugException(e1);
		}
		
		// Iterate over JAR entries searching for designs
		try (JarFile jarFile = new JarFile(file)) {

			// Loop through JAR entries searching for files to load
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.startsWith(DIRECTORY) && FILTER.accept(null, name)) {
					String urlName = "jar:" + fileUrl + "!/" + name;
					URL url = new URL(urlName);
					list.add(new ExampleDesignFile(url,
							name.substring(dirLength, name.length() - 4)));
				}
			}

		} catch (IOException e) {
			logger.error("IOException when processing jarFile", e);
			// Could be normal condition if not package in JAR
			return null;
		}
		
		return list.toArray(new ExampleDesignFile[0]);
	}
	
}