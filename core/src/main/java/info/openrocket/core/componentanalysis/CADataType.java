package info.openrocket.core.componentanalysis;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.simulation.DataType;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Groupable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static info.openrocket.core.util.Chars.ALPHA;

public class CADataType implements Comparable<CADataType>, Groupable<CADataTypeGroup>, DataType {
	private static final Translator trans = Application.getTranslator();

	private final String name;
	private final String symbol;
	private final UnitGroup units;
	private final CADataTypeGroup group;
	private final int priority;
	private final int hashCode;

	//// Stability
	public static final CADataType CP_X = new CADataType(trans.get("ComponentAnalysisGeneralTab.TabStability.Col.CP"),
			"CP", UnitGroup.UNITS_LENGTH, CADataTypeGroup.STABILITY, 10);
	public static final CADataType CNa = new CADataType("<html>C<sub>N<sub>" + ALPHA + "</sub></sub>",
			"CNa", UnitGroup.UNITS_NONE, CADataTypeGroup.STABILITY, 11);

	//// Drag
	public static final CADataType PRESSURE_CD = new CADataType(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.Pressure"),
			"CD,pressure", UnitGroup.UNITS_NONE, CADataTypeGroup.DRAG, 20);
	public static final CADataType BASE_CD = new CADataType(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.Base"),
			"CD,base", UnitGroup.UNITS_NONE, CADataTypeGroup.DRAG, 21);
	public static final CADataType FRICTION_CD = new CADataType(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.friction"),
			"CD,friction", UnitGroup.UNITS_NONE, CADataTypeGroup.DRAG, 22);
	public static final CADataType PER_INSTANCE_CD = new CADataType(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.perInstance"),
			"CD,instance", UnitGroup.UNITS_NONE, CADataTypeGroup.DRAG, 23);
	public static final CADataType TOTAL_CD = new CADataType(trans.get("ComponentAnalysisGeneralTab.dragTableModel.Col.total"),
			"CD,total", UnitGroup.UNITS_NONE, CADataTypeGroup.DRAG, 24);

	//// Roll
	public static final CADataType ROLL_FORCING_COEFFICIENT = new CADataType(trans.get("ComponentAnalysisGeneralTab.rollTableModel.Col.rollforc"),
			"Clf", UnitGroup.UNITS_NONE, CADataTypeGroup.ROLL, 30);
	public static final CADataType ROLL_DAMPING_COEFFICIENT = new CADataType(trans.get("ComponentAnalysisGeneralTab.rollTableModel.Col.rolldamp"),
			"Cld", UnitGroup.UNITS_NONE, CADataTypeGroup.ROLL, 31);
	public static final CADataType TOTAL_ROLL_COEFFICIENT = new CADataType(trans.get("ComponentAnalysisGeneralTab.rollTableModel.Col.total"),
			"Cl,tot", UnitGroup.UNITS_NONE, CADataTypeGroup.ROLL, 32);


	public static final CADataType[] ALL_TYPES = {
			CP_X, CNa,
			PRESSURE_CD, BASE_CD, FRICTION_CD, PER_INSTANCE_CD, TOTAL_CD,
			ROLL_FORCING_COEFFICIENT, ROLL_DAMPING_COEFFICIENT, TOTAL_ROLL_COEFFICIENT
	};

	protected CADataType(String typeName, String symbol, UnitGroup units, CADataTypeGroup group, int priority) {
		if (typeName == null)
			throw new IllegalArgumentException("typeName is null");
		if (units == null)
			throw new IllegalArgumentException("units is null");
		this.name = typeName;
		this.symbol = symbol;
		this.units = units;
		this.group = group;
		this.priority = priority;
		this.hashCode = this.name.toLowerCase(Locale.ENGLISH).hashCode();
	}

	public String getName() {
		return name;
	}

	public String getSymbol() {
		return symbol;
	}

	@Override
	public UnitGroup getUnitGroup() {
		return units;
	}

	@Override
	public CADataTypeGroup getGroup() {
		return group;
	}

	public int getPriority() {
		return priority;
	}

	/**
	 * Calculate all components that are relevant for a given CADataType.
	 * @param configuration the current flight configuration of the rocket
	 * @param type the CADataType to search for
	 * @return a list of all components that are relevant for the given CADataType
	 */
	public static List<RocketComponent> calculateComponentsForType(FlightConfiguration configuration, CADataType type) {
		List<RocketComponent> components = new ArrayList<>();

		// Iterate through all components in the rocket
		for (RocketComponent component : configuration.getAllActiveComponents()) {
			// Check if this component is relevant for the given CADataType
			if (isComponentRelevantForType(component, type)) {
				components.add(component);
			}
		}

		return components;
	}

	/**
	 * Determine if a component is relevant for a given CADataType.
	 * @param component the component to check
	 * @param type the CADataType to check
	 * @return true if the component is relevant for the given CADataType, false otherwise
	 */
	public static boolean isComponentRelevantForType(RocketComponent component, CADataType type) {
		// Only aerodynamic and rockets are relevant for any CADataType
		if (!component.isAerodynamic() && !(component instanceof Rocket)) {
			return false;
		}

		// Doesn't make sense to calculate per-instance drag for rockets
		if (component instanceof Rocket && type.equals(CADataType.PER_INSTANCE_CD)) {
			return false;
		}

		if (type.equals(CADataType.CP_X) || type.equals(CADataType.CNa) ||
				type.equals(CADataType.PRESSURE_CD) || type.equals(CADataType.BASE_CD) ||
				type.equals(CADataType.FRICTION_CD) || type.equals(CADataType.PER_INSTANCE_CD) || type.equals(CADataType.TOTAL_CD)) {
			return true;
		} else if (type.equals(CADataType.ROLL_FORCING_COEFFICIENT) || type.equals(CADataType.ROLL_DAMPING_COEFFICIENT) ||
				type.equals(CADataType.TOTAL_ROLL_COEFFICIENT)) {
			return component instanceof FinSet;
		}
		return false;
	}

	@Override
	public String toString() {
		return name; // +" ("+symbol+") "+units.getDefaultUnit().toString();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CADataType))
			return false;
		return this.name.compareToIgnoreCase(((CADataType)o).name) == 0;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}

	@Override
	public int compareTo(CADataType o) {
		final int groupCompare = this.getGroup().compareTo(o.getGroup());
		if (groupCompare != 0) {
			return groupCompare;
		}

		return this.priority - o.priority;
	}
}
