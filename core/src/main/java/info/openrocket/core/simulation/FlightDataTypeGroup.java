package info.openrocket.core.simulation;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

public class FlightDataTypeGroup implements Comparable<FlightDataTypeGroup> {
	private static final Translator trans = Application.getTranslator();

	public static final FlightDataTypeGroup TIME = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_TIME"), 0);
	public static final FlightDataTypeGroup POSITION_AND_MOTION = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_POSITION_AND_MOTION"), 10);
	public static final FlightDataTypeGroup ORIENTATION = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_ORIENTATION"), 20);
	public static final FlightDataTypeGroup MASS_AND_INERTIA = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_MASS_AND_INERTIA"), 30);
	public static final FlightDataTypeGroup STABILITY = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_STABILITY"), 40);
	public static final FlightDataTypeGroup THRUST_AND_DRAG = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_THRUST_AND_DRAG"), 50);
	public static final FlightDataTypeGroup COEFFICIENTS = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_COEFFICIENTS"), 60);
	public static final FlightDataTypeGroup ATMOSPHERIC_CONDITIONS = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_ATMOSPHERIC_CONDITIONS"), 70);
	public static final FlightDataTypeGroup CHARACTERISTIC_NUMBERS = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_CHARACTERISTIC_NUMBERS"), 80);
	public static final FlightDataTypeGroup REFERENCE_VALUES = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_REFERENCE_VALUES"), 90);
	public static final FlightDataTypeGroup SIMULATION_INFORMATION = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_SIMULATION_INFORMATION"), 100);

	public static final FlightDataTypeGroup CUSTOM = new FlightDataTypeGroup(trans.get("FlightDataTypeGroup.GROUP_CUSTOM"), 200);

	// An array of all the built-in groups
	public static final FlightDataTypeGroup[] ALL_GROUPS = {
			TIME,
			POSITION_AND_MOTION,
			ORIENTATION,
			MASS_AND_INERTIA,
			STABILITY,
			THRUST_AND_DRAG,
			COEFFICIENTS,
			ATMOSPHERIC_CONDITIONS,
			CHARACTERISTIC_NUMBERS,
			REFERENCE_VALUES,
			SIMULATION_INFORMATION,
			CUSTOM
	};

	private final String name;
	private final int priority;

	private FlightDataTypeGroup(String groupName, int priority) {
		this.name = groupName;
		this.priority = priority;
	}

	public String getName() {
		return name;
	}

	public int getPriority() {
		return priority;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FlightDataTypeGroup))
			return false;
		return this.compareTo((FlightDataTypeGroup) o) == 0;
	}

	@Override
	public int compareTo(FlightDataTypeGroup o) {
		return this.priority  - o.priority;
	}
}
