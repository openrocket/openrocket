package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;

import java.util.ServiceLoader;

public class RocketComponentShapeProvider {
	private static RocketComponentShapeProvider provider;
	private final ServiceLoader<RocketComponentShapeService> loader;

	private RocketComponentShapeProvider() {
		// Do not use the current thread's context class loader here.  AWT and native
		// rendering transitions may initialize this singleton on a thread whose context
		// loader cannot see OpenRocket's service registrations.  Use the service type's
		// application loader so built-in shapes (and the bootstrapped plugin classpath)
		// are resolved consistently.
		loader = ServiceLoader.load(RocketComponentShapeService.class,
				RocketComponentShapeService.class.getClassLoader());
	}

	public static RocketComponentShapeProvider getInstance() {
		if (provider == null) {
			provider = new RocketComponentShapeProvider();
		}
		return provider;
	}

	public static RocketComponentShapes[] getShapesSide(RocketComponent component, Transformation transformation) {
		RocketComponentShapeService service = findShapeService(component);
		return service.getShapesSide(component, transformation);
	}

	public static RocketComponentShapes[] getShapesBack(RocketComponent component, Transformation transformation) {
		RocketComponentShapeService service = findShapeService(component);
		return service.getShapesBack(component, transformation);
	}

	private static RocketComponentShapeService findShapeService(RocketComponent component) {
		RocketComponentShapeProvider provider = getInstance();
		ServiceLoader<RocketComponentShapeService> loader = provider.loader;
		Class<?> componentClass = component.getClass();

		while (componentClass != null && componentClass != Object.class) {
			for (RocketComponentShapeService service : loader) {
				if (service.getShapeClass().equals(componentClass)) {
					return service;
				}
			}
			componentClass = componentClass.getSuperclass(); // Move to the superclass if no provider found
		}

		throw new IllegalArgumentException("No suitable shape provider found for component: " + component);
	}
}
