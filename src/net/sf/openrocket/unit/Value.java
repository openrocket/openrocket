package net.sf.openrocket.unit;

/**
 * A class representing an SI value and a unit.  The toString() method yields the
 * current value in the current units.  This class may be used to encapsulate
 * a sortable value for example for tables.  The sorting is performed by the
 * value in the current units, ignoring the unit.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Value implements Comparable<Value> {
	
	private double value;
	private Unit unit;
	
	
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
	 * Get the value of this object.
	 * 
	 * @return the value
	 */
	public double getValue() {
		return value;
	}

	/**
	 * Set the value of this object.
	 * 
	 * @param value the value to set
	 */
	public void setValue(double value) {
		this.value = value;
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
	 * Set the value of this object in the current units.
	 * 
	 * @param value		the value in current units.
	 */
	public void setUnitValue(double value) {
		this.value = unit.fromUnit(value);
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
	 * Set the value of this object.
	 * 
	 * @param unit the unit to set (<code>null</code> not allowed)
	 */
	public void setUnit(Unit unit) {
		if (unit == null) {
			throw new IllegalArgumentException("unit is null");
		}
		this.unit = unit;
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
