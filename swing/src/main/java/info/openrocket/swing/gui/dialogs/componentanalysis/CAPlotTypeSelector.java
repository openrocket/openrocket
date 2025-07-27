package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.componentanalysis.CADataTypeGroup;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.StringUtils;
import info.openrocket.swing.gui.plot.PlotTypeSelector;
import info.openrocket.swing.gui.util.SwingPreferences;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class CAPlotTypeSelector extends PlotTypeSelector<CADataType, CADataTypeGroup> {
	private static final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

	private final JTextArea selectedComponentsLabel;
	private final JScrollPane selectedComponentsScrollPane;
	private final List<RocketComponent> selectedComponents = new ArrayList<>();
	private List<RocketComponent> componentsForType;
	private final List<ComponentSelectionListener> componentSelectionListeners = new ArrayList<>();

	public CAPlotTypeSelector(final ComponentAnalysisPlotExportPanel parent, OpenRocketDocument document,
							  int plotIndex, CADataType type, Unit unit, int position, List<CADataType> availableTypes,
							  List<RocketComponent> componentsForType, CAPlotConfiguration configuration,
							  List<RocketComponent> initialSelectedComponents) {
		super(plotIndex, type, unit, position, availableTypes, false);

		// Selected components label
		selectedComponentsLabel = new JTextArea();
		selectedComponentsLabel.setEditable(false);
		selectedComponentsLabel.setWrapStyleWord(true);
		selectedComponentsLabel.setLineWrap(true);
		selectedComponentsLabel.setOpaque(false);
		selectedComponentsLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, prefs.getUIFontSize() - 1));

		selectedComponentsScrollPane = new JScrollPane(selectedComponentsLabel);
		selectedComponentsScrollPane.setPreferredSize(new Dimension(200, 60)); // Adjust as needed
		selectedComponentsScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		// Update the data
		setComponentsForType(type, componentsForType);
		updateSelectedComponents(initialSelectedComponents, componentsForType, configuration, plotIndex);

		// Component selector
		final JPanel componentSelectorPanel = new JPanel(new MigLayout("ins 0"));
		JButton selectComponentButton = new JButton(trans.get("CAPlotTypeSelector.btn.SelectComponents"));
		selectComponentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ComponentSelectionDialog dialog = new ComponentSelectionDialog(parent.getParentWindow(), document,
						CAPlotTypeSelector.this.componentsForType, CAPlotTypeSelector.this.selectedComponents);
				List<RocketComponent> selectedComponents = dialog.showDialog();

				if (selectedComponents != null) {
					updateSelectedComponents(selectedComponents, CAPlotTypeSelector.this.componentsForType,
							configuration, plotIndex);
				}
			}
		});
		componentSelectorPanel.add(selectComponentButton, "spanx, wrap");
		componentSelectorPanel.add(selectedComponentsScrollPane, "wrap");
		this.add(componentSelectorPanel, "growx, gapleft para, gapright para");

		// Remove button
		addRemoveButton();

		typeSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CADataType type = (CADataType) typeSelector.getSelectedItem();
				List<RocketComponent> componentsForType = parent.getComponentsForType(type);
				setComponentsForType(type, componentsForType);
				updateSelectedComponents(null, componentsForType, configuration, plotIndex);
			}
		});
	}

	private void setComponentsForType(CADataType type, List<RocketComponent> componentsForType) {
		if (componentsForType.isEmpty()) {
			throw new IllegalArgumentException("No components for type " + type);
		}
		this.componentsForType = componentsForType;
	}

	private void updateSelectedComponents(List<RocketComponent> components, List<RocketComponent> componentsForType,
										  CAPlotConfiguration configuration, int plotIndex) {
		components = (components != null && !components.isEmpty()) ? components : componentsForType.subList(0, 1);
		this.selectedComponents.clear();
		this.selectedComponents.addAll(components);

		updateSelectedComponentsLabel(this.selectedComponents);
		configuration.setPlotDataComponents(plotIndex, this.selectedComponents);
		notifyComponentSelectionListeners();
	}

	private void updateSelectedComponentsLabel(List<RocketComponent> components) {
		List<String> names = new ArrayList<>(components.size());
		for (RocketComponent c : components) {
			names.add(c.getName());
		}
		selectedComponentsLabel.setText(StringUtils.join(", ", names));
		selectedComponentsLabel.setToolTipText(StringUtils.join(", ", names));	// Add tooltip in case the text is too long
		selectedComponentsScrollPane.revalidate();
		selectedComponentsScrollPane.repaint();
	}

	public void addComponentSelectionListener(ComponentSelectionListener listener) {
		componentSelectionListeners.add(listener);
	}

	public void removeComponentSelectionListener(ComponentSelectionListener listener) {
		componentSelectionListeners.remove(listener);
	}

	private void notifyComponentSelectionListeners() {
		for (ComponentSelectionListener listener : this.componentSelectionListeners) {
			listener.componentsSelected(this.selectedComponents);
		}
	}

	public List<RocketComponent> getSelectedComponents() {
		return this.selectedComponents;
	}

	@Override
	protected String getDisplayString(CADataType item) {
		return StringUtils.removeHTMLTags(item.getName());
	}

	public interface ComponentSelectionListener {
		void componentsSelected(List<RocketComponent> selectedComponents);
	}
}
