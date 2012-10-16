package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataEvent;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.dialogs.motor.MotorChooserDialog;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class FlightConfigurationDialog extends JDialog {
	
	final Rocket rocket;
	
	final MotorMount[] mounts;
	
	private final JTable configurationTable;
	final MotorConfigurationTableModel configurationTableModel;
	final FlightConfigurationModel flightConfigurationModel;
	
	private final JButton renameConfButton, removeConfButton;
	private final JButton selectMotorButton, removeMotorButton;
	
	String currentID = null;
	private MotorMount currentMount = null;
	
	static final Translator trans = Application.getTranslator();
	
	public FlightConfigurationDialog(final Rocket rocket, Window parent) {
		//// Edit motor configurations
		super(parent, trans.get("edtmotorconfdlg.title.Editmotorconf"));
		
		currentID = rocket.getDefaultConfiguration().getMotorConfigurationID();
		
		if (parent != null)
			this.setModalityType(ModalityType.DOCUMENT_MODAL);
		else
			this.setModalityType(ModalityType.APPLICATION_MODAL);
		
		this.rocket = rocket;
		
		mounts = getPotentialMotorMounts();
		
		JPanel panel = new JPanel(new MigLayout("fill, wrap 5"));

		JLabel label = new JLabel("Selected Configuration: ");
		panel.add(label,"gapbottom para");
		
		flightConfigurationModel = new FlightConfigurationModel(rocket.getDefaultConfiguration());
		JComboBox configSelector = new JComboBox(flightConfigurationModel);
		
		panel.add(configSelector,"gapright para");
		
		JButton newConfButton = new JButton(trans.get("edtmotorconfdlg.but.Newconfiguration"));
		newConfButton.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				FlightConfigurationDialog.this.addConfiguration();
			}
			
		});

		panel.add(newConfButton);
		
		renameConfButton = new JButton("Rename Configuration");
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
		panel.add(removeConfButton,"wrap");

		////  Motor mount selection
		//// <html><b>Motor mounts:</b>
		label = new JLabel(trans.get("edtmotorconfdlg.lbl.Motormounts"));
		panel.add(label, "gapbottom para");
		
		//// Motor selection
		//// <html><b>Motor configurations:</b>
		label = new JLabel(trans.get("edtmotorconfdlg.lbl.Motorconfig"));
		panel.add(label, "span 4, gapbottom para");

		//// <html>Select which components function as motor mounts:
		label = new JLabel(trans.get("edtmotorconfdlg.selectcomp"));
		panel.add(label, "ay 100%, w 1px, growx, wrap");
		
		//// Motor Mount selection 
		JTable table = new JTable(new MotorMountTableModel(this));
		table.setTableHeader(null);
		table.setShowVerticalLines(false);
		table.setRowSelectionAllowed(false);
		table.setColumnSelectionAllowed(false);
		
		TableColumnModel columnModel = table.getColumnModel();
		TableColumn col0 = columnModel.getColumn(0);
		int w = table.getRowHeight() + 2;
		col0.setMinWidth(w);
		col0.setPreferredWidth(w);
		col0.setMaxWidth(w);
		
		table.addMouseListener(new GUIUtil.BooleanTableClickListener(table));
		JScrollPane scroll = new JScrollPane(table);
		panel.add(scroll, "w 200lp, h 150lp, grow");
		
		//// Motor selection table.
		configurationTableModel = new MotorConfigurationTableModel(this);
		configurationTable = new JTable(configurationTableModel);
		configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configurationTable.setCellSelectionEnabled(true);
		
		configurationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (e.getClickCount() == 1) {
					
					// Single click updates selection
					updateButtonState();
					
				} else if (e.getClickCount() == 2) {
					
					// Double-click edits motor
					selectMotor();
					
				}
				
			}
		});
		
		scroll = new JScrollPane(configurationTable);
		panel.add(scroll, "span 4, w 500lp, h 150lp, grow");

		//// Select motor
		selectMotorButton = new JButton(trans.get("edtmotorconfdlg.but.Selectmotor"));
		selectMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectMotor();
			}
		});
		panel.add(selectMotorButton, "sizegroup button");
		
		//// Remove motor button
		removeMotorButton = new JButton(trans.get("edtmotorconfdlg.but.removemotor"));
		removeMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMotor();
			}
		});
		panel.add(removeMotorButton,"sizegroup button, wrap");
		
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
	
	
	private MotorMount[] getPotentialMotorMounts() {
		List<MotorMount> list = new ArrayList<MotorMount>();
		for (RocketComponent c : rocket) {
			if (c instanceof MotorMount) {
				list.add((MotorMount) c);
			}
		}
		return list.toArray(new MotorMount[0]);
	}

	void selectConfiguration( String id ) {
		currentID = id;
		rocket.getDefaultConfiguration().setMotorConfigurationID(currentID);
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	public void addConfiguration() {
		currentID = rocket.newMotorConfigurationID();
		rocket.getDefaultConfiguration().setMotorConfigurationID(currentID);
		configurationTableModel.fireTableDataChanged();
		flightConfigurationModel.fireContentsUpdated();
		updateButtonState();
	}
	
	public void changeConfigurationName( String newName ) {
		rocket.setMotorConfigurationName(currentID, newName);
		configurationTableModel.fireTableDataChanged();
		flightConfigurationModel.fireContentsUpdated();
	}
	
	public void removeConfiguration() {
		if (currentID == null)
			return;
		rocket.removeMotorConfigurationID(currentID);
		rocket.getDefaultConfiguration().setMotorConfigurationID(null);
		configurationTableModel.fireTableDataChanged();
		flightConfigurationModel.fireContentsUpdated();
		updateButtonState();
	}
	
	void updateButtonState() {
		removeConfButton.setEnabled(currentID != null);
		renameConfButton.setEnabled(currentID != null);
		selectMotorButton.setEnabled(currentMount != null && currentID != null);
		removeMotorButton.setEnabled(currentMount != null && currentID != null);
	}
	
	private void selectMotor() {
		if (currentID == null || currentMount == null)
			return;
		
		MotorChooserDialog dialog = new MotorChooserDialog(currentMount.getMotor(currentID),
				currentMount.getMotorDelay(currentID), currentMount.getMotorMountDiameter(), this);
		dialog.setVisible(true);
		Motor m = dialog.getSelectedMotor();
		double d = dialog.getSelectedDelay();
		
		if (m != null) {
			currentMount.setMotor(currentID, m);
			currentMount.setMotorDelay(currentID, d);
		}
		
		int row = configurationTable.getSelectedRow();
		configurationTableModel.fireTableRowsUpdated(row, row);
		updateButtonState();
	}
	
	
	private void removeMotor() {
		if (currentID == null || currentMount == null)
			return;
		
		currentMount.setMotor(currentID, null);
		
		int row = configurationTable.getSelectedRow();
		configurationTableModel.fireTableRowsUpdated(row, row);
		updateButtonState();
	}
	
	
	String findID(int row) {
		return rocket.getMotorConfigurationIDs()[row + 1];
	}
	
	
	MotorMount findMount(int column) {
		MotorMount mount = null;
		
		int count = column;
		for (MotorMount m : mounts) {
			if (m.isMotorMount())
				count--;
			if (count < 0) {
				mount = m;
				break;
			}
		}
		
		if (mount == null) {
			throw new IndexOutOfBoundsException("motor mount not found, column=" + column);
		}
		return mount;
	}
	
}
