package net.sf.openrocket.gui.dialogs.preset;


import java.awt.Dialog;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.preset.ComponentPreset;
import net.sf.openrocket.preset.TypedKey;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;

public class ComponentPresetChooserDialog extends JDialog {
	
	private static final Translator trans = Application.getTranslator();
	
	//	private final ThrustCurveMotorSelectionPanel selectionPanel;
	
	private final RocketComponent component;
	private final List<ComponentPreset> presets;
	
	private boolean okClicked = false;
	
	
	public ComponentPresetChooserDialog(Window owner, RocketComponent component, TypedKey<?>... columnKeys) {
		super(owner, trans.get("title"), Dialog.ModalityType.APPLICATION_MODAL);
		
		this.component = component;
		
		// FIXME: Make generic for component type
		presets = Application.getComponentPresetDao().listAll();
		
		
		
		
		JPanel panel = new JPanel(new MigLayout("fill"));
		
		/*
		Column[] columns = new Column[columnKeys.length];
		
		for (int i = 0; i < columnKeys.length; i++) {
			final TypedKey<?> key = columnKeys[i];
			columns[i] = new Column(trans.get("table.column." + columnKeys[i].getName())) {
				@Override
				public Object getValueAt(int row) {
					if (key.getType() == Double.class && key.getUnitGroup() != null) {
						return new Value(, null);
					}
					
					
					// TODO Auto-generated method stub
					return null;
				}
			};
		}
		
		ColumnTableModel tableModel = new ColumnTableModel(columns) {
			@Override
			public int getRowCount() {
				// FIXME Auto-generated method stub
				return 0;
			}
		}
		*/
		
		
		
		
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
