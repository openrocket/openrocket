package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.dialogs.motor.MotorChooserDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.Chars;

public class MotorConfigurationPanel extends JPanel {

	private final FlightConfigurationDialog flightConfigurationDialog;
	private final Rocket rocket;

	private final MotorConfigurationTableModel configurationTableModel;
	private final JButton selectMotorButton, removeMotorButton, selectIgnitionButton;

	private MotorMount currentMount = null;
	final MotorMount[] mounts;
	
	MotorConfigurationPanel(FlightConfigurationDialog flightConfigurationDialog, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.rocket = rocket;
		
		mounts = getPotentialMotorMounts();

		////  Motor mount selection
		//// <html><b>Motor mounts:</b>
		JLabel label = new JLabel(FlightConfigurationDialog.trans.get("edtmotorconfdlg.lbl.Motormounts"));
		this.add(label, "gapbottom para");

		//// Motor selection
		//// <html><b>Motor configurations:</b>
		label = new JLabel(FlightConfigurationDialog.trans.get("edtmotorconfdlg.lbl.Motorconfig"));
		this.add(label, "gapbottom para, wrap");

		//// <html>Select which components function as motor mounts:
		label = new JLabel(FlightConfigurationDialog.trans.get("edtmotorconfdlg.selectcomp"));
		this.add(label, "ay 100%, w 1px, growx, wrap");

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
		this.add(scroll, "w 200lp, h 150lp, grow");

		//// Motor selection table.
		configurationTableModel = new MotorConfigurationTableModel(this, true);
		final JTable configurationTable = new JTable(configurationTableModel);
		configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configurationTable.setRowSelectionAllowed(true);

		configurationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int row = configurationTable.getSelectedRow();

				if ( row >= 0 ) {
					currentMount = findMount(row);
				} else { 
					currentMount = null;
				}

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
		this.add(scroll, "w 500lp, h 150lp, grow, wrap");

		//// Select motor
		selectMotorButton = new JButton(FlightConfigurationDialog.trans.get("edtmotorconfdlg.but.Selectmotor"));
		selectMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectMotor();
			}
		});
		this.add(selectMotorButton, "skip, split, sizegroup button");

		//// Remove motor button
		removeMotorButton = new JButton(FlightConfigurationDialog.trans.get("edtmotorconfdlg.but.removemotor"));
		removeMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMotor();
			}
		});
		this.add(removeMotorButton,"sizegroup button");

		//// Select Ignition button
		selectIgnitionButton = new JButton(FlightConfigurationDialog.trans.get("edtmotorconfdlg.but.Selectignition"));
		selectIgnitionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectIgnition();
			}
		});
		this.add(selectIgnitionButton,"sizegroup button, wrap");

	}
	
	public void fireTableDataChanged() {
		currentMount = null;
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	public void updateButtonState() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		selectMotorButton.setEnabled(currentMount != null && currentID != null);
		removeMotorButton.setEnabled(currentMount != null && currentID != null);
		selectIgnitionButton.setEnabled(currentMount != null && currentID != null);
	}

	
	private void selectMotor() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		if (currentID == null || currentMount == null)
			return;

		MotorChooserDialog dialog = new MotorChooserDialog(
				currentMount.getMotor(currentID),
				currentMount.getMotorDelay(currentID),
				currentMount.getMotorMountDiameter(),
				flightConfigurationDialog);
		dialog.setVisible(true);
		Motor m = dialog.getSelectedMotor();
		double d = dialog.getSelectedDelay();

		if (m != null) {
			currentMount.setMotor(currentID, m);
			currentMount.setMotorDelay(currentID, d);
		}

		flightConfigurationDialog.fireContentsUpdated();
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}

	private void removeMotor() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		if (currentID == null || currentMount == null)
			return;

		currentMount.setMotor(currentID, null);

		flightConfigurationDialog.fireContentsUpdated();
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}
	
	private void selectIgnition() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		if (currentID == null || currentMount == null)
			return;

		SelectIgnitionConfigDialog dialog = new SelectIgnitionConfigDialog(
				this.flightConfigurationDialog,
				rocket,
				currentMount );
		dialog.setVisible(true);

		flightConfigurationDialog.fireContentsUpdated();
		configurationTableModel.fireTableDataChanged();
		updateButtonState();
	}

	public void makeMotorMount( MotorMount mount, boolean isMotorMount ) {
		mount.setMotorMount( isMotorMount );
		configurationTableModel.fireTableStructureChanged();
		updateButtonState();
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

	public MotorMount findMount(int row) {
		MotorMount mount = null;

		int count = row;
		for (MotorMount m : mounts) {
			if (m.isMotorMount())
				count--;
			if (count < 0) {
				mount = m;
				break;
			}
		}

		if (mount == null) {
			throw new IndexOutOfBoundsException("motor mount not found, row=" + row);
		}
		return mount;
	}

	public String findMotorForDisplay( int row ) {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount mount = findMount(row);
		Motor motor = mount.getMotor(currentID);
		if (motor == null)
			return null;

		String str = motor.getDesignation(mount.getMotorDelay(currentID));
		int count = mount.getMotorCount();
		if (count > 1) {
			str = "" + count + Chars.TIMES + " " + str;
		}
		return str;
	}

	public String findIgnitionForDisplay( int row ) {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount mount = findMount(row);
		MotorConfiguration motorConfig = mount.getFlightConfiguration(currentID);
		if (motorConfig == null)
			//// None
			return null;
		StringBuilder sb = new StringBuilder();
		MotorConfiguration.IgnitionEvent ignition = motorConfig.getIgnitionEvent();
		if ( ignition == null ) {
			sb.append("[").append(ellipsizeString(mount.getDefaultIgnitionEvent().toString(),15)).append("]");
		} else {
			sb.append(ellipsizeString(ignition.toString(),15));
		}
		Double ignitionDelay = motorConfig.getIgnitionDelay();
		if ( ignitionDelay == null ) {
			double defaultdelay = mount.getDefaultIgnitionDelay();
			if ( defaultdelay > 0 ) {
				sb.append(" + [").append(defaultdelay).append("s]");
			}
		} else {
			sb.append(" + ").append(ignitionDelay).append("s");
		}
		return sb.toString();
	}

	
	private static String ellipsizeString( String s, int length ) {
		if ( s.length() < length ) {
			return s;
		}
		String newString = s.substring(0, length) + "...";
		return newString;
	}

}
