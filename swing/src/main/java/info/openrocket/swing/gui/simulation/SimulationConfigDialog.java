package info.openrocket.swing.gui.simulation;

import net.miginfocom.swing.MigLayout;
import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.document.events.DocumentChangeEvent;
import info.openrocket.swing.gui.components.ConfigurationComboBox;
import info.openrocket.swing.gui.components.StyledLabel;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.widgets.SelectColorButton;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.FlightConfiguration;
import info.openrocket.core.rocketcomponent.FlightConfigurationId;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.simulation.extension.SimulationExtension;
import info.openrocket.core.startup.Application;
import info.openrocket.core.startup.Preferences;
import info.openrocket.core.util.StateChangeListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

public class SimulationConfigDialog extends JDialog {
	private static final long serialVersionUID = -1068127685642912715L;

	private final Window parentWindow;
	private final Simulation[] simulationList;
	private final OpenRocketDocument document;
	private final JTabbedPane tabbedPane;
	private JButton okButton;
	private JButton cancelButton;
	private static final Translator trans = Application.getTranslator();
	private static final Preferences preferences = Application.getPreferences();


	private final WindowListener applyChangesToSimsListener;
	private final Simulation initialSim;		// A copy of the first selected simulation before it was modified
	private final boolean initialIsSaved;		// Whether the document was saved before the dialog was opened
	private boolean isModified = false;			// Whether the simulation has been modified
	private final boolean isNewSimulation;		// Whether you are editing a new simulation, or an existing one

	private static final int SETTINGS_IDX = 0;
	private static final int WARNINGS_IDX = 1;
	private static final int PLOT_IDX = 2;
	private static final int EXPORT_IDX = 3;

	private final SimulationPlotPanel plotTab;
	private final SimulationExportPanel exportTab;

	private static Color multiCompEditColor;

	static {
		initColors();
	}

