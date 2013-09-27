package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.adaptors.FlightConfigurationModel;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

/**
 * Dialog for configuring all flight-configuration specific properties.
 * Content of individual tabs are in separate classes.
 */
public class FlightConfigurationDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	private final Rocket rocket;
	
	private FlightConfigurationModel flightConfigurationModel;
	
	private final JButton renameConfButton, removeConfButton, copyConfButton;
	
	private final MotorConfigurationPanel motorConfigurationPanel;
	private final RecoveryConfigurationPanel recoveryConfigurationPanel;
	private final SeparationConfigurationPanel separationConfigurationPanel;
	
	
	public FlightConfigurationDialog(final Rocket rocket, Window parent) {
		//// Edit motor configurations
		super(parent, trans.get("edtmotorconfdlg.title.Editmotorconf"), ModalityType.APPLICATION_MODAL);
		
		if (parent != null)
			this.setModalityType(ModalityType.DOCUMENT_MODAL);
		else
			this.setModalityType(ModalityType.APPLICATION_MODAL);
		
		this.rocket = rocket;
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		JLabel label = new JLabel(trans.get("edtmotorconfdlg.lbl.Selectedconf"));
		panel.add(label, "span, split");
		
		flightConfigurationModel = new FlightConfigurationModel(rocket.getDefaultConfiguration(), false);
		JComboBox configSelector = new JComboBox(flightConfigurationModel);
		configSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				configurationChanged();
			}
		});
		
		panel.add(configSelector, "growx, gapright para");
		
		JButton newConfButton = new JButton(trans.get("edtmotorconfdlg.but.Newconfiguration"));
		newConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addConfiguration();
			}
			
		});
		
		panel.add(newConfButton);
		
		renameConfButton = new JButton(trans.get("edtmotorconfdlg.but.Renameconfiguration"));
		renameConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renameConfiguration();
			}
		});
		panel.add(renameConfButton);
		
		removeConfButton = new JButton(trans.get("edtmotorconfdlg.but.Removeconfiguration"));
		removeConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeConfiguration();
			}
		});
		panel.add(removeConfButton);
		
		copyConfButton = new JButton(trans.get("edtmotorconfdlg.but.Copyconfiguration"));
		copyConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyConfiguration();
			}
		});
		panel.add(copyConfButton, "wrap para");
		
		
		//// Tabs for advanced view.
		JTabbedPane tabs = new JTabbedPane();
		panel.add(tabs, "grow, spanx, w 700lp, h 500lp, wrap");
		
		//// Motor tabs
		motorConfigurationPanel = new MotorConfigurationPanel(this, rocket);
		tabs.add(trans.get("edtmotorconfdlg.lbl.Motortab"), motorConfigurationPanel);
		//// Recovery tab
		recoveryConfigurationPanel = new RecoveryConfigurationPanel(this, rocket);
		tabs.add(trans.get("edtmotorconfdlg.lbl.Recoverytab"), recoveryConfigurationPanel);
		
		//// Stage tab
		separationConfigurationPanel = new SeparationConfigurationPanel(this, rocket);
		if (rocket.getStageCount() > 1) {
			tabs.add(trans.get("edtmotorconfdlg.lbl.Stagetab"), separationConfigurationPanel);
		}
		
		
		//// Close button
		JButton close = new JButton(trans.get("dlg.but.close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FlightConfigurationDialog.this.dispose();
			}
		});
		panel.add(close, "spanx, right");
		
		this.add(panel);
		this.validate();
		this.pack();
		
		updateButtonState();
		
		this.setLocationByPlatform(true);
		GUIUtil.setDisposableDialogOptions(this, close);
		
		// Undo description
		final OpenRocketDocument document = BasicFrame.findDocument(rocket);
		if (document != null) {
			//// Edit motor configurations
			document.startUndo(trans.get("edtmotorconfdlg.title.Editmotorconf"));
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					document.stopUndo();
				}
			});
		}
	}
	
	private void configurationChanged() {
		motorConfigurationPanel.fireTableDataChanged();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}
	
	private void addConfiguration() {
		String newId = rocket.newFlightConfigurationID();
		rocket.getDefaultConfiguration().setFlightConfigurationID(newId);
		
		// Create a new simulation for this configuration.
		createSimulationForNewConfiguration();
		
		configurationChanged();
	}
	
	private void copyConfiguration() {
		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		
		// currentID is the currently selected configuration.
		String newConfigId = rocket.newFlightConfigurationID();
		String oldName = rocket.getFlightConfigurationName(currentId);
		
		for (RocketComponent c : rocket) {
			if (c instanceof FlightConfigurableComponent) {
				((FlightConfigurableComponent) c).cloneFlightConfiguration(currentId, newConfigId);
			}
		}
		rocket.setFlightConfigurationName(currentId, oldName);
		rocket.getDefaultConfiguration().setFlightConfigurationID(newConfigId);
		
		// Create a new simulation for this configuration.
		createSimulationForNewConfiguration();
		
		configurationChanged();
	}
	
	/**
	 * prereq - assumes that the new configuration has been set as the default configuration.
	 */
	private void createSimulationForNewConfiguration() {
		Simulation newSim = new Simulation(rocket);
		OpenRocketDocument doc = BasicFrame.findDocument(rocket);
		newSim.setName(doc.getNextSimulationName());
		doc.addSimulation(newSim);
	}
	
	private void renameConfiguration() {
		new RenameConfigDialog(this, rocket).setVisible(true);
	}
	
	private void removeConfiguration() {
		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		if (currentId == null)
			return;
		rocket.removeFlightConfigurationID(currentId);
		rocket.getDefaultConfiguration().setFlightConfigurationID(null);
		configurationChanged();
	}
	
	private void updateButtonState() {
		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		removeConfButton.setEnabled(currentId != null);
		renameConfButton.setEnabled(currentId != null);
	}
	
}
