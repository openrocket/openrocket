package net.sf.openrocket.optimization.rocketoptimization;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ChangeSource;

/**
 * An interface what modifies a single parameter in a rocket simulation
 * based on a double value in the range [0...1].
 * <p>
 * The implementation must fire change events when the minimum and maximum ranges
 * are modified, NOT when the actual modified value changes.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public interface SimulationModifier extends ChangeSource {
	
	/**
	 * Return a short name describing this modifier.
	 * @return	a name describing this modifier.
	 */
	public String getName();
	
	/**
	 * Return a longer description describing this modifiers.
	 * @return	a description of the modifier.
	 */
	public String getDescription();
	
	/**
	 * Return the object this modifier is related to.  This is for example the
	 * rocket component this modifier is modifying.  This object can be used by a
	 * UI to group related modifiers.
	 * 
	 * @return	the object this modifier is related to, or <code>null</code>.
	 */
	public Object getRelatedObject();
	
	
	/**
	 * Return the current value of the modifier in SI units.
	 * @return	the current value of this parameter in SI units.
	 * @throws OptimizationException	if fetching the current value fails
	 */
	public double getCurrentSIValue(Simulation simulation) throws OptimizationException;
	
	
	/**
	 * Return the minimum value (corresponding to scaled value 0) in SI units.
	 * @return	the value corresponding to scaled value 0.
	 */
	public double getMinValue();
	
	/**
	 * Set the minimum value (corresponding to scaled value 0) in SI units.
	 * @param value	the value corresponding to scaled value 0.
	 */
	public void setMinValue(double value);
	
	
	/**
	 * Return the maximum value (corresponding to scaled value 1) in SI units.
	 * @return	the value corresponding to scaled value 1.
	 */
	public double getMaxValue();
	
	/**
	 * Set the maximum value (corresponding to scaled value 1) in SI units.
	 * @param value	the value corresponding to scaled value 1.
	 */
	public void setMaxValue(double value);
	
	
	/**
	 * Return the unit group used for the values returned by {@link #getCurrentSIValue(Simulation)} etc.
	 * @return	the unit group
	 */
	public UnitGroup getUnitGroup();
	
	
	/**
	 * Return the current scaled value.  This is normally within the range [0...1], but
	 * can be outside the range if the current value is outside of the min and max values.
	 * 
	 * @return	the current value of this parameter (normally between [0 ... 1])
	 * @throws OptimizationException 	if fetching the current value fails
	 */
	public double getCurrentScaledValue(Simulation simulation) throws OptimizationException;
	
	
	
	/**
	 * Modify the specified simulation to the corresponding parameter value.
	 * 
	 * @param simulation	the simulation to modify
	 * @param scaledValue	the scaled value in the range [0...1]
	 * @throws OptimizationException 	if the modification fails
	 */
	public void modify(Simulation simulation, double scaledValue) throws OptimizationException;
	
	/**
	 * Called once at the start of the optimization.
	 * This method can be used to ensure the simulation or rocket are properly configured.
	 * 
	 * @param simulation
	 */
	public void initialize(Simulation simulation) throws OptimizationException;
	
	/**
	 * Compare whether this SimulationModifier is equivalent to another simulation modifier.
	 * "Equivalent" means that the simulation modifier corresponds to the same modification in
	 * another rocket instance (e.g. the same modification on another rocket component that
	 * has the same component ID).
	 */
	@Override
	public boolean equals(Object obj);
}
