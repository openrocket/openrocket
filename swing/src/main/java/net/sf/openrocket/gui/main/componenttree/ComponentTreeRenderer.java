package net.sf.openrocket.gui.main.componenttree;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
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
		if (c.isMassOverridden() || c.getMassOverriddenBy() != null ||
				c.isCGOverridden() || c.getCGOverriddenBy() != null ||
				c.isCDOverridden() || c.getCDOverriddenBy() != null) {
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
			p.setBackground(UIManager.getColor("Tree.textBackground"));
			p.setForeground(UIManager.getColor("Tree.textForeground"));
			p.add(comp/* , BorderLayout.WEST */);
			if (c.getMassOverriddenBy() != null) {
				p.add(new JLabel(Icons.MASS_OVERRIDE_SUBCOMPONENT));
			} else if (c.isMassOverridden()) {
				p.add(new JLabel(Icons.MASS_OVERRIDE));
			}
			if (c.getCGOverriddenBy() != null) {
				p.add(new JLabel(Icons.CG_OVERRIDE_SUBCOMPONENT));
			} else if (c.isCGOverridden()) {
				p.add(new JLabel(Icons.CG_OVERRIDE));
			}
			if (c.getCDOverriddenBy() != null) {
				p.add(new JLabel(Icons.CD_OVERRIDE_SUBCOMPONENT));
			} else if (c.isCDOverridden()) {
				p.add(new JLabel(Icons.CD_OVERRIDE));
			}
			
			// Make sure the tooltip also works on the override icons
			if (components != null && components.size() > 1 && components.contains(c)) {
				p.setToolTipText(getToolTipMultipleComponents(components));
			} else {
				p.setToolTipText(getToolTipSingleComponent(c));
			}

			Font originalFont = tree.getFont();
			p.setFont(originalFont);
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

		// Component name title
		sb.append("<b>").append(c.getName()).append("</b>");

		// Only add mass information if mass is not overridden by a parent component
		RocketComponent overriddenBy = c.getMassOverriddenBy();
		if (overriddenBy == null) {
			if (c.isMassive() || c.isMassOverridden()) {
				sb.append(" (").append(
						UnitGroup.UNITS_MASS.toStringUnit(c.getMass()));
				if (c.getChildCount() > 0) {
					sb.append(" of ")
							.append(UnitGroup.UNITS_MASS.toStringUnit(c
									.getSectionMass())).append(" ").append(trans.get("ComponentTreeRenderer.total"));
				}
				sb.append(")");
			} else {
				if ((c.getChildCount() > 0) && (c.getSectionMass() > 0)) {
					sb.append(" (")
							.append(UnitGroup.UNITS_MASS.toStringUnit(c
									.getSectionMass())).append(" ").append(trans.get("ComponentTreeRenderer.total")).append(")");
				}
			}
		}

		// Add component override information in title
		if (c.isMassOverridden() && c.getMassOverriddenBy() == null) {
			sb.append(", ").append(trans.get("ComponentTree.ttip.massoverride"));
		}
		if (c.isCGOverridden() && c.getCGOverriddenBy() == null) {
			sb.append(", ").append(trans.get("ComponentTree.ttip.cgoverride"));
		}
		if (c.isCDOverridden() && c.getCDOverriddenBy() == null) {
			sb.append(", ").append(trans.get("ComponentTree.ttip.cdoverride"));
		}

		// Add parent component override information on new lines
		if (overriddenBy != null) {
			sb.append("<br>").append(String.format(trans.get("RocketCompCfg.lbl.MassOverriddenBy"), overriddenBy.getName()));
			sb.append(" (").append(
					UnitGroup.UNITS_MASS.toStringUnit(overriddenBy.getMass())).append(")");
		}
		overriddenBy = c.getCGOverriddenBy();
		if (overriddenBy != null) {
			sb.append("<br>").append(String.format(trans.get("RocketCompCfg.lbl.CGOverriddenBy"), overriddenBy.getName()));
			sb.append(" (").append(
					UnitGroup.UNITS_LENGTH.toStringUnit(overriddenBy.getOverrideCGX())).append(")");
		}
		overriddenBy = c.getCDOverriddenBy();
		if (overriddenBy != null) {
			sb.append("<br>").append(String.format(trans.get("RocketCompCfg.lbl.CDOverriddenBy"), overriddenBy.getName()));
			sb.append(" (").append(overriddenBy.getOverrideCD()).append(")");
		}

		// Add component comment on new line
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

		// Components title
		sb.append("<b>Components</b>");

		// Calculate the total mass of the selected components
		double totalMass = 0;
		double totalSectionMass = 0;
		boolean containsSectionMass = false;
		List<RocketComponent> overriddenByComponents = new java.util.ArrayList<>();	// Components that override the mass of the selected components
		for (RocketComponent c : components) {
			RocketComponent overriddenBy = c.getMassOverriddenBy();
			if (overriddenBy != null) {
				if (!components.contains(overriddenBy) && !overriddenByComponents.contains(overriddenBy)) {
					totalMass += overriddenBy.getMass();
					overriddenByComponents.add(overriddenBy);
				}
				continue;
			}

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

		// Add total mass of the selected components in the title
		sb.append(" (").append(UnitGroup.UNITS_MASS.toStringUnit(totalMass));
		if (containsSectionMass) {
			sb.append(" of ").append(UnitGroup.UNITS_MASS.toStringUnit(totalSectionMass))
					.append(" ").append(trans.get("ComponentTreeRenderer.total")).append(")");
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
