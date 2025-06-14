package info.openrocket.swing.startup;

import java.net.URL;

import info.openrocket.core.startup.jij.ClasspathUrlStreamHandler;
import info.openrocket.core.startup.jij.ConfigurableStreamHandlerFactory;
import info.openrocket.core.startup.jij.CurrentClasspathProvider;
import info.openrocket.core.startup.providers.JarInJarStarter;
import info.openrocket.core.startup.jij.ManifestClasspathProvider;
import info.openrocket.core.startup.jij.PluginClasspathProvider;

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
	
	private static final String STARTUP_CLASS = "info.openrocket.swing.startup.SwingStartup";
	
	public static void main(String[] args) {
		// Set OSX-specific properties
		OSXSetup.setupOSXProperties();

		// This property works around some fundamental bugs in TimSort in the java library which has had known issues
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
