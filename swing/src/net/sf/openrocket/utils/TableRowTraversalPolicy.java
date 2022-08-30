package net.sf.openrocket.utils;

import net.sf.openrocket.gui.main.SimulationPanel;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

/**
 * Helper class for setting a JTable to traverse rows rather than columns, when the tab/shift-tab, or right-arrow/left-arrow
 * key is pressed.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public abstract class TableRowTraversalPolicy {
    /**
     * Applies the row traversal policy to the given table.
     * @param table table to apply the row traversal policy to
     */
    public static void setTableRowTraversalPolicy(final JTable table) {
        InputMap im = table.getInputMap();
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "Action.NextRowCycle");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "Action.NextRow");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_DOWN_MASK), "Action.PreviousRowCycle");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "Action.PreviousRow");
        ActionMap am = table.getActionMap();
        am.put("Action.NextRow", new NextRowAction(table, false));
        am.put("Action.NextRowCycle", new NextRowAction(table, true));
        am.put("Action.PreviousRow", new PreviousRowAction(table, false));
        am.put("Action.PreviousRowCycle", new PreviousRowAction(table, true));
    }

    private static class NextRowAction extends AbstractAction {
        private final JTable table;
        private final boolean cycle;

        /**
         * Action for cycling through the next row of the table.
         * @param table table for which the action is intended
         * @param cycle whether to go back to the first row if the end is reached.
         */
        public NextRowAction(JTable table, boolean cycle) {
            this.table = table;
            this.cycle = cycle;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int nextRow = table.getSelectedRow() + 1;

            if (nextRow >= table.getRowCount()) {
                if (cycle) {
                    nextRow = 0;
                } else {
                    return;
                }
            }

            table.getSelectionModel().setSelectionInterval(nextRow, nextRow);
            table.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
        }

    }

    private static class PreviousRowAction extends AbstractAction {
        private final JTable table;
        private final boolean cycle;

        /**
         * Action for cycling through the previous row of the table.
         * @param table table for which the action is intended
         * @param cycle whether to go back to the last row if the current row is the first one of the table.
         */
        public PreviousRowAction(JTable table, boolean cycle) {
            this.table = table;
            this.cycle = cycle;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            int nextRow = table.getSelectedRow() - 1;

            if (nextRow < 0) {
                if (cycle) {
                    nextRow = table.getRowCount() - 1;
                } else {
                    return;
                }
            }

            table.getSelectionModel().setSelectionInterval(nextRow, nextRow);
            table.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);
        }
    }
}
