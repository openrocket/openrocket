package info.openrocket.swing.gui.dialogs.componentanalysis;

import info.openrocket.core.aerodynamics.AerodynamicCalculator;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.ComponentAssembly;
import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.adaptors.DoubleModel;
import info.openrocket.swing.gui.components.EditableSpinner;
import info.openrocket.swing.gui.util.GUIUtil;
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

public class ComponentAnalysisPlotExportDialog extends JDialog {
	private static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(ComponentAnalysisPlotExportDialog.class);

	private final Rocket rocket;

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

	private final Map<CADataType, List<RocketComponent>> componentCache;
	private boolean isCacheValid;

	public ComponentAnalysisPlotExportDialog(Window parent, CAParameters parameters,
											 AerodynamicCalculator aerodynamicCalculator, Rocket rocket) {
		super(parent, trans.get("CAPlotExportDialog.title"), JDialog.ModalityType.DOCUMENT_MODAL);

		final JPanel contentPanel = new JPanel(new MigLayout("fill, height 500px"));

		this.rocket = rocket;
		this.parameters = parameters;
		this.parameterSweep = new CAParameterSweep(parameters, aerodynamicCalculator, rocket);
		this.componentCache = new HashMap<>();
		this.isCacheValid = false;

		// ======== Top panel ========
		addTopPanel(contentPanel);

		// ======== Tabbed pane ========
		this.tabbedPane = new JTabbedPane();

		//// Plot data
		this.plotTab = CAPlotPanel.create(this);
		this.tabbedPane.addTab(trans.get("CAPlotExportDialog.tab.Plot"), this.plotTab);

		//// Export data
		this.exportTab = CAExportPanel.create(CADataType.ALL_TYPES);
		this.tabbedPane.addTab(trans.get("CAPlotExportDialog.tab.Export"), this.exportTab);

		contentPanel.add(tabbedPane, "grow, wrap");

		// ======== Bottom panel ========
		addBottomPanel(contentPanel);

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

		this.add(contentPanel);
		this.validate();
		this.pack();

		// Add listeners for events that would invalidate the cache
		rocket.addComponentChangeListener(e -> invalidateCache());

		this.setLocationByPlatform(true);
		GUIUtil.setDisposableDialogOptions(this, null);
	}

	private void addTopPanel(JPanel contentPanel) {
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
		minMaxPanel.add(minSpinner, "growx, wrap");

		// Max value
		minMaxPanel.add(new JLabel(trans.get("CAPlotExportDialog.lbl.MaxValue")));
		final EditableSpinner maxSpinner = new EditableSpinner(maxModel.getSpinnerModel());
		maxSpinner.setToolTipText(trans.get("CAPlotExportDialog.lbl.MaxValue.ttip"));
		minMaxPanel.add(maxSpinner, "growx, wrap");

		topPanel.add(minMaxPanel, "growx");

		// Step size
		topPanel.add(new JLabel(trans.get("CAPlotExportDialog.lbl.Delta")), "top, split 2");
		final EditableSpinner deltaSpinner = new EditableSpinner(deltaModel.getSpinnerModel());
		deltaSpinner.setToolTipText(trans.get("CAPlotExportDialog.lbl.Delta.ttip"));
		topPanel.add(deltaSpinner, "top, growx");

		// Update the models and spinners when the parameter selector changes
		parameterSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateModels(getSelectedParameter());
				minSpinner.setModel(minModel.getSpinnerModel());
				maxSpinner.setModel(maxModel.getSpinnerModel());
				deltaSpinner.setModel(deltaModel.getSpinnerModel());
			}
		});

		contentPanel.add(topPanel, "growx, wrap");
	}

	private void addBottomPanel(JPanel contentPanel) {
		JPanel bottomPanel = new JPanel(new MigLayout("fill, ins 0"));

		// Close button
		JButton closeButton = new JButton(trans.get("dlg.but.close"));
		closeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ComponentAnalysisPlotExportDialog.this.dispose();
			}
		});
		bottomPanel.add(closeButton, "gapbefore push, split 2, right");

		// OK button
		this.okButton = new JButton(trans.get("SimulationConfigDialog.btn.plot"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getSelectedIndex() == PLOT_IDX) {
					JDialog dialog = ComponentAnalysisPlotExportDialog.this.plotTab.doPlot(ComponentAnalysisPlotExportDialog.this);
					if (dialog != null) {
						dialog.setVisible(true);
					}
				} else if (tabbedPane.getSelectedIndex() == EXPORT_IDX) {
					ComponentAnalysisPlotExportDialog.this.exportTab.doExport();
				}
			}
		});
		bottomPanel.add(okButton, "wrap");

		contentPanel.add(bottomPanel, "growx, wrap");
	}

	public CADomainDataType getSelectedParameter() {
		return (CADomainDataType) parameterSelector.getSelectedItem();
	}

	private void updateModels(CADomainDataType type) {
		if (type == null) {
			throw new IllegalArgumentException("CADomainDataType cannot be null");
		}
		// TODO: use the maxModel for the max value of minModel and vice versa?
		this.minModel = new DoubleModel(type, "Min", 0);
		this.maxModel = new DoubleModel(type, "Max", 0);
		this.deltaModel = new DoubleModel(type, "Delta", type.getMinDelta());
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
			List<RocketComponent> components = CADataType.calculateComponentsForType(rocket, type);
			componentCache.put(type, components);
		}

		isCacheValid = true;
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

	private double getParameterValue(CADomainDataType parameterType) {
		if (parameterType.equals(CADomainDataType.MACH)) {
			return parameters.getMach();
		}
		// Add more cases here as more parameter types are implemented
		else {
			throw new IllegalArgumentException("Unsupported parameter type: " + parameterType);
		}
	}

}
