package net.sf.openrocket.gui.dialogs.flightconfiguration;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.rocketcomponent.Rocket;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class MotorMountConfigurationPanel extends JPanel
{
   public MotorMountConfigurationPanel(final Rocket rocket) {
      super(new MigLayout());

      JTable table = buildMotorMountTable(rocket);

      JScrollPane scroll = new JScrollPane(table);
      this.add(scroll);
   }

   private JTable buildMotorMountTable(Rocket rocket) {
      JTable table = new JTable(new MotorMountTableModel(rocket));
      table.setTableHeader(null);
      table.setShowVerticalLines(true);
      table.setShowHorizontalLines(true);
      table.setRowSelectionAllowed(false);
      table.setColumnSelectionAllowed(false);
      table.getColumnModel().getColumn(0).setPreferredWidth(36);
      table.setRowMargin(4);
      table.setRowHeight(30);
      TableColumnModel tcm = table.getColumnModel();
      tcm.getColumn(0).setPreferredWidth(50);
      tcm.getColumn(1).setPreferredWidth(350);
      table.setPreferredScrollableViewportSize(table.getPreferredSize());
      table.addMouseListener(new MouseAdapter()
      {
         public void mouseClicked(MouseEvent evt) {
            if ((evt.getModifiers() & InputEvent.BUTTON1_MASK) != 0) {
               onDataChanged(evt);
            }
         }
      });

      return table;
   }

   private void onDataChanged(MouseEvent e) {
      final JTable table = (JTable) e.getSource();

      final int booleanColumn = 0;

      Point p = e.getPoint();
      int clickedCol = table.columnAtPoint(p);
      int clickedRow = table.rowAtPoint(p);

      if ((clickedCol >= 0) && (clickedRow >= 0)) {
         int tableRow = table.convertRowIndexToModel(clickedRow);
         int tableCol = table.convertColumnIndexToModel(clickedCol);
         if (booleanColumn == tableCol) {
            TableModel model = table.getModel();
            Object value = model.getValueAt(tableRow, booleanColumn);
            if (!(value instanceof Boolean)) {
               throw new IllegalStateException("Table value at row=" + tableRow + " col=" +
                                               booleanColumn + " is not a Boolean, value=" + value);
            }
            boolean newValue = !(Boolean) value;
            model.setValueAt(newValue, tableRow, booleanColumn);
         }
      }
   }
}
