package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.EventObject;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.dialogs.flightconfiguration.RenameConfigDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
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
import net.sf.openrocket.gui.widgets.SelectColorButton;

@SuppressWarnings("serial")
public class FlightConfigurationPanel extends JPanel implements StateChangeListener {
	private static final Translator trans = Application.getTranslator();
	
	private final OpenRocketDocument document;
	private final Rocket rocket;

	private final BasicFrame basicFrame;
	private final JButton newConfButton, renameConfButton, removeConfButton, copyConfButton;
	
	private final JTabbedPane tabs;
	private final MotorConfigurationPanel motorConfigurationPanel;
	private final RecoveryConfigurationPanel recoveryConfigurationPanel;
	private final SeparationConfigurationPanel separationConfigurationPanel;

	private final static int MOTOR_TAB_INDEX = 0;
	private final static int RECOVERY_TAB_INDEX = 1;
	private final static int SEPARATION_TAB_INDEX = 2;

	public FlightConfigurationPanel(BasicFrame basicFrame, OpenRocketDocument doc) {
		super(new MigLayout("fill","[grow][][][][][grow]"));

		this.basicFrame = basicFrame;
		this.document = doc;
		this.rocket = doc.getRocket();
		this.rocket.addChangeListener(this);
		
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

		newConfButton = new SelectColorButton(trans.get("edtmotorconfdlg.but.Newconfiguration"));
		newConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newOrCopyConfigAction(false);
			}
			
		});
		
		this.add(newConfButton,"skip 1,gapright para");
		
		renameConfButton = new SelectColorButton(trans.get("edtmotorconfdlg.but.Renameconfiguration"));
		renameConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				renameConfiguration();
				configurationChanged(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
			}
		});
		this.add(renameConfButton,"gapright para");
		
		removeConfButton = new SelectColorButton(trans.get("edtmotorconfdlg.but.Removeconfiguration"));
		removeConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeConfiguration();
				configurationChanged(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
			}
		});
		this.add(removeConfButton,"gapright para");
		
		copyConfButton = new SelectColorButton(trans.get("edtmotorconfdlg.but.Copyconfiguration"));
		copyConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				newOrCopyConfigAction(true);
			}
		});
		this.add(copyConfButton, "wrap");

		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				// Trigger a selection of the motor/recovery/configuration item
				switch (tabs.getSelectedIndex()) {
					case MOTOR_TAB_INDEX:
						motorConfigurationPanel.updateButtonState();
						break;
					case RECOVERY_TAB_INDEX:
						recoveryConfigurationPanel.updateButtonState();
						break;
					case SEPARATION_TAB_INDEX:
						separationConfigurationPanel.updateButtonState();
						break;
				}
			}
		});

		updateButtonState();

		this.add(tabs, "spanx, grow, wrap rel");
	}

	/**
	 * Action for when the new configuration or copy configuration button is pressed.
	 * @param copy if True, then copy configuration operation, if False then create a new configuration
	 */
	private void newOrCopyConfigAction(boolean copy) {
		addOrCopyConfiguration(copy);
		configurationChanged(ComponentChangeEvent.MOTOR_CHANGE);
		stateChanged(null);
		switch (tabs.getSelectedIndex()) {
			case MOTOR_TAB_INDEX:
				motorConfigurationPanel.selectMotor();
				break;
			case RECOVERY_TAB_INDEX:
				recoveryConfigurationPanel.selectDeployment();
				break;
			case SEPARATION_TAB_INDEX:
				separationConfigurationPanel.selectSeparation();
				break;
		}
		configurationChanged(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);	// Trigger select
	}

	/**
	 * either create or copy configuration
	 * set new configuration as current
	 * create simulation for new configuration
	 */
	private void addOrCopyConfiguration(boolean copy) {
		FlightConfiguration newConfig;
		FlightConfigurationId newId;

		// create or copy configuration
		if (copy) {
			FlightConfigurationId oldId = this.motorConfigurationPanel.getSelectedConfigurationId();
			FlightConfiguration oldConfig = rocket.getFlightConfiguration(oldId);

			newConfig = oldConfig.copy(null);
			newId = newConfig.getId();

			for (RocketComponent c : rocket) {
				if (c instanceof FlightConfigurableComponent) {
					((FlightConfigurableComponent) c).copyFlightConfiguration(oldId, newId);
				}
			}
		} else {
			newConfig = new FlightConfiguration(rocket, null);
			newId = newConfig.getId();
		}
		
		// associate configuration with Id and select it
		rocket.setFlightConfiguration(newId, newConfig);
		rocket.setSelectedConfiguration(newId);

		// create simulation for configuration
		Simulation newSim = new Simulation(rocket);
		
		OpenRocketDocument doc = BasicFrame.findDocument(rocket);
        if (doc != null) {
            newSim.setName(doc.getNextSimulationName());
            doc.addSimulation(newSim);
        }
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
		configurationChanged(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	private void configurationChanged(int cce) {
		motorConfigurationPanel.fireTableDataChanged(cce);
		recoveryConfigurationPanel.fireTableDataChanged(cce);
		separationConfigurationPanel.fireTableDataChanged(cce);
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
		int recoveryDeviceCount = rocket.accept(new ListComponents<>(RecoveryDevice.class)).size();
		
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

	public void setSelectedComponent(RocketComponent component) {
		this.basicFrame.setSelectedComponent(component);
	}

	@Override
	public void stateChanged(EventObject e) {
		updateButtonState();
		motorConfigurationPanel.synchronizeConfigurationSelection();
		recoveryConfigurationPanel.synchronizeConfigurationSelection();
		separationConfigurationPanel.synchronizeConfigurationSelection();
	}
}
