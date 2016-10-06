package net.sf.openrocket.gui.dialogs.flightconfiguration;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.rocketcomponent.Rocket;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

@SuppressWarnings("serial")
public class MotorMountConfigurationPanel extends JPanel
{
   public MotorMountConfigurationPanel(final Rocket rocket) {
      super(new MigLayout("wrap 2", "[600:pref, fill, grow][20%]"));

      JTable table = buildMotorMountTable(rocket);

      JScrollPane scroll = new JScrollPane(table);
      this.add(scroll);

      this.setSize(150, 490);
      this.setPreferredSize(this.getSize());
   }

   private JTable buildMotorMountTable(Rocket rocket) {
      JTable table = new JTable(new MotorMountTableModel(rocket));
      table.setTableHeader(null);
      table.setShowVerticalLines(true);
      table.setShowHorizontalLines(true);
      table.setRowSelectionAllowed(false);
      table.setColumnSelectionAllowed(false);

      resizeColumnWidth(table);

      table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      table.setRowMargin(4);
      table.setRowHeight(30);
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

   public void resizeColumnWidth(JTable table) {
      final int minWidth = 35;
      final int maxWidth = 150;

      final TableColumnModel columnModel = table.getColumnModel();
      for (int column = 0; column < table.getColumnCount(); column++) {
         int width = minWidth;
         for (int row = 0; row < table.getRowCount(); row++) {
            TableCellRenderer renderer = table.getCellRenderer(row, column);
            Component comp = table.prepareRenderer(renderer, row, column);
            width = Math.max(comp.getPreferredSize().width + 1, width);
         }
         if (width > maxWidth) {
            width = maxWidth;
         }
         columnModel.getColumn(column).setPreferredWidth(width);
      }
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
            MotorMountTableModel model = (MotorMountTableModel) table.getModel();
            Object value = model.getValueAt(tableRow, booleanColumn);
            model.setMotorMount(!(Boolean) value, tableRow);
         }

         resizeColumnWidth(table);
      }
   }
}
