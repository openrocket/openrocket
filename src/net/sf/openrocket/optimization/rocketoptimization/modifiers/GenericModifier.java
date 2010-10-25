package net.sf.openrocket.optimization.rocketoptimization.modifiers;

import javax.swing.event.ChangeListener;

import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.optimization.rocketoptimization.SimulationModifier;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;
import net.sf.openrocket.util.MathUtil;
import net.sf.openrocket.util.Reflection.Method;

public class GenericModifier implements SimulationModifier {
	
	private final String name;
	private final Object relatedObject;
	private final UnitGroup unitGroup;
	private final double multiplier;
	private final Object modifiable;
	
	private final Method getter;
	private final Method setter;
	
	private double minValue;
	private double maxValue;
	
	



	public GenericModifier(String modifierName, Object relatedObject, UnitGroup unitGroup, double multiplier,
			Object modifiable, String methodName) {
		this.name = modifierName;
		this.relatedObject = relatedObject;
		this.unitGroup = unitGroup;
		this.multiplier = multiplier;
		this.modifiable = modifiable;
		
		if (MathUtil.equals(multiplier, 0)) {
			throw new IllegalArgumentException("multiplier is zero");
		}
		
		try {
			methodName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1);
			getter = new Method(modifiable.getClass().getMethod("get" + methodName));
			setter = new Method(modifiable.getClass().getMethod("set" + methodName, double.class));
		} catch (SecurityException e) {
			throw new BugException("Trying to find method get/set" + methodName + " in class " + modifiable.getClass(), e);
		} catch (NoSuchMethodException e) {
			throw new BugException("Trying to find method get/set" + methodName + " in class " + modifiable.getClass(), e);
		}
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
	public double getCurrentValue() {
		return ((Double) getter.invoke(modifiable)) * multiplier;
	}
	
	
	@Override
	public double getCurrentScaledValue() {
		double value = getCurrentValue();
		return toScaledValue(value);
	}
	
	@Override
	public void modify(Simulation simulation, double scaledValue) {
		double siValue = toBaseValue(scaledValue) / multiplier;
		setter.invoke(modifiable, siValue);
	}
	
	
	/**
	 * Returns the scaled value (normally within [0...1]).
	 */
	private double toScaledValue(double value) {
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
	 */
	private double toBaseValue(double value) {
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
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void removeChangeListener(ChangeListener listener) {
		// TODO Auto-generated method stub
		
	}
	
	
	private void fireChangeEvent() {
		// TODO Auto-generated method stub
		
	}
	
}
