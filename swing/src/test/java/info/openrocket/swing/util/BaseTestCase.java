package info.openrocket.swing.util;

import info.openrocket.swing.ServicesForTesting;
import info.openrocket.core.l10n.DebugTranslator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.startup.Application;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import org.junit.jupiter.api.BeforeAll;

public class BaseTestCase {
	@BeforeAll
	public static void setUp() throws Exception {
		Module applicationModule = new ServicesForTesting();
		Module debugTranslator = new AbstractModule() {

			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new DebugTranslator(null));
			}

		};
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator),
				pluginModule);
		Application.setInjector(injector);
	}
}

