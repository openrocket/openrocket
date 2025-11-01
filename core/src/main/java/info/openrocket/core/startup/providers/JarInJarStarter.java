package info.openrocket.core.startup.providers;

import info.openrocket.core.startup.jij.ClasspathProvider;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

public class JarInJarStarter {
	
	/**
	 * Runs a main class with an alternative classpath.
	 * 
	 * @param mainClass		the class containing the main method to call.
	 * @param args			the arguments to the main method.
	 * @param providers		the classpath sources.
	 */
	public static void runMain(String mainClass, String[] args, ClasspathProvider... providers) {
		List<URL> urls = new ArrayList<>();
		
		for (ClasspathProvider p : providers) {
			urls.addAll(p.getUrls());
		}
		
		if (System.getProperty("openrocket.debug") != null) {
			System.out.println("New classpath:");
			for (URL u : urls) {
				System.out.println("   " + u);
			}
		}
		
		URL[] urlArray = urls.toArray(new URL[0]);
		// Use platform classloader as parent (Java 9+) to ensure access to JDK modules
		// like java.net.http.
		ClassLoader parentLoader = ClassLoader.getPlatformClassLoader();
		ClassLoader loader = new URLClassLoader(urlArray, parentLoader);
		try {
			Thread.currentThread().setContextClassLoader(loader);
			Class<?> c = Class.forName(mainClass, true, loader);
			Method m = c.getMethod("main", args.getClass());
			m.invoke(null, (Object) args);
		} catch (Exception e) {
			throw new RuntimeException("Error starting OpenRocket", e);
		}
		
	}
	
}
