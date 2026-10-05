package info.openrocket.swing.gui.simulation.currentconditions;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.BorderFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/** Shared hover callout for weather details and cached-data help. */
public final class WeatherHelpButton extends JButton {
	private final Supplier<? extends JComponent> content;
	private final AWTEventListener dismiss = this::dismissOutside;
	private JWindow popup;
	private Point predictionOrigin;
	private boolean above;
	private final WindowAdapter ownerFocus = new WindowAdapter() {
		@Override
		public void windowDeactivated(WindowEvent event) {
			closePopup();
		}
	};

	public WeatherHelpButton(String accessibleName, Supplier<? extends JComponent> content) {
		this.content = content;
		getAccessibleContext().setAccessibleName(accessibleName);
		setMargin(new Insets(0, 0, 0, 0));
		setFocusable(false);
		setFocusPainted(false);
		putClientProperty("JButton.buttonType", "roundRect");
		putClientProperty("FlatLaf.style", "arc: 999");
		addActionListener(event -> showPopup());
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent event) {
				showPopup();
			}
		});
	}

	@Override
	protected void paintComponent(Graphics graphics) {
		super.paintComponent(graphics);
		Graphics copy = graphics.create();
		FontMetrics metrics = copy.getFontMetrics(getFont());
		copy.setColor(getForeground());
		copy.setFont(getFont());
		copy.drawString("?", (getWidth() - metrics.stringWidth("?")) / 2,
				(getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
		copy.dispose();
	}

	private void showPopup() {
		if (!isShowing()) return;
		var owner = SwingUtilities.getWindowAncestor(this);
		if (owner == null || !owner.isActive()) return;
		if (popup != null) {
			popup.toFront();
			return;
		}
		popup = new JWindow(owner);
		popup.setFocusableWindowState(false);
		popup.setAutoRequestFocus(false);
		popup.getOwner().addWindowListener(ownerFocus);
		Color border = UIManager.getColor("Component.borderColor");
		Color outline = border == null ? Color.GRAY : border;
		Color fill = UIManager.getColor("Panel.background");
		JPanel bubble = new JPanel(new BorderLayout()) {
			@Override
			protected void paintComponent(Graphics graphics) {
				Graphics2D drawing = (Graphics2D) graphics.create();
				drawing.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				if (above) {
					drawing.translate(0, getHeight());
					drawing.scale(1, -1);
				}
				double bodyTop = 9;
				Area callout = new Area(new RoundRectangle2D.Double(0.5, bodyTop, getWidth() - 1.0,
						getHeight() - bodyTop - 0.5, 14, 14));
				Path2D tail = new Path2D.Double();
				tail.moveTo(1.5, bodyTop + 2);
				tail.quadTo(7, 7, 11, 0.8);
				tail.quadTo(15, 7, 22, bodyTop + 2);
				tail.closePath();
				callout.add(new Area(tail));
				drawing.setColor(fill);
				drawing.fill(callout);
				drawing.setColor(outline);
				drawing.draw(callout);
				drawing.dispose();
			}
		};
		bubble.setOpaque(false);
		bubble.setBorder(BorderFactory.createEmptyBorder(10, 1, 1, 1));
		bubble.add(content.get(), BorderLayout.CENTER);
		popup.setBackground(new Color(0, 0, 0, 0));
		popup.setContentPane(bubble);
		popup.pack();
		Point anchor = getLocationOnScreen();
		Rectangle screen = getGraphicsConfiguration().getBounds();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
		above = anchor.y + getHeight() + popup.getHeight() > screen.y + screen.height - insets.bottom;
		if (above) bubble.setBorder(BorderFactory.createEmptyBorder(1, 1, 10, 1));
		predictionOrigin = new Point(anchor.x + getWidth() / 2, anchor.y + (above ? getHeight() : 0));
		popup.setLocation(anchor.x + getWidth() / 2 - 11,
				above ? anchor.y - popup.getHeight() + 1 : anchor.y + getHeight() - 1);
		Toolkit.getDefaultToolkit().addAWTEventListener(dismiss,
				AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
		popup.setVisible(true);
	}

	private void dismissOutside(AWTEvent event) {
		if (popup == null || !(event instanceof MouseEvent mouse)
				|| !(mouse.getSource() instanceof Component source)) return;
		if (source == this || SwingUtilities.isDescendingFrom(source, this)
				|| source == popup || SwingUtilities.isDescendingFrom(source, popup)) return;
		Rectangle bounds = popup.getBounds();
		int edge = above ? bounds.y : bounds.y + bounds.height;
		Polygon cone = new Polygon(new int[] { predictionOrigin.x, bounds.x, bounds.x + bounds.width },
				new int[] { predictionOrigin.y, edge, edge }, 3);
		if (mouse.getID() != MouseEvent.MOUSE_PRESSED && cone.contains(mouse.getLocationOnScreen())) return;
		closePopup();
	}

	private void closePopup() {
		if (popup == null) return;
		Toolkit.getDefaultToolkit().removeAWTEventListener(dismiss);
		popup.getOwner().removeWindowListener(ownerFocus);
		popup.dispose();
		popup = null;
	}

	@Override
	public void removeNotify() {
		closePopup();
		super.removeNotify();
	}
}
