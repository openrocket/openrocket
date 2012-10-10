package net.sf.openrocket.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Guice module definition that searches for plugins in a list of provided
 * JAR files and registers each found plugin to the corresponding plugin interface.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class PluginModule extends AbstractModule {
	
	private final File[] jars;
	private final ClassLoader classLoader;
	
	private Map<Class<?>, Multibinder<?>> binders = new HashMap<Class<?>, Multibinder<?>>();
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param jars			the JAR files to search for plugins
	 * @param classLoader	the class loader used to load classes from the JAR files
	 */
	public PluginModule(File[] jars, ClassLoader classLoader) {
		this.jars = jars.clone();
		this.classLoader = classLoader;
	}
	
	
	@Override
	protected void configure() {
		for (File jar : jars) {
			List<String> classNames = readClassNames(jar);
			for (String className : classNames) {
				checkForPlugin(className);
			}
		}
	}
	
	
	
	@SuppressWarnings("unchecked")
	private void checkForPlugin(String className) {
		try {
			
			Class<?> c = classLoader.loadClass(className);
			for (Class<?> intf : c.getInterfaces()) {
				System.out.println("Testing class " + c + " interface " + intf);
				
				if (isPluginInterface(intf)) {
					System.out.println("BINDING");
					// Ugly hack to enable dynamic binding...  Can this be done type-safely?
					Multibinder<Object> binder = (Multibinder<Object>) findBinder(intf);
					binder.addBinding().to(c);
				}
			}
			
		} catch (ClassNotFoundException e) {
			System.err.println("Could not load class " + className + ": " + e);
		}
	}
	
	
	private boolean isPluginInterface(Class<?> intf) {
		return intf.isAnnotationPresent(Plugin.class);
	}
	
	
	private Multibinder<?> findBinder(Class<?> intf) {
		Multibinder<?> binder = binders.get(intf);
		if (binder == null) {
			binder = Multibinder.newSetBinder(binder(), intf);
			binders.put(intf, binder);
		}
		return binder;
	}
	
	
	private List<String> readClassNames(File jar) {
		List<String> classNames = new ArrayList<String>();
		
		JarFile file = null;
		try {
			file = new JarFile(jar);
			Enumeration<JarEntry> entries = file.entries();
			while (entries.hasMoreElements()) {
				JarEntry entry = entries.nextElement();
				String name = entry.getName();
				if (name.toLowerCase().endsWith(".class") && !name.contains("$")) {
					name = name.substring(0, name.length() - 6);
					name = name.replace('/', '.');
					classNames.add(name);
				}
			}
		} catch (IOException e) {
			System.err.println("Error reading JAR file " + jar);
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException e) {
					// Curse all checked exceptions...
				}
			}
		}
		
		return classNames;
	}
	
}
