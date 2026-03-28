package info.openrocket.swing.gui.scalefigure.caliper.snap;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.Transformation;
import info.openrocket.swing.gui.scalefigure.caliper.CaliperManager;
import info.openrocket.swing.gui.scalefigure.RocketPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Registry for component snap providers using ServiceLoader pattern.
 * Similar to RocketComponentShapeProvider, finds the appropriate snap provider
 * for a given component class.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class CaliperSnapRegistry {
	
	private static final Logger log = LoggerFactory.getLogger(CaliperSnapRegistry.class);
	
	private static CaliperSnapRegistry instance;
	private final ServiceLoader<ComponentSnapProvider> loader;
	
	private CaliperSnapRegistry() {
		loader = ServiceLoader.load(ComponentSnapProvider.class);
	}
	
	/**
	 * Get the singleton instance of the registry.
	 */
	public static CaliperSnapRegistry getInstance() {
		if (instance == null) {
			instance = new CaliperSnapRegistry();
		}
		return instance;
	}
	
	/**
	 * Get snap targets for a component in the given view type and caliper mode.
	 *
	 * @param component the component to get snap targets for
	 * @param viewType the current view type
	 * @param caliperMode the current caliper mode
	 * @param transformation the transformation from component-local to absolute coordinates
	 * @return list of snap targets, or empty list if no provider found
	 */
	public List<CaliperSnapTarget> getSnapTargets(RocketComponent component,
												  RocketPanel.VIEW_TYPE viewType,
												  CaliperManager.CaliperMode caliperMode,
												  Transformation transformation) {
		ComponentSnapProvider provider = findSnapProvider(component);
		if (provider == null) {
			return new ArrayList<>();
		}

		return provider.getSnapTargets(component, viewType, caliperMode, transformation);
	}
	
	/**
	 * Find the appropriate snap provider for a component class.
	 * Searches up the class hierarchy to find a matching provider.
	 */
	private ComponentSnapProvider findSnapProvider(RocketComponent component) {
		Class<?> componentClass = component.getClass();

		// Reload the ServiceLoader to ensure it picks up all providers
		// ServiceLoader is lazy and may not have loaded providers yet
		loader.reload();

		while (componentClass != null && componentClass != Object.class) {
			for (ComponentSnapProvider provider : loader) {
				if (provider.getComponentClass().equals(componentClass)) {
					return provider;
				}
			}
			componentClass = componentClass.getSuperclass();
		}
		
		log.debug("findSnapProvider: no provider found for component={}, class={}", 
				component.getName(), component.getClass().getName());
		return null;
	}
}

