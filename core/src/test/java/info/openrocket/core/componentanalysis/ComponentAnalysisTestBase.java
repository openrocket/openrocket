package info.openrocket.core.componentanalysis;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import info.openrocket.core.ServicesForTesting;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.OpenRocketDocumentFactory;
import info.openrocket.core.plugin.PluginModule;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.startup.Application;

/**
 * Common test setup for component analysis tests.
 */
abstract class ComponentAnalysisTestBase {

	static {
		Module servicesModule = new ServicesForTesting();
		Module pluginModule = new PluginModule();
		Injector injector = Guice.createInjector(servicesModule, pluginModule);
		Application.setInjector(injector);
	}

	protected Rocket createRocket() {
		OpenRocketDocument document = OpenRocketDocumentFactory.createNewRocket();
		return document.getRocket();
	}
}
