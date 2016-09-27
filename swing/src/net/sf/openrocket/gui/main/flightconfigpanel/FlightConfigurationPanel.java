package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.dialogs.flightconfiguration.RenameConfigDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.FlightConfiguration;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.RecoveryDevice;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.rocketvisitors.ListComponents;
import net.sf.openrocket.rocketvisitors.ListMotorMounts;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.StateChangeListener;

public class FlightConfigurationPanel extends JPanel implements StateChangeListener {
	private static final long serialVersionUID = -5467500312467789009L;
	//private static final Logger log = LoggerFactory.getLogger(FlightConfigurationPanel.class);
	private static final Translator trans = Application.getTranslator();
	
	private final OpenRocketDocument document;
	private final Rocket rocket;
	
	private final JButton newConfButton, renameConfButton, removeConfButton, copyConfButton;
	
	private final JTabbedPane tabs;
	private final MotorConfigurationPanel motorConfigurationPanel;
	private final RecoveryConfigurationPanel recoveryConfigurationPanel;
	private final SeparationConfigurationPanel separationConfigurationPanel;

	private final static int MOTOR_TAB_INDEX = 0;
	private final static int RECOVERY_TAB_INDEX = 1;
	private final static int SEPARATION_TAB_INDEX = 2;

	@Override
	public void stateChanged(EventObject e) {
		updateButtonState();
	}

	public FlightConfigurationPanel(OpenRocketDocument doc) {
		super(new MigLayout("fill","[grow][][][][][grow]"));
		
		this.document = doc;
		this.rocket = doc.getRocket();
		
		//JPanel panel = new JPanel(new MigLayout("fill","[grow][][][][][grow]"));
		
		//// Tabs for advanced view.
		tabs = new JTabbedPane();
		
		//// Motor tabs
		motorConfigurationPanel = new MotorConfigurationPanel(this, rocket);
		tabs.add(trans.get("edtmotorconfdlg.lbl.Motortab"), motorConfigurationPanel);
		//// Recovery tab
		recoveryConfigurationPanel = new RecoveryConfigurationPanel(this, rocket);
		tabs.add(trans.get("edtmotorconfdlg.lbl.Recoverytab"), recoveryConfigurationPanel);
		
		//// Stage tab
		separationConfigurationPanel = new SeparationConfigurationPanel(this, rocket);
		tabs.add(trans.get("edtmotorconfdlg.lbl.Stagetab"), separationConfigurationPanel);

		newConfButton = new JButton(trans.get("edtmotorconfdlg.but.Newconfiguration"));
		newConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addConfiguration();
				configurationChanged();
			}
			
		});
		
		this.add(newConfButton,"skip 1,gapright para");
		
		renameConfButton = new JButton(trans.get("edtmotorconfdlg.but.Renameconfiguration"));
		renameConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renameConfiguration();
				configurationChanged();
			}
		});
		this.add(renameConfButton,"gapright para");
		
		removeConfButton = new JButton(trans.get("edtmotorconfdlg.but.Removeconfiguration"));
		removeConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeConfiguration();
				configurationChanged();
			}
		});
		this.add(removeConfButton,"gapright para");
		
		copyConfButton = new JButton(trans.get("edtmotorconfdlg.but.Copyconfiguration"));
		copyConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyConfiguration();
				configurationChanged();
			}
		});
		this.add(copyConfButton, "wrap");

		updateButtonState();

		this.add(tabs, "spanx, grow, wrap rel");

	}
	
	private void addConfiguration() {
		FlightConfigurationId newFCID = new FlightConfigurationId();
		FlightConfiguration newConfig = new FlightConfiguration( rocket, newFCID );
		
		rocket.setFlightConfiguration(newFCID, newConfig);
		
		// Create a new simulation for this configuration.
		createSimulationForNewConfiguration();
		
		configurationChanged();
	}
	
	private void copyConfiguration() {
		FlightConfiguration oldConfig = rocket.getSelectedConfiguration();
		FlightConfiguration newConfig = oldConfig.clone();
		FlightConfigurationId oldId = oldConfig.getFlightConfigurationID();
		FlightConfigurationId newId = newConfig.getFlightConfigurationID();
		
		for (RocketComponent c : rocket) {
			if (c instanceof FlightConfigurableComponent) {
				((FlightConfigurableComponent) c).cloneFlightConfiguration(oldId, newId);
			}
		}
		rocket.setFlightConfiguration(newId, newConfig);
		
		// Create a new simulation for this configuration.
		createSimulationForNewConfiguration();
		
		configurationChanged();
	}
	
	private void renameConfiguration() {
		FlightConfigurationId currentId = this.motorConfigurationPanel.getSelectedConfigurationId();
		new RenameConfigDialog(SwingUtilities.getWindowAncestor(this), rocket, currentId).setVisible(true);
	}
	
	private void removeConfiguration() {
		FlightConfigurationId currentId = this.motorConfigurationPanel.getSelectedConfigurationId();
		if (currentId == null)
			return;
		document.removeFlightConfigurationAndSimulations(currentId);
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
	
	private void configurationChanged() {
		motorConfigurationPanel.fireTableDataChanged();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}
	
	private void updateButtonState() {
		FlightConfigurationId currentId = rocket.getSelectedConfiguration().getFlightConfigurationID();
		// Enable the remove/rename/copy buttons only when a configuration is selected.
		removeConfButton.setEnabled(currentId.isValid());
		renameConfButton.setEnabled(currentId.isValid());
		copyConfButton.setEnabled(currentId.isValid());
		
		// Count the number of motor mounts
		int motorMountCount = rocket.accept(new ListMotorMounts()).size();
		
		// Count the number of recovery devices
		int recoveryDeviceCount = rocket.accept(new ListComponents<RecoveryDevice>(RecoveryDevice.class)).size();
		
		// Count the number of stages
		int stageCount = rocket.getStageCount();
		
		// Enable the new configuration button only when a motor mount is defined.
		newConfButton.setEnabled(motorMountCount > 0);
		
		// Only enable the recovery tab if there is a motor mount and there is a recovery device
		tabs.setEnabledAt(RECOVERY_TAB_INDEX, motorMountCount > 0 && recoveryDeviceCount > 0);
		
		// If the selected tab was the recovery tab, and there is no longer any recovery devices,
		// switch to the motor tab.
		if( recoveryDeviceCount == 0 && tabs.getSelectedIndex() == RECOVERY_TAB_INDEX ) {
			tabs.setSelectedIndex(MOTOR_TAB_INDEX);
		}
		
		tabs.setEnabledAt(SEPARATION_TAB_INDEX, motorMountCount > 0 && stageCount > 1);
		
		if ( stageCount ==1 && tabs.getSelectedIndex() == SEPARATION_TAB_INDEX ) {
			tabs.setSelectedIndex(MOTOR_TAB_INDEX);
		}

	}
	
}
