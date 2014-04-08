package net.sf.openrocket.gui.adaptors;

import java.util.Comparator;

import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.unit.ValueComparator;

public abstract class ValueColumn extends Column {

	private final UnitGroup unit;
	
	public ValueColumn( String name, UnitGroup unit ) {
		super(name);
		this.unit = unit;
	}
	
	public ValueColumn( String name, String toolTip, UnitGroup unit ) {
		super( name, toolTip );
		this.unit = unit;
	}
	
	@Override
	public Object getValueAt(int row) {
		Double d = valueAt(row);
		return (d == null ) ? null : new Value( valueAt( row ) , unit );
	}

	@Override
	public Comparator getComparator() {
		return ValueComparator.INSTANCE;
	}

	/**
	 * Returns the double value to show in the Value object
	 * 
	 * If the row index is out of bounds of the model, return null.
	 * 
	 * @param row
	 * @return
	 */
	public abstract Double valueAt( int row );

}
