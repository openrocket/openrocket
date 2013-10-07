package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.util.ArrayList;
import java.util.Collection;
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

	// configuration data used in the filter process
	private final ThrustCurveMotorDatabaseModel model;
	private List<ThrustCurveMotor> usedMotors = new ArrayList<ThrustCurveMotor>();

	// things which can be changed to modify filter behavior

	private Double maximumLength;

	// Limit motors based on minimum diameter
	private Double minimumDiameter;
	
	private Double maximumDiameter;

	// Collection of strings which match text in the motor
	private List<String> searchTerms = Collections.<String> emptyList();

	// Boolean which hides motors in the usedMotors list
	private boolean hideUsedMotors = false;

	// List of manufacturers to exclude.
	private List<Manufacturer> excludedManufacturers = new ArrayList<Manufacturer>();

	// Impulse class filtering
	private ImpulseClass minimumImpulse;
	private ImpulseClass maximumImpulse;
	

	public MotorRowFilter(ThrustCurveMotorDatabaseModel model) {
		super();
		this.model = model;
	}

	public void setMotorMount( MotorMount mount ) {
		if (mount != null) {
			for (MotorConfiguration m : mount.getMotorConfiguration()) {
				this.usedMotors.add((ThrustCurveMotor) m.getMotor());
			}
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

	Double getMaximumLength() {
		return maximumLength;
	}

	void setMaximumLength(Double maximumLength) {
		this.maximumLength = maximumLength;
	}

	Double getMinimumDiameter() {
		return minimumDiameter;
	}

	void setMinimumDiameter(Double minimumDiameter) {
		this.minimumDiameter = minimumDiameter;
	}

	Double getMaximumDiameter() {
		return maximumDiameter;
	}

	void setMaximumDiameter(Double maximumDiameter) {
		this.maximumDiameter = maximumDiameter;
	}

	void setHideUsedMotors(boolean hideUsedMotors) {
		this.hideUsedMotors = hideUsedMotors;
	}

	List<Manufacturer> getExcludedManufacturers() {
		return excludedManufacturers;
	}

	void setExcludedManufacturers(Collection<Manufacturer> excludedManufacturers) {
		this.excludedManufacturers.clear();
		this.excludedManufacturers.addAll(excludedManufacturers);
	}

	ImpulseClass getMinimumImpulse() {
		return minimumImpulse;
	}

	void setMinimumImpulse(ImpulseClass minimumImpulse) {
		this.minimumImpulse = minimumImpulse;
	}

	ImpulseClass getMaximumImpulse() {
		return maximumImpulse;
	}

	void setMaximumImpulse(ImpulseClass maximumImpulse) {
		this.maximumImpulse = maximumImpulse;
	}

	@Override
	public boolean include(RowFilter.Entry<? extends TableModel, ? extends Integer> entry) {
		int index = entry.getIdentifier();
		ThrustCurveMotorSet m = model.getMotorSet(index);
		return filterManufacturers(m) && filterUsed(m) && filterBySize(m) && filterByString(m) && filterByImpulseClass(m);
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

	private boolean filterBySize(ThrustCurveMotorSet m) {

		if ( minimumDiameter != null ) {
			if ( m.getDiameter() <= minimumDiameter - 0.0015 ) {
				return false;
			}
		}

		if ( maximumDiameter != null ) {
			if ( m.getDiameter() >= maximumDiameter + 0.0004 ) {
				return false;
			}
		}
		
		if ( maximumLength != null ) {
			if ( m.getLength() > maximumLength ) {
				return false;
			}
		}
		
		return true;
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

	private boolean filterByImpulseClass(ThrustCurveMotorSet m) {
		if ( minimumImpulse != null ) {
			if( m.getTotalImpuse() < minimumImpulse.getLow() ) {
				return false;
			}
		}
		
		if ( maximumImpulse != null ) {
			if( m.getTotalImpuse() > maximumImpulse.getHigh() ) {
				return false;
			}
		}

		return true;
	}

}
