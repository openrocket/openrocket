package info.openrocket.swing.gui.scalefigure;

import javax.swing.JScrollPane;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Borderless horizontal scroll pane for a fixed-height toolbar or footer row.
 *
 * <p>The preferred height grows when the horizontal scrollbar becomes visible,
 * leaving the full viewport height available to the row's controls.</p>
 */
class SingleRowScrollPane extends JScrollPane {
	private final Runnable scrollbarVisibilityChanged;

	SingleRowScrollPane(Component view, Runnable scrollbarVisibilityChanged) {
		super(view, VERTICAL_SCROLLBAR_NEVER, HORIZONTAL_SCROLLBAR_AS_NEEDED);
		this.scrollbarVisibilityChanged = scrollbarVisibilityChanged;
		applyBorderlessStyling();
		getHorizontalScrollBar().addComponentListener(new ComponentAdapter() {
			@Override
			public void componentShown(ComponentEvent event) {
				onScrollbarVisibilityChanged();
			}

			@Override
			public void componentHidden(ComponentEvent event) {
				onScrollbarVisibilityChanged();
			}
		});
	}

	@Override
	public void updateUI() {
		super.updateUI();
		applyBorderlessStyling();
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension size = super.getPreferredSize();
		if (getHorizontalScrollBar().isVisible()) {
			size.height += getHorizontalScrollBar().getPreferredSize().height;
		}
		return size;
	}

	private void applyBorderlessStyling() {
		setBorder(null);
		setViewportBorder(null);
	}

	private void onScrollbarVisibilityChanged() {
		revalidate();
		if (scrollbarVisibilityChanged != null) {
			scrollbarVisibilityChanged.run();
		}
	}
}
