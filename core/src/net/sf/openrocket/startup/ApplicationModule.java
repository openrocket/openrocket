package net.sf.openrocket.startup;

import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.formatting.RocketDescriptorImpl;
import net.sf.openrocket.gui.watcher.WatchService;
import net.sf.openrocket.gui.watcher.WatchServiceImpl;
import net.sf.openrocket.l10n.Translator;

import com.google.inject.AbstractModule;

public class ApplicationModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(Preferences.class).toInstance(Application.getPreferences());
		bind(Translator.class).toInstance(Application.getTranslator());
		bind(WatchService.class).to(WatchServiceImpl.class);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
	}
	
}
