package net.sf.openrocket.startup;

import net.sf.openrocket.logging.LogHelper;

import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(LogHelper.class).toInstance(Application.getLogger());
		bind(Preferences.class).toInstance(Application.getPreferences());
	}
	
}
