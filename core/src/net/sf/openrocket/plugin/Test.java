package net.sf.openrocket.plugin;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;

public class Test {
	
	@Inject
	private Set<ExamplePlugin> impls;
	
	
	public void run() {
		System.out.println("Plugin count: " + impls.size());
		for (ExamplePlugin i : impls) {
			i.doit();
		}
	}
	
	
	public static void main(String[] args) throws MalformedURLException {
		
		File[] jars = { new File("/home/sampo/Projects/OpenRocket/core/example.jar") };
		URL[] urls = { new File("/home/sampo/Projects/OpenRocket/core/example.jar").toURL() };
		URLClassLoader classLoader = new URLClassLoader(urls);
		Injector injector = Guice.createInjector(new PluginModule(jars, classLoader));
		injector.getInstance(Test.class).run();
	}
}
