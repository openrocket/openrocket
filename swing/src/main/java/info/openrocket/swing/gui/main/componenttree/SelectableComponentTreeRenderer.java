package info.openrocket.swing.gui.main.componenttree;

import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Set;

public class SelectableComponentTreeRenderer extends ComponentTreeRenderer {
	private final Set<RocketComponent> enabledComponents;

	private static Color disabledTextColor;
	private static Font regularFont;
	private static Font italicFont;

	static {
		initColors();
		initFonts();
	}

	public SelectableComponentTreeRenderer(Set<RocketComponent> enabledComponents) {
		this.enabledComponents = enabledComponents;
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SelectableComponentTreeRenderer::updateColors);
	}

	private static void initFonts() {
		regularFont = UIManager.getFont("Tree.font");
		italicFont = regularFont.deriveFont(Font.ITALIC);
	}

	public static void updateColors() {
		disabledTextColor = GUIUtil.getUITheme().getDisabledTextColor();
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
												  boolean sel, boolean expanded, boolean leaf, int row,
												  boolean hasFocus) {
		Component rendererComponent = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof RocketComponent component) {
			boolean isEnabled = enabledComponents.contains(component);
			rendererComponent.setEnabled(isEnabled);

			if (rendererComponent instanceof JPanel panel) {
				for (Component c : panel.getComponents()) {
					if (c instanceof JLabel label) {
						if (!isEnabled) {
							label.setForeground(disabledTextColor);
							label.setFont(italicFont);
							Icon icon = label.getIcon();
							if (icon != null) {
								label.setIcon(Icons.createDisabledIcon(icon));
							}
						} else {
							label.setFont(regularFont);
						}
					}
				}
			}
		}

		return rendererComponent;
	}
}