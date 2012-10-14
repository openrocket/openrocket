package net.sf.openrocket.plugin;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class JIJ {
	
	public static void main(String[] args) throws Exception {
		String cp = System.getProperty("java.class.path");
		String[] cps = cp.split(File.pathSeparator);
		
		URL[] urls = new URL[cps.length + 1];
		for (int i = 0; i < cps.length; i++) {
			urls[i] = new File(cps[i]).toURI().toURL();
		}
		urls[cps.length] = new File("/home/sampo/Projects/OpenRocket/core/example.jar").toURL();
		
		System.out.println("Classpath: " + Arrays.toString(urls));
		
		URLClassLoader loader = new URLClassLoader(urls, null);
		Class<?> c = loader.loadClass("net.sf.openrocket.plugin.Test");
		Method m = c.getMethod("main", args.getClass());
		m.invoke(null, (Object) args);
	}
	
}
