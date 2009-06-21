package net.sf.openrocket.gui.dialogs;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.document.OpenRocketDocument;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.main.MotorChooserDialog;
import net.sf.openrocket.rocketcomponent.Motor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.GUIUtil;

public class EditMotorConfigurationDialog extends JDialog {
	
	private final Rocket rocket;
	
	private final MotorMount[] mounts;

	private final JTable configurationTable;
	private final MotorConfigurationTableModel configurationTableModel;

	
	private final JButton newConfButton, removeConfButton;
	private final JButton selectMotorButton, removeMotorButton;
	private final JTextField configurationNameField;
	
	
	private String currentID = null;
	private MotorMount currentMount = null;
	
	public EditMotorConfigurationDialog(final Rocket rocket, Window parent) {
		super(parent, "Edit motor configurations");
		
		if (parent != null)
			this.setModalityType(ModalityType.DOCUMENT_MODAL);
		else
			this.setModalityType(ModalityType.APPLICATION_MODAL);
		
		this.rocket = rocket;
		
		ArrayList<MotorMount> mountList = new ArrayList<MotorMount>();
		Iterator<RocketComponent> iterator = rocket.deepIterator();
		while (iterator.hasNext()) {
			RocketComponent c = iterator.next();
			if (c instanceof MotorMount) {
				mountList.add((MotorMount)c);
			}
		}
		mounts = mountList.toArray(new MotorMount[0]);
		
		
		
		JPanel panel = new JPanel(new MigLayout("fill, flowy"));
		
		
		////  Motor mount selection
		
		JLabel label = new JLabel("<html><b>Motor mounts:</b>");
		panel.add(label, "gapbottom para");
		
		label = new JLabel("<html>Select which components function as motor mounts:");
		panel.add(label,"ay 100%, w 1px, growx");
		
		
		JTable table = new JTable(new MotorMountTableModel());
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
		
		JScrollPane scroll = new JScrollPane(table);
		panel.add(scroll, "w 200lp, h 150lp, grow, wrap 20lp");
		

		
		
		
		//// Motor selection
		
		label = new JLabel("<html><b>Motor configurations:</b>");
		panel.add(label, "spanx, gapbottom para");
		
		
		label = new JLabel("Configuration name:");
		String tip = "Leave name empty for default.";
		label.setToolTipText(tip);
		panel.add(label, "");
		
		configurationNameField = new JTextField(10);
		configurationNameField.setToolTipText(tip);
		configurationNameField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {
				update();
			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}
			private void update() {
				String text = configurationNameField.getText();
				if (currentID != null) {
					rocket.setMotorConfigurationName(currentID, text);
					int row = configurationTable.getSelectedRow();
					configurationTableModel.fireTableCellUpdated(row, 0);
					updateEnabled();
				}
			}
		});
		panel.add(configurationNameField, "cell 2 1, gapright para");
		
		newConfButton = new JButton("New configuration");
		newConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String id = rocket.newMotorConfigurationID();
				rocket.getDefaultConfiguration().setMotorConfigurationID(id);
				configurationTableModel.fireTableDataChanged();
				updateEnabled();
			}
		});
		panel.add(newConfButton, "cell 3 1");
		
		removeConfButton = new JButton("Remove configuration");
		removeConfButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (currentID == null)
					return;
				rocket.removeMotorConfigurationID(currentID);
				rocket.getDefaultConfiguration().setMotorConfigurationID(null);
				configurationTableModel.fireTableDataChanged();
				updateEnabled();
			}
		});
		panel.add(removeConfButton, "cell 4 1");
		
		

		
		configurationTableModel = new MotorConfigurationTableModel();
		configurationTable = new JTable(configurationTableModel);
		configurationTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		configurationTable.setCellSelectionEnabled(true);
		
		configurationTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				
				if (e.getClickCount() == 1) {
					
					// Single click updates selection
					updateEnabled();
					
				} else if (e.getClickCount() == 2) {
					
					// Double-click edits motor
					selectMotor();
					
				}
				
			}
		});
		

		scroll = new JScrollPane(configurationTable);
		panel.add(scroll, "cell 1 2, spanx, w 500lp, h 150lp, grow");
		
		
		selectMotorButton = new JButton("Select motor");
		selectMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectMotor();
			}
		});
		panel.add(selectMotorButton, "spanx, flowx, split 2, ax 50%");
		
		
		removeMotorButton = new JButton("Remove motor");
		removeMotorButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				removeMotor();
			}
		});
		panel.add(removeMotorButton, "ax 50%");
		
		
		
		//// Close button
		
		JButton close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EditMotorConfigurationDialog.this.dispose();
			}
		});
		panel.add(close, "spanx, right");
		
		this.add(panel);
		this.validate();
		this.pack();
		
		updateEnabled();
		
		GUIUtil.installEscapeCloseOperation(this);
		GUIUtil.setDefaultButton(close);
		
		// Undo description
		final OpenRocketDocument document = BasicFrame.findDocument(rocket);
		if (document != null) {
			document.startUndo("Edit motor configurations");
			this.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					document.stopUndo();
				}
			});
		}
	}

	
	
	
	private void updateEnabled() {
		int column = configurationTable.getSelectedColumn();
		int row = configurationTable.getSelectedRow();
		
		if (column < 0 || row < 0) {
			currentID = null;
			currentMount = null;
		} else {
			
			currentID = findID(row);
			if (column == 0) {
				currentMount = null;
			} else {
				currentMount = findMount(column);
			}
			rocket.getDefaultConfiguration().setMotorConfigurationID(currentID);
			
		}

		configurationNameField.setEnabled(currentID != null);
		if (currentID == null) {
			configurationNameField.setText("");
		} else {
			configurationNameField.setText(rocket.getMotorConfigurationName(currentID));
		}
		removeConfButton.setEnabled(currentID != null);
		selectMotorButton.setEnabled(currentMount != null && currentID != null);
		removeMotorButton.setEnabled(currentMount != null && currentID != null);
	}
	

	
	
	private void selectMotor() {
		if (currentID == null || currentMount == null)
			return;
		
		MotorChooserDialog dialog = new MotorChooserDialog(currentMount.getMotor(currentID),
				currentMount.getMotorDelay(currentID), currentMount.getMotorMountDiameter());
		dialog.setVisible(true);
		Motor m = dialog.getSelectedMotor();
		double d = dialog.getSelectedDelay();
		
		if (m != null) {
			currentMount.setMotor(currentID, m);
			currentMount.setMotorDelay(currentID, d);
		}

		int row = configurationTable.getSelectedRow();
		configurationTableModel.fireTableRowsUpdated(row, row);
		updateEnabled();
	}
	
	
	private void removeMotor() {
		if (currentID == null || currentMount == null)
			return;
		
		currentMount.setMotor(currentID, null);
		
		int row = configurationTable.getSelectedRow();
		configurationTableModel.fireTableRowsUpdated(row, row);
		updateEnabled();
	}
	
	
	private String findID(int row) {
		return rocket.getMotorConfigurationIDs()[row+1];
	}

	
	private MotorMount findMount(int column) {
		MotorMount mount = null;

		int count = column;
		for (MotorMount m: mounts) {
			if (m.isMotorMount())
				count--;
			if (count <= 0) {
				mount = m;
				break;
			}
		}
		
		if (mount == null) {
			throw new IndexOutOfBoundsException("motor mount not found, column="+column);
		}
		return mount;
	}

	
	/**
	 * The table model for selecting whether components are motor mounts or not.
	 */
	private class MotorMountTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public int getRowCount() {
			return mounts.length;
		}
		
		@Override
		public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:
				return Boolean.class;
				
			case 1:
				return String.class;
				
			default:
				throw new IndexOutOfBoundsException("column="+column);
			}
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:
				return new Boolean(mounts[row].isMotorMount());
				
			case 1:
				return mounts[row].toString();
				
			default:
				throw new IndexOutOfBoundsException("column="+column);
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}
		
		@Override
		public void setValueAt(Object value, int row, int column) {
			if (column != 0 || !(value instanceof Boolean)) {
				throw new IllegalArgumentException("column="+column+", value="+value);
			}
			
			mounts[row].setMotorMount((Boolean)value);
			configurationTableModel.fireTableStructureChanged();
			updateEnabled();
		}
	}
	
	
	
	/**
	 * The table model for selecting and editing the motor configurations.
	 */
	private class MotorConfigurationTableModel extends AbstractTableModel {
		
		@Override
		public int getColumnCount() {
			int count = 1;
			for (MotorMount m: mounts) {
				if (m.isMotorMount())
					count++;
			}
			return count;
		}

		@Override
		public int getRowCount() {
			return rocket.getMotorConfigurationIDs().length-1;
		}

		@Override
		public Object getValueAt(int row, int column) {
			
			String id = findID(row);

			if (column == 0) {
				return rocket.getMotorConfigurationNameOrDescription(id);
			}
			
			MotorMount mount = findMount(column);
			Motor motor = mount.getMotor(id);
			if (motor == null)
				return "None";
			
			String str = motor.getDesignation(mount.getMotorDelay(id)); 
			int count = mount.getMotorCount();
			if (count > 1) {
				str = "" + count + "\u00d7 " + str;
			}
			return str;
		}


		@Override
		public String getColumnName(int column) {
			if (column == 0) {
				return "Configuration name";
			}
			
			MotorMount mount = findMount(column);
			String name = mount.toString();
			int count = mount.getMotorCount();
			if (count > 1) {
				name = name + " (\u00d7" + count + ")";
			}
			return name;
		}
		
		
	}

	
	
	
}
