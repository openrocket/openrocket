package net.sf.openrocket.gui.dialogs.preferences;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.database.Database;
import net.sf.openrocket.database.Databases;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.dialogs.CustomMaterialDialog;
import net.sf.openrocket.material.Material;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.unit.Value;

public class MaterialEditPanel extends JPanel {

	private final JTable table;
	
	private final JButton addButton;
	private final JButton editButton;
	private final JButton deleteButton;
	private final JButton revertButton;
	
	
	public MaterialEditPanel() {
		super(new MigLayout("fill"));
		
		
		// TODO: LOW: Create sorter that keeps material types always in order
		final ColumnTableModel model = new ColumnTableModel(
				new Column("Material") {
					@Override
					public Object getValueAt(int row) {
						return getMaterial(row).getName();
					}
				},
				
				new Column("Type") {
					@Override
					public Object getValueAt(int row) {
						return getMaterial(row).getType().toString();
					}
					@Override
					public int getDefaultWidth() {
						return 15;
					}
				},
				
				new Column("Density") {
					@Override
					public Object getValueAt(int row) {
						Material m = getMaterial(row);
						double d = m.getDensity();
						switch (m.getType()) {
						case LINE:
							return UnitGroup.UNITS_DENSITY_LINE.toValue(d);
							
						case SURFACE:
							return UnitGroup.UNITS_DENSITY_SURFACE.toValue(d);
							
						case BULK:
							return UnitGroup.UNITS_DENSITY_BULK.toValue(d);
							
						default:
							throw new IllegalStateException("Material type " + m.getType());
						}
					}
					@Override
					public int getDefaultWidth() {
						return 15;
					}
					@Override
					public Class<?> getColumnClass() {
						return Value.class;
					}
				}
		) {
			@Override
			public int getRowCount() {
				return Databases.BULK_MATERIAL.size() + Databases.SURFACE_MATERIAL.size() +
					Databases.LINE_MATERIAL.size();
			}
		};
		
		table = new JTable(model);
		model.setColumnWidths(table.getColumnModel());
		table.setAutoCreateRowSorter(true);
		table.setDefaultRenderer(Object.class, new MaterialCellRenderer());
		this.add(new JScrollPane(table), "w 200px, h 100px, grow 100");
		
		
		
		addButton = new JButton("New");
		addButton.setToolTipText("Add a new material");
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CustomMaterialDialog dialog = new CustomMaterialDialog(
							SwingUtilities.getWindowAncestor(MaterialEditPanel.this),
							null, false, "Add a custom material");
				dialog.setVisible(true);
				if (dialog.getOkClicked()) {
					Material mat = dialog.getMaterial();
					getDatabase(mat).add(mat);
					model.fireTableDataChanged();
					setButtonStates();
				}
			}
		});
		this.add(addButton, "gap rel rel para para, w 70lp, split 5, flowy, growx 1, top");
		
		
		editButton = new JButton("Edit");
		editButton.setToolTipText("Edit an existing material");
		editButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = table.getSelectedRow();
				if (sel < 0)
					return;
				sel = table.convertRowIndexToModel(sel);
				Material m = getMaterial(sel);
				
				CustomMaterialDialog dialog;
				if (m.isUserDefined()) {
					dialog = new CustomMaterialDialog(
							SwingUtilities.getWindowAncestor(MaterialEditPanel.this),
							m, false, "Edit material");
				} else {
					dialog = new CustomMaterialDialog(
							SwingUtilities.getWindowAncestor(MaterialEditPanel.this),
							m, false, "Add a custom material", 
							"The built-in materials cannot be modified.");
				}
				
				dialog.setVisible(true);
				
				if (dialog.getOkClicked()) {
					if (m.isUserDefined()) {
						getDatabase(m).remove(m);
					}
					Material mat = dialog.getMaterial();
					getDatabase(mat).add(mat);
					model.fireTableDataChanged();
					setButtonStates();
				}
			}
		});
		this.add(editButton, "gap rel rel para para, growx 1, top");
		
		
		deleteButton = new JButton("Delete");
		deleteButton.setToolTipText("Delete a user-defined material");
		deleteButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = table.getSelectedRow();
				if (sel < 0)
					return;
				sel = table.convertRowIndexToModel(sel);
				Material m = getMaterial(sel);
				if (!m.isUserDefined())
					return;
				getDatabase(m).remove(m);
				model.fireTableDataChanged();
				setButtonStates();
			}
		});
		this.add(deleteButton, "gap rel rel para para, growx 1, top");
		
		
		this.add(new JPanel(), "grow 1");
		
		revertButton = new JButton("Revert all");
		revertButton.setToolTipText("Delete all user-defined materials");
		revertButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int sel = JOptionPane.showConfirmDialog(MaterialEditPanel.this, 
						"Delete all user-defined materials?", "Revert all?", 
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
				if (sel == JOptionPane.YES_OPTION) {
					Iterator<Material> iterator;

					iterator = Databases.LINE_MATERIAL.iterator();
					while (iterator.hasNext()) {
						if (iterator.next().isUserDefined())
							iterator.remove();
					}

					iterator = Databases.SURFACE_MATERIAL.iterator();
					while (iterator.hasNext()) {
						if (iterator.next().isUserDefined())
							iterator.remove();
					}

					iterator = Databases.BULK_MATERIAL.iterator();
					while (iterator.hasNext()) {
						if (iterator.next().isUserDefined())
							iterator.remove();
					}
					model.fireTableDataChanged();
					setButtonStates();
				}
			}
		});
		this.add(revertButton, "gap rel rel para para, growx 1, bottom, wrap");
		
		setButtonStates();
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				setButtonStates();
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					editButton.doClick();
				}
			}
		});
		
		
		this.add(new JLabel("<html><i>Editing materials will not affect existing " +
				"rocket designs.</i>"), "span");
		
		
	}
	
	
	private Database<Material> getDatabase(Material m) {
		switch (m.getType()) {
		case BULK:
			return Databases.BULK_MATERIAL;
			
		case SURFACE:
			return Databases.SURFACE_MATERIAL;
			
		case LINE:
			return Databases.LINE_MATERIAL;

		default:
			throw new IllegalArgumentException("Material type invalid, m="+m);
		}
	}
	
	
	private void setButtonStates() {
		int sel = table.getSelectedRow();
		
		// Add button always enabled
		addButton.setEnabled(true);
		
		// Edit button enabled if a material is selected
		editButton.setEnabled(sel >= 0);
		
		// Delete button enabled if a user-defined material is selected
		if (sel >= 0) {
			int modelRow = table.convertRowIndexToModel(sel);
			deleteButton.setEnabled(getMaterial(modelRow).isUserDefined());
		} else {
			deleteButton.setEnabled(false);
		}

		// Revert button enabled if any user-defined material exists
		boolean found = false;
		
		for (Material m: Databases.BULK_MATERIAL) {
			if (m.isUserDefined()) {
				found = true;
				break;
			}
		}
		if (!found) {
			for (Material m: Databases.SURFACE_MATERIAL) {
				if (m.isUserDefined()) {
					found = true;
					break;
				}
			}
		}
		if (!found) {
			for (Material m: Databases.LINE_MATERIAL) {
				if (m.isUserDefined()) {
					found = true;
					break;
				}
			}
		}
		revertButton.setEnabled(found);
		
	}
	
	private Material getMaterial(int origRow) {
		int row = origRow;
		int n;
		
		n = Databases.BULK_MATERIAL.size();
		if (row < n) {
			return Databases.BULK_MATERIAL.get(row);
		}
		row -= n;
		
		n = Databases.SURFACE_MATERIAL.size();
		if (row < n) {
			return Databases.SURFACE_MATERIAL.get(row);
		}
		row -= n;
		
		n = Databases.LINE_MATERIAL.size();
		if (row < n) {
			return Databases.LINE_MATERIAL.get(row);
		}
		throw new IndexOutOfBoundsException("row="+origRow+" while material count" +
				" bulk:" + Databases.BULK_MATERIAL.size() + 
				" surface:" + Databases.SURFACE_MATERIAL.size() +
				" line:" + Databases.LINE_MATERIAL.size());
	}
	
	
	private class MaterialCellRenderer extends DefaultTableCellRenderer {

		/* (non-Javadoc)
		 * @see javax.swing.table.DefaultTableCellRenderer#getTableCellRendererComponent(javax.swing.JTable, java.lang.Object, boolean, boolean, int, int)
		 */
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, 
					hasFocus, row, column);
			if (c instanceof JLabel) {
				JLabel label = (JLabel)c;
				Material m = getMaterial(row);
				
				if (isSelected) {
					if (m.isUserDefined())
						label.setForeground(table.getSelectionForeground());
					else
						label.setForeground(Color.GRAY);
				} else {
					if (m.isUserDefined())
						label.setForeground(table.getForeground());
					else
						label.setForeground(Color.GRAY);
				}
			}
			return c;
		}
		
	}

}
