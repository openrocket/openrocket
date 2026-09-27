package info.openrocket.swing.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatClientProperties;

/**
 * A disclosure panel with a full-width header and content that can be removed
 * from the layout when collapsed.
 *
 * <p>The header uses the active look and feel's tree disclosure icons, so the
 * arrow shape, scale, color, hover state, and focus indication remain
 * consistent with the rest of the application.</p>
 */
public class CollapsiblePanel extends JPanel {
	private static final long serialVersionUID = -4943638486882327964L;
	private static final float HEADER_FONT_SIZE_REDUCTION = 1.0f;

	private final JToggleButton headerButton;
	private final JPanel contentContainer;

	/**
	 * Creates a collapsible section.
	 *
	 * @param title header text
	 * @param content component displayed below the header
	 * @param expanded whether the content is initially expanded
	 */
	public CollapsiblePanel(String title, JComponent content, boolean expanded) {
		super(new BorderLayout());

		headerButton = new JToggleButton(title, expanded);
		headerButton.setHorizontalAlignment(SwingConstants.LEADING);
		headerButton.setIconTextGap(8);
		headerButton.setRolloverEnabled(true);
		headerButton.getAccessibleContext().setAccessibleName(title);
		headerButton.putClientProperty(FlatClientProperties.BUTTON_TYPE,
				FlatClientProperties.BUTTON_TYPE_TOOLBAR_BUTTON);
		headerButton.putClientProperty(FlatClientProperties.STYLE, "arc: 0; margin: 6, 8, 6, 8");
		headerButton.addActionListener(event -> setExpanded(headerButton.isSelected()));
		add(headerButton, BorderLayout.NORTH);

		contentContainer = new JPanel(new BorderLayout());
		contentContainer.add(content, BorderLayout.CENTER);
		setExpanded(expanded);
		updateAppearance();
	}

	/**
	 * Changes whether the content participates in this panel's layout.
	 *
	 * @param expanded {@code true} to show the content
	 */
	public void setExpanded(boolean expanded) {
		headerButton.setSelected(expanded);
		if (expanded && contentContainer.getParent() == null) {
			add(contentContainer, BorderLayout.CENTER);
		} else if (!expanded && contentContainer.getParent() != null) {
			remove(contentContainer);
		}
		invalidateLayoutHierarchy();
		revalidate();
		repaint();
	}

	/**
	 * Clears cached preferred sizes above this section. This is important when
	 * the panel lives in a scroll pane, whose scrollbar range depends on the
	 * preferred width of all expanded content.
	 */
	private void invalidateLayoutHierarchy() {
		Container component = this;
		while (component != null) {
			component.invalidate();
			component = component.getParent();
		}
	}

	/**
	 * Returns whether the content is currently part of the layout.
	 *
	 * @return {@code true} when expanded
	 */
	public boolean isExpanded() {
		return contentContainer.getParent() == this;
	}

	/**
	 * Returns the disclosure header for accessibility and UI integration.
	 *
	 * @return the full-width toggle button
	 */
	public JToggleButton getHeaderButton() {
		return headerButton;
	}

	@Override
	public void updateUI() {
		super.updateUI();
		updateAppearance();
	}

	private void updateAppearance() {
		if (headerButton == null || contentContainer == null) {
			return;
		}

		Icon collapsedIcon = UIManager.getIcon("Tree.collapsedIcon");
		Icon expandedIcon = UIManager.getIcon("Tree.expandedIcon");
		headerButton.setIcon(collapsedIcon);
		headerButton.setSelectedIcon(expandedIcon);
		Font labelFont = UIManager.getFont("Label.font");
		if (labelFont == null) {
			labelFont = headerButton.getFont();
		}
		float headerFontSize = Math.max(9.0f, labelFont.getSize2D() - HEADER_FONT_SIZE_REDUCTION);
		headerButton.setFont(labelFont.deriveFont(Font.BOLD, headerFontSize));

		Color borderColor = UIManager.getColor("Component.borderColor");
		if (borderColor == null) {
			borderColor = UIManager.getColor("Separator.foreground");
		}
		if (borderColor == null) {
			borderColor = Color.GRAY;
		}
		setBorder(BorderFactory.createLineBorder(borderColor));
		contentContainer.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createMatteBorder(1, 0, 0, 0, borderColor),
				BorderFactory.createEmptyBorder(4, 8, 6, 8)));
	}
}
