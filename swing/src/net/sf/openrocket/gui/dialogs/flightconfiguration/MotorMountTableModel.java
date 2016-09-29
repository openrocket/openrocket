package net.sf.openrocket.gui.dialogs.flightconfiguration;

import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * The table model for selecting whether components are motor mounts or not.
 */
class MotorMountTableModel extends AbstractTableModel implements ComponentChangeListener
{
   private static final Logger LOG = LoggerFactory.getLogger(MotorMountTableModel.class);

   private final List<MotorMount> potentialMounts = new ArrayList<>();
   private final Rocket rocket;

   public MotorMountTableModel(Rocket rocket) {
      this.rocket = rocket;

      loadListOfPotentialMounts();
      rocket.addComponentChangeListener(this);
   }

   @Override
   public void componentChanged(ComponentChangeEvent e) {
      if (e.isMotorChange() || e.isTreeChange()) {
         loadListOfPotentialMounts();
      }
   }

   @Override
   public boolean isCellEditable(int row, int column) {
      return column == 0;
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
      ensureValidColumn(column, "MotorMountTableModel.getColumnClass()");

      return ((column == 0) ? Boolean.class : String.class);
   }

   @Override
   public Object getValueAt(int row, int column) {
      ensureValidRow(row, "MotorMountTableModel.getValueAt()");
      ensureValidColumn(column, "MotorMountTableModel.getValueAt()");

      return ((column == 0) ? (Boolean) potentialMounts.get(row).isMotorMount() : potentialMounts.get(row).toString());
   }

   @Override
   public void setValueAt(Object boolValue, int row, int column) {
      ensureValidRow(row, "MotorMountTableModel.setValueAt()");
      ensureValidColumn(column, "MotorMountTableModel.setValueAt()");

      potentialMounts.get(row).setMotorMount((Boolean)boolValue);
      loadListOfPotentialMounts();
   }

   private void loadListOfPotentialMounts() {
      rocket.loadPotentialMounts(potentialMounts);
      fireTableStructureChanged();
   }

   private void ensureValidRow(int row, String method) {
      if (row >= potentialMounts.size()) {
         LOG.debug(method + " was sent an invalid row (" + row + ").");
         throw new IndexOutOfBoundsException("row=" + row);
      }
   }

   private void ensureValidColumn(int column, String method) {
      if ((column < 0) || (column > 1)) {
         LOG.debug(method + " was sent an invalid column (" + column + ").");
         throw new IndexOutOfBoundsException("column=" + column);
      }
   }
}