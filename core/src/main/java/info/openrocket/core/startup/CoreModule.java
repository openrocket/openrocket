package info.openrocket.core.startup;

import info.openrocket.core.database.ComponentPresetDatabase;
import info.openrocket.core.database.motor.MotorDatabase;
import info.openrocket.core.database.motor.ThrustCurveMotorSetDatabase;
import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.formatting.RocketDescriptorImpl;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.preferences.ApplicationPreferences;
import info.openrocket.core.database.ComponentPresetDatabaseLoader;
import info.openrocket.core.database.MotorDatabaseLoader;
import info.openrocket.core.startup.providers.TranslatorProvider;
import info.openrocket.core.startup.providers.CoreApplicationPreferencesProvider;
import info.openrocket.core.startup.providers.CoreComponentPresetDatabaseProvider;
import info.openrocket.core.startup.providers.CoreMotorDatabaseProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * CoreModule is the Guice Module for the OpenRocket core functionality.
 * This module provides the basic dependencies needed to use OpenRocket core
 * functionality in external applications without the Swing GUI.
 * 
 * Usage:
 * <code>
 * CoreModule coreModule = new CoreModule();
 * Injector injector = Guice.createInjector(coreModule, new PluginModule());
 * Application.setInjector(injector);
 * coreModule.startLoader();
 * </code>
 * 
 * @author OpenRocket Team
 */
public class CoreModule extends AbstractModule {
	
	private final ComponentPresetDatabaseLoader presetLoader = new ComponentPresetDatabaseLoader();
	private final MotorDatabaseLoader motorLoader = new MotorDatabaseLoader();
	
	public CoreModule() {
	}
	
	@Override
	protected void configure() {
		bind(ApplicationPreferences.class).toProvider(CoreApplicationPreferencesProvider.class).in(Scopes.SINGLETON);
		bind(Translator.class).toProvider(TranslatorProvider.class).in(Scopes.SINGLETON);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class).in(Scopes.SINGLETON);
		
		CoreComponentPresetDatabaseProvider componentDatabaseProvider = new CoreComponentPresetDatabaseProvider(presetLoader);
		bind(ComponentPresetDatabase.class).toProvider(componentDatabaseProvider).in(Scopes.SINGLETON);
		
		CoreMotorDatabaseProvider motorDatabaseProvider = new CoreMotorDatabaseProvider(motorLoader);
		bind(ThrustCurveMotorSetDatabase.class).toProvider(motorDatabaseProvider).in(Scopes.SINGLETON);
		bind(MotorDatabase.class).to(ThrustCurveMotorSetDatabase.class).in(Scopes.SINGLETON);
	}
	
	/**
	 * startLoader must be called after the Injector created with this module is registered
	 * in the Application object. This starts loading the database data in the background.
	 */
	public void startLoader() {
		boolean bypassPresets = System.getProperty("openrocket.bypass.presets") != null;
		boolean bypassMotors = System.getProperty("openrocket.bypass.motors") != null;

		if (!bypassPresets) {
			presetLoader.startLoading();
		} else {
			presetLoader.markAsLoaded();
		}
		if (!bypassMotors) {
			motorLoader.startLoading();
		} else {
			motorLoader.markAsLoaded();
		}
	}
}