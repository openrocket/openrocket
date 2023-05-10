package net.sf.openrocket.startup.jij;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * A URL handler for classpath:// URLs.
 * 
 * From http://stackoverflow.com/questions/861500/url-to-load-resources-from-the-classpath-in-java
 */
public class ClasspathUrlStreamHandler extends URLStreamHandler {
	
	/** The classloader to find resources from. */
	private final ClassLoader classLoader;
	
	public ClasspathUrlStreamHandler() {
		this.classLoader = getClass().getClassLoader();
	}
	
	public ClasspathUrlStreamHandler(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	@Override
	protected URLConnection openConnection(URL u) throws IOException {
		final URL resourceUrl = classLoader.getResource(u.getPath());
		return resourceUrl.openConnection();
	}
	
}
