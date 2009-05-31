package net.sf.openrocket.startup;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.JOptionPane;


/**
 * A startup class that checks that a suitable JRE environment is being run.
 * If the environment is too old the execution is canceled, and if OpenJDK is being
 * used warns the user of problems and confirms whether to continue.
 * <p>
 * Note:  This class must be Java 1.4 compatible and calls the next class using
 * only reflection.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Startup {
	
	public static final String START_CLASS = "net.sf.openrocket.gui.main.BasicFrame";
	
	public static final int REQUIRED_MAJOR_VERSION = 1;
	public static final int REQUIRED_MINOR_VERSION = 6;


	public static void main(String[] args) {
		
		checkVersion();
		checkHead();
		checkOpenJDK();
		
		// Load and execute START_CLASS
		try {
			
			Class cls = Class.forName(START_CLASS);
			Method m = cls.getMethod("main", String[].class);
			m.invoke(null, new Object[] { args });
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			error("Error starting main class!", "Please report a bug.");
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			error("Error starting main class!", "Please report a bug.");
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			error("Error starting main class!", "Please report a bug.");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			error("Error starting main class!", "Please report a bug.");
		}

	}
	
	
	/**
	 * Check that the JRE version is high enough.
	 */
	private static void checkVersion() {
		String[] version = System.getProperty("java.specification.version", "").split("\\.");
		
		String jreName = System.getProperty("java.vm.name", "(unknown)");
		String jreVersion = System.getProperty("java.runtime.version", "(unknown)");
		String jreVendor = System.getProperty("java.vendor", "(unknown)");
		
		int major, minor;

		try {
			major = Integer.parseInt(version[0]);
			minor = Integer.parseInt(version[1]);
			
			if (major < REQUIRED_MAJOR_VERSION || 
					(major == REQUIRED_MAJOR_VERSION && minor < REQUIRED_MINOR_VERSION)) {
				error("Java SE version 6 is required to run OpenRocket.",
						"You are currently running " + jreName + " version " +
						jreVersion + " by " + jreVendor);
			}
			
		} catch (RuntimeException e) {
			
			confirm("The Java version in use could not be detected.",
					"OpenRocket requires at least Java SE 6.",
					"Continue anyway?");
			
		}
		
	}

	
	/**
	 * Check that the JRE is not running headless.
	 */
	private static void checkHead() {
		
		if (GraphicsEnvironment.isHeadless()) {
			System.err.println();
			System.err.println("OpenRocket cannot currently be run without the graphical " +
					"user interface.");
			System.err.println();
			System.exit(1);
		}
		
	}
	
	
	/**
	 * Check whether OpenJDK is being used, and if it is warn the user about
	 * problems and confirm whether to continue.
	 */
	private static void checkOpenJDK() {
		
		if (System.getProperty("java.runtime.name", "").toLowerCase().indexOf("icedtea")>=0 ||
				System.getProperty("java.vm.name", "").toLowerCase().indexOf("openjdk")>=0) {

			String jreName = System.getProperty("java.vm.name", "(unknown)");
			String jreVersion = System.getProperty("java.runtime.version", "(unknown)");
			String jreVendor = System.getProperty("java.vendor", "(unknown)");

			confirm("OpenJDK is known to have problems running OpenRocket.",
					" ",
					"You are currently running " + jreName + " version " +
					jreVersion + " by " + jreVendor,
					"Do you want to continue?");
			
		}
	}

	
	
	
	/**
	 * Presents an error message to the user and exits the application.
	 * 
	 * @param message	an array of messages to present.
	 */
	private static void error(String ... message) {

		System.err.println();
		System.err.println("Error starting OpenRocket:");
		System.err.println();
		for (int i=0; i < message.length; i++) {
			System.err.println(message[i]);
		}
		System.err.println();

		
		if (!GraphicsEnvironment.isHeadless()) {

			JOptionPane.showMessageDialog(null, message, "Error starting OpenRocket",
					JOptionPane.ERROR_MESSAGE);
			
		}
		
		System.exit(1);
	}
	
	
	/**
	 * Presents the user with a message dialog and asks whether to continue.
	 * If the user does not select "Yes" the the application exits.
	 * 
	 * @param message	the message Strings to show.
	 */
	private static void confirm(String ... message) {

		if (!GraphicsEnvironment.isHeadless()) {
			
			if (JOptionPane.showConfirmDialog(null, message, "Error starting OpenRocket",
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				System.exit(1);
			}
			
		}

	}
	
	
}
