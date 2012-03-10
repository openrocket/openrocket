package net.sf.openrocket.gui.help.tours;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URL;

import javax.imageio.ImageIO;

import net.sf.openrocket.util.BugException;

/**
 * An individual slide in a guided tour.  It contains a image (or reference to an
 * image file) plus a text description (in HTML).
 * 
 * @author Sampo Niskanen <sampo.niskanen@iki.fi>
 */
public class Slide {
	private static final String NO_IMAGE = "none";
	
	private final String imageFile;
	private SoftReference<BufferedImage> imageReference = null;
	
	private final String text;
	
	
	
	public Slide(String imageFile, String text) {
		this.imageFile = imageFile;
		this.text = text;
	}
	
	
	
	public BufferedImage getImage() {
		
		if (imageFile.equals(NO_IMAGE)) {
			return new BufferedImage(0, 0, BufferedImage.TYPE_INT_ARGB);
		}
		
		// Check the cache
		if (imageReference != null) {
			BufferedImage image = imageReference.get();
			if (image != null) {
				return image;
			}
		}
		
		// Otherwise load and cache
		BufferedImage image = loadImage();
		imageReference = new SoftReference<BufferedImage>(image);
		
		return image;
	}
	
	public String getText() {
		return text;
	}
	
	
	
	private BufferedImage loadImage() {
		BufferedImage img;
		
		try {
			URL url = ClassLoader.getSystemResource(imageFile);
			if (url != null) {
				img = ImageIO.read(url);
			} else {
				throw new BugException("Could not find image " + imageFile);
			}
		} catch (IOException e) {
			throw new BugException("Error reading image " + imageFile, e);
		}
		
		return img;
	}
}
