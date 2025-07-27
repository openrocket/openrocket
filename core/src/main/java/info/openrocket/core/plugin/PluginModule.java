package info.openrocket.core.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

/**
 * Guice module definition that searches for plugins in a list of provided
 * JAR files and registers each found plugin to the corresponding plugin
 * interface.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class PluginModule extends AbstractModule {

	private final Map<Class<?>, Multibinder<?>> binders = new HashMap<>();
	private final AnnotationFinder finder = new AnnotationFinderImpl();

	@SuppressWarnings("unchecked")
	@Override
	protected void configure() {
		List<Class<?>> classes = finder.findAnnotatedTypes(Plugin.class);
		List<Class<?>> interfaces = new ArrayList<>();
		List<Class<?>> unusedInterfaces;

		// Find plugin interfaces
		for (Class<?> c : classes) {
			if (c.isInterface()) {
				interfaces.add(c);
			}
		}
		unusedInterfaces = new ArrayList<>(interfaces);

		// Find plugin implementations
		for (Class<?> c : classes) {
			if (c.isInterface())
				continue;

			for (Class<?> intf : interfaces) {
				if (intf.isAssignableFrom(c)) {
					// Ugly hack to enable dynamic binding... Can this be done type-safely?
					Multibinder<Object> binder = (Multibinder<Object>) findBinder(intf);
					binder.addBinding().to(c);
					unusedInterfaces.remove(intf);
				}
			}
		}

		// TODO: Unused plugin interfaces should be bound to an empty set - how?
	}

	private Multibinder<?> findBinder(Class<?> intf) {
		Multibinder<?> binder = binders.get(intf);
		if (binder == null) {
			binder = Multibinder.newSetBinder(binder(), intf);
			binders.put(intf, binder);
		}
		return binder;
	}

}
