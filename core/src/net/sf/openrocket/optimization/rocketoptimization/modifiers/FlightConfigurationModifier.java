package net.sf.openrocket.optimization.rocketoptimization.modifiers;

import java.util.Locale;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.rocketcomponent.FlightConfigurableParameter;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.Reflection.Method;

public class FlightConfigurationModifier<E extends FlightConfigurableParameter<E>> extends GenericModifier<E> {
	
	private final Class<? extends RocketComponent> componentClass;
	private final String componentId;
	private final Method configGetter;
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param modifierName			the name of this modifier (returned by {@link #getName()})
	 * @param modifierDescription	the description of this modifier (returned by {@link #getDescription()})
	 * @param relatedObject			the related object (returned by {@link #getRelatedObject()})
	 * @param unitGroup				the unit group (returned by {@link #getUnitGroup()})
	 * @param multiplier			the multiplier by which the value returned by the getter is multiplied
	 * 								to obtain the desired value
	 * @param componentClass		the RocketComponent class type that is being modified
	 * @param componentId			the ID of the component to modify
	 * @param configName            the name of the configuration object (base name of the getter)
	 * @param flightConfigClass     the class of the FlightConfigurableParameter
	 * @param methodName			the base name of the getter/setter methods (without "get"/"set")
	 */
	public FlightConfigurationModifier(
			String modifierName,
			String modifierDescription,
			Object relatedObject,
			UnitGroup unitGroup,
			double multiplier,
			Class<? extends RocketComponent> componentClass,
			String componentId,
			String configName,
			Class<E> flightConfigClass,
			String methodName) {
		super(modifierName, modifierDescription, relatedObject, unitGroup, multiplier, flightConfigClass, methodName);
		
		this.componentClass = componentClass;
		this.componentId = componentId;
		
		try {
			configName = configName.substring(0, 1).toUpperCase(Locale.ENGLISH) + configName.substring(1);
			configGetter = new Method(componentClass.getMethod("get" + configName));
		} catch (SecurityException e) {
			throw new BugException("Trying to find method get/set" + configName + " in class " + componentClass, e);
		} catch (NoSuchMethodException e) {
			throw new BugException("Trying to find method get/set" + configName + " in class " + componentClass, e);
		}
		
	}
	
	
	@Override
	protected E getModifiedObject(Simulation simulation) throws OptimizationException {
		
		RocketComponent c = simulation.getRocket().findComponent(componentId);
		if (c == null) {
			throw new OptimizationException("Could not find component of type " + componentClass.getSimpleName()
					+ " with correct ID");
		}
		
		FlightConfiguration<E> configs = (FlightConfiguration<E>) configGetter.invoke(c);
		return configs.get(simulation.getConfiguration().getFlightConfigurationID());
	}
	
}
