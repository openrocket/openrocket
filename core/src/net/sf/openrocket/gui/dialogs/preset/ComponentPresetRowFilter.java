package net.sf.openrocket.gui.dialogs.preset;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import net.sf.openrocket.unit.Value;
import net.sf.openrocket.util.MathUtil;

public class ComponentPresetRowFilter extends RowFilter<TableModel, Object> {
	
	private final double value;
	private final int column;
	private final double epsilon;
	
	ComponentPresetRowFilter(double value, int column) {
		this.value = value;
		this.column = column;
		/*
		 * Accept 5% difference, but at least 1mm.
		 */
		this.epsilon = MathUtil.max(value * 0.05, 0.001);
	}
	
	@Override
	public boolean include(RowFilter.Entry<? extends TableModel, ? extends Object> entry) {
		Object o = entry.getValue(column);
		if (o instanceof Value) {
			Value v = (Value) o;
			return Math.abs(value - v.getValue()) < epsilon;
		}
		if (o instanceof Double) {
			Double d = (Double) o;
			return Math.abs(value - d) < epsilon;
		}
		return true;
	}
	
}
