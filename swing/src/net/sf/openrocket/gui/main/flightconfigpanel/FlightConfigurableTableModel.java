package net.sf.openrocket.gui.main.flightconfigpanel;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.*;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Pair;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class FlightConfigurableTableModel<T extends FlightConfigurableComponent> extends AbstractTableModel implements ComponentChangeListener
{
   private static final long serialVersionUID = 3168465083803936363L;
   private static final Translator trans = Application.getTranslator();
   private static final String CONFIGURATION = trans.get("edtmotorconfdlg.col.configuration");

   protected final Rocket rocket;
   private final Class<T> clazz;
   private final List<T> components = new ArrayList<>();

   public FlightConfigurableTableModel(Class<T> clazz, Rocket rocket) {
      super();
      this.rocket = rocket;
      this.clazz = clazz;
      this.rocket.addComponentChangeListener(this);

      initialize();
   }

   @Override
   public void componentChanged(ComponentChangeEvent cce) {
      if (cce.isMotorChange() || cce.isTreeChange()) {
         initialize();
         fireTableStructureChanged();
      }
   }

   /**
    * Return true if this component should be included in the table.
    */
   protected boolean includeComponent(T component) {
      return true;
   }

   @SuppressWarnings("unchecked")
   protected void initialize() {
      components.clear();
      for (RocketComponent c : rocket) {
         if (clazz.isAssignableFrom(c.getClass()) && includeComponent((T) c)) {
            components.add((T) c);
         }
      }
   }

   @Override
   public int getRowCount() {
      return rocket.getConfigurationCount();
   }

   @Override
   public int getColumnCount() {
      return components.size() + 1;
   }

   @Override
   public Object getValueAt(int row, int column) {
      FlightConfigurationId fcid = rocket.getId(row);

      Object retVal;
      if (column == 0) {
         retVal = fcid;
      } else {
         int index = column - 1;
         T d = components.get(index);
         retVal = new Pair<>(fcid, d);
      }

      return retVal;
   }

   @Override
   public String getColumnName(int column) {
      String retVal;

      if (column == 0) {
         retVal = CONFIGURATION;
      } else {
         int index = column - 1;
         T d = components.get(index);
         retVal = d.toString();

      }

      return retVal;
   }
}