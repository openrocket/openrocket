package net.sf.openrocket.util.BaseTestCase;

import net.sf.openrocket.ServicesForTesting;
import net.sf.openrocket.l10n.DebugTranslator;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.startup.Application;

import org.junit.BeforeClass;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.util.Modules;

public class BaseTestCase {
	
	@BeforeClass
	public static void setUp() throws Exception {
		Module applicationModule = new ServicesForTesting();
		Module debugTranslator = new AbstractModule() {
			
			@Override
			protected void configure() {
				bind(Translator.class).toInstance(new DebugTranslator(null));
			}
			
		};
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(Modules.override(applicationModule).with(debugTranslator), pluginModule);
		Application.setInjector(injector);
	}
}
