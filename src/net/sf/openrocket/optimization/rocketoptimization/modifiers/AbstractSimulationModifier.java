package net.sf.openrocket.optimization.rocketoptimization.modifiers;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.general.OptimizationException;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.MathUtil;

/**
 * An abstract implementation of the SimulationModifier interface.  An implementation
 * needs only to implement the {@link #getCurrentSIValue(Simulation)} and
 * {@link #modify(net.sf.openrocket.document.Simulation, double)} methods.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public abstract class AbstractSimulationModifier implements SimulationModifier {
	
	private final String name;
	private final Object relatedObject;
	private final UnitGroup unitGroup;
	
	private double minValue = 0.0;
	private double maxValue = 1.0;
	
	private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
	
	
	/**
	 * Sole constructor.
	 * 
	 * @param modifierName		the name of this modifier (returned by {@link #getName()})
	 * @param relatedObject		the related object (returned by {@link #getRelatedObject()})
	 * @param unitGroup			the unit group (returned by {@link #getUnitGroup()})
	 */
	public AbstractSimulationModifier(String modifierName, Object relatedObject, UnitGroup unitGroup) {
		this.name = modifierName;
		this.relatedObject = relatedObject;
		this.unitGroup = unitGroup;
	}
	
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public Object getRelatedObject() {
		return relatedObject;
	}
	
	@Override
	public double getCurrentScaledValue(Simulation simulation) throws OptimizationException {
		double value = getCurrentSIValue(simulation);
		return toScaledValue(value);
	}
	
	

	/**
	 * Returns the scaled value (normally within [0...1]).  If the min...max range is singular,
	 * this method returns 0.0, 1.0 or 0.5 depending on whether the value is less than,
	 * greater than or equal to the limit.
	 * 
	 * @param value		the value in SI units
	 * @return			the value in scaled range (normally within [0...1])
	 */
	protected double toScaledValue(double value) {
		if (MathUtil.equals(minValue, maxValue)) {
			if (value > maxValue)
				return 1.0;
			if (value < minValue)
				return 0.0;
			return 0.5;
		}
		
		return MathUtil.map(value, minValue, maxValue, 0.0, 1.0);
	}
	
	
	/**
	 * Returns the base value (in SI units).
	 * 
	 * @param value		the value in scaled range (normally within [0...1])
	 * @return			the value in SI units
	 */
	protected double toBaseValue(double value) {
		System.out.println("value=" + value + " minValue=" + minValue + " maxValue=" + maxValue);
		return MathUtil.map(value, 0.0, 1.0, minValue, maxValue);
	}
	
	

	@Override
	public double getMinValue() {
		return minValue;
	}
	
	@Override
	public void setMinValue(double value) {
		if (MathUtil.equals(minValue, value))
			return;
		this.minValue = value;
		if (maxValue < minValue)
			maxValue = minValue;
		fireChangeEvent();
	}
	
	@Override
	public double getMaxValue() {
		return maxValue;
	}
	
	@Override
	public void setMaxValue(double value) {
		if (MathUtil.equals(maxValue, value))
			return;
		this.maxValue = value;
		if (minValue > maxValue)
			minValue = maxValue;
		fireChangeEvent();
	}
	
	@Override
	public UnitGroup getUnitGroup() {
		return unitGroup;
	}
	
	
	@Override
	public void addChangeListener(ChangeListener listener) {
		listeners.add(listener);
	}
	
	@Override
	public void removeChangeListener(ChangeListener listener) {
		listeners.remove(listener);
	}
	
	
	/**
	 * Fire a change event to the listeners.
	 */
	protected void fireChangeEvent() {
		ChangeListener[] array = listeners.toArray(new ChangeListener[0]);
		ChangeEvent event = new ChangeEvent(this);
		for (ChangeListener l : array) {
			l.stateChanged(event);
		}
	}
	
}
