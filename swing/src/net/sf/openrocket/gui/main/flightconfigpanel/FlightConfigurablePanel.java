package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.formatting.RocketDescriptor;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.ComponentChangeEvent;
import net.sf.openrocket.rocketcomponent.ComponentChangeListener;
import net.sf.openrocket.rocketcomponent.FlightConfigurableComponent;
import net.sf.openrocket.rocketcomponent.FlightConfigurationId;
import net.sf.openrocket.rocketcomponent.Rocket;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Pair;


@SuppressWarnings("serial")
public abstract class FlightConfigurablePanel<T extends FlightConfigurableComponent> extends JPanel implements ComponentChangeListener {

	protected static final Translator trans = Application.getTranslator();
	private static final Logger log = LoggerFactory.getLogger(FlightConfigurablePanel.class);
	protected RocketDescriptor descriptor = Application.getInjector().getInstance(RocketDescriptor.class);

	protected final FlightConfigurationPanel flightConfigurationPanel;
	protected final Rocket rocket;
	protected final JTable table;
	
	public FlightConfigurablePanel(final FlightConfigurationPanel flightConfigurationPanel, Rocket rocket) {
		super(new MigLayout("fill"));
		this.flightConfigurationPanel = flightConfigurationPanel;
		this.rocket = rocket;
		table = doTableInitialization();

		installTableListener();
		synchronizeConfigurationSelection();
	}

	/**
	 * Update the data in the table, with component change event type {cce}
	 * @param cce index of the ComponentChangeEvent to use (e.g. ComponentChangeEvent.NONFUNCTIONAL_CHANGE)
	 */
	public void fireTableDataChanged(int cce) {
		int selectedRow = table.getSelectedRow();
		int selectedColumn = table.getSelectedColumn();
		this.rocket.fireComponentChangeEvent(cce);
		((AbstractTableModel)table.getModel()).fireTableDataChanged();
		restoreSelection(selectedRow,selectedColumn);
		updateButtonState();
	}

	protected abstract void updateButtonState();
	
	@Override
	public void componentChanged(ComponentChangeEvent e) {
		this.synchronizeConfigurationSelection();
	}
	
	/**
	 * Initialize the table using the specific implementation's initializeTable
	 * method and then select the row to match what the rocket's current selected
	 * configuration is.
	 * 
	 * @return the JTable created
	 */
	private final JTable doTableInitialization() {
		JTable table = this.initializeTable();
		FlightConfigurationId current = this.rocket.getSelectedConfiguration().getFlightConfigurationID();
		int col = (table.getColumnCount() > 1) ? 1 : 0;
		for (int row = 0; row < table.getRowCount(); row++) {
			FlightConfigurationId rowFCID = rocket.getId(row);
			if (rowFCID.equals(current)) {
				table.changeSelection(row, col, false, false);
				break;
			}
		}
		return table;
	}

	protected final void synchronizeConfigurationSelection() {
		FlightConfigurationId currentRocketFCID = rocket.getSelectedConfiguration().getFlightConfigurationID();
		FlightConfigurationId selectedFCID = getSelectedConfigurationId();
		
		if ( currentRocketFCID == FlightConfigurationId.DEFAULT_VALUE_FCID ) {
			// need to unselect
			table.clearSelection();
		} else if ( !currentRocketFCID.equals(selectedFCID)){			
			// Need to change selection
			// We'll select the correct row, in the currently selected column.
			int col = table.getSelectedColumn();
			if ( col < 0 ) {
				col = (table.getColumnCount() > 1) ? 1 : 0;
			}
			
			for( int rowNum = 0; rowNum < table.getRowCount(); rowNum++ ) {
				FlightConfigurationId rowFCID = rocket.getId(rowNum );
				if ( rowFCID.equals(currentRocketFCID) ) {
					table.changeSelection(rowNum, col, false, false);
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
				
				/* Find the selected row and set it as the current selected configuration
				 * for the rocket. This will propagate the event to ensure that other
				 * pieces of the UI are updated and match the table selection.
				 */
				int firstrow = e.getFirstIndex();
				int lastrow = e.getLastIndex();
				ListSelectionModel model = (ListSelectionModel) e.getSource();
				for( int row = firstrow; row <= lastrow; row ++) {
					if ( model.isSelectedIndex(row) ) {
						FlightConfigurationId fcid = (FlightConfigurationId) table.getValueAt(row, table.convertColumnIndexToView(0));
						rocket.setSelectedConfiguration(fcid);
						return;
					}
				}
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
			@SuppressWarnings("unchecked")
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
			@SuppressWarnings("unchecked")
			Pair<FlightConfigurationId,T> selectedComponent = (Pair<FlightConfigurationId,T>) tableValue;
			FlightConfigurationId fcid = selectedComponent.getU();
			return fcid;
		} else if ( tableValue instanceof FlightConfigurationId ){
			return (FlightConfigurationId) tableValue;
		}
		return FlightConfigurationId.ERROR_FCID;
	}

	protected abstract class FlightConfigurableCellRenderer extends DefaultTableCellRenderer {

		@Override
		public Component getTableCellRendererComponent(JTable table, Object newValue, boolean isSelected, boolean hasFocus, int row, int column) {
			JLabel label = (JLabel) super.getTableCellRendererComponent(table, newValue, isSelected, hasFocus, row, column);
			Object oldValue = table.getModel().getValueAt(row, column);
			
			// this block is more for the benefit of the reader than the computer -- 
			// this assignment is technically redundant, but useful to point out that the new value here is often null, 
			// while the old value seems to always be valid.
			if( null == newValue ){
				log.warn("Detected null newValue to render... (oldValue: "+oldValue+")");
				newValue = oldValue;
			}

		    column = table.convertColumnIndexToModel(column);
			switch (column) {
			case 0: {
				label.setText(descriptor.format(rocket, (FlightConfigurationId) oldValue));
				regular(label);
				setSelected(label, table, isSelected, hasFocus);
				return label;
			}
			default: {
				@SuppressWarnings("unchecked")
				Pair<FlightConfigurationId, T> v = (Pair<FlightConfigurationId, T>) oldValue;
				
				if(v!=null){
					FlightConfigurationId fcid = v.getU();
					T component = v.getV();
					label = format(component, fcid, label );
				}
				for (Component c : label.getComponents()) {
					if (c instanceof JLabel) {
						setSelected((JLabel)c, table, isSelected, hasFocus);
					}
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
				c.setForeground((Color)UIManager.get("Table.selectionForeground"));
			} else {
				c.setBackground(table.getBackground());
				c.setForeground(c.getForeground());
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