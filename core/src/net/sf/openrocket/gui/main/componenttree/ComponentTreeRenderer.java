package net.sf.openrocket.gui.main.componenttree;



import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.openrocket.gui.main.ComponentIcons;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.util.TextUtil;

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
		
		// Set icon
		setIcon(ComponentIcons.getSmallIcon(value.getClass()));
		
		// Set tooltip
		RocketComponent c = (RocketComponent) value;
		String comment = c.getComment().trim();
		if (comment.length() > 0) {
			comment = TextUtil.htmlEncode(comment);
			comment = "<html>" + comment.replace("\n", "<br>");
			this.setToolTipText(comment);
		} else {
			this.setToolTipText(null);
		}
		
		return this;
	}
	
}
