package info.openrocket.swing.gui.simulation;

import info.openrocket.swing.gui.widgets.GroupableAndSearchableComboBox;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.simulation.FlightDataTypeGroup;
import info.openrocket.core.startup.Application;

import javax.swing.JComboBox;
import java.util.List;

public class FlightDataComboBox extends JComboBox<FlightDataType> {
	private static final Translator trans = Application.getTranslator();

	public static GroupableAndSearchableComboBox<FlightDataTypeGroup, FlightDataType> createComboBox(List<FlightDataType> types) {
		return new GroupableAndSearchableComboBox<>(types, trans.get("FlightDataComboBox.placeholder"));
	}
}
