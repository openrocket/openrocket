package net.sf.openrocket.gui.dialogs.flightconfiguration;

import java.awt.Component;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.rocketcomponent.Rocket;

public abstract class MotorMountConfigurationPanel extends JPanel {

	
	private final Rocket rocket;
	private final Component parent;
	
	public MotorMountConfigurationPanel( Component parent, Rocket rocket ) {
		super(new MigLayout("") );
		
		this.parent = parent;
		this.rocket = rocket;
		
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

	}

	public abstract void onDataChanged();
}
