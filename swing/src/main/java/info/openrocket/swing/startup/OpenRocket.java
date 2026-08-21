package info.openrocket.swing.startup;

import java.net.URL;

import info.openrocket.core.startup.jij.ClasspathUrlStreamHandler;
import info.openrocket.core.startup.jij.ConfigurableStreamHandlerFactory;
import info.openrocket.core.startup.jij.CurrentClasspathProvider;
import info.openrocket.core.startup.providers.JarInJarStarter;
import info.openrocket.core.startup.jij.ManifestClasspathProvider;
import info.openrocket.core.startup.jij.PluginClasspathProvider;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.arch.SystemInfo.Platform;

/**
 * First step in the OpenRocket startup sequence, responsible for
 * classpath setup.
 * 
 * The startup class sequence is the following:
 *   1. OpenRocket
 *   2. SwingStartup
 * 
 * This class changes the current classpath to contain the jar-in-jar
 * library dependencies and plugins in the current classpath, and
 * then launches the next step of the startup sequence.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class OpenRocket {
	static final String CLASSPATH_READY_PROPERTY = "openrocket.startup.classpath-ready";
	private static final String STARTUP_CLASS = "info.openrocket.swing.startup.SwingStartup";

	public static void main(String[] args) {
		// Set OSX-specific properties
		if (SystemInfo.getPlatform() == Platform.MAC_OS) {
			OSXSetup.setupOSXProperties();
		}

		// This property works around some fundamental bugs in TimSort in the java library which has had known issues
		// since it was introduced in JDK 1.7.  In OpenRocket it manifests when you sort the motors in the motor chooser dialog
		// by designation.
		System.setProperty("java.util.Arrays.useLegacyMergeSort","true");
		addClasspathUrlHandler();

		String previousClasspathReady = System.getProperty(CLASSPATH_READY_PROPERTY);
		System.setProperty(CLASSPATH_READY_PROPERTY, Boolean.TRUE.toString());
		try {
			JarInJarStarter.runMain(STARTUP_CLASS, args, new CurrentClasspathProvider(),
					new ManifestClasspathProvider(), new PluginClasspathProvider());
		} finally {
			restoreClasspathReadyProperty(previousClasspathReady);
		}
	}

	/**
	 * Check whether the first startup stage has prepared the application and plugin classpath.
	 *
	 * @return True when {@link SwingStartup} is running through the bootstrap classloader.
	 */
	static boolean isClasspathReady() {
		return Boolean.parseBoolean(System.getProperty(CLASSPATH_READY_PROPERTY));
	}

	private static void restoreClasspathReadyProperty(String previousValue) {
		if (previousValue == null) {
			System.clearProperty(CLASSPATH_READY_PROPERTY);
		} else {
			System.setProperty(CLASSPATH_READY_PROPERTY, previousValue);
		}
	}
	
	private static void addClasspathUrlHandler() {
		ConfigurableStreamHandlerFactory factory = new ConfigurableStreamHandlerFactory();
		factory.addHandler("classpath", new ClasspathUrlStreamHandler());
		URL.setURLStreamHandlerFactory(factory);
	}
	
}
