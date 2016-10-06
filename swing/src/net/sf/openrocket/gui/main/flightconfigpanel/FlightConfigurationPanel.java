package net.sf.openrocket.gui.main.flightconfigpanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.document.Simulation;
import net.sf.openrocket.gui.dialogs.flightconfiguration.RenameConfigDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.rocketvisitors.ListComponents;
import net.sf.openrocket.rocketvisitors.ListMotorMounts;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.StateChangeListener;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EventObject;

@SuppressWarnings("serial")
public class FlightConfigurationPanel extends JPanel implements StateChangeListener
{
   private static final Translator trans = Application.getTranslator();

   private final OpenRocketDocument document;
   private final Rocket rocket;

   private JButton newConfButton, renameConfButton, removeConfButton, copyConfButton;

   private JTabbedPane tabs;
   private MotorConfigurationPanel motorConfigurationPanel;
   private RecoveryConfigurationPanel recoveryConfigurationPanel;
   private SeparationConfigurationPanel stagesConfigurationPanel;

   private final static int MOTOR_TAB_INDEX = 0;
   private final static int RECOVERY_TAB_INDEX = 1;
   private final static int SEPARATION_TAB_INDEX = 2;

   public FlightConfigurationPanel(OpenRocketDocument doc) {
      super(new MigLayout("fill", "[grow][][][][][grow]"));

      this.document = doc;
      this.rocket = doc.getRocket();

      buildTabs();
      buildNewConfigurationButton();
      buildRenameConfigurationButton();
      buildRemoveConfigurationButton();
      buildCopyConfigurationButton();

      updateButtonState();
   }

   @Override
   public void stateChanged(EventObject e) {
      updateButtonState();
   }

   private void buildTabs() {
      tabs = new JTabbedPane();

      motorConfigurationPanel = new MotorConfigurationPanel(this, rocket);
      tabs.add(trans.get("edtmotorconfdlg.lbl.Motortab"), motorConfigurationPanel);

      recoveryConfigurationPanel = new RecoveryConfigurationPanel(this, rocket);
      tabs.add(trans.get("edtmotorconfdlg.lbl.Recoverytab"), recoveryConfigurationPanel);

      stagesConfigurationPanel = new SeparationConfigurationPanel(this, rocket);
      tabs.add(trans.get("edtmotorconfdlg.lbl.Stagetab"), stagesConfigurationPanel);

      this.add(tabs, "spanx, grow, wrap rel");
   }

