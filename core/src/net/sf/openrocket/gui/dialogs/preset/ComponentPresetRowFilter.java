package net.sf.openrocket.gui.dialogs.preset;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import net.sf.openrocket.unit.Value;

public class ComponentPresetRowFilter extends RowFilter<TableModel, Object> {
	
	private final double value;
	private final int column;
	// FIXME - what should epsilon be?
	private final double epsilon = .0002;
	
	ComponentPresetRowFilter( double value, int column ) {
		this.value = value;
		this.column = column;
	}

	@Override
	public boolean include( RowFilter.Entry<? extends TableModel, ? extends Object> entry) {
		Object o = entry.getValue(column);
		if ( o instanceof Value ) {
			Value v = (Value)o;
			return Math.abs( value - v.getValue() ) < epsilon;
		}
		if ( o instanceof Double ) {
			Double d = (Double) o;
			return Math.abs( value - d ) < epsilon;
		}
		return true;
	}

}
