package net.sf.openrocket.gui.dialogs.motor.thrustcurve;

import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.BugException;

/**
 * NAR approved motor classes (http://www.nar.org/NARmotors.html).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public enum MotorClass {
	
	A18("1/8A", 0, 0.3125),
	A14("1/4A", 0.3125, 0.625),
	A12("1/2A", 0.625, 1.25),
	A("A", 1.25, 2.5),
	B("B", 2.5, 5),
	C("C", 5, 10),
	D("D", 10, 20),
	E("E", 20, 40),
	F("F", 40, 80),
	G("G", 80, 160),
	H("H", 160, 320),
	I("I", 320, 640),
	J("J", 640, 1280),
	K("K", 1280, 2560),
	L("L", 2560, 5120),
	M("M", 5120, 10240),
	N("N", 10240, 20480),
	O("O", 20480, 40960),
	OVER("> O", 40960, Double.MAX_VALUE) {
		@Override
		public String getDescription(double impulse) {
			return "Over O";
		}
		
		@Override
		public String getClassDescription() {
			return "Over O-class (over " + UnitGroup.UNITS_IMPULSE.toStringUnit(40960) + ")";
		}
	};
	
	
	private final String className;
	private final double min;
	private final double max;
	
	
	private MotorClass(String className, double min, double max) {
		this.className = className;
		this.min = min;
		this.max = max;
	}
	
	
	public String getDescription(double impulse) {
		double percent = (impulse - min) / (max - min) * 100;
		if (percent < 1) {
			// 0% looks stupid
			percent = 1;
		}
		return String.format("%d%% %s", Math.round(percent), className);
	}
	
	public String getClassDescription() {
		return "Class " + className + " (" + UnitGroup.UNITS_IMPULSE.toStringUnit(min) + " - " + UnitGroup.UNITS_IMPULSE.toStringUnit(max) + ")";
	}
	
	
	/**
	 * Find the appropriate motor class for the provided impulse.
	 */
	public static MotorClass getMotorClass(double impulse) {
		double epsilon = 0.0000001;
		
		// Round large values so 640.1 Ns (which is displayed as 640 Ns) is counted as I-class
		if (impulse >= 100) {
			impulse = Math.rint(impulse);
		}
		
		for (MotorClass m : MotorClass.values()) {
			if (impulse <= m.max + epsilon) {
				return m;
			}
		}
		throw new BugException("Could not find motor class for impulse " + impulse);
	}
	
}
