package net.sf.openrocket.gui.main.componenttree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import net.sf.openrocket.gui.main.ComponentIcons;
import net.sf.openrocket.gui.util.GUIUtil;
import net.sf.openrocket.gui.util.UITheme;
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
	
	private static Color textSelectionBackgroundColor;
	private static Color textSelectionForegroundColor;
	private static Color componentTreeBackgroundColor;
	private static Color componentTreeForegroundColor;
	private static Icon massOverrideSubcomponentIcon;
	private static Icon massOverrideIcon;
	private static Icon CGOverrideSubcomponentIcon;
	private static Icon CGOverrideIcon;
	private static Icon CDOverrideSubcomponentIcon;
	private static Icon CDOverrideIcon;

	static {
		initColors();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus1) {

		// Create a new JPanel
		JPanel panel = new JPanel();
		panel.setOpaque(false); // Set this to false if you want to keep the tree's default background intact
		panel.setLayout(new BorderLayout());

		// Create two JLabels, one for the icon and one for the text
		JLabel iconLabel = new JLabel();
		JLabel textLabel = new JLabel();

		// Retrieve the component from the super method
		Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus1);
		if (tree == null) return comp;
		TreePath[] paths = tree.getSelectionPaths();
		List<RocketComponent> components = null;
		if (paths != null && paths.length > 0) {
			components = new ArrayList<>(ComponentTreeModel.componentsFromPaths(paths));
		}
		if (comp instanceof JLabel) {
			textLabel.setText(((JLabel) comp).getText());

			// Set the font to the tree font
			Font treeFont = UIManager.getFont("Tree.font");
			textLabel.setFont(treeFont);
		}

		// Set the icon
		RocketComponent c = (RocketComponent) value;
		Border iconMarginBorder = BorderFactory.createEmptyBorder(0, 0, 0, 4); // 4-pixel gap to the right of the icon
		if (c.getClass().isAssignableFrom(MassComponent.class)) {
			MassComponentType t = ((MassComponent) c).getMassComponentType();
			iconLabel.setIcon(ComponentIcons.getSmallMassTypeIcon(t));
			iconLabel.setBorder(iconMarginBorder);
		} else {
			iconLabel.setIcon(ComponentIcons.getSmallIcon(value.getClass()));
			iconLabel.setBorder(iconMarginBorder);
		}

		// Add the JLabels to the JPanel
		panel.add(iconLabel, BorderLayout.WEST);
		panel.add(textLabel, BorderLayout.CENTER);

		// Set the background and foreground colors of the text JLabel
		if (sel) {
			textLabel.setOpaque(true);
			textLabel.setBackground(textSelectionBackgroundColor);
			textLabel.setForeground(textSelectionForegroundColor);
		} else {
			textLabel.setOpaque(true); // Set this to true to allow the background color to be visible
			textLabel.setBackground(componentTreeBackgroundColor);
			textLabel.setForeground(componentTreeForegroundColor);
		}

		applyToolTipText(components, c, panel);

		comp = panel;

		// Add mass/CG/CD overridden icons
		if (c.isMassOverridden() || c.getMassOverriddenBy() != null ||
				c.isCGOverridden() || c.getCGOverriddenBy() != null ||
				c.isCDOverridden() || c.getCDOverriddenBy() != null) {
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.LEFT, 1, 1));
			p.setBackground(componentTreeBackgroundColor);
			p.setForeground(componentTreeForegroundColor);
			p.add(comp/* , BorderLayout.WEST */);
			if (c.getMassOverriddenBy() != null) {
				p.add(new JLabel(massOverrideSubcomponentIcon));
			} else if (c.isMassOverridden()) {
				p.add(new JLabel(massOverrideIcon));
			}
			if (c.getCGOverriddenBy() != null) {
				p.add(new JLabel(CGOverrideSubcomponentIcon));
			} else if (c.isCGOverridden()) {
				p.add(new JLabel(CGOverrideIcon));
			}
			if (c.getCDOverriddenBy() != null) {
				p.add(new JLabel(CDOverrideSubcomponentIcon));
			} else if (c.isCDOverridden()) {
				p.add(new JLabel(CDOverrideIcon));
			}
			
			// Make sure the tooltip also works on the override icons
			applyToolTipText(components, c, p);

			Font originalFont = tree.getFont();
			p.setFont(originalFont);
			comp = p;
		}

		applyToolTipText(components, c, this);

		return comp;
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(ComponentTreeRenderer::updateColors);
	}
	
	private static void updateColors() {
		textSelectionBackgroundColor = GUIUtil.getUITheme().getTextSelectionBackgroundColor();
		textSelectionForegroundColor = GUIUtil.getUITheme().getTextSelectionForegroundColor();
		componentTreeBackgroundColor = GUIUtil.getUITheme().getComponentTreeBackgroundColor();
		componentTreeForegroundColor = GUIUtil.getUITheme().getComponentTreeForegroundColor();

		massOverrideSubcomponentIcon = GUIUtil.getUITheme().getMassOverrideSubcomponentIcon();
		massOverrideIcon = GUIUtil.getUITheme().getMassOverrideIcon();
		CGOverrideSubcomponentIcon = GUIUtil.getUITheme().getCGOverrideSubcomponentIcon();
		CGOverrideIcon = GUIUtil.getUITheme().getCGOverrideIcon();
		CDOverrideSubcomponentIcon = GUIUtil.getUITheme().getCDOverrideSubcomponentIcon();
		CDOverrideIcon = GUIUtil.getUITheme().getCDOverrideIcon();
	}

	private void applyToolTipText(List<RocketComponent> components, RocketComponent c, JComponent comp) {
		String tooltipText;
		if (components != null && components.size() > 1 && components.contains(c)) {
			tooltipText = getToolTipMultipleComponents(components);
		} else {
			tooltipText = getToolTipSingleComponent(c);
		}
		comp.setToolTipText(tooltipText);
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
