package info.openrocket.swing.utils;

import info.openrocket.core.formatting.RocketDescriptor;
import info.openrocket.core.formatting.RocketDescriptorImpl;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Preferences;
import info.openrocket.swing.startup.providers.TranslatorProvider;

import com.google.inject.AbstractModule;

public class CoreServicesModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(Preferences.class).to(SwingPreferences.class);
		bind(Translator.class).toProvider(TranslatorProvider.class);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
	}
	
}
