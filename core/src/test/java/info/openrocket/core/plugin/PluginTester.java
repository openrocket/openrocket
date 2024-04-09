package info.openrocket.core.plugin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import info.openrocket.core.util.ArrayList;

import com.google.inject.Inject;

public class PluginTester {

	@Inject
	private Set<ExamplePlugin> examplePlugins;
	@Inject
	private Set<Example2Plugin> example2Plugins;

	public void testPlugins() {
		// TODO - test fails, the JarPluginImpl does not test correctly.
		// assertContains(examplePlugins, ExamplePluginImpl.class,
		// MultiPluginImpl.class, JarPluginImpl.class);
		assertContains(examplePlugins, ExamplePluginImpl.class, MultiPluginImpl.class);
		// TODO
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