	public SimulationConfigDialog(Window parent, final OpenRocketDocument document, boolean isNewSimulation, Simulation... sims) {
		super(parent, sims.length == 1 ? trans.get("simedtdlg.title.Editsim") : trans.get("simedtdlg.title.MultiSimEdit"),
				JDialog.ModalityType.DOCUMENT_MODAL);
		this.document = document;
		this.parentWindow = parent;
		this.simulationList = sims;
		this.initialSim = simulationList[0].clone();
		this.initialIsSaved = document.isSaved();
		this.isNewSimulation = isNewSimulation;

		simulationList[0].addChangeListener(new StateChangeListener() {
			@Override
			public void stateChanged(EventObject e) {
				isModified = true;
				setTitle("* " + getTitle());			// Add component changed indicator to the title
				simulationList[0].removeChangeListener(this);
			}
		});

		final JPanel contentPanel = new JPanel(new MigLayout("fill"));

		// ======== Top panel ========
		addTopPanel(document, contentPanel);


		// ======== Tabbed pane ========
		this.tabbedPane = new JTabbedPane();

		//// Simulation Settings
		final SimulationSettingsPanel settingsTab = new SimulationSettingsPanel(document, simulationList[0]);
		tabbedPane.addTab(trans.get("SimulationConfigDialog.tab.Settings"), settingsTab);

		//// Simulation Warnings
		final SimulationWarningsPanel warningsTab = new SimulationWarningsPanel(simulationList[0]);
		tabbedPane.addTab(trans.get("SimulationConfigDialog.tab.Warnings"), warningsTab);
		if (isMultiCompEdit()) {
			tabbedPane.setEnabledAt(WARNINGS_IDX, false);
			tabbedPane.setToolTipTextAt(WARNINGS_IDX, trans.get("SimulationConfigDialog.tab.warnDis.ttip"));
		}

		//// Plot data
		boolean hasData = simulationList[0].hasSimulationData();
		if (hasData) {
			this.plotTab = new SimulationPlotPanel(simulationList[0]);
		} else {
			this.plotTab = null;
		}
		tabbedPane.addTab(trans.get("SimulationConfigDialog.tab.Plotdata"), plotTab);
		if (isMultiCompEdit() || !hasData) {
			tabbedPane.setEnabledAt(PLOT_IDX, false);
			String ttip = hasData ? trans.get("SimulationConfigDialog.tab.plotDis.ttip") : trans.get("SimulationConfigDialog.tab.plotNoData.ttip");
			tabbedPane.setToolTipTextAt(PLOT_IDX, ttip);
		}

		//// Export data
		if (hasData) {
			this.exportTab = new SimulationExportPanel(simulationList[0]);
		} else {
			this.exportTab = null;
		}
		tabbedPane.addTab(trans.get("SimulationConfigDialog.tab.Exportdata"), exportTab);
		if (isMultiCompEdit() || !hasData) {
			tabbedPane.setEnabledAt(EXPORT_IDX, false);
			String ttip = hasData ? trans.get("SimulationConfigDialog.tab.expDis.ttip") : trans.get("SimulationConfigDialog.tab.expNoData.ttip");
			tabbedPane.setToolTipTextAt(EXPORT_IDX, ttip);
		}

		contentPanel.add(tabbedPane, "grow, wrap");


		// ======== Bottom panel ========
		addBottomPanel(contentPanel);


		tabbedPane.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				if (okButton == null) {
					return;
				}
				int selectedIndex = tabbedPane.getSelectedIndex();
				switch (selectedIndex) {
					case SETTINGS_IDX:
						okButton.setText(trans.get("dlg.but.ok"));
						cancelButton.setText(trans.get("dlg.but.cancel"));
						cancelButton.setVisible(true);
						SimulationConfigDialog.this.revalidate();
						break;
					case WARNINGS_IDX:
						okButton.setText(trans.get("dlg.but.close"));
						cancelButton.setVisible(false);
						SimulationConfigDialog.this.revalidate();
						break;
					case PLOT_IDX:
						okButton.setText(trans.get("SimulationConfigDialog.btn.plot"));
						cancelButton.setText(trans.get("dlg.but.close"));
						cancelButton.setVisible(true);
						SimulationConfigDialog.this.revalidate();
						break;
					case EXPORT_IDX:
						okButton.setText(trans.get("SimulationConfigDialog.btn.export"));
						cancelButton.setText(trans.get("dlg.but.close"));
						cancelButton.setVisible(true);
						SimulationConfigDialog.this.revalidate();
						break;
				}
			}

		});

		this.add(contentPanel);
		this.validate();
		this.pack();

		this.setLocationByPlatform(true);

		this.applyChangesToSimsListener = new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				copyChangesToAllSims();
			}
		};
		this.addWindowListener(applyChangesToSimsListener);

		GUIUtil.setDisposableDialogOptions(this, null);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SimulationConfigDialog::updateColors);
	}

	private static void updateColors() {
		multiCompEditColor = GUIUtil.getUITheme().getMultiCompEditColor();
	}

	public void switchToSettingsTab() {
		tabbedPane.setSelectedIndex(SETTINGS_IDX);
	}

	public void switchToWarningsTab() {
		tabbedPane.setSelectedIndex(WARNINGS_IDX);
	}

	public void switchToPlotTab() {
		tabbedPane.setSelectedIndex(PLOT_IDX);
	}

	public void switchToExportTab() {
		tabbedPane.setSelectedIndex(EXPORT_IDX);
	}

	private void addTopPanel(OpenRocketDocument document, JPanel contentPanel) {
		JPanel topPanel = new JPanel(new MigLayout("fill, ins 0"));

		//// Name:
		topPanel.add(new JLabel(trans.get("simedtdlg.lbl.Simname") + " "), "growx 0, gapright para");
		final JTextField field = new JTextField(simulationList[0].getName());
		field.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				setText();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				setText();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				setText();
			}

			private void setText() {
				String name = field.getText();
				if (name == null || name.equals(""))
					return;
				simulationList[0].setName(name);

			}
		});
		topPanel.add(field, "growx, wrap");

		//// Flight selector
		//// Flight configuration:
		JLabel label = new JLabel(trans.get("simedtdlg.lbl.Flightcfg"));
		//// Select the motor configuration to use.
		label.setToolTipText(trans.get("simedtdlg.lbl.ttip.Flightcfg"));
		topPanel.add(label, "growx 0, gapright para");

		final Rocket rkt = document.getRocket();
		final FlightConfiguration config = rkt.getFlightConfiguration(simulationList[0].getFlightConfigurationId());
		final ConfigurationComboBox configComboBox = new ConfigurationComboBox(rkt, false);
		configComboBox.setSelectedItem(config);

		//// Select the motor configuration to use.
		configComboBox.setToolTipText(trans.get("simedtdlg.combo.ttip.Flightcfg"));
		configComboBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FlightConfiguration config = (FlightConfiguration)configComboBox.getSelectedItem();
				FlightConfigurationId id = config.getId();

				simulationList[0].setFlightConfigurationId( id );
			}
		});
		topPanel.add(configComboBox, "span");

		topPanel.add(new JPanel(), "growx, wrap");

		contentPanel.add(topPanel, "growx, wrap");
	}

	private void addBottomPanel(JPanel contentPanel) {
		final JPanel bottomPanel = new JPanel(new MigLayout("fill, ins 0"));

		//// Multi-simulation edit
		if (isMultiCompEdit()) {
			StyledLabel multiSimEditLabel = new StyledLabel("", -1, StyledLabel.Style.BOLD);
			multiSimEditLabel.setFontColor(multiCompEditColor);
			multiSimEditLabel.setText(trans.get("simedtdlg.title.MultiSimEdit"));
			StringBuilder components = new StringBuilder(trans.get("simedtdlg.title.MultiSimEdit.ttip"));
			for (int i = 0; i < simulationList.length; i++) {
				if (i < simulationList.length - 1) {
					components.append(simulationList[i].getName()).append(", ");
				} else {
					components.append(simulationList[i].getName());
				}
			}
			multiSimEditLabel.setToolTipText(components.toString());
			bottomPanel.add(multiSimEditLabel, "align left");
		}

		//// Run simulation button
		// TODO: disable when sim is up to date?
		/*JButton button = new SelectColorButton(trans.get("SimulationEditDialog.btn.simulateAndPlot"));
		if (!isSingleEdit()) {
			button.setText(trans.get("SimulationEditDialog.btn.simulate"));
		}
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyChangesToAllSims();
				SimulationRunDialog dialog = SimulationRunDialog.runSimulations(parentWindow, SimulationEditDialog.this.document, simulationList);
				if (allowsPlotMode() && dialog.isAllSimulationsSuccessful()) {
					refreshView();
					setPlotMode();
				}
			}
		});
		simEditPanel.add(button, "align right, gapright 10lp, tag ok");*/

		//// Cancel button
		this.cancelButton = new SelectColorButton(trans.get("dlg.but.cancel"));
		this.cancelButton.setToolTipText(trans.get("SimulationConfigDialog.btn.Cancel.ttip"));
		this.cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (tabbedPane.getSelectedIndex() == SETTINGS_IDX) {
					cancelSimEdit();
				} else {
					// Normal close action
					closeDialog();
				}

				// TODO: include plot/export undo?
			}
		});
		bottomPanel.add(this.cancelButton, "split 2, tag ok");

		//// Ok button
		this.okButton = new SelectColorButton(trans.get("dlg.but.ok"));
		this.okButton.setToolTipText(trans.get("SimulationConfigDialog.btn.OK.ttip"));
		this.okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyChangesToAllSims();

				// Run outdated simulations
				Simulation[] outdatedSims = getOutdatedSimulations();
				if (outdatedSims.length > 0) {
					new SimulationRunDialog(SimulationConfigDialog.this.parentWindow, document, outdatedSims).setVisible(true);
				}

				int tabIdx = tabbedPane.getSelectedIndex();
				if (tabIdx == PLOT_IDX) {
					if (plotTab == null) {
						closeDialog();
						return;
					}
					JDialog plot = plotTab.doPlot(SimulationConfigDialog.this.parentWindow);
					if (plot != null) {
						plot.setVisible(true);
					}
					closeDialog();
					return;
				} else if (tabIdx == EXPORT_IDX) {
					if (exportTab == null || exportTab.doExport()) {
						closeDialog();
					}
					return;
				}

				closeDialog();
			}
		});
		bottomPanel.add(this.okButton, "tag ok");

		contentPanel.add(bottomPanel, "growx, wrap");
	}

	private void copyChangesToAllSims() {
		if (isMultiCompEdit()) {
			for (int i = 1; i < simulationList.length; i++) {
				simulationList[i].getOptions().copyConditionsFrom(simulationList[0].getOptions());
				simulationList[i].getSimulationExtensions().clear();
				for (SimulationExtension c : simulationList[0].getSimulationExtensions()) {
					simulationList[i].getSimulationExtensions().add(c.clone());
				}
			}
		}
	}

	private Simulation[] getOutdatedSimulations() {
		List<Simulation> outdated = new ArrayList<>();
		for (Simulation sim : simulationList) {
			if (!Simulation.isStatusUpToDate(sim.getStatus())) {
				outdated.add(sim);
			}
		}
		return outdated.toArray(new Simulation[0]);
	}

	private boolean isMultiCompEdit() {
		return simulationList.length > 1;
	}

	private void closeDialog() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SimulationConfigDialog.this.removeWindowListener(applyChangesToSimsListener);
				SimulationConfigDialog.this.dispose();
			}
		});
	}

	private JPanel createCancelOperationContent() {
		JPanel panel = new JPanel(new MigLayout());
		String msg = isNewSimulation ? trans.get("SimulationConfigDialog.CancelOperation.msg.undoAdd") :
				trans.get("SimulationConfigDialog.CancelOperation.msg.discardChanges");
		JLabel msgLabel = new JLabel(msg);
		JCheckBox dontAskAgain = new JCheckBox(trans.get("SimulationConfigDialog.CancelOperation.checkbox.dontAskAgain"));
		dontAskAgain.setSelected(false);
		dontAskAgain.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					preferences.setShowDiscardSimulationConfirmation(false);
				}
				// Unselected state should be not be possible and thus not be handled
			}
		});

		panel.add(msgLabel, "left, wrap");
		panel.add(dontAskAgain, "left, gaptop para");

		return panel;
	}

	private void cancelSimEdit() {
		// Don't do anything on cancel if you are editing an existing simulation, and it is not modified
		if (!isNewSimulation && !isModified) {
			closeDialog();
			return;
		}

		// Apply the cancel operation if set to auto discard in preferences
		if (!preferences.isShowDiscardSimulationConfirmation()) {
			discardChanges();
			return;
		}

		// Yes/No dialog: Are you sure you want to discard your changes?
		JPanel msg = createCancelOperationContent();
		int resultYesNo = JOptionPane.showConfirmDialog(SimulationConfigDialog.this, msg,
				trans.get("SimulationConfigDialog.CancelOperation.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (resultYesNo == JOptionPane.YES_OPTION) {
			discardChanges();
		}
	}

	private void discardChanges() {
		if (isNewSimulation) {
			document.removeSimulation(simulationList[0]);
		} else {
			undoSimulationChanges();
		}
		document.setSaved(this.initialIsSaved);			// Restore the saved state of the document
		document.fireDocumentChangeEvent(new DocumentChangeEvent(this));

		closeDialog();
	}

	private void undoSimulationChanges() {
		if (simulationList == null || simulationList.length == 0) {
			return;
		}
		simulationList[0].loadFrom(initialSim);
	}
}
