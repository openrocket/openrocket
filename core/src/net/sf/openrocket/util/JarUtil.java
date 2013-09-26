package net.sf.openrocket.util;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;

import net.sf.openrocket.database.Database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JarUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(JarUtil.class);
	
	/**
	 * Return the a File object pointing to the JAR file that this class belongs to,
	 * or <code>null</code> if it cannot be found.
	 * 
	 * @return	a File object of the current Java archive, or <code>null</code>
	 */
	public static File getCurrentJarFile() {
		// Find the jar file this class is contained in
		
		URL jarUrl = null;
		
		CodeSource codeSource;
		try {
			codeSource = new URL("rsrc:.").openConnection().getClass().getProtectionDomain().getCodeSource();
			logger.debug("Found jar file using rsrc URL");
		} catch (Throwable e) {
			codeSource = Database.class.getProtectionDomain().getCodeSource();
		}
		
		if (codeSource != null) {
			logger.debug("Found jar file using codeSource");
			jarUrl = codeSource.getLocation();
		}
		
		if (jarUrl == null) {
			try {
				ClassLoader cl = Thread.currentThread().getContextClassLoader();
				Enumeration<URL> urls = cl.getResources("README.TXT");
				if (!urls.hasMoreElements()) {
					return null;
				}
				URL readmeURL = urls.nextElement();
				jarUrl = readmeURL;
				String urlString = readmeURL.toString();
				// cut off the trailing path component:
				urlString = urlString.substring(0, urlString.length() - "!/README.TXT".length());
				// cut off the prefix jar:
				urlString = urlString.substring("jar:".length());
				logger.debug("jar file location using README.TXT is {}", urlString);
				jarUrl = new URL(urlString);
				logger.debug("Found jar file using README.TXT location");
			} catch (IOException e1) {
			}
		}
		if (jarUrl == null) {
			return null;
		}
		
		File file = urlToFile(jarUrl);
		if (file.isFile())
			return file;
		return null;
	}
	
	public static File urlToFile(URL url) {
		URI uri;
		try {
			uri = url.toURI();
		} catch (URISyntaxException e) {
			try {
				uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(),
						url.getPort(), url.getPath(), url.getQuery(), url.getRef());
			} catch (URISyntaxException e1) {
				throw new IllegalArgumentException("Broken URL: " + url);
			}
		}
		return new File(uri);
	}
	
	
}
