package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.RowFilter;
import javax.swing.table.TableModel;

import net.sf.openrocket.database.motor.ThrustCurveMotorSet;
import net.sf.openrocket.motor.Manufacturer;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;

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
	
	// configuration data used in the filter process
	private final ThrustCurveMotorDatabaseModel model;
	private final Double diameter;
	private List<ThrustCurveMotor> usedMotors = new ArrayList<ThrustCurveMotor>();
	
	// things which can be changed to modify filter behavior
	private List<String> searchTerms = Collections.<String> emptyList();
	private DiameterFilterControl diameterControl = DiameterFilterControl.ALL;
	private boolean hideUsedMotors = false;
	private List<Manufacturer> excludedManufacturers = new ArrayList<Manufacturer>();
	
	public MotorRowFilter(MotorMount mount, ThrustCurveMotorDatabaseModel model) {
		super();
		this.model = model;
		if (mount != null) {
			this.diameter = mount.getMotorMountDiameter();
			for (MotorConfiguration m : mount.getMotorConfiguration()) {
				this.usedMotors.add((ThrustCurveMotor) m.getMotor());
			}
		} else {
			this.diameter = null;
		}
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
	
	void setHideUsedMotors(boolean hideUsedMotors) {
		this.hideUsedMotors = hideUsedMotors;
	}
	
	void setExcludedManufacturers(List<Manufacturer> excludedManufacturers) {
		this.excludedManufacturers.clear();
		this.excludedManufacturers.addAll(excludedManufacturers);
	}
	
	@Override
	public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
		int index = entry.getIdentifier();
		ThrustCurveMotorSet m = model.getMotorSet(index);
		return filterManufacturers(m) && filterUsed(m) && filterByDiameter(m) && filterByString(m);
	}
	
	private boolean filterManufacturers(ThrustCurveMotorSet m) {
		if (excludedManufacturers.contains(m.getManufacturer())) {
			return false;
		} else {
			return true;
		}
	}
	
	private boolean filterUsed(ThrustCurveMotorSet m) {
		if (!hideUsedMotors) {
			return true;
		}
		for (ThrustCurveMotor motor : usedMotors) {
			if (m.matches(motor)) {
				return false;
			}
		}
		return true;
	}
	
	private boolean filterByDiameter(ThrustCurveMotorSet m) {
		if (diameter == null) {
			return true;
		}
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
	
	
	private boolean filterByString(ThrustCurveMotorSet m) {
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
