package info.openrocket.swing.gui.simulation;

import info.openrocket.swing.gui.widgets.SearchableAndCategorizableComboBox;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightDataTypeGroup;
import info.openrocket.core.startup.Application;

import javax.swing.JComboBox;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FlightDataComboBox extends JComboBox<FlightDataType> {
	private static final Translator trans = Application.getTranslator();

	public static SearchableAndCategorizableComboBox<FlightDataTypeGroup, FlightDataType> createComboBox(FlightDataTypeGroup[] allGroups, FlightDataType[] types) {
		final Map<FlightDataTypeGroup, FlightDataType[]> typeGroupMap = createFlightDataGroupMap(allGroups, types);
		return new SearchableAndCategorizableComboBox<>(typeGroupMap, trans.get("FlightDataComboBox.placeholder"));
	}

	/**
	 * Create a map of flight data group and corresponding flight data types.
	 * @param groups the groups
	 * @param types the types
	 * @return the map linking the types to their groups
	 */
	private static Map<FlightDataTypeGroup, FlightDataType[]> createFlightDataGroupMap(
			FlightDataTypeGroup[] groups, FlightDataType[] types) {
		// Sort the groups based on priority (lower number = higher priority)
		FlightDataTypeGroup[] sortedGroups = groups.clone();
		Arrays.sort(sortedGroups, Comparator.comparingInt(FlightDataTypeGroup::getPriority));

		Map<FlightDataTypeGroup, FlightDataType[]> map = new LinkedHashMap<>();
		for (FlightDataTypeGroup group : sortedGroups) {
			List<FlightDataType> itemsForGroup = new ArrayList<>();
			for (FlightDataType type : types) {
				if (type.getGroup().equals(group)) {
					itemsForGroup.add(type);
				}
			}
			// Sort the types within each group based on priority
			itemsForGroup.sort(Comparator.comparingInt(FlightDataType::getGroupPriority));

			map.put(group, itemsForGroup.toArray(new FlightDataType[0]));
		}

		return map;
	}
}
