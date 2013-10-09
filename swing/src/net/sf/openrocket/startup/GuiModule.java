package net.sf.openrocket.startup;

import net.sf.openrocket.database.ComponentPresetDao;
import net.sf.openrocket.database.ComponentPresetDatabaseLoader;
import net.sf.openrocket.database.MotorDatabaseLoader;
import net.sf.openrocket.database.motor.MotorDatabase;
import net.sf.openrocket.database.motor.ThrustCurveMotorSetDatabase;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.formatting.RocketDescriptorImpl;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.gui.watcher.WatchService;
import net.sf.openrocket.gui.watcher.WatchServiceImpl;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.providers.BlockingComponentPresetDatabaseProvider;
import net.sf.openrocket.startup.providers.BlockingMotorDatabaseProvider;
import net.sf.openrocket.startup.providers.TranslatorProvider;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

/**
 * GuiModule is the Guice Module for the OpenRocket Swing application.
 * 
 * Because the swing application does not fully utilize injection of dependencies,
 * proper use of this module requires a little more work on the part of the startup code.
 * 
 * <code>
 * GuiModule module = new GuiModule();
 * Application.setInjector( Guice.createInjector(guiModule));
 * module.startLoading();
 * </code>
 * 
 * @author kruland
 *
 */
public class GuiModule extends AbstractModule {
	
	private final ComponentPresetDatabaseLoader presetLoader = new ComponentPresetDatabaseLoader();
	private final MotorDatabaseLoader motorLoader = new MotorDatabaseLoader();
	
	
	public GuiModule() {
	}
	
	@Override
	protected void configure() {
		
		bind(Preferences.class).to(SwingPreferences.class).in(Scopes.SINGLETON);
		bind(Translator.class).toProvider(TranslatorProvider.class).in(Scopes.SINGLETON);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class).in(Scopes.SINGLETON);
		bind(WatchService.class).to(WatchServiceImpl.class).in(Scopes.SINGLETON);
		
		BlockingComponentPresetDatabaseProvider componentDatabaseProvider = new BlockingComponentPresetDatabaseProvider(presetLoader);
		bind(ComponentPresetDao.class).toProvider(componentDatabaseProvider).in(Scopes.SINGLETON);
		
		BlockingMotorDatabaseProvider motorDatabaseProvider = new BlockingMotorDatabaseProvider(motorLoader);
		bind(ThrustCurveMotorSetDatabase.class).toProvider(motorDatabaseProvider).in(Scopes.SINGLETON);
		bind(MotorDatabase.class).toProvider(motorDatabaseProvider).in(Scopes.SINGLETON);
		
	}
	
	/**
	 * startLoader must be called after the Injector created with this module is registered
	 * in the Application object.  This is because loading the database data requires the Application
	 * object's locator methods to return the correct objects.
	 */
	public void startLoader() {
		presetLoader.startLoading();
		motorLoader.startLoading();
	}
	
}
