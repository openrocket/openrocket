/*
 * CheckTreeManager.java
 */
package net.sf.openrocket.gui.print.components;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * This class manages mouse clicks within the JTree, handling selection/deselections within the JCheckBox of each cell in the tree.
 */
public class CheckTreeManager extends MouseAdapter implements TreeSelectionListener {
    
    /** The selection model. */
    private CheckTreeSelectionModel selectionModel;
    /** The actual JTree instance. */
    private JTree tree;
    /** The number of pixels of width of the check box.  Clicking anywhere within the box will trigger actions. */
    int hotspot = new JCheckBox().getPreferredSize().width;

    /**
     * Construct a check box tree manager.
     * 
     * @param theTree  the actual tree being managed
     */
    public CheckTreeManager (RocketPrintTree theTree) {
        tree = theTree;
        selectionModel = new CheckTreeSelectionModel(tree.getModel());
        theTree.setCheckBoxSelectionModel(selectionModel);
        tree.setCellRenderer(new CheckTreeCellRenderer((DefaultTreeCellRenderer)tree.getCellRenderer(), selectionModel));
        tree.addMouseListener(this);
        selectionModel.addTreeSelectionListener(this);
        
        for (int x = 0; x < tree.getRowCount(); x++) {
            tree.getSelectionModel().setSelectionPath(tree.getPathForRow(x));
        }
    }

    public void addTreeSelectionListener (TreeSelectionListener tsl) {
        selectionModel.addTreeSelectionListener(tsl);
    }
    
    /**
     * Called when the mouse clicks within the tree.
     * 
     * @param me  the event that triggered this
     */
    @Override
    public void mouseClicked (MouseEvent me) {
        TreePath path = tree.getPathForLocation(me.getX(), me.getY());
        if (path == null) {
            return;
        }
        if (me.getX() > tree.getPathBounds(path).x + hotspot) {
            return;
        }

        boolean selected = selectionModel.isPathSelected(path, true);
        selectionModel.removeTreeSelectionListener(this);

        try {
            if (selected) {
                selectionModel.removeSelectionPath(path);
            }
            else {
                selectionModel.addSelectionPath(path);
            }
        }
        finally {
            selectionModel.addTreeSelectionListener(this);
            tree.treeDidChange();
        }
    }

    /**
     * Get the selection model being used by this manager.
     * 
     * @return the selection model
     */
    public CheckTreeSelectionModel getSelectionModel () {
        return selectionModel;
    }

    /**
     * Notify the tree that it changed.
     * 
     * @param e unused 
     */
    @Override
    public void valueChanged (TreeSelectionEvent e) {
        tree.treeDidChange();
    }
}