package info.openrocket.swing.gui.dialogs.flightconfiguration;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import info.openrocket.core.rocketcomponent.ComponentChangeEvent;
import info.openrocket.core.rocketcomponent.ComponentChangeListener;
import info.openrocket.core.rocketcomponent.MotorMount;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.util.ArrayList;

/**
 * The table model for selecting whether components are motor mounts or not.
 */
class MotorMountTableModel extends AbstractTableModel implements ComponentChangeListener {
	private static final long serialVersionUID = 1956400848559941228L;

    private final List<MotorMount> potentialMounts = new ArrayList<>();
	
	private final Rocket rocket;
	
	/**
	 * @param rocket the rocket to select motor mounts from
	 */
	MotorMountTableModel( Rocket rocket) {
		this.rocket = rocket;
		
		initialize();
		rocket.addComponentChangeListener(this);
	}
	
	private void initialize() {
		potentialMounts.clear();
		for (RocketComponent c : rocket) {
			if (c instanceof MotorMount) {
				potentialMounts.add((MotorMount) c);
			}
		}
	}

	@Override
	public void componentChanged(ComponentChangeEvent e) {
		if ( e.isMotorChange() || e.isTreeChange() ) {
			initialize();
			fireTableStructureChanged();
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
	
	@Override
	public int getRowCount() {
		return potentialMounts.size();
	}
	
	@Override
	public Class<?> getColumnClass(int column) {
		return switch (column) {
			case 0 -> Boolean.class;
			case 1 -> String.class;
			default -> throw new IndexOutOfBoundsException("column=" + column);
		};
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		return switch (column) {
			case 0 -> Boolean.valueOf(potentialMounts.get(row).isMotorMount());
			case 1 -> potentialMounts.get(row).toString();
			default -> throw new IndexOutOfBoundsException("column=" + column);
		};
	}
	
	@Override
	public boolean isCellEditable(int row, int column) {
		return column == 0;
	}
	
	@Override
	public void setValueAt(Object value, int row, int column) {
		if (column != 0 || !(value instanceof Boolean)) {
			throw new IllegalArgumentException("column=" + column + ", value=" + value);
		}
		
        MotorMount mount = potentialMounts.get(row);
        mount.setMotorMount((Boolean) value);
	}
}