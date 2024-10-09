package info.openrocket.core.componentanalysis;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;

public class CADomainDataType extends CADataType {
	private static final Translator trans = Application.getTranslator();

	private double min;
	private double max;
	private double delta;
	private final double minDelta;

	public static final CADomainDataType WIND_DIRECTION = new CADomainDataType(trans.get("CADomainDataType.lbl.winddir"),
			"d", UnitGroup.UNITS_ANGLE, CADataTypeGroup.DOMAIN_PARAMETER, 0.0, 2 * Math.PI, Math.PI / 30, Math.PI / 1800, 0);
	public static final CADomainDataType AOA = new CADomainDataType(trans.get("CADomainDataType.lbl.angleofattack"),
			"a", UnitGroup.UNITS_ANGLE, CADataTypeGroup.DOMAIN_PARAMETER, 0.0, Math.PI, Math.PI / 30, Math.PI / 1800, 1);
	public static final CADomainDataType MACH = new CADomainDataType(trans.get("CADomainDataType.lbl.machnumber"),
			"M", UnitGroup.UNITS_COEFFICIENT, CADataTypeGroup.DOMAIN_PARAMETER, 0, 3.0, 0.025, 0.001, 2);
	public static final CADomainDataType ROLL_RATE = new CADomainDataType(trans.get("CADomainDataType.lbl.rollrate"),
			"r", UnitGroup.UNITS_ROLL, CADataTypeGroup.DOMAIN_PARAMETER, -20 * 2 * Math.PI, 20 * 2 * Math.PI,
			Math.PI, Math.PI / 10, 3);

	public static final CADomainDataType[] ALL_DOMAIN_TYPES = {
			WIND_DIRECTION,
			MACH,
			AOA,
			ROLL_RATE
	};

	/**
	 * Constructor
	 * @param typeName the name of the data type
	 * @param symbol the (mathematical) symbol of the data type
	 * @param units the unit group of the data type
	 * @param group the group of the data type
	 * @param min the minimum value of the data type
	 * @param max the maximum value of the data type
	 * @param delta the step size of the data type
	 * @param minDelta the minimum step size of the data type
	 * @param priority the priority of the data type
	 */
	private CADomainDataType(String typeName, String symbol, UnitGroup units, CADataTypeGroup group,
							 double min, double max, double delta, double minDelta, int priority) {
		super(typeName, symbol, units, group, priority);

		this.min = min;
		this.max = max;
		this.delta = delta;
		this.minDelta = minDelta;
	}

	public double getMin() {
		return min;
	}

	public void setMin(double min) {
		this.min = min;
	}

	public double getMax() {
		return max;
	}

	public void setMax(double max) {
		this.max = max;
	}

	public double getDelta() {
		return delta;
	}

	public void setDelta(double delta) {
		this.delta = delta;
	}

	public double getMinDelta() {
		return minDelta;
	}
}
