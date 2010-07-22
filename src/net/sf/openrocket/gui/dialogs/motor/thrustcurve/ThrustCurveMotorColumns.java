package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.text.Collator;
import java.util.Comparator;

import net.sf.openrocket.database.ThrustCurveMotorSet;
import net.sf.openrocket.motor.DesignationComparator;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.unit.ValueComparator;

/**
 * Enum defining the table columns.
 */
enum ThrustCurveMotorColumns {
	MANUFACTURER("Manufacturer", 100) {
		@Override
		public String getValue(ThrustCurveMotorSet m) {
			return m.getManufacturer().getDisplayName();
		}
		
		@Override
		public Comparator<?> getComparator() {
			return Collator.getInstance();
		}
	},
	DESIGNATION("Designation") {
		@Override
		public String getValue(ThrustCurveMotorSet m) {
			return m.getDesignation();
		}
		
		@Override
		public Comparator<?> getComparator() {
			return new DesignationComparator();
		}
	},
	TYPE("Type") {
		@Override
		public String getValue(ThrustCurveMotorSet m) {
			return m.getType().getName();
		}
		
		@Override
		public Comparator<?> getComparator() {
			return Collator.getInstance();
		}
	},
	DIAMETER("Diameter") {
		@Override
		public Object getValue(ThrustCurveMotorSet m) {
			return new Value(m.getDiameter(), UnitGroup.UNITS_MOTOR_DIMENSIONS);
		}
		
		@Override
		public Comparator<?> getComparator() {
			return ValueComparator.INSTANCE;
		}
	},
	LENGTH("Length") {
		@Override
		public Object getValue(ThrustCurveMotorSet m) {
			return new Value(m.getLength(), UnitGroup.UNITS_MOTOR_DIMENSIONS);
		}
		
		@Override
		public Comparator<?> getComparator() {
			return ValueComparator.INSTANCE;
		}
	};
	

	private final String title;
	private final int width;
	
	ThrustCurveMotorColumns(String title) {
		this(title, 50);
	}
	
	ThrustCurveMotorColumns(String title, int width) {
		this.title = title;
		this.width = width;
	}
	
	
	public abstract Object getValue(ThrustCurveMotorSet m);
	
	public abstract Comparator<?> getComparator();
	
	public String getTitle() {
		return title;
	}
	
	public int getWidth() {
		return width;
	}
	
	public String getToolTipText(ThrustCurveMotor m) {
		String tip = "<html>";
		tip += "<b>" + m.toString() + "</b>";
		tip += " (" + m.getMotorType().getDescription() + ")<br><hr>";
		
		String desc = m.getDescription().trim();
		if (desc.length() > 0) {
			tip += "<i>" + desc.replace("\n", "<br>") + "</i><br><hr>";
		}
		
		tip += ("Diameter: " +
				UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(m.getDiameter()) +
				"<br>");
		tip += ("Length: " +
				UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(m.getLength()) +
				"<br>");
		tip += ("Maximum thrust: " +
				UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(m.getMaxThrustEstimate()) +
				"<br>");
		tip += ("Average thrust: " +
				UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(m.getAverageThrustEstimate()) +
				"<br>");
		tip += ("Burn time: " +
				UnitGroup.UNITS_SHORT_TIME.getDefaultUnit()
						.toStringUnit(m.getBurnTimeEstimate()) + "<br>");
		tip += ("Total impulse: " +
				UnitGroup.UNITS_IMPULSE.getDefaultUnit()
						.toStringUnit(m.getTotalImpulseEstimate()) + "<br>");
		tip += ("Launch mass: " +
				UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(m.getLaunchCG().weight) +
				"<br>");
		tip += ("Empty mass: " +
				UnitGroup.UNITS_MASS.getDefaultUnit()
						.toStringUnit(m.getEmptyCG().weight));
		return tip;
	}
	
}