package net.sf.openrocket.gui.main.componenttree;



import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.openrocket.gui.main.ComponentIcons;
import net.sf.openrocket.rocketcomponent.MassComponent;
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
		
		Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus1);
		
		// Set icon
		setIcon(ComponentIcons.getSmallIcon(value.getClass()));
		
		RocketComponent c = (RocketComponent) value;

		if ( c.isMassOverridden() ) {
			JPanel p = new JPanel();
			p.setLayout( new BorderLayout() );
			p.setBackground( UIManager.getColor("Tree.textBackground"));
			p.setForeground( UIManager.getColor("Tree.textForeground"));
			p.add(comp, BorderLayout.WEST);
			p.add(new JLabel( ComponentIcons.getSmallIcon(MassComponent.class) ), BorderLayout.EAST );
			p.setToolTipText(getToolTip(c));
			comp = p;
		}
		
		// Set tooltip
		this.setToolTipText(getToolTip(c));
		
		return comp;
	}
	
	
	private static String getToolTip(RocketComponent c) {
		StringBuilder sb = new StringBuilder();
		sb.append("<html>");
		
		sb.append("<b>").append(c.getName()).append("</b>");
		if (c.isMassive() || c.isMassOverridden() ) {
			sb.append(" (").append(UnitGroup.UNITS_MASS.toStringUnit(c.getMass())).append(")");
		}
		
		if ( c.isMassOverridden() ) {
			sb.append(" mass override");
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
