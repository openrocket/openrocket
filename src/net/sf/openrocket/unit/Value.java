package net.sf.openrocket.unit;

import net.sf.openrocket.util.MathUtil;

/**
 * An immutable class representing an SI value and a unit.  The toString() method yields the
 * current value in the current units.  This class may be used to encapsulate
 * a sortable value for example for tables.  The sorting is performed by the
 * value in the current units, ignoring the unit.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Value implements Comparable<Value> {
	
	private final double value;
	private final Unit unit;
	
	
	/**
	 * Create a new Value object.
	 * 
	 * @param value		the value to set.
	 * @param unit		the unit to set (<code>null</code> not allowed)
	 */
	public Value(double value, Unit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("unit is null");
		}
		this.value = value;
		this.unit = unit;
	}
	
	
	/**
	 * Creates a new Value object using unit group.  Currently it simply uses the default
	 * unit of the group, but may later change.
	 * 
	 * @param value		the value to set.
	 * @param group		the group the value belongs to.
	 */
	public Value(double value, UnitGroup group) {
		this(value, group.getDefaultUnit());
	}
	
	
	/**
	 * Get the value of this object (in SI units).
	 * 
	 * @return the value
	 */
	public double getValue() {
		return value;
	}
	
	

	/**
	 * Get the value of this object in the current units.
	 * 
	 * @return	the value in the current units.
	 */
	public double getUnitValue() {
		return unit.toUnit(value);
	}
	
	
	/**
	 * Get the unit of this object.
	 * 
	 * @return the unit
	 */
	public Unit getUnit() {
		return unit;
	}
	
	
	/**
	 * Return a string formatted using the {@link Unit#toStringUnit(double)} method
	 * of the current unit.  If the unit is <code>null</code> then the UNITS_NONE
	 * group is used.
	 */
	@Override
	public String toString() {
		return unit.toStringUnit(value);
	}
	
	

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Value other = (Value) obj;
		if (this.unit != other.unit) {
			return false;
		}
		
		if (!MathUtil.equals(this.value, other.value)) {
			return false;
		}
		
		return true;
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}
	
	
	/**
	 * Compare this value to another value.  The comparison is performed primarily by
	 * the unit text, secondarily the value in the unit values.
	 */
	@Override
	public int compareTo(Value o) {
		int n = this.getUnit().getUnit().compareTo(o.getUnit().getUnit());
		if (n != 0)
			return n;
		
		double us = this.getUnitValue();
		double them = o.getUnitValue();
		
		if (Double.isNaN(us)) {
			if (Double.isNaN(them))
				return 0;
			else
				return 1;
		}
		if (Double.isNaN(them))
			return -1;
		
		if (us < them)
			return -1;
		else if (us > them)
			return 1;
		else
			return 0;
	}
	
}
