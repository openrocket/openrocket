package net.sf.openrocket.startup;

import java.net.URL;

import net.sf.openrocket.startup.jij.ClasspathUrlStreamHandler;
import net.sf.openrocket.startup.jij.ConfigurableStreamHandlerFactory;
import net.sf.openrocket.startup.jij.CurrentClasspathProvider;
import net.sf.openrocket.startup.jij.JarInJarStarter;
import net.sf.openrocket.startup.jij.ManifestClasspathProvider;
import net.sf.openrocket.startup.jij.PluginClasspathProvider;

/**
 * First step in the OpenRocket startup sequence, responsible for
 * classpath setup.
 * 
 * The startup class sequence is the following:
 *   1. Startup
 *   2. SwingStartup
 * 
 * This class changes the current classpath to contain the jar-in-jar
 * library dependencies and plugins in the current classpath, and
 * then launches the next step of the startup sequence.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenRocket {
	
	private static final String STARTUP_CLASS = "net.sf.openrocket.startup.SwingStartup";
	
	public static void main(String[] args) {
		// This property works around some fundimental bugs in TimSort in the java library which has had known issues
		// since it was introduced in JDK 1.7.  In OpenRocket it manifests when you sort the motors in the motor chooser dialog
		// by designation.
		System.setProperty("java.util.Arrays.useLegacyMergeSort","true");
		addClasspathUrlHandler();
		JarInJarStarter.runMain(STARTUP_CLASS, args, new CurrentClasspathProvider(),
				new ManifestClasspathProvider(), new PluginClasspathProvider());
	}
	
	private static void addClasspathUrlHandler() {
		ConfigurableStreamHandlerFactory factory = new ConfigurableStreamHandlerFactory();
		factory.addHandler("classpath", new ClasspathUrlStreamHandler());
		URL.setURLStreamHandlerFactory(factory);
	}
	
}
