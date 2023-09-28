package net.sf.openrocket.simulation;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

public class FlightDataTypeGroup {
	private static final Translator trans = Application.getTranslator();

	public static final FlightDataTypeGroup POSITION_AND_MOTION = new FlightDataTypeGroup("Position and motion", 0);

	public static final FlightDataTypeGroup CUSTOM = new FlightDataTypeGroup("Custom", 100);

	// An array of all the built-in groups
	public static final FlightDataTypeGroup[] ALL_GROUPS = {
		POSITION_AND_MOTION,
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
		return name;
	}
}
