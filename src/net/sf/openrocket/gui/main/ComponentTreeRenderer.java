package net.sf.openrocket.gui.main;



import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

public class ComponentTreeRenderer extends DefaultTreeCellRenderer {

    @Override
	public Component getTreeCellRendererComponent(
            JTree tree,
            Object value,
            boolean sel,
            boolean expanded,
            boolean leaf,
            int row,
            boolean hasFocus) {

    	super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
    	
    	setIcon(ComponentIcons.getSmallIcon(value.getClass()));
    	
    	return this;
    }
	
}
