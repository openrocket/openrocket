package net.sf.openrocket.plugin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.sf.openrocket.util.ArrayList;

import com.google.inject.Inject;

public class PluginTester {
	
	@Inject
	private Set<ExamplePlugin> examplePlugins;
	@Inject
	private Set<Example2Plugin> example2Plugins;
	
	
	public void testPlugins() {
		assertContains(examplePlugins, ExamplePluginImpl.class, MultiPluginImpl.class, JarPluginImpl.class);
		assertContains(example2Plugins, MultiPluginImpl.class);
	}
	
	
	private void assertContains(Set<?> set, Class<?>... classes) {
		assertEquals(classes.length, set.size());
		
		List<Class<?>> list = new ArrayList<Class<?>>(Arrays.asList(classes));
		for (Object o : set) {
			Class<?> c = o.getClass();
			assertTrue(list.contains(c));
			list.remove(c);
		}
	}
}
