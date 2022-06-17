package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
	private final JButton newConfButton, renameConfButton, removeConfButton, duplicateConfButton;
	
	private final JTabbedPane tabs;
	private final MotorConfigurationPanel motorConfigurationPanel;
	private final RecoveryConfigurationPanel recoveryConfigurationPanel;
	private final SeparationConfigurationPanel separationConfigurationPanel;

	private final JPopupMenu popupMenuConfig;
	private final AbstractAction newConfigAction;
	private final AbstractAction renameConfigAction;
	private final AbstractAction removeConfigAction;
	private final AbstractAction duplicateConfigAction;

	private final static int MOTOR_TAB_INDEX = 0;
	private final static int RECOVERY_TAB_INDEX = 1;
	private final static int SEPARATION_TAB_INDEX = 2;

	public FlightConfigurationPanel(BasicFrame basicFrame, OpenRocketDocument doc) {
		super(new MigLayout("fill","[grow][][][][][grow]"));

		this.basicFrame = basicFrame;
		this.document = doc;
		this.rocket = doc.getRocket();
		this.rocket.addChangeListener(this);

		// Populate the popup menu
		popupMenuConfig = new JPopupMenu();
		newConfigAction = new NewConfigAction();
		renameConfigAction = new RenameConfigAction();
		removeConfigAction = new RemoveConfigAction();
		duplicateConfigAction = new DuplicateConfigAction();
		popupMenuConfig.add(newConfigAction);
		popupMenuConfig.add(renameConfigAction);
		popupMenuConfig.add(removeConfigAction);
		popupMenuConfig.add(duplicateConfigAction);
		
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

		//// New configuration
		newConfButton = new SelectColorButton(newConfigAction);
		this.add(newConfButton,"skip 1,gapright para");

		//// Rename configuration
		renameConfButton = new SelectColorButton(renameConfigAction);
		this.add(renameConfButton,"gapright para");

		//// Remove configuration
		removeConfButton = new SelectColorButton(removeConfigAction);
		this.add(removeConfButton,"gapright para");

		//// Duplicate configuration
		duplicateConfButton = new SelectColorButton(duplicateConfigAction);
		this.add(duplicateConfButton, "wrap");

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

		this.add(tabs, "spanx, grow, pushy, wrap rel");
	}

	/**
	 * Action for when the new configuration or duplicate configuration button is pressed.
	 * @param duplicate if True, then duplicate configuration operation, if False then create a new configuration
	 */
	private void newOrDuplicateConfigAction(boolean duplicate) {
		addOrDuplicateConfiguration(duplicate);
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
	 * either create or duplicate configuration
	 * set new configuration as current
	 * create simulation for new configuration
	 */
	private void addOrDuplicateConfiguration(boolean duplicate) {
		final Map<FlightConfigurationId, FlightConfiguration> newConfigs = new LinkedHashMap<>();

		// create or duplicate configuration
		if (duplicate) {
			List<FlightConfigurationId> oldIds = getSelectedConfigurationIds();
			if (oldIds == null || oldIds.size() == 0) return;

			for (FlightConfigurationId oldId : oldIds) {
				final FlightConfiguration oldConfig = rocket.getFlightConfiguration(oldId);

				final FlightConfiguration newConfig = oldConfig.copy(null);
				final FlightConfigurationId newId = newConfig.getId();

				for (RocketComponent c : rocket) {
					if (c instanceof FlightConfigurableComponent) {
						((FlightConfigurableComponent) c).copyFlightConfiguration(oldId, newId);
					}
				}

				newConfigs.put(newId, newConfig);
			}
		} else {
			final FlightConfiguration newConfig = new FlightConfiguration(rocket, null);
			final FlightConfigurationId newId = newConfig.getId();

			newConfigs.put(newId, newConfig);
		}

		OpenRocketDocument doc = BasicFrame.findDocument(rocket);
		if (doc == null) return;

		for (Map.Entry<FlightConfigurationId, FlightConfiguration> config : newConfigs.entrySet()) {
			// associate configuration with Id and select it
			rocket.setFlightConfiguration(config.getKey(), config.getValue());
			rocket.setSelectedConfiguration(config.getKey());

			// create simulation for configuration
			Simulation newSim = new Simulation(rocket);

			newSim.setName(doc.getNextSimulationName());
			doc.addSimulation(newSim);
		}

		// Reset to first selected flight config
		rocket.setSelectedConfiguration((FlightConfigurationId) newConfigs.keySet().toArray()[0]);
	}
	
	private void renameConfigurationAction() {
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if (fcIds == null) return;
		FlightConfigurationId initFcId = fcIds.get(0);
		new RenameConfigDialog(SwingUtilities.getWindowAncestor(this), rocket, initFcId).setVisible(true);
		String newName = rocket.getFlightConfiguration(initFcId).getNameRaw();
		for (int i = 1; i < fcIds.size(); i++) {
			rocket.getFlightConfiguration(fcIds.get(i)).setName(newName);
		}
		configurationChanged(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}
	
	private void removeConfigurationAction() {
		List<FlightConfigurationId> fcIds = getSelectedConfigurationIds();
		if (fcIds == null || fcIds.size() == 0)
			return;

		for (FlightConfigurationId fcId : fcIds) {
			document.removeFlightConfigurationAndSimulations(fcId);
		}

		configurationChanged(ComponentChangeEvent.NONFUNCTIONAL_CHANGE);
	}

	public void doPopupConfig(MouseEvent e) {
		popupMenuConfig.show(e.getComponent(), e.getX(), e.getY());
	}

	public AbstractAction getNewConfigAction() {
		return newConfigAction;
	}

	public AbstractAction getRenameConfigAction() {
		return renameConfigAction;
	}

	public AbstractAction getRemoveConfigAction() {
		return removeConfigAction;
	}

	public AbstractAction getDuplicateConfigAction() {
		return duplicateConfigAction;
	}

	private void configurationChanged(int cce) {
		motorConfigurationPanel.fireTableDataChanged(cce);
		recoveryConfigurationPanel.fireTableDataChanged(cce);
		separationConfigurationPanel.fireTableDataChanged(cce);
	}
	
	private void updateButtonState() {
		FlightConfigurationId currentId = rocket.getSelectedConfiguration().getFlightConfigurationID();
		// Enable the remove/rename/duplicate buttons only when a configuration is selected.
		removeConfButton.setEnabled(currentId.isValid());
		renameConfButton.setEnabled(currentId.isValid());
		duplicateConfButton.setEnabled(currentId.isValid());
		
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

	private List<FlightConfigurationId> getSelectedConfigurationIds() {
		switch (tabs.getSelectedIndex()) {
			case MOTOR_TAB_INDEX:
				return this.motorConfigurationPanel.getSelectedConfigurationIds();
			case RECOVERY_TAB_INDEX:
				return this.recoveryConfigurationPanel.getSelectedConfigurationIds();
			case SEPARATION_TAB_INDEX:
				return this.separationConfigurationPanel.getSelectedConfigurationIds();
			default:
				return null;
		}
	}
	
	public void setSelectedComponent(RocketComponent component) {
		this.basicFrame.setSelectedComponent(component);
	}

	public void setSelectedComponents(List<RocketComponent> components) {
		this.basicFrame.setSelectedComponents(components);
	}

	@Override
	public void stateChanged(EventObject e) {
		updateButtonState();
		motorConfigurationPanel.synchronizeConfigurationSelection();
		recoveryConfigurationPanel.synchronizeConfigurationSelection();
		separationConfigurationPanel.synchronizeConfigurationSelection();
	}

	private class NewConfigAction extends AbstractAction {
		public NewConfigAction() {
			putValue(NAME, trans.get("edtmotorconfdlg.but.Newconfiguration"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			newOrDuplicateConfigAction(false);
		}
	}

	private class RenameConfigAction extends AbstractAction {
		public RenameConfigAction() {
			putValue(NAME, trans.get("edtmotorconfdlg.but.Renameconfiguration"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			renameConfigurationAction();
		}
	}

	private class RemoveConfigAction extends AbstractAction {
		public RemoveConfigAction() {
			putValue(NAME, trans.get("edtmotorconfdlg.but.Removeconfiguration"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			removeConfigurationAction();
		}
	}

	private class DuplicateConfigAction extends AbstractAction {
		public DuplicateConfigAction() {
			putValue(NAME, trans.get("edtmotorconfdlg.but.Duplicateconfiguration"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			newOrDuplicateConfigAction(true);
		}
	}
}
