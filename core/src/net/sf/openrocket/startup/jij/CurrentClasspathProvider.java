package net.sf.openrocket.startup.jij;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class CurrentClasspathProvider implements ClasspathProvider {
	
	@Override
	public List<URL> getUrls() {
		List<URL> urls = new ArrayList<URL>();
		
		String classpath = System.getProperty("java.class.path");
		String[] cps = classpath.split(File.pathSeparator);
		
		for (String cp : cps) {
			try {
				urls.add(new File(cp).toURI().toURL());
			} catch (MalformedURLException e) {
				System.err.println("Error initializing classpath " + e);
			}
		}
		
		return urls;
	}
	
}
