package info.openrocket.swing.gui.figureelements;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

/**
 * A FigureElement that displays a snap mode instruction message.
 * Shows "Select the edge or point to snap caliper %d to" when in snap mode.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class SnapModeInfo implements FigureElement {

	private static final Translator trans = Application.getTranslator();
	private static final int MARGIN = 8;
	private static final Font FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
	private static final Font HINT_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 11);

	private int caliperNumber;
	private static Color snapHighlightColor;

	static {
		initColors();
	}

	public SnapModeInfo(int caliperNumber) {
		this.caliperNumber = caliperNumber;
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(SnapModeInfo::updateColors);
	}

	public static void updateColors() {
		snapHighlightColor = GUIUtil.getUITheme().getCaliperSnapHighlightColor();
	}

	@Override
	public void paint(Graphics2D g2, double scale) {
		paint(g2, scale, null);
	}

	@Override
	public void paint(Graphics2D g2, double scale, Rectangle visible) {
		if (visible == null) {
			return;
		}

		// Create the main message text
		String mainMessage = String.format(trans.get("CaliperManager.snapModeMessage"), caliperNumber);
		GlyphVector mainText = FONT.createGlyphVector(g2.getFontRenderContext(), mainMessage);

		// Create the escape hint text
		String hintMessage = trans.get("CaliperManager.snapModeEscapeHint");
		GlyphVector hintText = HINT_FONT.createGlyphVector(g2.getFontRenderContext(), hintMessage);

		// Calculate bounds for the text
		Rectangle2D mainBounds = mainText.getVisualBounds();
		Rectangle2D hintBounds = hintText.getVisualBounds();

		// Draw the main message at top center of visible area in snap highlight color
		float mainX = (float)(visible.x + (visible.width - mainBounds.getWidth()) / 2);
		float mainY = visible.y + MARGIN + (float)mainBounds.getHeight();
		g2.setColor(snapHighlightColor);
		g2.drawGlyphVector(mainText, mainX, mainY);

		// Draw the escape hint below the main message in a dimmer color
		float hintX = (float)(visible.x + (visible.width - hintBounds.getWidth()) / 2);
		float hintY = mainY + (float)hintBounds.getHeight() + 2;
		g2.setColor(new Color(snapHighlightColor.getRed(), snapHighlightColor.getGreen(),
				snapHighlightColor.getBlue(), (int)(snapHighlightColor.getAlpha() * 0.65)));
		g2.drawGlyphVector(hintText, hintX, hintY);
	}
}

