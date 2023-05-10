package net.sf.openrocket.utils;

import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.formatting.RocketDescriptorImpl;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.startup.providers.TranslatorProvider;

import com.google.inject.AbstractModule;

public class CoreServicesModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(Preferences.class).to(SwingPreferences.class);
		bind(Translator.class).toProvider(TranslatorProvider.class);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
	}
	
}
