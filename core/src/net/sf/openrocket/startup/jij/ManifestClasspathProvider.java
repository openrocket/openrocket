package net.sf.openrocket.startup.jij;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import net.sf.openrocket.util.BugException;

public class ManifestClasspathProvider implements ClasspathProvider {
	
	private static final String MANIFEST_ATTRIBUTE = "Classpath-Jars";
	
	@Override
	public List<URL> getUrls() {
		try {
			List<String> manifest = readManifestLine(MANIFEST_ATTRIBUTE);
			
			List<URL> urls = new ArrayList<URL>();
			for (String s : manifest) {
				parseManifestLine(urls, s);
			}
			
			return urls;
		} catch (IOException e) {
			throw new BugException(e);
		}
	}
	
	
	private List<String> readManifestLine(String name) throws IOException {
		List<String> lines = new ArrayList<String>();
		
		Enumeration<URL> resources = getClass().getClassLoader().getResources("META-INF/MANIFEST.MF");
		
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			InputStream stream = url.openStream();
			Manifest manifest = new Manifest(stream);
			stream.close();
			
			Attributes attr = manifest.getMainAttributes();
			if (attr == null) {
				continue;
			}
			
			String value = attr.getValue(name);
			if (value == null) {
				continue;
			}
			
			lines.add(value);
		}
		return lines;
	}
	
	
	
	private void parseManifestLine(List<URL> urls, String manifest) throws MalformedURLException {
		String[] array = manifest.split("\\s");
		for (String s : array) {
			if (s.length() > 0) {
				if (getClass().getClassLoader().getResource(s) != null) {
					urls.add(new URL("classpath:" + s));
				} else {
					System.err.println("Library " + s + " not found on classpath, ignoring.");
				}
			}
		}
	}
	
}
