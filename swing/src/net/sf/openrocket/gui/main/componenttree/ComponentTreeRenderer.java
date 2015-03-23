package net.sf.openrocket.gui.main.componenttree;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

import net.sf.openrocket.gui.main.ComponentIcons;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MassComponent.MassComponentType;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.TextUtil;

public class ComponentTreeRenderer extends DefaultTreeCellRenderer {

	private static final Translator trans = Application.getTranslator();

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus1) {

		Component comp = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus1);

		// Set icon

		RocketComponent c = (RocketComponent) value;

		if (c.getClass().isAssignableFrom(MassComponent.class)) {
			MassComponentType t = ((MassComponent) c).getMassComponentType();
			setIcon(ComponentIcons.getSmallMassTypeIcon(t));
		} else {
			setIcon(ComponentIcons.getSmallIcon(value.getClass()));
		}
		if (c.isMassOverridden() || c.isCGOverridden()) {
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
			p.setBackground(UIManager.getColor("Tree.textBackground"));
			p.setForeground(UIManager.getColor("Tree.textForeground"));
			p.add(comp/* , BorderLayout.WEST */);
			if (c.isMassOverridden()) {
				p.add(new JLabel(Icons.MASS_OVERRIDE));
			}
			if (c.isCGOverridden()) {
				p.add(new JLabel(Icons.CG_OVERRIDE));
			}
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
		if (c.isMassive() || c.isMassOverridden()) {
			sb.append(" (").append(
					UnitGroup.UNITS_MASS.toStringUnit(c.getMass()));
			if (c.getChildCount() > 0) {
				sb.append(" of ")
						.append(UnitGroup.UNITS_MASS.toStringUnit(c
								.getSectionMass())).append(" total");
			}
			sb.append(")");
		} else {
			if ((c.getChildCount() > 0) && (c.getSectionMass() > 0)) {
				sb.append(" (")
						.append(UnitGroup.UNITS_MASS.toStringUnit(c
								.getSectionMass())).append(" total)");
			}
		}

		if (c.isMassOverridden()) {
			sb.append(" ").append(trans.get("ComponentTree.ttip.massoverride"));
		}

		if (c.isCGOverridden()) {
			sb.append(" ").append(trans.get("ComponentTree.ttip.cgoverride"));
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
