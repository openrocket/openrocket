package net.sf.openrocket.gui.simulation;


import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.document.events.DocumentChangeEvent;
import net.sf.openrocket.gui.components.ConfigurationComboBox;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.extension.SimulationExtension;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.startup.Preferences;
import net.sf.openrocket.util.StateChangeListener;


public class SimulationEditDialog extends JDialog {
	private static final long serialVersionUID = -4468157685542912715L;
	private final Window parentWindow;
	private final Simulation[] simulationList;
	private final OpenRocketDocument document;
	private static final Translator trans = Application.getTranslator();
	private static final Preferences preferences = Application.getPreferences();
	
	JPanel cards;
	private final static String EDITMODE = "EDIT";
	private final static String PLOTMODE = "PLOT";

	private final WindowListener applyChangesToSimsListener;
	private final Simulation initialSim;		// A copy of the first selected simulation before it was modified
	private final boolean initialIsSaved;		// Whether the document was saved before the dialog was opened
	private boolean isModified = false;			// Whether the simulation has been modified
	private final boolean isNewSimulation;		// Whether you are editing a new simulation, or an existing one
	
	public SimulationEditDialog(Window parent, final OpenRocketDocument document, boolean isNewSimulation, Simulation... sims) {
		//// Edit simulation
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
		
		this.cards = new JPanel(new CardLayout());
		this.add(cards);
		buildEditCard();
		buildPlotCard();
		
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
	
	private boolean isSingleEdit() {
		return simulationList.length == 1;
	}
	
	private boolean allowsPlotMode() {
		return simulationList.length == 1 && simulationList[0].hasSimulationData();
	}
	
	public void setEditMode() {
		String baseTitle = simulationList.length == 1 ?
				trans.get("simedtdlg.title.Editsim") : trans.get("simedtdlg.title.MultiSimEdit");
		setTitle((isModified ? "* " : "") + baseTitle);
		CardLayout cl = (CardLayout) (cards.getLayout());
		cl.show(cards, EDITMODE);
		cards.validate();
		this.addWindowListener(applyChangesToSimsListener);
	}
	
	public void setPlotMode() {
		if (!allowsPlotMode()) {
			return;
		}
		this.removeWindowListener(applyChangesToSimsListener);
		setTitle((isModified ? "* " : "") + trans.get("simplotpanel.title.Plotsim"));
		CardLayout cl = (CardLayout) (cards.getLayout());
		cl.show(cards, PLOTMODE);
		cards.validate();
	}
	
	private void copyChangesToAllSims() {
		if (simulationList.length > 1) {
			for (int i = 1; i < simulationList.length; i++) {
				simulationList[i].getOptions().copyConditionsFrom(simulationList[0].getOptions());
				simulationList[i].getSimulationExtensions().clear();
				for (SimulationExtension c : simulationList[0].getSimulationExtensions()) {
					simulationList[i].getSimulationExtensions().add(c.clone());
				}
			}
		}
	}
	
	private void refreshView() {
		cards.removeAll();
		buildEditCard();
		buildPlotCard();
		this.validate();
	}
	
	private void buildEditCard() {
		JPanel simEditPanel = new JPanel(new MigLayout("fill, hidemode 1"));
		
		if (isSingleEdit()) {
			JPanel panel = new JPanel(new MigLayout("fill, ins 0"));
			
			//// Simulation name:
			panel.add(new JLabel(trans.get("simedtdlg.lbl.Simname") + " "), "growx 0, gapright para");
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
			panel.add(field, "growx, wrap");
			
			//// Flight selector
			//// Flight configuration:
			JLabel label = new JLabel(trans.get("simedtdlg.lbl.Flightcfg"));
			//// Select the motor configuration to use.
			label.setToolTipText(trans.get("simedtdlg.lbl.ttip.Flightcfg"));
			panel.add(label, "growx 0, gapright para");
			
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
			panel.add(configComboBox, "span");
			
			panel.add(new JPanel(), "growx, wrap");
			
			simEditPanel.add(panel, "growx, wrap");
		}
		JTabbedPane tabbedPane = new JTabbedPane();
		
		//// Launch conditions
		tabbedPane.addTab(trans.get("simedtdlg.tab.Launchcond"), new SimulationConditionsPanel(simulationList[0]));
		//// Simulation options
		tabbedPane.addTab(trans.get("simedtdlg.tab.Simopt"), new SimulationOptionsPanel(document, simulationList[0]));
		
		tabbedPane.setSelectedIndex(0);
		
		simEditPanel.add(tabbedPane, "spanx, grow, wrap");
		
		
		//// Open Plot button
		JButton button = new SelectColorButton(trans.get("SimulationEditDialog.btn.plot") + " >>");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationEditDialog.this.setPlotMode();
			}
			
		});
		simEditPanel.add(button, "spanx, split 5, align left");
		if (allowsPlotMode()) {
			button.setVisible(true);
		} else {
			button.setVisible(false);
		}

		//// Multi-simulation edit
		if (simulationList.length > 1) {
			StyledLabel multiSimEditLabel = new StyledLabel("", -1, StyledLabel.Style.BOLD);
			multiSimEditLabel.setFontColor(new Color(170, 0, 100));
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
			simEditPanel.add(multiSimEditLabel, "align left");
		}
		
		//// Run simulation button
		button = new SelectColorButton(trans.get("SimulationEditDialog.btn.simulateAndPlot"));
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
		simEditPanel.add(button, "align right, gapright 10lp, tag ok");

		//// Cancel button
		JButton cancelButton = new SelectColorButton(trans.get("dlg.but.cancel"));
		cancelButton.setToolTipText(trans.get("SimulationEditDialog.btn.Cancel.ttip"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// Don't do anything on cancel if you are editing an existing simulation, and it is not modified
				if (!isNewSimulation && !isModified) {
					SimulationEditDialog.this.removeWindowListener(applyChangesToSimsListener);
					SimulationEditDialog.this.dispose();
					return;
				}

				// Apply the cancel operation if set to auto discard in preferences
				if (!preferences.isShowDiscardSimulationConfirmation()) {
					discardChanges();
					return;
				}

				// Yes/No dialog: Are you sure you want to discard your changes?
				JPanel msg = createCancelOperationContent();
				int resultYesNo = JOptionPane.showConfirmDialog(SimulationEditDialog.this, msg,
						trans.get("SimulationEditDialog.CancelOperation.title"), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (resultYesNo == JOptionPane.YES_OPTION) {
					discardChanges();
				}
			}
		});
		simEditPanel.add(cancelButton, "tag ok");

		//// Ok button
		JButton okButton = new SelectColorButton(trans.get("dlg.but.ok"));
		okButton.setToolTipText(trans.get("SimulationEditDialog.btn.OK.ttip"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyChangesToAllSims();
				SimulationEditDialog.this.dispose();
			}
		});
		simEditPanel.add(okButton, "tag ok");
		
		cards.add(simEditPanel, EDITMODE);
	}
	
	private void buildPlotCard() {
		if (allowsPlotMode()) {
			JPanel plotExportPanel = new JPanel(new MigLayout("fill"));
			
			//// Simulation name:
			plotExportPanel.add(new JLabel(trans.get("simedtdlg.lbl.Simname") + " "), "span, split 2, shrink");
			final JTextField field = new JTextField(simulationList[0].getName());
			field.setEditable(false);
			plotExportPanel.add(field, "shrinky, growx, wrap");
			
			final JTabbedPane tabbedPane = new JTabbedPane();
			
			//// Plot data
			final SimulationPlotPanel plotTab = new SimulationPlotPanel(simulationList[0]);
			tabbedPane.addTab(trans.get("simedtdlg.tab.Plotdata"), plotTab);
			//// Export data
			final SimulationExportPanel exportTab = new SimulationExportPanel(simulationList[0]);
			tabbedPane.addTab(trans.get("simedtdlg.tab.Exportdata"), exportTab);
			
			plotExportPanel.add(tabbedPane, "grow, wrap");
			
			JButton button = new SelectColorButton("<< " + trans.get("SimulationEditDialog.btn.edit"));
			button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SimulationEditDialog.this.setEditMode();
				}
				
			});
			
			plotExportPanel.add(button, "spanx, split 3, align left");
			
			final JButton ok = new SelectColorButton(trans.get("SimulationEditDialog.btn.plot"));
			
			tabbedPane.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					int selectedIndex = tabbedPane.getSelectedIndex();
					switch (selectedIndex) {
					case 0:
						ok.setText(trans.get("SimulationEditDialog.btn.plot"));
						plotExportPanel.revalidate();
						break;
					case 1:
						ok.setText(trans.get("SimulationEditDialog.btn.export"));
						plotExportPanel.revalidate();
						break;
					}
				}
				
			});
			
			ok.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// If the simulation is out of date, run the simulation.
					if (!Simulation.isStatusUpToDate(simulationList[0].getStatus())) {
						new SimulationRunDialog(SimulationEditDialog.this.parentWindow, document, simulationList[0]).setVisible(true);
					}
					
					if (tabbedPane.getSelectedIndex() == 0) {
						JDialog plot = plotTab.doPlot(SimulationEditDialog.this.parentWindow);
						if (plot != null) {
							plot.setVisible(true);
						}
					} else {
						if (exportTab.doExport()) {
							SimulationEditDialog.this.dispose();
						}
					}
				}
			});
			plotExportPanel.add(ok, "tag ok, split 2");
			
			//// Close button 
			JButton close = new SelectColorButton(trans.get("dlg.but.close"));
			close.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					SimulationEditDialog.this.dispose();
				}
			});
			plotExportPanel.add(close, "tag cancel");
			//plotExportPanel.validate();
			cards.add(plotExportPanel, PLOTMODE);
			
		}
	}

	private JPanel createCancelOperationContent() {
		JPanel panel = new JPanel(new MigLayout());
		String msg = isNewSimulation ? trans.get("SimulationEditDialog.CancelOperation.msg.undoAdd") :
				trans.get("SimulationEditDialog.CancelOperation.msg.discardChanges");
		JLabel msgLabel = new JLabel(msg);
		JCheckBox dontAskAgain = new JCheckBox(trans.get("SimulationEditDialog.CancelOperation.checkbox.dontAskAgain"));
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

	private void discardChanges() {
		if (isNewSimulation) {
			document.removeSimulation(simulationList[0]);
		} else {
			undoSimulationChanges();
		}
		document.setSaved(this.initialIsSaved);			// Restore the saved state of the document
		document.fireDocumentChangeEvent(new DocumentChangeEvent(this));

		SimulationEditDialog.this.removeWindowListener(applyChangesToSimsListener);
		SimulationEditDialog.this.dispose();
	}

	private void undoSimulationChanges() {
		if (simulationList == null || simulationList.length == 0) {
			return;
		}
		simulationList[0].loadFrom(initialSim);
	}
}
