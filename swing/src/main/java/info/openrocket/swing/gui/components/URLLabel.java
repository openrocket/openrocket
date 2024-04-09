package info.openrocket.swing.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;
import info.openrocket.swing.gui.util.URLUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A label of a URL that is clickable.  Clicking the URL will launch the URL in
 * the default browser if the Desktop class is supported.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class URLLabel extends SelectableLabel {
	private static final Logger log = LoggerFactory.getLogger(URLLabel.class);

	private static Color URLColor;

	static {
		initColors();
	}

	/**
	 * Create a label showing the url it will direct to.
	 * 
	 * @param url	the URL.
	 */
	public URLLabel(String url) {
		this(url, url);
	}
	
	/**
	 * Create a label with separate URL and label.
	 * 
	 * @param url	the URL clicking will open.
	 * @param label	the label.
	 */
	public URLLabel(final String url, String label) {
		super();
		
		setText(label);

		if (Desktop.isDesktopSupported()) {
			
			// Blue, underlined font
			Map<TextAttribute, Object> map = new HashMap<TextAttribute, Object>();
			map.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
			this.setFont(this.getFont().deriveFont(map));
			this.setForeground(URLColor);
			
			this.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			
			
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						URLUtil.openWebpage(url);
					} catch (Exception e1) {
						log.error("Unable to launch browser: " + e1.getMessage(), e1);
					}
				}
			});

		}
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(URLLabel::updateColors);
	}

	private static void updateColors() {
		URLColor = GUIUtil.getUITheme().getURLColor();
	}
}
