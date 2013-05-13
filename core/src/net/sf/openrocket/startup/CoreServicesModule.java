package net.sf.openrocket.startup;

import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.formatting.RocketDescriptorImpl;
import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.l10n.Translator;

import com.google.inject.AbstractModule;

public class CoreServicesModule extends AbstractModule {
	
	@Override
	protected void configure() {
		bind(Preferences.class).to(SwingPreferences.class);
		bind(Translator.class).toProvider(TranslatorProvider.class);
		bind(RocketDescriptor.class).to(RocketDescriptorImpl.class);
	}
	
}
