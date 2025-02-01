package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.Unit;
import info.openrocket.core.util.StringUtils;
import info.openrocket.swing.gui.components.CsvOptionPanel;
import info.openrocket.swing.gui.util.FileHelper;
import info.openrocket.swing.gui.util.SaveCSVWorker;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.swing.gui.widgets.CSVExportPanel;
import info.openrocket.swing.gui.widgets.SaveFileChooser;
import net.miginfocom.swing.MigLayout;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CAExportPanel extends CSVExportPanel<CADataType> {
	private static final long serialVersionUID = 4423905472892675964L;
	private static final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

	private final ComponentAnalysisPlotExportPanel parent;
	private final Map<CADataType, List<RocketComponent>> selectedComponentsMap = new HashMap<>();
	private List<JTextArea> selectedComponentsLabels;
	private List<JScrollPane> selectedComponentsScrollPanes;

	private static final int OPTION_COMPONENT_ANALYSIS_COMMENTS = 0;
	private static final int OPTION_FIELD_DESCRIPTIONS = 1;

	public CAExportPanel(ComponentAnalysisPlotExportPanel parent, CADataType[] types, boolean[] selected) {
		super(types, selected,
				new CsvOptionPanel(CAExportPanel.class, true,
						trans.get("CAExportPanel.checkbox.Includecadesc"),
						trans.get("CAExportPanel.checkbox.ttip.Includecadesc"),
						trans.get("SimExpPan.checkbox.Includefielddesc"),
						trans.get("SimExpPan.checkbox.ttip.Includefielddesc")),
				false);

		this.parent = parent;

		// Initialize selected components map
		for (int i = 0; i < types.length; i++) {
			updateSelectedComponents(types[i], new ArrayList<>(), parent.getComponentsForType(types[i]),
					selectedComponentsLabels.get(i), selectedComponentsScrollPanes.get(i));
		}
	}

	@Override
	protected Component createExtraComponent(CADataType type, int index) {
		JPanel panel = new JPanel(new MigLayout("ins 0, fill", "[grow]", "[][]"));

		// Label for displaying selected components
		JTextArea selectedComponentsLabel = new JTextArea();
		selectedComponentsLabel.setEditable(false);
		selectedComponentsLabel.setWrapStyleWord(true);
		selectedComponentsLabel.setLineWrap(true);
		selectedComponentsLabel.setOpaque(false);
		selectedComponentsLabel.setFont(UIManager.getFont("Label.font").deriveFont(Font.PLAIN, prefs.getUIFontSize() - 1));

		JScrollPane scrollPane = new JScrollPane(selectedComponentsLabel);
		scrollPane.setPreferredSize(new Dimension(200, 50)); // Adjust as needed
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		panel.add(scrollPane, "growx, wrap");

		selectedComponentsLabels = selectedComponentsLabels != null ? selectedComponentsLabels : new ArrayList<>();
		selectedComponentsScrollPanes = selectedComponentsScrollPanes != null ? selectedComponentsScrollPanes : new ArrayList<>();
		selectedComponentsLabels.add(selectedComponentsLabel);
		selectedComponentsScrollPanes.add(scrollPane);

		// Select components button
		JButton selectComponentsBtn = new JButton(trans.get("CAExportPanel.btn.SelectComponents"));
		panel.add(selectComponentsBtn, "growx, wrap");

		selectComponentsBtn.addActionListener(e -> {
			List<RocketComponent> availableComponents = parent.getComponentsForType(type);
			ComponentSelectionDialog dialog = new ComponentSelectionDialog(
					SwingUtilities.getWindowAncestor(this),
					parent.getDocument(),
					availableComponents,
					selectedComponentsMap.get(type)
			);
			List<RocketComponent> newSelectedComponents = dialog.showDialog();
			updateSelectedComponents(type, newSelectedComponents, availableComponents, selectedComponentsLabel, scrollPane);
		});

		return panel;
	}

	@Override
	protected String getExtraColumnLabelKey() {
		return "CAExportPanel.Col.Components";
	}

	private void updateSelectedComponents(CADataType type, List<RocketComponent> components,
										  List<RocketComponent> componentsForType, JTextArea label,
										  JScrollPane scrollPane) {
		components = (components != null && !components.isEmpty()) ? components : componentsForType.subList(0, 1);
		List<RocketComponent> selectedComponentsList = this.selectedComponentsMap.computeIfAbsent(type, k -> new ArrayList<>());
		selectedComponentsList.clear();
		selectedComponentsList.addAll(components);

		updateSelectedComponentsLabel(label, scrollPane, components);
	}

	private void updateSelectedComponentsLabel(JTextArea label, JScrollPane scrollPane, List<RocketComponent> components) {
		String componentNames = components.stream()
				.map(RocketComponent::getName)
				.reduce((a, b) -> a + ", " + b)
				.orElse("");
		label.setText(componentNames);
		label.setToolTipText(componentNames); // Add tooltip in case the text is too long
		scrollPane.revalidate();
		scrollPane.repaint();
	}

	public static CAExportPanel create(ComponentAnalysisPlotExportPanel parent, CADataType[] types) {
		boolean[] selected = new boolean[types.length];
		for (int i = 0; i < types.length; i++) {
			selected[i] = ((SwingPreferences) Application.getPreferences()).isComponentAnalysisDataTypeExportSelected(types[i]);
		}

		return new CAExportPanel(parent, types, selected);
	}

	@Override
	public boolean doExport() {
		CADataBranch branch = this.parent.runParameterSweep();

		// Check for data types with no selected components
		List<CADataType> typesWithNoComponents = new ArrayList<>();
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				List<RocketComponent> selectedComponents = this.selectedComponentsMap.get(types[i]);
				if (!selectedComponents.isEmpty()) {
					typesWithNoComponents.add(types[i]);
				}
			}
		}

		// Show warning dialog if there are data types with no selected components
		if (!typesWithNoComponents.isEmpty()) {
			StringBuilder message = new StringBuilder(trans.get("CAExportPanel.dlg.MissingComponents.txt1"));
			message.append("\n\n");
			for (CADataType type : typesWithNoComponents) {
				message.append("- ").append(StringUtils.removeHTMLTags(type.getName())).append("\n");
			}
			message.append("\n").append(trans.get("CAExportPanel.dlg.MissingComponents.txt2"));

			int result = JOptionPane.showConfirmDialog(
					this,
					message.toString(),
					trans.get("CAExportPanel.dlg.MissingComponents.title"),
					JOptionPane.YES_NO_OPTION,
					JOptionPane.WARNING_MESSAGE
			);

			if (result != JOptionPane.YES_OPTION) {
				return false;
			}
		}

		JFileChooser chooser = new SaveFileChooser();
		chooser.setFileFilter(FileHelper.CSV_FILTER);
		chooser.setCurrentDirectory(((SwingPreferences) Application.getPreferences()).getDefaultDirectory());

		if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION)
			return false;

		File file = chooser.getSelectedFile();
		if (file == null)
			return false;

		file = FileHelper.forceExtension(file, "csv");
		if (!FileHelper.confirmWrite(file, this)) {
			return false;
		}


		String commentChar = csvOptions.getCommentCharacter();
		String fieldSep = csvOptions.getFieldSeparator();
		int decimalPlaces = csvOptions.getDecimalPlaces();
		boolean isExponentialNotation = csvOptions.isExponentialNotation();
		boolean analysisComments = csvOptions.getSelectionOption(OPTION_COMPONENT_ANALYSIS_COMMENTS);
		boolean fieldDescriptions = csvOptions.getSelectionOption(OPTION_FIELD_DESCRIPTIONS);
		csvOptions.storePreferences();

		// Store preferences and export
		((SwingPreferences) Application.getPreferences()).setDefaultDirectory(chooser.getCurrentDirectory());
		for (int i = 0; i < selected.length; i++) {
			((SwingPreferences) Application.getPreferences()).setComponentAnalysisExportSelected(types[i], selected[i]);
		}

		List<CADataType> fieldTypes = new ArrayList<>();
		List<Unit> fieldUnits = new ArrayList<>();
		Map<CADataType, List<RocketComponent>> components = new HashMap<>();

		// Iterate through the table to get selected items
		for (int i = 0; i < selected.length; i++) {
			if (selected[i]) {
				List<RocketComponent> selectedComponentsList = new ArrayList<>(selectedComponentsMap.get(types[i]));
				if (!selectedComponentsList.isEmpty()) {
					fieldTypes.add(types[i]);
					fieldUnits.add(units[i]);
					components.put(types[i], selectedComponentsList);
				}
			}
		}


		if (fieldSep.equals(SPACE)) {
			fieldSep = " ";
		} else if (fieldSep.equals(TAB)) {
			fieldSep = "\t";
		}


		SaveCSVWorker.exportCAData(file, parent.getParameters(), branch, parent.getSelectedParameter(),
				fieldTypes.toArray(new CADataType[0]), components, fieldUnits.toArray(new Unit[0]), fieldSep,
				decimalPlaces, isExponentialNotation, commentChar, analysisComments,
				fieldDescriptions, SwingUtilities.getWindowAncestor(this));

		return true;
	}
}
