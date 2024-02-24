package info.openrocket.swing.gui.rocketfigure;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;

import java.util.ServiceLoader;

public class RocketComponentShapeProvider {
	private static RocketComponentShapeProvider provider;
	private final ServiceLoader<RocketComponentShapeService> loader;

	private RocketComponentShapeProvider() {
		loader = ServiceLoader.load(RocketComponentShapeService.class);
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
