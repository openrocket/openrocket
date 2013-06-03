package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import java.text.Collator;
import java.util.Comparator;

import net.sf.openrocket.database.motor.ThrustCurveMotorSet;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.DesignationComparator;
import net.sf.openrocket.motor.ThrustCurveMotor;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;
import net.sf.openrocket.unit.ValueComparator;


/**
 * Enum defining the table columns.
 */

enum ThrustCurveMotorColumns {
	//// Manufacturer
	MANUFACTURER("TCurveMotorCol.MANUFACTURER", 100) {
		@Override
		public String getValue(ThrustCurveMotorSet m) {
			return m.getManufacturer().getDisplayName();
		}
		
		@Override
		public Comparator<?> getComparator() {
			return Collator.getInstance();
		}
	},
	//// Designation
	DESIGNATION("TCurveMotorCol.DESIGNATION") {
		@Override
		public String getValue(ThrustCurveMotorSet m) {
			return m.getDesignation();
		}
		
		@Override
		public Comparator<?> getComparator() {
			return new DesignationComparator();
		}
	},
	//// TotalImpulse
	TOTAL_IMPULSE("TCurveMotorCol.TOTAL_IMPULSE") {
		@Override
		public Object getValue(ThrustCurveMotorSet m) {
			return m.getTotalImpuse();
		}
		
		@Override
		public Comparator<?> getComparator() {
			return new Comparator<Long>() {
				public int compare(Long o1, Long o2) {
					return o1.compareTo(o2);
				}
			};
		}
	},
	//// Type
	TYPE("TCurveMotorCol.TYPE") {
		@Override
		public String getValue(ThrustCurveMotorSet m) {
			return m.getType().getName();
		}
		
		@Override
		public Comparator<?> getComparator() {
			return Collator.getInstance();
		}
	},
	//// Diameter
	DIAMETER("TCurveMotorCol.DIAMETER") {
		@Override
		public Object getValue(ThrustCurveMotorSet m) {
			return new Value(m.getDiameter(), UnitGroup.UNITS_MOTOR_DIMENSIONS);
		}
		
		@Override
		public Comparator<?> getComparator() {
			return ValueComparator.INSTANCE;
		}
	},
	//// Length
	LENGTH("TCurveMotorCol.LENGTH") {
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
	private static final Translator trans = Application.getTranslator();
	
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
		return trans.get(title);
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
		
		tip += (trans.get("TCurveMotor.ttip.diameter") + " " +
				UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(m.getDiameter()) +
				"<br>");
		tip += (trans.get("TCurveMotor.ttip.length") + " " +
				UnitGroup.UNITS_MOTOR_DIMENSIONS.getDefaultUnit().toStringUnit(m.getLength()) +
				"<br>");
		tip += (trans.get("TCurveMotor.ttip.maxThrust") + " " +
				UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(m.getMaxThrustEstimate()) +
				"<br>");
		tip += (trans.get("TCurveMotor.ttip.avgThrust") + " " +
				UnitGroup.UNITS_FORCE.getDefaultUnit().toStringUnit(m.getAverageThrustEstimate()) +
				"<br>");
		tip += (trans.get("TCurveMotor.ttip.burnTime") + " " +
				UnitGroup.UNITS_SHORT_TIME.getDefaultUnit()
						.toStringUnit(m.getBurnTimeEstimate()) + "<br>");
		tip += (trans.get("TCurveMotor.ttip.totalImpulse") + " " +
				UnitGroup.UNITS_IMPULSE.getDefaultUnit()
						.toStringUnit(m.getTotalImpulseEstimate()) + "<br>");
		tip += (trans.get("TCurveMotor.ttip.launchMass") + " " +
				UnitGroup.UNITS_MASS.getDefaultUnit().toStringUnit(m.getLaunchCG().weight) +
				"<br>");
		tip += (trans.get("TCurveMotor.ttip.emptyMass") + " " +
				UnitGroup.UNITS_MASS.getDefaultUnit()
						.toStringUnit(m.getEmptyCG().weight));
		return tip;
	}
	
}