package net.sf.openrocket.gui.adaptors;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.TextFieldListener;
import net.sf.openrocket.gui.components.ResizeLabel;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.Configuration;
import net.sf.openrocket.rocketcomponent.Motor;
import net.sf.openrocket.rocketcomponent.MotorMount;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.GUIUtil;

public class MotorConfigurationModel implements ComboBoxModel, ChangeListener {

	private static final String EDIT = "Edit configurations";
	
	
	private EventListenerList listenerList = new EventListenerList();
	
	private final Configuration config;
	private final Rocket rocket;
	
	private Map<String, ID> map = new HashMap<String, ID>();
	

	public MotorConfigurationModel(Configuration config) {
		this.config = config;
		this.rocket = config.getRocket();
		config.addChangeListener(this);
	}
	
	
	
	@Override
	public Object getElementAt(int index) {
		String[] ids = rocket.getMotorConfigurationIDs();
		if (index < 0  ||  index > ids.length)
			return null;
		
		if (index == ids.length)
			return EDIT;
		
		return get(ids[index]);
	}

	@Override
	public int getSize() {
		return rocket.getMotorConfigurationIDs().length + 1;
	}

	@Override
	public Object getSelectedItem() {
		return get(config.getMotorConfigurationID());
	}

	@Override
	public void setSelectedItem(Object item) {
		if (item == EDIT) {
			
			// Open edit dialog in the future, after combo box has closed
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					EditConfigurationDialog dialog = new EditConfigurationDialog();
					dialog.setVisible(true);
					
					if (dialog.isRowSelected()) {
						rocket.getDefaultConfiguration().setMotorConfigurationID(
								dialog.getSelectedID());
					}
				}
			});

			return;
		}
		if (!(item instanceof ID))
			return;
		
		ID idObject = (ID) item;
		config.setMotorConfigurationID(idObject.getID());
	}


	
	////////////////  Event/listener handling  ////////////////
	
	
	@Override
	public void addListDataListener(ListDataListener l) {
		listenerList.add(ListDataListener.class, l);
	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listenerList.remove(ListDataListener.class, l);
	}

	protected void fireListDataEvent() {
		Object[] listeners = listenerList.getListenerList();
		ListDataEvent e = null;

		for (int i = listeners.length-2; i>=0; i-=2) {
			if (listeners[i] == ListDataListener.class) {
				if (e == null)
					e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, getSize());
				((ListDataListener) listeners[i+1]).contentsChanged(e);
			}
		}
	}
	 
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e instanceof ComponentChangeEvent) {
			// Ignore unnecessary changes
			if (!((ComponentChangeEvent)e).isMotorChange())
				return;
		}
		fireListDataEvent();
	}
	
	
	
	/*
	 * The ID class is an adapter, that contains the actual configuration ID,
	 * but gives the configuration description as its String representation.
	 * The get(id) method retrieves ID objects and caches them for reuse.
	 */
	
	private ID get(String id) {
		ID idObject = map.get(id);
		if (idObject != null)
			return idObject;
		
		idObject = new ID(id);
		map.put(id, idObject);
		return idObject;
	}
	
	
	private class ID {
		private final String id;
		
		public ID(String id) {
			this.id = id;
		}
		
		public String getID() {
			return id;
		}
		
		@Override
		public String toString() {
			return rocket.getMotorConfigurationNameOrDescription(id);
		}
	}
	
	
	private class EditConfigurationDialog extends JDialog {
		private final ColumnTableModel tableModel;
		private String[] ids;
		int selection = -1;
		
		private final JButton addButton;
		private final JButton removeButton;
		private final JTextField nameField;
		
		private final JTable table;
		
		
		public boolean isRowSelected() {
			return selection >= 0;
		}
		
		public String getSelectedID() {
			if (selection >= 0)
				return ids[selection];
			return null;
		}
		
		
		public EditConfigurationDialog() {
			super((JFrame)null, "Edit configurations", true);

			ids = rocket.getMotorConfigurationIDs();
			
			// Create columns
			ArrayList<Column> columnList = new ArrayList<Column>();
			columnList.add(new Column("Name") {
				@Override
				public Object getValueAt(int row) {
					return rocket.getMotorConfigurationNameOrDescription(ids[row]);
				}
			});
			
			// Create columns from the motor mounts
			Iterator<RocketComponent> iterator = rocket.deepIterator();
			while (iterator.hasNext()) {
				RocketComponent c = iterator.next();
				if (!(c instanceof MotorMount))
					continue;
				
				final MotorMount mount = (MotorMount)c;
				if (!mount.isMotorMount())
					continue;
				
				Column col = new Column(c.getName()) {
					@Override
					public Object getValueAt(int row) {
						Motor motor = mount.getMotor(ids[row]);
						if (motor == null)
							return "";
						return motor.getDesignation(mount.getMotorDelay(ids[row]));
					}
				};
				columnList.add(col);
			}
			tableModel = new ColumnTableModel(columnList.toArray(new Column[0])) {
				@Override
				public int getRowCount() {
					return ids.length;
				}
			};

			
			
			// Create the panel
			JPanel panel = new JPanel(new MigLayout("fill","[shrink][grow]"));
			
			
			panel.add(new JLabel("Configuration name:"), "gapright para");
			nameField = new JTextField();
			new TextFieldListener() {
				@Override
				public void setText(String text) {
					if (selection < 0 || ids[selection] == null)
						return;
					rocket.setMotorConfigurationName(ids[selection], text);
					fireChange();
				}
			}.listenTo(nameField);
			panel.add(nameField, "growx, wrap");
			
			panel.add(new ResizeLabel("Leave empty for default description", -2), 
					"skip, growx, wrap para");
			
			
			table = new JTable(tableModel);
			table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				@Override
				public void valueChanged(ListSelectionEvent e) {
					updateSelection();
				}
			});

			// Mouse listener to act on double-clicks
			table.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
						EditConfigurationDialog.this.dispose();
					}
				}
			});
			 
			
			
			JScrollPane scrollpane = new JScrollPane(table);
			panel.add(scrollpane, "spanx, height 150lp, width 400lp, grow, wrap");
			
			
			addButton = new JButton("New");
			addButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String id = rocket.newMotorConfigurationID();
					ids = rocket.getMotorConfigurationIDs();
					tableModel.fireTableDataChanged();
					int sel;
					for (sel=0; sel < ids.length; sel++) {
						if (id.equals(ids[sel]))
							break;
					}
					table.getSelectionModel().addSelectionInterval(sel, sel);
				}
			});
			panel.add(addButton, "growx, spanx, split 2");
			
			removeButton = new JButton("Remove");
			removeButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					int sel = table.getSelectedRow();
					if (sel < 0 || sel >= ids.length || ids[sel] == null)
						return;
					rocket.removeMotorConfigurationID(ids[sel]);
					ids = rocket.getMotorConfigurationIDs();
					tableModel.fireTableDataChanged();
					if (sel >= ids.length)
						sel--;
					table.getSelectionModel().addSelectionInterval(sel, sel);
				}
			});
			panel.add(removeButton, "growx, wrap para");
			
			
			JButton close = new JButton("Close");
			close.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					EditConfigurationDialog.this.dispose();
				}
			});
			panel.add(close, "spanx, alignx 100%");
			
			this.getRootPane().setDefaultButton(close);
			
			
			this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
			GUIUtil.installEscapeCloseOperation(this);
			this.setLocationByPlatform(true);
			
			updateSelection();
			
			this.add(panel);
			this.validate();
			this.pack();
		}
		
		private void fireChange() {
			int sel = table.getSelectedRow();
			tableModel.fireTableDataChanged();
			table.getSelectionModel().addSelectionInterval(sel, sel);
		}
		
		private void updateSelection() {
			selection = table.getSelectedRow();
			if (selection < 0  ||  ids[selection] == null) {
				removeButton.setEnabled(false);
				nameField.setEnabled(false);
				nameField.setText("");
			} else {
				removeButton.setEnabled(true);
				nameField.setEnabled(true);
				nameField.setText(rocket.getMotorConfigurationName(ids[selection]));
			}
		}
	}
	
}

