package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import net.sf.openrocket.database.motor.ThrustCurveMotorSet;

////////  Row filters

/**
 * Abstract adapter class.
 */
class MotorRowFilter extends RowFilter<TableModel, Integer> {
	
	public enum DiameterFilterControl {
		ALL,
		EXACT,
		SMALLER
	};
	
	private final ThrustCurveMotorDatabaseModel model;
	private final double diameter;
	
	private List<String> searchTerms = Collections.<String> emptyList();
	private DiameterFilterControl diameterControl = DiameterFilterControl.ALL;
	
	public MotorRowFilter(ThrustCurveMotorDatabaseModel model, double diameter) {
		super();
		this.model = model;
		this.diameter = diameter;
	}
	
	public void setSearchTerms(final List<String> searchTerms) {
		this.searchTerms = new ArrayList<String>();
		for (String s : searchTerms) {
			s = s.trim().toLowerCase(Locale.getDefault());
			if (s.length() > 0) {
				this.searchTerms.add(s);
			}
		}
	}
	
	void setDiameterControl(DiameterFilterControl diameterControl) {
		this.diameterControl = diameterControl;
	}
	
	@Override
	public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
		int index = entry.getIdentifier();
		ThrustCurveMotorSet m = model.getMotorSet(index);
		return filterByDiameter(m) && filterByString(m);
	}
	
	public boolean filterByDiameter(ThrustCurveMotorSet m) {
		switch (diameterControl) {
		default:
		case ALL:
			return true;
		case EXACT:
			return ((m.getDiameter() <= diameter + 0.0004) && (m.getDiameter() >= diameter - 0.0015));
		case SMALLER:
			return (m.getDiameter() <= diameter + 0.0004);
		}
	}
	
	
	public boolean filterByString(ThrustCurveMotorSet m) {
		main: for (String s : searchTerms) {
			for (ThrustCurveMotorColumns col : ThrustCurveMotorColumns.values()) {
				String str = col.getValue(m).toString().toLowerCase(Locale.getDefault());
				if (str.indexOf(s) >= 0)
					continue main;
			}
			return false;
		}
		return true;
	}
}