   private void buildNewConfigurationButton() {
      newConfButton = new JButton(trans.get("edtmotorconfdlg.but.Newconfiguration"));
      newConfButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e) {
            addConfiguration();
         }
      });
      this.add(newConfButton, "skip 1,gapright para");
   }

   private void buildRenameConfigurationButton() {
      renameConfButton = new JButton(trans.get("edtmotorconfdlg.but.Renameconfiguration"));
      renameConfButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e) {
            renameConfiguration();
         }
      });
      this.add(renameConfButton, "gapright para");
   }

   private void buildRemoveConfigurationButton() {
      removeConfButton = new JButton(trans.get("edtmotorconfdlg.but.Removeconfiguration"));
      removeConfButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e) {
            removeConfiguration();
         }
      });
      this.add(removeConfButton, "gapright para");
   }

   private void buildCopyConfigurationButton() {
      copyConfButton = new JButton(trans.get("edtmotorconfdlg.but.Copyconfiguration"));
      copyConfButton.addActionListener(new ActionListener()
      {
         @Override
         public void actionPerformed(ActionEvent e) {
            copyConfiguration();
         }
      });
      this.add(copyConfButton, "wrap");
   }

   private void addConfiguration() {
      FlightConfigurationId newId = new FlightConfigurationId();
      FlightConfiguration newConfig = new FlightConfiguration(rocket, newId);
      rocket.setFlightConfiguration(newId, newConfig);

      createSimulationForNewConfiguration();

      configurationChanged();
   }

   private void copyConfiguration() {
      ArrayList<FlightConfigurationId> oldId = motorConfigurationPanel.getSelectedConfigurationIds();
      FlightConfiguration oldConfig = rocket.getFlightConfiguration(oldId.get(0));

      FlightConfigurationId newId = new FlightConfigurationId();
      FlightConfiguration newConfig = oldConfig.copy(newId);

      for (RocketComponent c : rocket) {
         if (c instanceof FlightConfigurableComponent) {
            ((FlightConfigurableComponent) c).copyFlightConfiguration(oldId.get(0), newId);
         }
      }
      rocket.setFlightConfiguration(newId, newConfig);

      createSimulationForNewConfiguration();

      configurationChanged();
   }

   private void renameConfiguration() {
      ArrayList<FlightConfigurationId> currentId = this.motorConfigurationPanel.getSelectedConfigurationIds();
      new RenameConfigDialog(SwingUtilities.getWindowAncestor(this), rocket, currentId.get(0)).setVisible(true);
      configurationChanged();
   }

   private void removeConfiguration() {
      ArrayList<FlightConfigurationId> currentIds = this.motorConfigurationPanel.getSelectedConfigurationIds();
      if (currentIds != null) {
         for (FlightConfigurationId currentId : currentIds) {
            document.removeFlightConfigurationAndSimulations(currentId);
         }
         configurationChanged();
      }
   }

   /**
    * prereq - assumes that the new configuration has been set as the default configuration.
    */
   private void createSimulationForNewConfiguration() {
      Simulation newSim = new Simulation(rocket);
      OpenRocketDocument doc = BasicFrame.findDocument(rocket);
      if (doc != null) {
         newSim.setName(doc.getNextSimulationName());
         doc.addSimulation(newSim);
      }
   }

   private void configurationChanged() {
      motorConfigurationPanel.fireTableDataChanged();
      recoveryConfigurationPanel.fireTableDataChanged();
      stagesConfigurationPanel.fireTableDataChanged();
      updateButtonState();
   }

   public void updateButtonState() {
      ArrayList<FlightConfigurationId> selectedConfigurations = this.motorConfigurationPanel.getSelectedConfigurationIds();
      int selectedConfigurationsCount = selectedConfigurations.size();

      int motorMountCount = rocket.accept(new ListMotorMounts()).size();
      int recoveryDeviceCount = rocket.accept(new ListComponents<>(RecoveryDevice.class)).size();
      int stageCount = rocket.getStageCount();

      if (selectedConfigurationsCount == 0) {
         newConfButton.setEnabled(motorMountCount > 0);
         removeConfButton.setEnabled(false);
         renameConfButton.setEnabled(false);
         copyConfButton.setEnabled(false);

      } else if (selectedConfigurationsCount == 1) {
         FlightConfigurationId currentId = rocket.getSelectedConfiguration().getFlightConfigurationID();

         // Enable the remove/rename/copy buttons only when a configuration is selected.
         removeConfButton.setEnabled(currentId.isValid());
         renameConfButton.setEnabled(currentId.isValid());
         copyConfButton.setEnabled(currentId.isValid());

         // Enable the new configuration button only when a motor mount is defined.
         newConfButton.setEnabled(motorMountCount > 0);

         // Only enable the recovery tab if there is a motor mount and there is a recovery device
         tabs.setEnabledAt(RECOVERY_TAB_INDEX, motorMountCount > 0 && recoveryDeviceCount > 0);

         // If the selected tab was the recovery tab, and there is no longer any recovery devices,
         // switch to the motor tab.
         if (recoveryDeviceCount == 0 && tabs.getSelectedIndex() == RECOVERY_TAB_INDEX) {
            tabs.setSelectedIndex(MOTOR_TAB_INDEX);
         }

         tabs.setEnabledAt(SEPARATION_TAB_INDEX, motorMountCount > 0 && stageCount > 1);

         if (stageCount == 1 && tabs.getSelectedIndex() == SEPARATION_TAB_INDEX) {
            tabs.setSelectedIndex(MOTOR_TAB_INDEX);
         }

      } else {  // > 1
         newConfButton.setEnabled(motorMountCount > 0);
         removeConfButton.setEnabled(true);
         renameConfButton.setEnabled(false);
         copyConfButton.setEnabled(false);
      }
   }
}
