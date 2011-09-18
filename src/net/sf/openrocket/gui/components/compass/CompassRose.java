package net.sf.openrocket.gui.components.compass;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;

import javax.swing.JComponent;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;

/**
 * A component that draws a compass rose.  This class has no other functionality, but superclasses
 * may add functionality to it.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class CompassRose extends JComponent {
	private static final Translator trans = Application.getTranslator();
	

	private static final Color MAIN_COLOR = new Color(0.4f, 0.4f, 1.0f);
	private static final float MAIN_LENGTH = 0.95f;
	private static final float MAIN_WIDTH = 0.15f;
	
	private static final int CIRCLE_BORDER = 2;
	private static final Color CIRCLE_HIGHLIGHT = new Color(1.0f, 1.0f, 1.0f, 0.7f);
	private static final Color CIRCLE_SHADE = new Color(0.0f, 0.0f, 0.0f, 0.2f);
	
	private static final Color MARKER_COLOR = Color.BLACK;
	

	private double scaler;
	
	private double markerRadius;
	private Font markerFont;
	
	
	/**
	 * Construct a compass rose with the default settings.
	 */
	public CompassRose() {
		this(0.8, 1.1, Font.decode("Serif-PLAIN-16"));
	}
	
	
	/**
	 * Construct a compass rose with the specified settings.
	 * 
	 * @param scaler		The scaler of the rose.  The bordering circle will we this portion of the component dimensions.
	 * @param markerRadius	The radius for the marker positions (N/E/S/W), or NaN for no markers.  A value greater than one
	 * 						will position the markers outside of the bordering circle.
	 * @param markerFont	The font used for the markers.
	 */
	public CompassRose(double scaler, double markerRadius, Font markerFont) {
		this.scaler = scaler;
		this.markerRadius = markerRadius;
		this.markerFont = markerFont;
	}
	
	

	@Override
	public void paintComponent(Graphics g) {
		
		Graphics2D g2 = (Graphics2D) g;
		
		int[] x = new int[3];
		int[] y = new int[3];
		Dimension dimension = this.getSize();
		
		int width = Math.min(dimension.width, dimension.height);
		int mid = width / 2;
		width = (int) (scaler * width);
		
		int mainLength = (int) (width * MAIN_LENGTH / 2);
		int mainWidth = (int) (width * MAIN_WIDTH / 2);
		

		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2.setColor(MAIN_COLOR);
		
		// North
		x[0] = mid;
		y[0] = mid;
		x[1] = mid;
		y[1] = mid - mainLength;
		x[2] = mid - mainWidth;
		y[2] = mid - mainWidth;
		g2.fillPolygon(x, y, 3);
		
		x[2] = mid + mainWidth;
		g2.drawPolygon(x, y, 3);
		
		// East
		x[0] = mid;
		y[0] = mid;
		x[1] = mid + mainLength;
		y[1] = mid;
		x[2] = mid + mainWidth;
		y[2] = mid - mainWidth;
		g2.fillPolygon(x, y, 3);
		
		y[2] = mid + mainWidth;
		g2.drawPolygon(x, y, 3);
		
		// South
		x[0] = mid;
		y[0] = mid;
		x[1] = mid;
		y[1] = mid + mainLength;
		x[2] = mid + mainWidth;
		y[2] = mid + mainWidth;
		g2.fillPolygon(x, y, 3);
		
		x[2] = mid - mainWidth;
		g2.drawPolygon(x, y, 3);
		
		// West
		x[0] = mid;
		y[0] = mid;
		x[1] = mid - mainLength;
		y[1] = mid;
		x[2] = mid - mainWidth;
		y[2] = mid + mainWidth;
		g2.fillPolygon(x, y, 3);
		
		y[2] = mid - mainWidth;
		g2.drawPolygon(x, y, 3);
		

		// Border circle
		g2.setColor(CIRCLE_SHADE);
		g2.drawArc(mid - width / 2 + CIRCLE_BORDER, mid - width / 2 + CIRCLE_BORDER,
					width - 2 * CIRCLE_BORDER, width - 2 * CIRCLE_BORDER, 45, 180);
		g2.setColor(CIRCLE_HIGHLIGHT);
		g2.drawArc(mid - width / 2 + CIRCLE_BORDER, mid - width / 2 + CIRCLE_BORDER,
				width - 2 * CIRCLE_BORDER, width - 2 * CIRCLE_BORDER, 180 + 45, 180);
		

		// Draw direction markers
		if (!Double.isNaN(markerRadius) && markerFont != null) {
			
			int pos = (int) (width * markerRadius / 2);
			
			g2.setColor(MARKER_COLOR);
			drawMarker(g2, mid, mid - pos, trans.get("lbl.north"));
			drawMarker(g2, mid + pos, mid, trans.get("lbl.east"));
			drawMarker(g2, mid, mid + pos, trans.get("lbl.south"));
			drawMarker(g2, mid - pos, mid, trans.get("lbl.west"));
			
		}
		
	}
	
	

	private void drawMarker(Graphics2D g2, float x, float y, String str) {
		GlyphVector gv = markerFont.createGlyphVector(g2.getFontRenderContext(), str);
		Rectangle2D rect = gv.getVisualBounds();
		
		x -= rect.getWidth() / 2;
		y += rect.getHeight() / 2;
		
		g2.drawGlyphVector(gv, x, y);
		
	}
	
	



	public double getScaler() {
		return scaler;
	}
	
	
	public void setScaler(double scaler) {
		this.scaler = scaler;
		repaint();
	}
	
	
	public double getMarkerRadius() {
		return markerRadius;
	}
	
	
	public void setMarkerRadius(double markerRadius) {
		this.markerRadius = markerRadius;
		repaint();
	}
	
	
	public Font getMarkerFont() {
		return markerFont;
	}
	
	
	public void setMarkerFont(Font markerFont) {
		this.markerFont = markerFont;
		repaint();
	}
	
	@Override
	public Dimension getPreferredSize() {
		Dimension dim = super.getPreferredSize();
		int min = Math.min(dim.width, dim.height);
		dim.setSize(min, min);
		return dim;
	}
	

}
