package net.sf.openrocket.gui.dialogs.preset;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.MathUtil;

public class ComponentPresetRowFilter extends RowFilter<TableModel, Object> {

	private Boolean bvalue = false;
	private double dvalue = 0.0;
	private double epsilon = 0.0;
	private final int column;
	
	ComponentPresetRowFilter(double value, int column) {
		this.dvalue = value;
		this.column = column;
		/*
		 * Accept 5% difference, but at least 1mm.
		 */
		this.epsilon = MathUtil.max(value * 0.05, 0.001);
	}

	ComponentPresetRowFilter(Boolean value, int column) {
		this.bvalue = value;
		this.column = column;
	}
	
	
	@Override
	public boolean include(RowFilter.Entry<? extends TableModel, ? extends Object> entry) {
		Object o = entry.getValue(column);
		if (o instanceof Value) {
			Value v = (Value) o;
			return Math.abs(dvalue - v.getValue()) < epsilon;
		}
		
		if (o instanceof Double) {
			Double d = (Double) o;
			return Math.abs(dvalue - d) < epsilon;
		}
		
		if (o instanceof Boolean) {
			Boolean b = (Boolean) o;
			return b.equals(bvalue);
		}
		
		return true;
	}
	
}
