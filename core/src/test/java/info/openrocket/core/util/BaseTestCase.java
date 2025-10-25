package info.openrocket.core.util;

import info.openrocket.core.ServicesForTesting;
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
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(pluginModule));
		Application.setInjector(injector);
	}
}
