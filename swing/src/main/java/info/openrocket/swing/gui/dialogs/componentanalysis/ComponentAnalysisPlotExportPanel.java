package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.componentanalysis.CADataBranch;
import info.openrocket.core.componentanalysis.CADataType;
import info.openrocket.core.componentanalysis.CADomainDataType;
import info.openrocket.core.componentanalysis.CAParameterSweep;
import info.openrocket.core.componentanalysis.CAParameters;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.EditableSpinner;
import info.openrocket.swing.gui.components.UnitSelector;
import info.openrocket.swing.gui.plot.PlotPanel;
import net.miginfocom.swing.MigLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComponentAnalysisPlotExportPanel extends JPanel implements PlotPanel.PlotConfigurationListener<CAPlotConfiguration> {
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(ComponentAnalysisPlotExportPanel.class);

	private final Window parent;
	private final OpenRocketDocument document;
	private final JTabbedPane tabbedPane;
	JComboBox<CADomainDataType> parameterSelector;
	private JButton okButton;

	private static final int PLOT_IDX = 0;
	private static final int EXPORT_IDX = 1;

	private final CAPlotPanel plotTab;
	private final CAExportPanel exportTab;

	private DoubleModel minModel;
	private DoubleModel maxModel;
	private DoubleModel deltaModel;

	private final CAParameters parameters;
	private final CAParameterSweep parameterSweep;

	private final CADataType[] types;
	private final Map<CADataType, List<RocketComponent>> componentCache;
	private boolean isCacheValid;

	public ComponentAnalysisPlotExportPanel(ComponentAnalysisDialog parent, OpenRocketDocument document,
											CAParameters parameters, AerodynamicCalculator aerodynamicCalculator,
											Rocket rocket) {
		super(new MigLayout("fill, height 700px!", "[]", "[grow]"));

		this.parent = parent;
		this.document = document;
		this.parameters = parameters;
		this.parameterSweep = new CAParameterSweep(parameters, aerodynamicCalculator, rocket);
		this.componentCache = new HashMap<>();
		this.isCacheValid = false;

		this.types = getValidTypes();

		// ======== Top panel ========
		addTopPanel();

		// ======== Tabbed pane ========
		this.tabbedPane = new JTabbedPane();
		this.add(tabbedPane, "spanx, growx, growy, pushy, wrap");

		//// Plot data
		this.plotTab = CAPlotPanel.create(this, types);
		this.tabbedPane.addTab(trans.get("CAPlotExportDialog.tab.Plot"), null, this.plotTab);
		this.plotTab.addPlotConfigurationListener(this);

		//// Export data
		this.exportTab = CAExportPanel.create(this, types);
		this.tabbedPane.addTab(trans.get("CAPlotExportDialog.tab.Export"), null, this.exportTab);

		// Create the OK button
		createOkButton();

		// Update the OK button text based on the selected tab
		tabbedPane.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (okButton == null) {
					return;
				}
				int selectedIndex = tabbedPane.getSelectedIndex();
				switch (selectedIndex) {
					case PLOT_IDX:
						okButton.setText(trans.get("SimulationConfigDialog.btn.plot"));
						break;
					case EXPORT_IDX:
						okButton.setText(trans.get("SimulationConfigDialog.btn.export"));
						break;
				}
			}
		});

		validate();

		// Add listeners for events that would invalidate the cache
		rocket.addComponentChangeListener(e -> invalidateCache());
	}

	private void addTopPanel() {
		JPanel topPanel = new JPanel(new MigLayout("fill"));
		TitledBorder border = BorderFactory.createTitledBorder(trans.get("CAPlotExportDialog.XAxisConfiguration"));
		topPanel.setBorder(border);

		// Domain parameter selector
		topPanel.add(new JLabel(trans.get("CAPlotExportDialog.lbl.XAxis")), "top, split 2");
		this.parameterSelector = new JComboBox<>(CADomainDataType.ALL_DOMAIN_TYPES);
		this.parameterSelector.setToolTipText(trans.get("CAPlotExportDialog.lbl.XAxis.ttip"));
		parameterSelector.setSelectedItem(CADomainDataType.MACH);
		topPanel.add(parameterSelector, "top, growx");

		// Update the models
		updateModels(getSelectedParameter());

		JPanel minMaxPanel = new JPanel(new MigLayout("fill, ins 0"));

		// Min value
		minMaxPanel.add(new JLabel(trans.get("CAPlotExportDialog.lbl.MinValue")));
		final EditableSpinner minSpinner = new EditableSpinner(minModel.getSpinnerModel());
		minSpinner.setToolTipText(trans.get("CAPlotExportDialog.lbl.MinValue.ttip"));
		minMaxPanel.add(minSpinner, "growx");
		final UnitSelector minUnitSelector = new UnitSelector(minModel);
		minMaxPanel.add(minUnitSelector, "wrap");

		// Max value
		minMaxPanel.add(new JLabel(trans.get("CAPlotExportDialog.lbl.MaxValue")));
		final EditableSpinner maxSpinner = new EditableSpinner(maxModel.getSpinnerModel());
		maxSpinner.setToolTipText(trans.get("CAPlotExportDialog.lbl.MaxValue.ttip"));
		minMaxPanel.add(maxSpinner, "growx");
		final UnitSelector maxUnitSelector = new UnitSelector(maxModel);
		minMaxPanel.add(maxUnitSelector, "wrap");

		topPanel.add(minMaxPanel, "growx");

		// Step size
		topPanel.add(new JLabel(trans.get("CAPlotExportDialog.lbl.Delta")), "top, split 2");
		final EditableSpinner deltaSpinner = new EditableSpinner(deltaModel.getSpinnerModel());
		deltaSpinner.setToolTipText(trans.get("CAPlotExportDialog.lbl.Delta.ttip"));
		topPanel.add(deltaSpinner, "top, growx");
		final UnitSelector deltaUnitSelector = new UnitSelector(deltaModel);
		topPanel.add(deltaUnitSelector, "top");

		// Update the models and spinners when the parameter selector changes
		parameterSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CADomainDataType type = getSelectedParameter();
				updateModels(type);

				// Update spinners and unit selectors
				minSpinner.setModel(minModel.getSpinnerModel());
				maxSpinner.setModel(maxModel.getSpinnerModel());
				deltaSpinner.setModel(deltaModel.getSpinnerModel());
				minUnitSelector.setModel(minModel);
				maxUnitSelector.setModel(maxModel);
				deltaUnitSelector.setModel(deltaModel);

				// Ensure the unit selectors show the correct unit
				minUnitSelector.invalidate();
				maxUnitSelector.invalidate();
				deltaUnitSelector.invalidate();

				// Update the displayed values
				minSpinner.setValue(minUnitSelector.getSelectedUnit().toValue(type.getMin()));
				maxSpinner.setValue(minUnitSelector.getSelectedUnit().toValue(type.getMax()));
				deltaSpinner.setValue(minUnitSelector.getSelectedUnit().toValue(type.getDelta()));
				minSpinner.invalidate();
				maxSpinner.invalidate();
				deltaSpinner.invalidate();

				if (plotTab != null) {
					plotTab.setXAxis(type);
				}
			}
		});

		this.add(topPanel, "growx, wrap");
	}

	private void createOkButton() {
		// OK button
		this.okButton = new JButton(trans.get("SimulationConfigDialog.btn.plot"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getSelectedIndex() == PLOT_IDX) {
					JDialog dialog = ComponentAnalysisPlotExportPanel.this.plotTab.doPlot(parent);
					if (dialog != null) {
						dialog.setVisible(true);
					}
				} else if (tabbedPane.getSelectedIndex() == EXPORT_IDX) {
					ComponentAnalysisPlotExportPanel.this.exportTab.doExport();
				}
			}
		});
	}

	public OpenRocketDocument getDocument() {
		return document;
	}

	public CADomainDataType getSelectedParameter() {
		return (CADomainDataType) parameterSelector.getSelectedItem();
	}

	public CAParameters getParameters() {
		return parameters;
	}

	private void updateModels(CADomainDataType type) {
		if (type == null) {
			throw new IllegalArgumentException("CADomainDataType cannot be null");
		}

		// Create new models
		this.minModel = new DoubleModel(type, "Min", type.getUnitGroup(), type.getMin(), type.getMax());
		this.maxModel = new DoubleModel(type, "Max", type.getUnitGroup(), type.getMin(), type.getMax());
		this.deltaModel = new DoubleModel(type, "Delta", type.getUnitGroup(), type.getMinDelta());

		// Set the values and units
		minModel.setValue(type.getMin());
		maxModel.setValue(type.getMax());
		deltaModel.setValue(type.getDelta());

		// Set the mutual dependencies
		this.minModel.setMaxModel(maxModel);
		this.maxModel.setMinModel(minModel);
	}

	private void invalidateCache() {
		this.isCacheValid = false;
		this.componentCache.clear();
	}

	public List<RocketComponent> getComponentsForType(CADataType type) {
		if (!isCacheValid || !componentCache.containsKey(type)) {
			updateCacheForType(type);
		}
		return new ArrayList<>(componentCache.get(type));
	}

	private void updateCacheForType(CADataType type) {
		if (!isCacheValid) {
			componentCache.clear();
		}

		if (!componentCache.containsKey(type)) {
			List<RocketComponent> components = CADataType.calculateComponentsForType(parameters.getSelectedConfiguration(), type);
			componentCache.put(type, components);
		}

		isCacheValid = true;
	}

	/**
	 * Returns the valid types for the current rocket, i.e. types that have at least once component bound to it.
	 * @return all valid Component Analysis types for the current rocket
	 */
	private CADataType[] getValidTypes() {
		List<CADataType> validTypes = new ArrayList<>();
		List<RocketComponent> components;
		for (CADataType type : CADataType.ALL_TYPES) {
			components = getComponentsForType(type);
			if (!components.isEmpty()) {
				validTypes.add(type);
			}
		}
		return validTypes.toArray(new CADataType[0]);
	}

	/**
	 * Run the parameter sweep and return the data branch.
	 * @return the data branch containing the results of the parameter sweep
	 */
	public CADataBranch runParameterSweep() {
		double min = minModel.getValue();
		double max = maxModel.getValue();
		double delta = deltaModel.getValue();

		CADomainDataType domainType = getSelectedParameter();
		CADataBranch dataBranch = parameterSweep.sweep(domainType, min, max, delta, getParameterValue(domainType));
		log.info("Parameter sweep completed. Data stored in dataBranch.");
		return dataBranch;
	}

	public Window getParentWindow() {
		return parent;
	}

	public JButton getOkButton() {
		return okButton;
	}

	private double getParameterValue(CADomainDataType parameterType) {
		if (parameterType.equals(CADomainDataType.MACH)) {
			return parameters.getMach();
		} else if (parameterType.equals(CADomainDataType.AOA)) {
			return parameters.getAOA();
		} else if (parameterType.equals(CADomainDataType.ROLL_RATE)) {
			return parameters.getRollRate();
		} else if (parameterType.equals(CADomainDataType.WIND_DIRECTION)) {
			return parameters.getTheta();
		}
		// Add more cases here as more parameter types are implemented
		else {
			throw new IllegalArgumentException("Unsupported parameter type: " + parameterType);
		}
	}

	@Override
	public void onPlotConfigurationChanged(CAPlotConfiguration newConfiguration) {
		CADomainDataType type = (CADomainDataType) newConfiguration.getDomainAxisType();
		this.parameterSelector.setSelectedItem(type);
	}
}
