package info.openrocket.core.optimization.rocketoptimization.modifiers;

import java.util.Locale;
import java.util.UUID;

import info.openrocket.core.document.Simulation;
import info.openrocket.core.optimization.general.OptimizationException;
import info.openrocket.core.rocketcomponent.FlightConfigurableParameter;
import info.openrocket.core.rocketcomponent.FlightConfigurableParameterSet;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.BugException;
import info.openrocket.core.util.Reflection.Method;

public class FlightConfigurationModifier<E extends FlightConfigurableParameter<E>> extends GenericModifier<E> {

	private final Class<? extends RocketComponent> componentClass;
	private final UUID componentId;
	private final Method configGetter;

	/**
	 * Sole constructor.
	 * 
	 * @param modifierName        the name of this modifier (returned by
	 *                            {@link #getName()})
	 * @param modifierDescription the description of this modifier (returned by
	 *                            {@link #getDescription()})
	 * @param relatedObject       the related object (returned by
	 *                            {@link #getRelatedObject()})
	 * @param unitGroup           the unit group (returned by
	 *                            {@link #getUnitGroup()})
	 * @param multiplier          the multiplier by which the value returned by the
	 *                            getter is multiplied
	 *                            to obtain the desired value
	 * @param componentClass      the RocketComponent class type that is being
	 *                            modified
	 * @param componentId         the ID of the component to modify
	 * @param configName          the name of the configuration object (base name of
	 *                            the getter)
	 * @param flightConfigClass   the class of the FlightConfigurableParameter
	 * @param methodName          the base name of the getter/setter methods
	 *                            (without "get"/"set")
	 */
	public FlightConfigurationModifier(
			String modifierName,
			String modifierDescription,
			Object relatedObject,
			UnitGroup unitGroup,
			double multiplier,
			Class<? extends RocketComponent> componentClass,
			UUID componentId,
			String configName,
			Class<E> flightConfigClass,
			String methodName) {
		super(modifierName, modifierDescription, relatedObject, unitGroup, multiplier, flightConfigClass, methodName);

		this.componentClass = componentClass;
		this.componentId = componentId;

		try {
			configName = configName.substring(0, 1).toUpperCase(Locale.ENGLISH) + configName.substring(1);
			configGetter = new Method(componentClass.getMethod("get" + configName));
		} catch (SecurityException | NoSuchMethodException e) {
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

		FlightConfigurableParameterSet<E> configSet = (FlightConfigurableParameterSet<E>) configGetter.invoke(c);
		return configSet.get(simulation.getFlightConfigurationId());
	}

}
