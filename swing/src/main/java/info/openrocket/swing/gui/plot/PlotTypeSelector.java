package info.openrocket.swing.gui.plot;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.Group;
import info.openrocket.core.util.Groupable;
import info.openrocket.core.util.UnitValue;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.util.Icons;

import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.io.Serial;
import java.util.List;

import info.openrocket.swing.gui.widgets.GroupableAndSearchableComboBox;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class PlotTypeSelector<T extends Groupable<G> & UnitValue, G extends Group> extends JPanel {
	protected static final Translator trans = Application.getTranslator();
	private static final long serialVersionUID = 9056324972817542570L;

	private final String[] POSITIONS = {Util.PlotAxisSelection.AUTO.getName(),
			Util.PlotAxisSelection.LEFT.getName(), Util.PlotAxisSelection.RIGHT.getName()};

	private final int index;
	protected final GroupableAndSearchableComboBox<G, T> typeSelector;
	private final UnitSelector unitSelector;
	private final JComboBox<String> axisSelector;
	private final JButton removeButton;

	public PlotTypeSelector(int plotIndex, T type, Unit unit, int position, List<T> availableTypes,
							boolean addRemoveButton) {
		super(new MigLayout("ins 0"));

		this.index = plotIndex;

		typeSelector = new GroupableAndSearchableComboBox<>(availableTypes, trans.get("FlightDataComboBox.placeholder")) {
			@Serial
			private static final long serialVersionUID = 1L;

			@Override
			public String getDisplayString(T item) {
				return PlotTypeSelector.this.getDisplayString(item);
			}
		};
		typeSelector.setSelectedItem(type);
		this.add(typeSelector, "gapright para, top");

		this.add(new JLabel("Unit:"), "top");
		unitSelector = new UnitSelector(type.getUnitGroup());
		if (unit != null) {
			unitSelector.setSelectedUnit(unit);
		}
		this.add(unitSelector, "width 40lp, gapright para, top");

		this.add(new JLabel("Axis:"), "top");
		axisSelector = new JComboBox<>(POSITIONS);
		axisSelector.setSelectedIndex(position + 1);
		this.add(axisSelector, "top");

		removeButton = new JButton(Icons.EDIT_DELETE);
		removeButton.setToolTipText("Remove this plot");
		removeButton.setBorderPainted(false);
		if (addRemoveButton) {
			addRemoveButton();
		}
	}

	public PlotTypeSelector(int plotIndex, T type, Unit unit, int position, List<T> availableTypes) {
		this(plotIndex, type, unit, position, availableTypes, true);
	}

	protected void addRemoveButton() {
		this.add(removeButton, "gapright 0, top");
	}

	public int getIndex() {
		return index;
	}

	public T getSelectedType() {
		return (T) typeSelector.getSelectedItem();
	}

	public Unit getSelectedUnit() {
		return unitSelector.getSelectedUnit();
	}

	public int getSelectedAxis() {
		return axisSelector.getSelectedIndex() - 1;
	}

	public void addTypeSelectionListener(ItemListener listener) {
		typeSelector.addItemListener(listener);
	}

	public void addUnitSelectionListener(ItemListener listener) {
		unitSelector.addItemListener(listener);
	}

	public void addAxisSelectionListener(ItemListener listener) {
		axisSelector.addItemListener(listener);
	}

	public void addRemoveButtonListener(ActionListener listener) {
		removeButton.addActionListener(listener);
	}

	public void setUnitGroup(UnitGroup unitGroup) {
		unitSelector.setUnitGroup(unitGroup);
	}

	protected String getDisplayString(T item) {
		return item.toString();
	}
}
