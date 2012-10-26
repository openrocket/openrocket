package net.sf.openrocket.gui.main;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.sf.openrocket.file.iterator.DirectoryIterator;
import net.sf.openrocket.file.iterator.FileIterator;
import net.sf.openrocket.gui.util.SimpleFileFilter;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.JarUtil;
import net.sf.openrocket.util.Pair;

public class ExampleDesignFile  implements Comparable<ExampleDesignFile> {

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
			designs = getDirFileNames();
		}
		if (designs == null || designs.length == 0) {
			return null;
		}

		Arrays.sort(designs);

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

		try {
			dir = JarUtil.urlToFile(url);
		} catch (Exception e1) {
			dir = new File(DIRECTORY);
		}

		// Get the list of files
		File[] files = dir.listFiles(FILTER);
		if (files == null)
			return null;

		ExampleDesignFile[] designs = new ExampleDesignFile[files.length];

		for (int i=0; i<files.length; i++) {
			String name = files[i].getName();
			try {
				designs[i] = new ExampleDesignFile(files[i].toURI().toURL(), 
						name.substring(0, name.length()-4));
			} catch (MalformedURLException e) {
				throw new BugException(e);
			}
		}
		return designs;
	}



	private static ExampleDesignFile[] getJarFileNames() {

		ArrayList<ExampleDesignFile> list = new ArrayList<ExampleDesignFile>();
		int dirLength = DIRECTORY.length();

		// Find and open the jar file this class is contained in
		File file = JarUtil.getCurrentJarFile();
		if (file == null)
			return null;


		// Generate URL pointing to JAR file
		URL fileUrl;
		try {
			fileUrl = file.toURI().toURL();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
			throw new BugException(e1);
		}

		// Iterate over JAR entries searching for designs
		JarFile jarFile = null;
		try {
			jarFile = new JarFile(file);

			// Loop through JAR entries searching for files to load
			Enumeration<JarEntry> entries = jarFile.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.startsWith(DIRECTORY) && FILTER.accept(null, name)) {
					String urlName = "jar:" + fileUrl + "!/" + name;
					URL url = new URL(urlName);
					list.add(new ExampleDesignFile(url, 
							name.substring(dirLength, name.length()-4)));
				}
			}

		} catch (IOException e) {
			// Could be normal condition if not package in JAR
			return null;
		} finally {
			if (jarFile != null) {
				try {
					jarFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return list.toArray(new ExampleDesignFile[0]);
	}

}