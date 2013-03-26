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
import net.sf.openrocket.gui.adaptors.FlightConfigurationModel;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketvisitors.CopyFlightConfigurationVisitor;
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
		
		// FIXME: Localize
		JLabel label = new JLabel(trans.get("edtmotorconfdlg.lbl.Selectedconf"));
		panel.add(label, "span, split");
		
		flightConfigurationModel = new FlightConfigurationModel(rocket.getDefaultConfiguration());
		JComboBox configSelector = new JComboBox(flightConfigurationModel);
		configSelector.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				
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
				new RenameConfigDialog(FlightConfigurationDialog.this, rocket).setVisible(true);
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
	
	public void addConfiguration() {
		String id = rocket.newFlightConfigurationID();
		rocket.getDefaultConfiguration().setFlightConfigurationID(id);
		motorConfigurationPanel.fireTableDataChanged();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}
	
	public void copyConfiguration() {
		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		
		// currentID is the currently selected configuration.
		String newConfigId = rocket.newFlightConfigurationID();
		String oldName = rocket.getFlightConfigurationName(currentId);
		CopyFlightConfigurationVisitor v = new CopyFlightConfigurationVisitor(currentId, newConfigId);
		v.visit(rocket);
		// Select the new configuration
		rocket.getDefaultConfiguration().setFlightConfigurationID(newConfigId);
		
		// Copy the name.
		this.changeConfigurationName(oldName);
		motorConfigurationPanel.fireTableDataChanged();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}
	
	public void changeConfigurationName(String newName) {
		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		rocket.setFlightConfigurationName(currentId, newName);
	}
	
	private void removeConfiguration() {
		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		if (currentId == null)
			return;
		rocket.removeFlightConfigurationID(currentId);
		rocket.getDefaultConfiguration().setFlightConfigurationID(null);
		motorConfigurationPanel.fireTableDataChanged();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}
	
	/**
	 * Call this from other panels when a change might cause the names of the configurations to change.
	 */
	public void fireContentsUpdated() {
	}
	
	private void updateButtonState() {
		String currentId = rocket.getDefaultConfiguration().getFlightConfigurationID();
		removeConfButton.setEnabled(currentId != null);
		renameConfButton.setEnabled(currentId != null);
		motorConfigurationPanel.updateButtonState();
		recoveryConfigurationPanel.updateButtonState();
		separationConfigurationPanel.updateButtonState();
	}
	
	
}
