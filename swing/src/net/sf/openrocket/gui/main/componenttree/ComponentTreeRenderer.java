package net.sf.openrocket.gui.main.componenttree;

import java.awt.Component;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sf.openrocket.gui.main.ComponentIcons;
import net.sf.openrocket.gui.util.Icons;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MassComponent.MassComponentType;
import net.sf.openrocket.rocketcomponent.RocketComponent;
import net.sf.openrocket.startup.Application;
import net.sf.openrocket.unit.UnitGroup;
import net.sf.openrocket.util.ArrayList;
import net.sf.openrocket.util.TextUtil;

@SuppressWarnings("serial")
public class ComponentTreeRenderer extends DefaultTreeCellRenderer {

	private static final Translator trans = Application.getTranslator();

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus1) {

		Component comp = super.getTreeCellRendererComponent(tree, value, sel,
				expanded, leaf, row, hasFocus1);
		if (tree == null) return comp;
		TreePath[] paths = tree.getSelectionPaths();
		List<RocketComponent> components = null;
		if (paths != null && paths.length > 0) {
			components = new ArrayList<>(ComponentTreeModel.componentsFromPaths(paths));
		}

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

			if (components != null && components.size() > 1 && components.contains(c)) {
				p.setToolTipText(getToolTipMultipleComponents(components));
			} else {
				p.setToolTipText(getToolTipSingleComponent(c));
			}

			comp = p;
		}

		if (components != null && components.size() > 1 && components.contains(c)) {
			this.setToolTipText(getToolTipMultipleComponents(components));
		} else {
			this.setToolTipText(getToolTipSingleComponent(c));
		}

		return comp;
	}

	private static String getToolTipSingleComponent(RocketComponent c) {
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

	private static String getToolTipMultipleComponents(List<RocketComponent> components) {
		if (components == null || components.size() == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		sb.append("<html>");

		sb.append("<b>Components</b>");
		double totalMass = 0;
		double totalSectionMass = 0;
		boolean containsSectionMass = false;
		for (RocketComponent c : components) {
			if (c.isMassive() || c.isMassOverridden()) {
				totalMass += c.getMass();
				// Don't add this component's mass to the section mass if its parent is in the list, otherwise you add up duplicate mass
				if (!RocketComponent.listContainsParent(components, c)) {
					if (c.getChildCount() > 0 && c.getSectionMass() > 0) {
						totalSectionMass += c.getSectionMass();
						containsSectionMass = true;
					} else {
						totalSectionMass += c.getMass();
					}
				}
			} else if ((c.getChildCount() > 0) && (c.getSectionMass() > 0)) {
				totalMass = c.getSectionMass();
				containsSectionMass = false;
				break;
			}
		}

		sb.append(" (").append(UnitGroup.UNITS_MASS.toStringUnit(totalMass));
		if (containsSectionMass) {
			sb.append(" of ").append(UnitGroup.UNITS_MASS.toStringUnit(totalSectionMass)).append(" total)");
		} else {
			sb.append(")");
		}

		// Set the component names as description
		sb.append("<br>");
		for (int i = 0; i < components.size(); i++) {
			if (i < components.size() - 1) {
				sb.append(components.get(i).getName()).append(", ");
			} else {
				sb.append(components.get(i).getName());
			}
		}

		return sb.toString();
	}

}
