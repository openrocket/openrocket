package net.sf.openrocket.gui.util;


import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Component;

/**
 * An improved list cell renderer, with alternating row background colors.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class BetterListCellRenderer extends DefaultListCellRenderer {

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Alternating row colors
        if (!isSelected) {
            if (index % 2 == 0) {
                label.setBackground(GUIUtil.getUITheme().getRowBackgroundDarkerColor());
            } else {
                label.setBackground(GUIUtil.getUITheme().getRowBackgroundLighterColor());
            }
        }
        // Text color
        if (isSelected) {
            label.setForeground(GUIUtil.getUITheme().getTextSelectionForegroundColor());
        } else {
            label.setForeground(GUIUtil.getUITheme().getTextColor());
        }
        return label;
    }
}
