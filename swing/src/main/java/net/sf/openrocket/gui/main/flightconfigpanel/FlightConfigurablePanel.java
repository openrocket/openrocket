package net.sf.openrocket.gui.main.flightconfigpanel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

import net.sf.openrocket.gui.main.FlightConfigurationPanel;
import net.sf.openrocket.util.ArrayList;
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
	 * @param ids: the IDs of the flight configurations that are affected by the change
	 */
	public void fireTableDataChanged(int cce, FlightConfigurationId[] ids) {
		int selectedRow = table.getSelectedRow();
		int selectedColumn = table.getSelectedColumn();
		this.rocket.fireComponentChangeEvent(cce, ids);
		((AbstractTableModel)table.getModel()).fireTableDataChanged();
		restoreSelection(selectedRow,selectedColumn);
		updateButtonState();
	}

	public abstract void updateButtonState();
	
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
		table.setFillsViewportHeight(true);
		FlightConfigurationId current = this.rocket.getSelectedConfiguration().getFlightConfigurationID();
		int col = (table.getColumnCount() > 1) ? table.getColumnCount() - 1 : 0;
		for (int row = 0; row < table.getRowCount(); row++) {
			FlightConfigurationId rowFCID = rocket.getId(row);
			if (rowFCID.equals(current)) {
				table.changeSelection(row, col, false, false);
				break;
			}
		}
		return table;
	}

	public final void synchronizeConfigurationSelection() {
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
				col = (table.getColumnCount() > 1) ? table.getColumnCount() - 1 : 0;
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
		table.requestFocusInWindow();
	}

	protected void installTableListener() {
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				if ( e.getValueIsAdjusting() ) {
					return;
				}

				// Don't update the flight configuration for multi-selections
				if (table.getSelectionModel().getSelectedItemsCount() > 1) {
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

		// Clear the table selection when clicked outside the table rows.
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1) {
					int row = table.rowAtPoint(e.getPoint());
					int col = table.columnAtPoint(e.getPoint());
					if (row == -1 || col == -1) {
						clearSelection();
					}
				}
			}
		});

		table.getColumnModel().getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				updateRocketViewSelection(e);
			}
		});
	}

	/**
	 * Override this method to create the embedded JTable and it's backing Model.
	 * 
	 * @return
	 */
	protected abstract JTable initializeTable();

	public void clearSelection() {
		table.clearSelection();
	}

	public void updateRocketViewSelection() {
		ListSelectionEvent e = new ListSelectionEvent(this, 0, 0, false);
		updateRocketViewSelection(e);
	}

	/**
	 * Update the selection in the rocket design view, based on the currently selected motor, recovery device, or stage.
	 */
	public abstract void updateRocketViewSelection(ListSelectionEvent e);

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

	protected List<T> getSelectedComponents() {
		int[] cols = Arrays.stream(table.getSelectedColumns()).map(table::convertRowIndexToModel).toArray();
		int[] rows = Arrays.stream(table.getSelectedRows()).map(table::convertRowIndexToModel).toArray();
		if (Arrays.stream(cols).min().isEmpty() || Arrays.stream(rows).min().isEmpty() ||
				Arrays.stream(cols).min().getAsInt() < 0 || Arrays.stream(rows).min().getAsInt() < 0) {
			return null;
		}
		List<T> components = new ArrayList<>();
		for (int row : rows) {
			for (int col : cols) {
				Object tableValue = table.getModel().getValueAt(row, col);
				if (tableValue instanceof Pair) {
					@SuppressWarnings("unchecked")
					Pair<String, T> selectedComponent = (Pair<String, T>) tableValue;
					components.add(selectedComponent.getV());
				}
			}
		}
		return components;
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

	public List<FlightConfigurationId> getSelectedConfigurationIds() {
		int col = table.convertColumnIndexToModel(table.getSelectedColumn());
		int[] rows = Arrays.stream(table.getSelectedRows()).map(table::convertRowIndexToModel).toArray();
		if (Arrays.stream(rows).min().isEmpty() || Arrays.stream(rows).min().getAsInt() < 0 || col < 0 ||
				Arrays.stream(rows).max().getAsInt() >= table.getRowCount() || col >= table.getColumnCount() ) {
			return null;
		}
		Object[] tableValues = Arrays.stream(rows).mapToObj(c -> table.getModel().getValueAt(c, col)).toArray();
		List<FlightConfigurationId> Ids = new ArrayList<>();
		for (Object tableValue : tableValues) {
			if (tableValue instanceof Pair) {
				@SuppressWarnings("unchecked")
				Pair<FlightConfigurationId, T> selectedComponent = (Pair<FlightConfigurationId, T>) tableValue;
				FlightConfigurationId fcid = selectedComponent.getU();
				Ids.add(fcid);
			} else if (tableValue instanceof FlightConfigurationId) {
				Ids.add((FlightConfigurationId) tableValue);
			} else {
				Ids.add(FlightConfigurationId.ERROR_FCID);
			}
		}

		return Ids;
	}

	/**
	 * Select the rows of the table that correspond to the given FlightConfigurationIds. The second column of the table
	 * will be used for the selection.
	 * @param fids flight configuration ids to select
	 */
	public void setSelectedConfigurationIds(List<FlightConfigurationId> fids) {
		if (fids == null || fids.isEmpty() || table.getColumnCount() == 0) return;

		if (getSelectedConfigurationIds() != null && new HashSet<>(getSelectedConfigurationIds()).containsAll(fids)) return;

		table.clearSelection();
		for (FlightConfigurationId id : fids) {
			if (id == FlightConfigurationId.DEFAULT_VALUE_FCID) continue;
			for (int rowNum = 0; rowNum < table.getRowCount(); rowNum++) {
				FlightConfigurationId rowFCID = rocket.getId(rowNum );
				if (rowFCID.equals(id)) {
					table.changeSelection(rowNum, 1, true, false);
					break;
				}
			}
		}
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

	/**
	 * Focus on the table
	 */
	public void takeTheSpotlight() {
		table.requestFocusInWindow();
	}

}