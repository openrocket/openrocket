package info.openrocket.core.plugin;

import info.openrocket.core.ServicesForTesting;

import org.junit.jupiter.api.Test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

/**
 * Test the plugin loading system using Guice.
 * 
 * This is more of an integration than a unit test. It uses the
 * PluginModule to load a Guice injector, and then verifies that it
 * has found the appropriate plugins.
 */
public class PluginTest {

	@Test
	public void testPluginModule() {

		Module applicationModule = new ServicesForTesting();

		Injector injector = Guice.createInjector(applicationModule, new PluginModule());
		PluginTester tester = injector.getInstance(PluginTester.class);
		tester.testPlugins();

	}

}
