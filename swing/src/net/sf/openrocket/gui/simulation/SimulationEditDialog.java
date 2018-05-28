package net.sf.openrocket.gui.simulation;


import java.awt.CardLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import net.sf.openrocket.gui.components.ConfigurationComboBox;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.simulation.extension.SimulationExtension;
import net.sf.openrocket.startup.Application;


public class SimulationEditDialog extends JDialog {
	private static final long serialVersionUID = -4468157685542912715L;
	private final Window parentWindow;
	private final Simulation[] simulationList;
	private final OpenRocketDocument document;
	private static final Translator trans = Application.getTranslator();
	
	JPanel cards;
	private final static String EDITMODE = "EDIT";
	private final static String PLOTMODE = "PLOT";
	
	public SimulationEditDialog(Window parent, final OpenRocketDocument document, Simulation... sims) {
		//// Edit simulation
		super(parent, trans.get("simedtdlg.title.Editsim"), JDialog.ModalityType.DOCUMENT_MODAL);
		this.document = document;
		this.parentWindow = parent;
		this.simulationList = sims;
		
		this.cards = new JPanel(new CardLayout());
		this.add(cards);
		buildEditCard();
		buildPlotCard();
		
		this.validate();
		this.pack();
		
		this.setLocationByPlatform(true);
		
		GUIUtil.setDisposableDialogOptions(this, null);
	}
	
	private boolean isSingleEdit() {
		return simulationList.length == 1;
	}
	
	private boolean allowsPlotMode() {
		return simulationList.length == 1 && simulationList[0].hasSimulationData();
	}
	
	public void setEditMode() {
		CardLayout cl = (CardLayout) (cards.getLayout());
		cl.show(cards, EDITMODE);
		cards.validate();
	}
	
	public void setPlotMode() {
		if (!allowsPlotMode()) {
			return;
		}
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
		JPanel simEditPanel = new JPanel(new MigLayout("fill"));
		
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
					//System.out.println("Setting name:" + name);
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
			final ConfigurationComboBox configComboBox = new ConfigurationComboBox(rkt);
			
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
		JButton button = new JButton(trans.get("SimulationEditDialog.btn.plot") + " >>");
		button.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				SimulationEditDialog.this.setPlotMode();
			}
			
		});
		simEditPanel.add(button, "spanx, split 3, align left");
		if (allowsPlotMode()) {
			button.setVisible(true);
		} else {
			button.setVisible(false);
		}
		
		//// Run simulation button
		button = new JButton(trans.get("SimulationEditDialog.btn.simulateAndPlot"));
		if (!isSingleEdit()) {
			button.setText(trans.get("SimulationEditDialog.btn.simulate"));
		}
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyChangesToAllSims();
				SimulationRunDialog.runSimulations(parentWindow, SimulationEditDialog.this.document, simulationList);
				refreshView();
				if (allowsPlotMode()) {
					setPlotMode();
				} else {
					setVisible(false);
				}
			}
		});
		simEditPanel.add(button, " align right, tag ok");
		
		//// Close button 
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyChangesToAllSims();
				SimulationEditDialog.this.dispose();
			}
		});
		simEditPanel.add(close, "tag ok");
		
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
			
			JButton button = new JButton("<< " + trans.get("SimulationEditDialog.btn.edit"));
			button.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) {
					SimulationEditDialog.this.setEditMode();
				}
				
			});
			
			plotExportPanel.add(button, "spanx, split 3, align left");
			
			final JButton ok = new JButton(trans.get("SimulationEditDialog.btn.plot"));
			
			tabbedPane.addChangeListener(new ChangeListener() {
				
				@Override
				public void stateChanged(ChangeEvent e) {
					int selectedIndex = tabbedPane.getSelectedIndex();
					switch (selectedIndex) {
					case 0:
						ok.setText(trans.get("SimulationEditDialog.btn.plot"));
						break;
					case 1:
						ok.setText(trans.get("SimulationEditDialog.btn.export"));
						break;
					}
				}
				
			});
			
			ok.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					// If the simulation is out of date, run the simulation.
					if (simulationList[0].getStatus() != Simulation.Status.UPTODATE) {
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
			JButton close = new JButton(trans.get("dlg.but.close"));
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
}
