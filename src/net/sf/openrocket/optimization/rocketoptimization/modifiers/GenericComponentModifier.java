package net.sf.openrocket.optimization.rocketoptimization.modifiers;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;

/**
 * A generic simulation modifier that modifies a value of a certain RocketComponent
 * based on the component's ID and the value name.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class GenericComponentModifier extends GenericModifier<RocketComponent> {
	
	private final Class<? extends RocketComponent> componentClass;
	private final String componentId;
	
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
	 * @param methodName			the base name of the getter/setter methods (without "get"/"set")
	 */
	public GenericComponentModifier(String modifierName, String modifierDescription, Object relatedObject, UnitGroup unitGroup,
			double multiplier, Class<? extends RocketComponent> componentClass, String componentId, String methodName) {
		super(modifierName, modifierDescription, relatedObject, unitGroup, multiplier, componentClass, methodName);
		
		this.componentClass = componentClass;
		this.componentId = componentId;
	}
	
	@Override
	protected RocketComponent getModifiedObject(Simulation simulation) throws OptimizationException {
		RocketComponent c = simulation.getRocket().findComponent(componentId);
		if (c == null) {
			throw new OptimizationException("Could not find component of type " + componentClass.getSimpleName()
					+ " with correct ID");
		}
		return c;
	}
	
}
