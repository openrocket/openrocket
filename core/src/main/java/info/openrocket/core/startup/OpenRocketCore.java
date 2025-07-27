package info.openrocket.core.startup;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import info.openrocket.core.plugin.PluginModule;

/**
 * Helper class to initialize OpenRocket core functionality for external applications.
 * This class provides a simple way to bootstrap the OpenRocket core module without
 * the Swing GUI dependencies.
 * 
 * Example usage:
 * <pre>
 * // Initialize OpenRocket core
 * OpenRocketCore.initialize();
 * 
 * // Now you can use OpenRocket core functionality
 * Translator translator = Application.getTranslator();
 * ApplicationPreferences prefs = Application.getPreferences();
 * MotorDatabase motorDb = Application.getMotorSetDatabase();
 * </pre>
 * 
 * @author OpenRocket Team
 */
public class OpenRocketCore {
	
	private static boolean initialized = false;
	private static CoreModule coreModule;
	
	/**
	 * Initialize OpenRocket core with default modules.
	 * This method is safe to call multiple times - subsequent calls will be ignored.
	 */
	public static synchronized void initialize() {
		initialize(new PluginModule());
	}
	
	/**
	 * Initialize OpenRocket core with custom modules.
	 * This method is safe to call multiple times - subsequent calls will be ignored.
	 * 
	 * @param additionalModules additional Guice modules to include
	 */
	public static synchronized void initialize(Module... additionalModules) {
		if (initialized) {
			return;
		}
		
		// Create the core module
		coreModule = new CoreModule();
		
		// Create array of all modules
		Module[] allModules = new Module[additionalModules.length + 1];
		allModules[0] = coreModule;
		System.arraycopy(additionalModules, 0, allModules, 1, additionalModules.length);
		
		// Create and set the injector
		Injector injector = Guice.createInjector(allModules);
		Application.setInjector(injector);
		
		// Start loading databases
		coreModule.startLoader();
		
		initialized = true;
	}
	
	/**
	 * Check if OpenRocket core has been initialized.
	 * 
	 * @return true if initialized, false otherwise
	 */
	public static boolean isInitialized() {
		return initialized;
	}
	
	/**
	 * Get the core module instance.
	 * Only available after initialization.
	 * 
	 * @return the core module instance
	 * @throws IllegalStateException if not initialized
	 */
	public static CoreModule getCoreModule() {
		if (!initialized) {
			throw new IllegalStateException("OpenRocket core not initialized. Call initialize() first.");
		}
		return coreModule;
	}
}