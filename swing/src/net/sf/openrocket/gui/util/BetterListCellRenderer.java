package net.sf.openrocket.gui.util;


import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import java.awt.Color;
import java.awt.Component;

/**
 * An improved list cell renderer, with alternating row background colors.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class BetterListCellRenderer extends DefaultListCellRenderer {
    private static Color rowBackgroundDarkerColor;
    private static Color rowBackgroundLighterColor;
    private static Color textSelectionForegroundColor;
    private static Color textColor;

    static {
        initColors();
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        // Alternating row colors
        if (!isSelected) {
            if (index % 2 == 0) {
                label.setBackground(rowBackgroundDarkerColor);
            } else {
                label.setBackground(rowBackgroundLighterColor);
            }
        }
        // Text color
        if (isSelected) {
            label.setForeground(textSelectionForegroundColor);
        } else {
            label.setForeground(textColor);
        }
        return label;
    }

    private static void initColors() {
        updateColors();
        UITheme.Theme.addUIThemeChangeListener(BetterListCellRenderer::updateColors);
    }

    private static void updateColors() {
        rowBackgroundDarkerColor = GUIUtil.getUITheme().getRowBackgroundDarkerColor();
        rowBackgroundLighterColor = GUIUtil.getUITheme().getRowBackgroundLighterColor();
        textSelectionForegroundColor = GUIUtil.getUITheme().getTextSelectionForegroundColor();
        textColor = GUIUtil.getUITheme().getTextColor();
    }
}
