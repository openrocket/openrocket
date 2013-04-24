package net.sf.openrocket;

import net.sf.openrocket.gui.util.SwingPreferences;
import net.sf.openrocket.plugin.PluginModule;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.ApplicationModule;

import org.junit.BeforeClass;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;


public abstract class AbstractBaseApplication {
	
	
	@BeforeClass
	public static void setUp() throws Exception {
		Application.setInjector(initializeGuice());
	}
	
	private static Injector initializeGuice() {
		Application.setPreferences(new SwingPreferences());
		
		Module applicationModule = new ApplicationModule();
		Module pluginModule = new PluginModule();
		
		return Guice.createInjector(applicationModule, pluginModule);
	}
	
}
