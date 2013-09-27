package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.DescriptionArea;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.components.StyledLabel.Style;
import net.sf.openrocket.gui.dialogs.motor.MotorChooserDialog;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.motor.Motor;
import net.sf.openrocket.rocketcomponent.MotorConfiguration;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class MotorConfigurationPanel extends JPanel {
	
	private static final Translator trans = Application.getTranslator();
	
	private final FlightConfigurationDialog flightConfigurationDialog;
	private final Rocket rocket;
	
	private final JTable configurationTable;
	private final MotorConfigurationTableModel configurationTableModel;
	private final JButton selectMotorButton, removeMotorButton, selectIgnitionButton, resetIgnitionButton;
	
	
	MotorConfigurationPanel(FlightConfigurationDialog flightConfigurationDialog, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationDialog = flightConfigurationDialog;
		this.rocket = rocket;
		
		DescriptionArea desc = new DescriptionArea(trans.get("description"), 3, -1);
		this.add(desc, "spanx, growx, wrap para");
		
		
		////  Motor mount selection
		JLabel label = new StyledLabel(trans.get("lbl.motorMounts"), Style.BOLD);
		this.add(label, "");
		
		//// Motor selection
		label = new StyledLabel(trans.get("lbl.motorConfiguration"), Style.BOLD);
		this.add(label, "wrap rel");
		
		
		//// Motor Mount selection 
		JTable table = new JTable(new MotorMountTableModel(this, rocket));
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
		configurationTableModel = new MotorConfigurationTableModel(rocket);
		configurationTable = new JTable(configurationTableModel);
		configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configurationTable.setRowSelectionAllowed(true);
		configurationTable.setDefaultRenderer(Object.class, new MotorTableCellRenderer());
		
		configurationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				updateButtonState();
				int selectedColumn = configurationTable.getSelectedColumn();
				if (e.getClickCount() == 2) {
					if (selectedColumn == 2) {
						// user double clicked in ignition column
						selectIgnition();
					} else {
						// Double-click edits motor
						selectMotor();
					}
				}
			}
		});
		
		scroll = new JScrollPane(configurationTable);
		this.add(scroll, "w 500lp, h 150lp, grow, wrap");
		
		//// Select motor
		selectMotorButton = new JButton(trans.get("MotorConfigurationPanel.btn.selectMotor"));
		selectMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectMotor();
			}
		});
		this.add(selectMotorButton, "skip, split, sizegroup button");
		
		//// Remove motor button
		removeMotorButton = new JButton(trans.get("MotorConfigurationPanel.btn.removeMotor"));
		removeMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMotor();
			}
		});
		this.add(removeMotorButton, "sizegroup button");
		
		//// Select Ignition button
		selectIgnitionButton = new JButton(trans.get("MotorConfigurationPanel.btn.selectIgnition"));
		selectIgnitionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectIgnition();
			}
		});
		this.add(selectIgnitionButton, "sizegroup button");
		
		//// Reset Ignition button
		resetIgnitionButton = new JButton(trans.get("MotorConfigurationPanel.btn.resetIgnition"));
		resetIgnitionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetIgnition();
			}
		});
		this.add(resetIgnitionButton, "sizegroup button, wrap");
		
	}
	
	public void fireTableDataChanged() {
		int selected = configurationTable.getSelectedRow();
		configurationTableModel.fireTableDataChanged();
		if (selected >= 0) {
			selected = Math.min(selected, configurationTable.getRowCount() - 1);
			configurationTable.getSelectionModel().setSelectionInterval(selected, selected);
		}
		updateButtonState();
	}
	
	private void updateButtonState() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount currentMount = getCurrentMount();
		selectMotorButton.setEnabled(currentMount != null && currentID != null);
		removeMotorButton.setEnabled(currentMount != null && currentID != null);
		selectIgnitionButton.setEnabled(currentMount != null && currentID != null);
		resetIgnitionButton.setEnabled(currentMount != null && currentID != null);
	}
	
	
	private MotorMount getCurrentMount() {
		int row = configurationTable.getSelectedRow();
		if (row < 0) {
			return null;
		}
		
		return getMount(row);
	}
	
	
	private MotorMount getMount(int row) {
		int count = 0;
		for (RocketComponent c : rocket) {
			if (c instanceof MotorMount) {
				MotorMount mount = (MotorMount) c;
				if (mount.isMotorMount()) {
					count++;
				}
				if (count > row) {
					return mount;
				}
			}
		}
		
		throw new IndexOutOfBoundsException("Invalid row, row=" + row + " count=" + count);
	}
	
	
	
	private void selectMotor() {
		String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount mount = getCurrentMount();
		if (id == null || mount == null)
			return;
		
		MotorConfiguration config = mount.getMotorConfiguration().get(id);
		
		MotorChooserDialog dialog = new MotorChooserDialog(
				config.getMotor(),
				config.getEjectionDelay(),
				mount.getMotorMountDiameter(),
				flightConfigurationDialog);
		dialog.setVisible(true);
		Motor m = dialog.getSelectedMotor();
		double d = dialog.getSelectedDelay();
		
		if (m != null) {
			config = new MotorConfiguration();
			config.setMotor(m);
			config.setEjectionDelay(d);
			mount.getMotorConfiguration().set(id, config);
		}
		
		fireTableDataChanged();
	}
	
	private void removeMotor() {
		String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount mount = getCurrentMount();
		if (id == null || mount == null)
			return;
		
		mount.getMotorConfiguration().resetDefault(id);
		
		fireTableDataChanged();
	}
	
	private void selectIgnition() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount currentMount = getCurrentMount();
		if (currentID == null || currentMount == null)
			return;
		
		IgnitionSelectionDialog dialog = new IgnitionSelectionDialog(
				this.flightConfigurationDialog,
				rocket,
				currentMount);
		dialog.setVisible(true);
		
		fireTableDataChanged();
	}
	
	
	private void resetIgnition() {
		String currentID = rocket.getDefaultConfiguration().getFlightConfigurationID();
		MotorMount currentMount = getCurrentMount();
		if (currentID == null || currentMount == null)
			return;
		
		currentMount.getIgnitionConfiguration().resetDefault(currentID);
		
		fireTableDataChanged();
	}
	
	
	private class MotorTableCellRenderer extends DefaultTableCellRenderer {
		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (!(c instanceof JLabel)) {
				return c;
			}
			JLabel label = (JLabel) c;
			
			MotorMount mount = getMount(row);
			String id = rocket.getDefaultConfiguration().getFlightConfigurationID();
			
			switch (column) {
			case 0:
				regular(label);
				break;
			
			case 1:
				if (mount.getMotorConfiguration().get(id).getMotor() != null) {
					regular(label);
				} else {
					shaded(label);
				}
				break;
			
			case 2:
				if (mount.getIgnitionConfiguration().isDefault(id)) {
					shaded(label);
				} else {
					regular(label);
				}
				break;
			}
			
			return label;
		}
		
		private void shaded(JLabel label) {
			GUIUtil.changeFontStyle(label, Font.ITALIC);
			label.setForeground(Color.GRAY);
		}
		
		private void regular(JLabel label) {
			GUIUtil.changeFontStyle(label, Font.PLAIN);
			label.setForeground(Color.BLACK);
		}
		
	}
	
}
