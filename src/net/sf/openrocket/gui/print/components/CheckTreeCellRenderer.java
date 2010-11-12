/*
 * CheckTreeCellRenderer.java
 */
package net.sf.openrocket.gui.print.components;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Component;

/**
 * A cell renderer for JCheckBoxes within nodes of a JTree.
 * <p/>
 * Based in part on a blog by Santhosh Kumar.  http://www.jroller.com/santhosh/date/20050610
 */
public class CheckTreeCellRenderer extends JPanel implements TreeCellRenderer {

    /**
     * The selection model.
     */
    private CheckTreeSelectionModel selectionModel;
    /**
     * The delegated cell renderer.
     */
    private DefaultTreeCellRenderer delegate;
    /**
     * The check box within this cell.
     */
    private JCheckBox checkBox = new JCheckBox();

    /**
     * Constructor.
     *
     * @param theDelegate       the delegated cell renderer
     * @param theSelectionModel the selection model
     */
    public CheckTreeCellRenderer (DefaultTreeCellRenderer theDelegate, CheckTreeSelectionModel theSelectionModel) {
        delegate = theDelegate;

        delegate.setLeafIcon(null);
        delegate.setClosedIcon(null);
        delegate.setOpenIcon(null);


        selectionModel = theSelectionModel;
        setLayout(new BorderLayout());
        setOpaque(false);
        checkBox.setOpaque(false);
        checkBox.setSelected(true);
    }

    /**
     * @{inheritDoc}
     */
    @Override
    public Component getTreeCellRendererComponent (JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean hasFocus) {
        Component renderer = delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row,
                                                                   hasFocus);

        TreePath path = tree.getPathForRow(row);
        if (path != null) {
            final boolean b = selectionModel.isPathSelected(path, true);
            checkBox.setSelected(b);
            if (value instanceof DefaultMutableTreeNode) {
                Object obj = ((DefaultMutableTreeNode) value).getUserObject();
                if (obj instanceof CheckBoxNode) {
                    ((CheckBoxNode) obj).setSelected(b);
                }
            }
        }

        removeAll();
        add(checkBox, BorderLayout.WEST);
        add(renderer, BorderLayout.CENTER);
        return this;
    }
} 