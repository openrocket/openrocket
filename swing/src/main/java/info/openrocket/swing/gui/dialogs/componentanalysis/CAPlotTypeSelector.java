package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.componentanalysis.CADataTypeGroup;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.unit.Unit;
import info.openrocket.swing.gui.plot.PlotTypeSelector;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.util.List;

public class CAPlotTypeSelector extends PlotTypeSelector<CADataType, CADataTypeGroup> {
	private final JComboBox<RocketComponent> componentSelector;

	public CAPlotTypeSelector(final ComponentAnalysisPlotExportPanel parent, int plotIndex,
							  CADataType type, Unit unit, int position, List<CADataType> availableTypes,
							  List<RocketComponent> componentsForType, CAPlotConfiguration configuration) {
		super(plotIndex, type, unit, position, availableTypes, false);

		if (componentsForType.isEmpty()) {
			throw new IllegalArgumentException("No components for type " + type);
		}

		// Component selector
		this.add(new JLabel(trans.get("CAPlotTypeSelector.lbl.component")));
		componentSelector = new JComboBox<>(componentsForType.toArray(new RocketComponent[0]));
		configuration.setPlotDataComponent(plotIndex, componentsForType.get(0));
		this.add(componentSelector, "gapright para");

		addRemoveButton();

		typeSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CADataType type = (CADataType) typeSelector.getSelectedItem();
				List<RocketComponent> componentsForType = parent.getComponentsForType(type);
				componentSelector.removeAllItems();
				for (RocketComponent component : componentsForType) {
					componentSelector.addItem(component);
				}
				componentSelector.setSelectedIndex(0);
				configuration.setPlotDataComponent(plotIndex, (RocketComponent) componentSelector.getSelectedItem());
			}
		});
	}

	public void addComponentSelectionListener(ItemListener listener) {
		componentSelector.addItemListener(listener);
	}

	public RocketComponent getSelectedComponent() {
		return (RocketComponent) componentSelector.getSelectedItem();
	}
}
