package net.sf.openrocket.gui.main.componenttree;



import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.openrocket.gui.main.ComponentIcons;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.unit.UnitGroup;
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
			boolean hasFocus1) {
		
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus1);
		
		// Set icon
		setIcon(ComponentIcons.getSmallIcon(value.getClass()));
		
		// Set tooltip
		RocketComponent c = (RocketComponent) value;
		this.setToolTipText(getToolTip(c));
		
		return this;
	}
	
	
	private String getToolTip(RocketComponent c) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		
		sb.append("<b>").append(c.getName()).append("</b>");
		if (c.isMassive()) {
			sb.append(" (").append(UnitGroup.UNITS_MASS.toStringUnit(c.getMass())).append(")");
		}
		
		String comment = c.getComment().trim();
		if (comment.length() > 0) {
			comment = TextUtil.escapeXML(comment);
			comment = comment.replace("\n", "<br>");
			sb.append("<br>").append(comment);
		}
		
		return sb.toString();
	}
	
}
