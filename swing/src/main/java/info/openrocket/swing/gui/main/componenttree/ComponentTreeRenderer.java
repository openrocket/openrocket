package info.openrocket.swing.gui.main.componenttree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;

import info.openrocket.swing.gui.main.ComponentIcons;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.MassComponent.MassComponentType;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.startup.Application;
import info.openrocket.core.unit.UnitGroup;
import info.openrocket.core.util.ArrayList;
import info.openrocket.core.util.TextUtil;

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
		BorderLayout layout = new BorderLayout();
		layout.setHgap(4);
		JPanel panel = new JPanel(layout);
		JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		label.setIcon(null);
		panel.add(label, BorderLayout.CENTER);

		// Set the background color based on the selection state
		panel.setOpaque(false);
		if (sel) {
			label.setOpaque(true);
			label.setBackground(new Color(UIManager.getColor("Tree.selectionBackground").getRGB()));
			label.setForeground(new Color(UIManager.getColor("Tree.selectionForeground").getRGB()));
		} else {
			label.setOpaque(false);
			label.setForeground(new Color(UIManager.getColor("Tree.textForeground").getRGB()));
		}

		if (tree == null) {
			return label;
		}

		TreePath[] paths = tree.getSelectionPaths();
		List<RocketComponent> components = null;
		if (paths != null && paths.length > 0) {
			components = new ArrayList<>(ComponentTreeModel.componentsFromPaths(paths));
		}

		// Set the tooltip text
		RocketComponent c = (RocketComponent) value;
		applyToolTipText(components, c, panel);

		// Set the tree icon
		final Icon treeIcon;
		if (c.getClass().isAssignableFrom(MassComponent.class)) {
			MassComponentType t = ((MassComponent) c).getMassComponentType();
			treeIcon = ComponentIcons.getSmallMassTypeIcon(t);
		} else {
			treeIcon = ComponentIcons.getSmallIcon(value.getClass());
		}

		panel.add(new JLabel(treeIcon), BorderLayout.WEST);

		// Add mass/CG/CD overridden icons
		if (c.isMassOverridden() || c.getMassOverriddenBy() != null ||
				c.isCGOverridden() || c.getCGOverriddenBy() != null ||
				c.isCDOverridden() || c.getCDOverriddenBy() != null) {
			List<Icon> icons = new LinkedList<>();
			if (c.getMassOverriddenBy() != null) {
				icons.add(massOverrideSubcomponentIcon);
			} else if (c.isMassOverridden()) {
				icons.add(massOverrideIcon);
			}
			if (c.getCGOverriddenBy() != null) {
				icons.add(CGOverrideSubcomponentIcon);
			} else if (c.isCGOverridden()) {
				icons.add(CGOverrideIcon);
			}
			if (c.getCDOverriddenBy() != null) {
				icons.add(CDOverrideSubcomponentIcon);
			} else if (c.isCDOverridden()) {
				icons.add(CDOverrideIcon);
			}

			Icon combinedIcon = combineIcons(3, icons.toArray(new Icon[0]));
			JLabel overrideIconsLabel = new JLabel(combinedIcon);
			panel.add(overrideIconsLabel, BorderLayout.EAST);
		}

		return panel;
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(ComponentTreeRenderer::updateColors);
	}

	private static void updateColors() {
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

	private static Icon combineIcons(int iconSpacing, Icon... icons) {
		if (icons == null || icons.length == 0) {
			return null;
		}

		int width = 0;
		int height = 0;

		for (int i = 0; i < icons.length; i++) {
			Icon icon = icons[i];
			if (icon != null) {
				int spacing = (i == icons.length-1) ? 0 : iconSpacing;
				width += (icon.getIconWidth() + spacing);
				height = Math.max(height, icon.getIconHeight());
			}
		}

		final int finalWidth = width;
		final int finalHeight = height;

		return new Icon() {
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				int xPosition = x;

				for (int i = 0; i < icons.length; i++) {
					Icon icon = icons[i];
					if (icon != null) {
						int spacing = (i == icons.length-1) ? 0 : iconSpacing;
						icon.paintIcon(c, g, xPosition, y);
						xPosition += (icon.getIconWidth() + spacing);
					}
				}
			}

			@Override
			public int getIconWidth() {
				return finalWidth;
			}

			@Override
			public int getIconHeight() {
				return finalHeight;
			}
		};
	}
}
