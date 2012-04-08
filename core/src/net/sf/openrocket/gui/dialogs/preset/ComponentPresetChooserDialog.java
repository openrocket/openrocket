package net.sf.openrocket.gui.dialogs.preset;


import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.Value;

public class ComponentPresetChooserDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	//	private final ThrustCurveMotorSelectionPanel selectionPanel;
	
	private final RocketComponent component;
	private final List<ComponentPreset> presets;
	
	private boolean okClicked = false;
	
	
	public ComponentPresetChooserDialog(Window owner, RocketComponent component, final TypedKey<?>... columnKeys) {
		super(owner, trans.get("title"), Dialog.ModalityType.APPLICATION_MODAL);
		
		this.component = component;
		
		presets = Application.getComponentPresetDao().listAll();
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		final Column[] columns = new Column[columnKeys.length+1];
		
		columns[0] = new Column(trans.get("table.column.Favorite") ) {
			@Override
			public Object getValueAt(int row) {
				return Boolean.valueOf(ComponentPresetChooserDialog.this.presets.get(row).isFavorite());
			}
			
			@Override
			public void setValueAt(int row, Object value) {
				Application.getComponentPresetDao().setFavorite(ComponentPresetChooserDialog.this.presets.get(row), (Boolean) value);
			}

			@Override
			public Class<?> getColumnClass() {
				return Boolean.class;
			}
			
		};
		for (int i = 0; i < columnKeys.length; i++) {
			final TypedKey<?> key = columnKeys[i];
			columns[i+1] = new Column(trans.get("table.column." + columnKeys[i].getName())) {
				@Override
				public Object getValueAt(int row) {
					if (key.getType() == Double.class && key.getUnitGroup() != null) {
						double v = (Double) ComponentPresetChooserDialog.this.presets.get(row).get(key);
						return new Value( v, key.getUnitGroup() );
					} else {
						return ComponentPresetChooserDialog.this.presets.get(row).get(key);
					}
				}
			};
		}
		
		ColumnTableModel tableModel = new ColumnTableModel(columns) {
			@Override
			public int getRowCount() {
				return ComponentPresetChooserDialog.this.presets.size();
			}

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return columnIndex == 0;
			}
			
		};
		
		final JTable table = new JTable( tableModel );
		
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel);
		// FIXME we might need some custom sorters.
		/*for (int i = 0; i < columnKeys.length; i++) {
			TypedKey<?> column = columnKeys[i];
			sorter.setComparator(i, column.getComparator());
		}*/
		table.setRowSorter(sorter);

		JScrollPane scrollpane = new JScrollPane();
		scrollpane.setViewportView(table);
		panel.add(scrollpane, "grow, width :500:, height :300:, spanx, wrap para");

		
		// OK / Cancel buttons
		JButton okButton = new JButton(trans.get("dlg.but.ok"));
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close(true);
			}
		});
		panel.add(okButton, "tag ok, spanx, split");
		
		//// Cancel button
		JButton cancelButton = new JButton(trans.get("dlg.but.cancel"));
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close(false);
			}
		});
		panel.add(cancelButton, "tag cancel");
		
		this.add(panel);
		
		this.setModal(true);
		this.pack();
		this.setLocationByPlatform(true);
		GUIUtil.setDisposableDialogOptions(this, okButton);
		
		//JComponent focus = selectionPanel.getDefaultFocus();
		//if (focus != null) {
		//	focus.grabFocus();
		//}
		
		// Set the closeable dialog after all initialization
		//selectionPanel.setCloseableDialog(this);
	}
	
	/**
	 * Return the motor selected by this chooser dialog, or <code>null</code> if the selection has been aborted.
	 * 
	 * @return	the selected motor, or <code>null</code> if no motor has been selected or the selection was canceled.
	 */
	public ComponentPreset getSelectedComponentPreset() {
		if (!okClicked)
			return null;
		//return selectionPanel.getSelectedMotor();
		return null;
	}
	
	public void close(boolean ok) {
		okClicked = ok;
		this.setVisible(false);
		
		ComponentPreset preset = getSelectedComponentPreset();
		if (okClicked && preset != null) {
			//selectionPanel.selectedMotor(selected);
		}
	}
	
}
