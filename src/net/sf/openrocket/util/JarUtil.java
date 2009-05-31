package net.sf.openrocket.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;

import net.sf.openrocket.database.Database;

public class JarUtil {

	/**
	 * Return the a File object pointing to the JAR file that this class belongs to,
	 * or <code>null</code> if it cannot be found.
	 * 
	 * @return	a File object of the current Java archive, or <code>null</code>
	 */
	public static File getCurrentJarFile() {
		// Find the jar file this class is contained in
		URL jarUrl = null;
		CodeSource codeSource = Database.class.getProtectionDomain().getCodeSource();
		if (codeSource != null)
			jarUrl = codeSource.getLocation();
		
		if (jarUrl == null) {
			return null;
		}
		return urlToFile(jarUrl);
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
