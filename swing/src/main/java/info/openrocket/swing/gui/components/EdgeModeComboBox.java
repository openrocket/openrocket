package info.openrocket.swing.gui.components;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BaseMultiResolutionImage;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.plaf.basic.ComboPopup;

import info.openrocket.core.appearance.Decal.EdgeMode;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Combo box for selecting a texture edge mode. Hovering an option (or the closed combo box)
 * shows a popup with an example of how that mode wraps a texture around the component.
 */
public class EdgeModeComboBox extends JComboBox<EdgeMode> {
	private static final long serialVersionUID = 1L;
	private static final Translator trans = Application.getTranslator();

	private static final Logger log = LoggerFactory.getLogger(EdgeModeComboBox.class);

	private static final String PREVIEW_PATH = "pix/edgemodes/";
	/** Width of the example images, which the description wraps to. */
	private static final int PREVIEW_WIDTH = 240;

	private static final Map<EdgeMode, ImageIcon> PREVIEW_CACHE = new EnumMap<>(EdgeMode.class);

	private final JPanel content;
	private final JLabel titleLabel;
	private final JLabel imageLabel;
	private final JTextArea descriptionArea;
	private Popup popup;
	private JList<?> popupList;

	private final MouseAdapter listHoverListener = new MouseAdapter() {
		@Override
		public void mouseMoved(MouseEvent e) {
			handleListHover(e);
		}

		@Override
		public void mouseExited(MouseEvent e) {
			hidePreview();
		}
	};

	private final MouseAdapter comboHoverListener = new MouseAdapter() {
		@Override
		public void mouseEntered(MouseEvent e) {
			if (!isPopupVisible()) {
				showSelectedPreview();
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {
			if (!isPopupVisible()) {
				hidePreview();
			}
		}
	};

	public EdgeModeComboBox(ComboBoxModel<EdgeMode> model) {
		super(model);

		content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(UIManager.getColor("ToolTip.foreground")),
				BorderFactory.createEmptyBorder(8, 8, 8, 8)));
		content.setBackground(UIManager.getColor("ToolTip.background"));

		titleLabel = new JLabel();
		titleLabel.setFont(titleLabel.getFont().deriveFont(titleLabel.getFont().getStyle() | Font.BOLD));
		content.add(titleLabel, BorderLayout.NORTH);

		imageLabel = new JLabel();
		content.add(imageLabel, BorderLayout.CENTER);

		descriptionArea = new JTextArea();
		descriptionArea.setEditable(false);
		descriptionArea.setWrapStyleWord(true);
		descriptionArea.setLineWrap(true);
		descriptionArea.setOpaque(false);
		descriptionArea.setFocusable(false);
		descriptionArea.setBorder(BorderFactory.createEmptyBorder());
		descriptionArea.setFont(titleLabel.getFont().deriveFont(Font.PLAIN));
		content.add(descriptionArea, BorderLayout.SOUTH);

		addMouseListener(comboHoverListener);
		addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				hidePreview();
				installListListener();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				hidePreview();
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				hidePreview();
			}
		});
	}

	@Override
	public void removeNotify() {
		hidePreview();
		if (popupList != null) {
			popupList.removeMouseMotionListener(listHoverListener);
			popupList.removeMouseListener(listHoverListener);
			popupList = null;
		}
		super.removeNotify();
	}

	private void installListListener() {
		Object popupComponent = getUI().getAccessibleChild(this, 0);
		if (!(popupComponent instanceof ComboPopup comboPopup)) {
			return;
		}
		JList<?> list = comboPopup.getList();
		if (list == null || list == popupList) {
			return;
		}
		popupList = list;
		popupList.addMouseMotionListener(listHoverListener);
		popupList.addMouseListener(listHoverListener);
	}

	private void handleListHover(MouseEvent e) {
		JList<?> list = (JList<?>) e.getSource();
		int index = list.locationToIndex(e.getPoint());
		Rectangle bounds = index < 0 ? null : list.getCellBounds(index, index);
		if (bounds == null || !bounds.contains(e.getPoint())
				|| !(list.getModel().getElementAt(index) instanceof EdgeMode mode)) {
			hidePreview();
			return;
		}
		Point location = new Point(bounds.x + bounds.width + 8, bounds.y);
		SwingUtilities.convertPointToScreen(location, list);
		showPreview(list, location, mode);
	}

	private void showSelectedPreview() {
		if (!isEnabled() || !(getSelectedItem() instanceof EdgeMode mode)) {
			return;
		}
		Point location = new Point(getWidth() + 8, 0);
		SwingUtilities.convertPointToScreen(location, this);
		showPreview(this, location, mode);
	}

	private void showPreview(JComponent invoker, Point screenLocation, EdgeMode mode) {
		titleLabel.setText(mode.toString());
		imageLabel.setIcon(getPreviewIcon(mode));
		descriptionArea.setText(trans.get(getDescriptionKey(mode)));
		// Fix the width so the description wraps to the preview width instead of one long line
		descriptionArea.setSize(PREVIEW_WIDTH, Short.MAX_VALUE);
		descriptionArea.setPreferredSize(null);
		descriptionArea.setPreferredSize(descriptionArea.getPreferredSize());
		hidePreview();
		popup = PopupFactory.getSharedInstance().getPopup(invoker, content, screenLocation.x, screenLocation.y);
		popup.show();
	}

	private void hidePreview() {
		if (popup != null) {
			popup.hide();
			popup = null;
		}
	}

	private static String getDescriptionKey(EdgeMode mode) {
		return switch (mode) {
			case REPEAT -> "TextureWrap.Repeat.desc";
			case MIRROR -> "TextureWrap.Mirror.desc";
			case CLAMP -> "TextureWrap.Clamp.desc";
			case STICKER -> "TextureWrap.Sticker.desc";
		};
	}

	/**
	 * Loads the example image for a mode. The images are generated by
	 * artwork/texture-edge-modes/GenerateEdgeModePreviews.java.
	 */
	private static synchronized ImageIcon getPreviewIcon(EdgeMode mode) {
		return PREVIEW_CACHE.computeIfAbsent(mode, m -> {
			String base = PREVIEW_PATH + m.name().toLowerCase(Locale.ROOT);
			try {
				BufferedImage image = readImage(base + ".png");
				BufferedImage image2x = readImage(base + "@2x.png");
				return new ImageIcon(new BaseMultiResolutionImage(image, image2x));
			} catch (IOException e) {
				log.warn("Could not load texture edge mode preview " + base, e);
				return null;
			}
		});
	}

	private static BufferedImage readImage(String path) throws IOException {
		URL url = ClassLoader.getSystemResource(path);
		if (url == null) {
			throw new IOException("Resource not found: " + path);
		}
		return ImageIO.read(url);
	}
}
