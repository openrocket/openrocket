package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.EventObject;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Pair;
import net.sf.openrocket.util.StateChangeListener;

public abstract class FlightConfigurablePanel<T extends FlightConfigurableComponent> extends JPanel {

	private static final long serialVersionUID = 3359871704879603700L;
	protected static final Translator trans = Application.getTranslator();
	protected RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	protected final FlightConfigurationPanel flightConfigurationPanel;
	protected final Rocket rocket;
	protected final JTable table;
	
	public FlightConfigurablePanel(final FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationPanel = flightConfigurationPanel;
		this.rocket = rocket;
		table = initializeTable();

		installTableListener();
		synchronizeConfigurationSelection();
	}

	public void fireTableDataChanged() {
		int selectedRow = table.getSelectedRow();
		int selectedColumn = table.getSelectedColumn();	
		((AbstractTableModel)table.getModel()).fireTableDataChanged();
		restoreSelection(selectedRow,selectedColumn);
		updateButtonState();
	}

	protected abstract void updateButtonState();

	protected final void synchronizeConfigurationSelection() {
		FlightConfigurationId defaultFCID = rocket.getSelectedConfiguration().getFlightConfigurationID();
		FlightConfigurationId selectedFCID = getSelectedConfigurationId();
		
		if ( selectedFCID == null ) {
			// need to unselect
			table.clearSelection();
		} else if ( !defaultFCID.equals(selectedFCID)){			
			// Need to change selection
			// We'll select the correct row, in the currently selected column.
			int col = table.getSelectedColumn();
			if ( col < 0 ) {
				col = (table.getColumnCount() > 1) ? 1 : 0;
			}
			
			for( int rowNum = 0; rowNum < table.getRowCount(); rowNum++ ) {
				FlightConfigurationId rowFCID = rocket.getId(rowNum );
				if ( rowFCID.equals(selectedFCID) ) {
					table.changeSelection(rowNum, col, true, false);
					break;
				}
			}
		}
	}

	protected void restoreSelection( int row, int col ) {
		if ( row <= 0 || col <= 0 ) {
			synchronizeConfigurationSelection();
			return;
		}
		if ( row >= table.getRowCount() || col >= table.getColumnCount() ) {
			synchronizeConfigurationSelection();
			return;
		}
		table.changeSelection(row, col, true, false);
	}

	private final void installTableListener() {
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if ( e.getValueIsAdjusting() ) {
					return;
				}
//				int firstrow = e.getFirstIndex();
//				int lastrow = e.getLastIndex();
//				ListSelectionModel model = (ListSelectionModel) e.getSource();
//				for( int row = firstrow; row <= lastrow; row ++) {
//					if ( model.isSelectedIndex(row) ) {
//						FlightConfigurationID fcid = (FlightConfigurationID) table.getValueAt(row, table.convertColumnIndexToView(0));
//						FlightConfiguration config = rocket.getConfigurationSet().get(fcid);
//						return;
//					}
//				}
			}

		});
	}

	/**
	 * Override this method to create the embedded JTable and it's backing Model.
	 * 
	 * @return
	 */
	protected abstract JTable initializeTable();

	protected T getSelectedComponent() {

		int col = table.convertColumnIndexToModel(table.getSelectedColumn());
		int row = table.convertRowIndexToModel(table.getSelectedRow());
		if ( row < 0 || col < 0 ) {
			return null;
		}
		Object tableValue = table.getModel().getValueAt(row, col);
		if ( tableValue instanceof Pair ) {
			Pair<String,T> selectedComponent = (Pair<String,T>) tableValue;
			return selectedComponent.getV();
		}
		return null;
	}

	protected FlightConfigurationId  getSelectedConfigurationId() {
		int col = table.convertColumnIndexToModel(table.getSelectedColumn());
		int row = table.convertRowIndexToModel(table.getSelectedRow());
		if ( row < 0 || col < 0 || row >= table.getRowCount() || col >= table.getColumnCount() ) {
			return null;
		}
		Object tableValue = table.getModel().getValueAt(row, col);
		if ( tableValue instanceof Pair ) {
			Pair<FlightConfigurationId,T> selectedComponent = (Pair<FlightConfigurationId,T>) tableValue;
			FlightConfigurationId fcid = selectedComponent.getU();
			return fcid;
		} else if ( tableValue instanceof FlightConfigurationId ){
			return (FlightConfigurationId) tableValue;
		}
		return FlightConfigurationId.ERROR_FCID;
	}

	protected abstract class FlightConfigurableCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 2026945220957913776L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			JLabel label = (JLabel) c;

			column = table.convertColumnIndexToModel(column);
			switch (column) {
			case 0: {
				label.setText(descriptor.format(rocket, (FlightConfigurationId) value));
				regular(label);
				setSelected(label, table, isSelected, hasFocus);
				return label;
			}
			default: {
				@SuppressWarnings("unchecked")
				Pair<FlightConfigurationId, T> v = (Pair<FlightConfigurationId, T>) value;
				
				if(v!=null){
					FlightConfigurationId fcid = v.getU();
					T component = v.getV();
					label = format(component, fcid, label );
				}
				setSelected(label, table, isSelected, hasFocus);
				return label;
			}
			}	
		}

		private final void setSelected( JComponent c, JTable table, boolean isSelected, boolean hasFocus ) {
			c.setOpaque(true);
			if ( isSelected) {
				c.setBackground(table.getSelectionBackground());
			} else {
				c.setBackground(table.getBackground());
			}
			Border b = null;
			if ( hasFocus ) {
				if (isSelected) {
					b = UIManager.getBorder("Table.focusSelectedCellHighlightBorder");
				} else {
					b = UIManager.getBorder("Table.focusCellHighligtBorder");
				}
			} else {
				b = new EmptyBorder(1,1,1,1);
			}
			c.setBorder(b);
		}

		protected final void shaded(JLabel label) {
			GUIUtil.changeFontStyle(label, Font.ITALIC);
			label.setForeground(Color.GRAY);
		}

		protected final void regular(JLabel label) {
			GUIUtil.changeFontStyle(label, Font.PLAIN);
			label.setForeground(Color.BLACK);
		}

		protected abstract JLabel format( T component, FlightConfigurationId configId, JLabel label );

	}

}