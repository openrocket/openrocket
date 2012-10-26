package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Dialog;
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
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.StageSeparationConfiguration;
import net.sf.openrocket.rocketvisitors.CopyFlightConfigurationVisitor;
import net.sf.openrocket.startup.Application;

public class FlightConfigurationDialog extends JDialog {

	static final Translator trans = Application.getTranslator();

	private final Rocket rocket;

	private FlightConfigurationModel flightConfigurationModel;

	private final JButton renameConfButton, removeConfButton, copyConfButton;

	private final MotorConfigurationPanel motorConfigurationPanel;
	private final RecoveryConfigurationPanel recoveryConfigurationPanel;
	private final SeparationConfigurationPanel separationConfigurationPanel;

	private String currentID = null;

	public FlightConfigurationDialog(final Rocket rocket, Window parent) {
		//// Edit motor configurations
		super(parent, trans.get("edtmotorconfdlg.title.Editmotorconf"), Dialog.ModalityType.APPLICATION_MODAL);

		currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();

		if (parent != null)
			this.setModalityType(ModalityType.DOCUMENT_MODAL);
		else
			this.setModalityType(ModalityType.APPLICATION_MODAL);

		this.rocket = rocket;

		JPanel panel = new JPanel(new MigLayout("fill"));

		JLabel label = new JLabel("Selected Configuration: ");
		panel.add(label);

		flightConfigurationModel = new FlightConfigurationModel(this, rocket.getDefaultConfiguration());
		JComboBox configSelector = new JComboBox(flightConfigurationModel);

		panel.add(configSelector,"gapright para");

		JButton newConfButton = new JButton(trans.get("edtmotorconfdlg.but.Newconfiguration"));
		newConfButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				addConfiguration();
			}

		});

		panel.add(newConfButton);

		renameConfButton = new JButton(trans.get("edtmotorconfdlg.but.Renameconfiguration"));
		renameConfButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new RenameConfigDialog( rocket, FlightConfigurationDialog.this).setVisible(true);
			}
		});
		panel.add(renameConfButton);

		//// Remove configuration
		removeConfButton = new JButton(trans.get("edtmotorconfdlg.but.Removeconfiguration"));
		removeConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeConfiguration();
			}
		});
		panel.add(removeConfButton);

		//// Copy configuration
		copyConfButton = new JButton(trans.get("edtmotorconfdlg.but.Copyconfiguration"));
		copyConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyConfiguration();
			}
		});
		panel.add(copyConfButton,"wrap");


		//// Tabs for advanced view.
		JTabbedPane tabs = new JTabbedPane();
		panel.add( tabs, "grow, spanx, w 700lp, h 500lp, wrap");

		//// Motor tabs
		motorConfigurationPanel = new MotorConfigurationPanel(this,rocket);
		tabs.add(trans.get("edtmotorconfdlg.lbl.Motortab"), motorConfigurationPanel);

		//// Recovery tab
		recoveryConfigurationPanel = new RecoveryConfigurationPanel(this,rocket);
		tabs.add(trans.get("edtmotorconfdlg.lbl.Recoverytab"), recoveryConfigurationPanel );

		//// Stage tab
		separationConfigurationPanel = new SeparationConfigurationPanel(this,rocket);
		if ( rocket.getStageCount() > 1 ) {
			tabs.add(trans.get("edtmotorconfdlg.lbl.Stagetab"), separationConfigurationPanel );
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

	public void selectConfiguration( String id ) {
		currentID = id;
		rocket.getDefaultConfiguration().setFlightConfigurationID(currentID);
		motorConfigurationPanel.fireTableDataChanged();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}

	public void addConfiguration() {
		currentID = rocket.newFlightConfigurationID();
		rocket.getDefaultConfiguration().setFlightConfigurationID(currentID);
		motorConfigurationPanel.fireTableDataChanged();
		flightConfigurationModel.fireContentsUpdated();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}

	public void copyConfiguration() {
		// currentID is the currently selected configuration.
		String newConfigId = rocket.newFlightConfigurationID();
		CopyFlightConfigurationVisitor v = new CopyFlightConfigurationVisitor(currentID, newConfigId);
		v.visit(rocket);
		motorConfigurationPanel.fireTableDataChanged();
		flightConfigurationModel.fireContentsUpdated();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}

	public void changeConfigurationName( String newName ) {
		rocket.setFlightConfigurationName(currentID, newName);
		flightConfigurationModel.fireContentsUpdated();
	}

	public void removeConfiguration() {
		if (currentID == null)
			return;
		rocket.removeFlightConfigurationID(currentID);
		rocket.getDefaultConfiguration().setFlightConfigurationID(null);
		motorConfigurationPanel.fireTableDataChanged();
		flightConfigurationModel.fireContentsUpdated();
		recoveryConfigurationPanel.fireTableDataChanged();
		separationConfigurationPanel.fireTableDataChanged();
		updateButtonState();
	}

	/**
	 * Call this from other panels when a change might cause the names of the configurations to change.
	 */
	public void fireContentsUpdated() {
		flightConfigurationModel.fireContentsUpdated();
	}

	private void updateButtonState() {
		removeConfButton.setEnabled(currentID != null);
		renameConfButton.setEnabled(currentID != null);
		motorConfigurationPanel.updateButtonState();
		recoveryConfigurationPanel.updateButtonState();
		separationConfigurationPanel.updateButtonState();
	}


}
