package net.sf.openrocket.gui.dialogs;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.aerodynamics.Warning;
import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.gui.adaptors.Column;
import net.sf.openrocket.gui.adaptors.ColumnTable;
import net.sf.openrocket.gui.adaptors.ColumnTableModel;
import net.sf.openrocket.gui.main.BasicFrame;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.widgets.SelectColorButton;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.ORColor;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GeometryWarningsDialog extends JDialog {
    private static final Translator trans = Application.getTranslator();

    private static GeometryWarningsDialog instance = null;
    private final BasicFrame basicFrame;
    private final JTable table;

    private GeometryWarningsDialog(Window owner, WarningSet warnings, final BasicFrame basicFrame) {
        super(owner, trans.get("GeometryWarningsDialog.title"), ModalityType.MODELESS);
        this.setPreferredSize(new Dimension(500, 200));
        this.setLocationRelativeTo(null);

        this.basicFrame = basicFrame;

        JPanel panel = new JPanel(new MigLayout("fill"));
        add(panel);

        { //// Warnings table
            WarningsTableModel model = new WarningsTableModel(new ArrayList<>(warnings));
            this.table = new ColumnTable(model) {
                private static final long serialVersionUID = 1627455047380596654L;
            };
            Font f = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
            table.setFont(f);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            table.setDefaultRenderer(Object.class, new WarningsTableRenderer());
            model.setColumnWidths(table.getColumnModel());
            table.setFillsViewportHeight(true);

            // Table header
            JTableHeader header = table.getTableHeader();
            f = f.deriveFont(13f);
            f = f.deriveFont(Font.BOLD);
            header.setFont(f);
            //// NOTE: you don't have to add this to the panel, it's added automatically if you use a scroll pane.
            //// In fact, if you do decide to add it, guess what? THE F***ING CLOSE BUTTON DOES NOT WORK ANYMORE,
            //// YOU CAN'T CLICK IT!!??!?
            //// Here's a fun joke: What has two thumbs and lost 2 hours of his life because of this? THIS GUY.

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setFocusable(false);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setViewportView(table);
            panel.add(scrollPane, "spanx, grow, push, wrap");

            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
                private final List<Warning> warningList = new ArrayList<>(warnings);

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    if (e.getValueIsAdjusting() || basicFrame == null) {
                        return;
                    }
                    int row = table.getSelectedRow();
                    if (row < 0 || row >= warningList.size()) {
                        return;
                    }
                    Warning warning = warningList.get(row);
                    RocketComponent[] sources = warning.getSources();
                    basicFrame.setSelectedComponents(sources);
                }
            });
        }

        //// Close button
        JButton closeButton = new SelectColorButton(trans.get("button.close"));
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GeometryWarningsDialog.this.dispose();
            }
        });
        panel.add(closeButton, "spanx, pushx, right");

        GUIUtil.setDisposableDialogOptions(this, closeButton);
        GUIUtil.rememberWindowSize(this);
        GUIUtil.rememberWindowPosition(this);
    }

    public static GeometryWarningsDialog showDialog(Window owner, WarningSet warnings, BasicFrame basicFrame) {
        if (warnings.isEmpty()) {
            return null;
        }
        if (instance != null) {
            instance.dispose();
        }
        instance = new GeometryWarningsDialog(owner, warnings, basicFrame);
        instance.setVisible(true);
        return instance;
    }

    public void updateWarnings(WarningSet warnings) {
        WarningsTableModel model = new WarningsTableModel(new ArrayList<>(warnings));
        table.setModel(model);
        model.setColumnWidths(table.getColumnModel());
    }

    private static class WarningsTableModel extends ColumnTableModel {
        private final List<Warning> warnings;

        public WarningsTableModel(List<Warning> warnings) {
            super(
                    //// Warning
                    new Column(trans.get("GeometryWarningsDialog.table.header1")) {
                        @Override
                        public Object getValueAt(int row) {
                            if (row < 0 || row >= warnings.size()) {
                                return null;
                            }
                            return warnings.get(row).getWarningDescription();
                        }

                        @Override
                        public int getDefaultWidth() {
                            return 350;
                        }
                    },
                    //// Source(s)
                    new Column(trans.get("GeometryWarningsDialog.table.header2")) {
                        @Override
                        public Object getValueAt(int row) {
                            if (row < 0 || row >= warnings.size()) {
                                return null;
                            }
                            RocketComponent[] sources = warnings.get(row).getSources();
                            if (sources == null) {
                                return "N/A";
                            } else {
                                String[] sourceNames = new String[sources.length];
                                for (int i = 0; i < sources.length; i++) {
                                    sourceNames[i] = sources[i].getName();
                                }
                                return String.join(", ", sourceNames);
                            }
                        }

                        @Override
                        public int getDefaultWidth() {
                            return 50;
                        }
                    }
            );
            this.warnings = warnings;
        }

        @Override
        public int getRowCount() {
            return this.warnings.size();
        }
    }

    private static class WarningsTableRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            // Alternating row colors
            if (!isSelected) {
                if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(245, 245, 245));
                }
            }
            if (c instanceof JLabel) {
                JLabel label = (JLabel) c;

                // Text color
                if (isSelected) {
                    label.setForeground(Color.WHITE);
                } else {
                    label.setForeground(ORColor.DARK_RED.toAWTColor());
                }
            }
            return c;
        }
    }
}
