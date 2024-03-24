package net.sf.openrocket.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.gui.components.StyledLabel;
import net.sf.openrocket.gui.util.BetterListCellRenderer;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.UITheme;
import net.sf.openrocket.logging.Error;
import net.sf.openrocket.logging.ErrorSet;
import net.sf.openrocket.logging.Warning;
import net.sf.openrocket.logging.WarningSet;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * A message dialog displaying errors and warnings.
 */
@SuppressWarnings("serial")
public abstract class ErrorWarningDialog {
    private static Border border;
    private static Color darkErrorColor;
    private static Color textSelectionForegroundColor;

    static {
        initColors();
    }

    private static void initColors() {
        updateColors();
        UITheme.Theme.addUIThemeChangeListener(ErrorWarningDialog::updateColors);
    }

    private static void updateColors() {
        border = GUIUtil.getUITheme().getBorder();
        darkErrorColor = GUIUtil.getUITheme().getDarkErrorColor();
        textSelectionForegroundColor = GUIUtil.getUITheme().getTextSelectionForegroundColor();
    }

    public static void showErrorsAndWarnings(Component parent, Object message, String title, ErrorSet errors, WarningSet warnings) {
        JPanel content = new JPanel(new MigLayout("ins 0, fillx"));

        StyledLabel label = new StyledLabel("Errors");
        label.setFontColor(darkErrorColor);
        content.add(label, "wrap, gaptop 15lp");

        Error[] e = errors.toArray(new Error[0]);
        final JList<Error> errorList = new JList<>(e);
        errorList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        errorList.setCellRenderer(new BetterListCellRenderer(darkErrorColor));
        JScrollPane errorPane = new JScrollPane(errorList);
        errorList.setBorder(border);
        content.add(errorPane, "wrap, growx");

        // Deselect items if clicked on blank region
        errorList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selectedIndex = errorList.locationToIndex(e.getPoint());
                if (selectedIndex < 0 || !errorList.getCellBounds(0, errorList.getLastVisibleIndex()).contains(e.getPoint())) {
                    errorList.clearSelection();
                }
            }
        });

        content.add(new JSeparator(JSeparator.HORIZONTAL), "wrap");

        content.add(new JLabel("Warnings:"), "wrap");

        Warning[] w = warnings.toArray(new Warning[0]);
        final JList<Warning> warningList = new JList<>(w);
        warningList.setCellRenderer(new BetterListCellRenderer());
        JScrollPane warningPane = new JScrollPane(warningList);
        warningList.setBorder(border);
        content.add(warningPane, "wrap, growx");

        // Deselect items if clicked on blank region
        warningList.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int selectedIndex = warningList.locationToIndex(e.getPoint());
                if (selectedIndex < 0 || !warningList.getCellBounds(0, warningList.getLastVisibleIndex()).contains(e.getPoint())) {
                    warningList.clearSelection();
                }
            }
        });

        JOptionPane.showMessageDialog(parent, new Object[] { message, content },
                title, JOptionPane.WARNING_MESSAGE);

    }
}
