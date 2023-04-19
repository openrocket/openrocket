package net.sf.openrocket.gui.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import net.sf.openrocket.util.MathUtil;

/**
 * Draws a BufferedImage centered and scaled to fit to the component.
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class ImageDisplayComponent extends JPanel {
	
	private BufferedImage image;
	
	public ImageDisplayComponent() {
		this(null);
	}
	
	public ImageDisplayComponent(BufferedImage image) {
		this.image = image;
	}
	
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		if (image == null) {
			return;
		}
		
		final int width = Math.max(this.getWidth(), 1);
		final int height = Math.max(this.getHeight(), 1);
		
		final int origWidth = Math.max(image.getWidth(), 1);
		final int origHeight = Math.max(image.getHeight(), 1);
		

		// Determine scaling factor
		double scaleX = ((double) width) / origWidth;
		double scaleY = ((double) height) / origHeight;
		
		double scale = MathUtil.min(scaleX, scaleY);
		
		if (scale >= 1) {
			scale = 1.0;
		}
		

		// Center in the middle of the component
		int finalWidth = (int) Math.round(origWidth * scale);
		int finalHeight = (int) Math.round(origHeight * scale);
		
		int posX = (width - finalWidth) / 2;
		int posY = (height - finalHeight) / 2;
		

		// Draw the image
		int dx1 = posX;
		int dy1 = posY;
		int dx2 = posX + finalWidth;
		int dy2 = posY + finalHeight;
		int sx1 = 0;
		int sy1 = 0;
		int sx2 = origWidth;
		int sy2 = origHeight;
		
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
		
	}
	
	
	public BufferedImage getImage() {
		return image;
	}
	
	
	public void setImage(BufferedImage image) {
		this.image = image;
		this.repaint();
	}
	
	
	public static void main(String[] args) throws Exception {
		final BufferedImage image = ImageIO.read(new File("test.png"));
		
		SwingUtilities.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				
				JFrame frame = new JFrame();
				
				JPanel panel = new JPanel(new MigLayout("fill"));
				panel.setBackground(Color.red);
				frame.add(panel);
				
				ImageDisplayComponent c = new ImageDisplayComponent(image);
				panel.add(c, "grow");
				
				frame.setSize(500, 500);
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.setVisible(true);
				
			}
		});
	}
	
}
