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
import net.sf.openrocket.startup.providers.APIBlockingMotorDatabaseProvider;
import net.sf.openrocket.startup.providers.TranslatorProvider;

import com.google.inject.Scopes;

public class APIGuiModule extends GuiModule {

	private final ComponentPresetDatabaseLoader presetLoader = new ComponentPresetDatabaseLoader();
	private final MotorDatabaseLoader motorLoader = new MotorDatabaseLoader();
	
	@Override
	protected void configure() {
				
		bind(Preferences.class).to(SwingPreferences.class).in(Scopes.SINGLETON);
		bind(Translator.class).toProvider(TranslatorProvider.class).in(Scopes.SINGLETON);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class).in(Scopes.SINGLETON);
		bind(WatchService.class).to(WatchServiceImpl.class).in(Scopes.SINGLETON);
		
		BlockingComponentPresetDatabaseProvider componentDatabaseProvider = new BlockingComponentPresetDatabaseProvider(presetLoader);
		bind(ComponentPresetDao.class).toProvider(componentDatabaseProvider).in(Scopes.SINGLETON);
		
		APIBlockingMotorDatabaseProvider motorDatabaseProvider = new APIBlockingMotorDatabaseProvider(motorLoader);
		bind(ThrustCurveMotorSetDatabase.class).toProvider(motorDatabaseProvider).in(Scopes.SINGLETON);
		bind(MotorDatabase.class).toProvider(motorDatabaseProvider).in(Scopes.SINGLETON);
	}
	
	@Override
	public void startLoader() {
		presetLoader.startLoading();
		motorLoader.startLoading();
	}
}
