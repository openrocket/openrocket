package net.sf.openrocket.file.rocksim.importt;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import net.sf.openrocket.aerodynamics.WarningSet;
import net.sf.openrocket.appearance.AppearanceBuilder;
import net.sf.openrocket.appearance.Decal.EdgeMode;
import net.sf.openrocket.document.Attachment;
import net.sf.openrocket.file.DocumentLoadingContext;
import net.sf.openrocket.file.rocksim.RocksimCommonConstants;
import net.sf.openrocket.util.Color;

public class RockSimAppearanceBuilder extends AppearanceBuilder {
	
	boolean preventSeam = false;
	boolean repeat = false;
	
	private final DocumentLoadingContext context;
	
	public RockSimAppearanceBuilder(DocumentLoadingContext context) {
		this.context = context;
	}
	
	public void processElement(String element, String content, WarningSet warnings) {
		try {
			if (RocksimCommonConstants.TEXTURE.equals(element)) {
				parseTexture(content);
			} else if ("Ambient".equals(element)) {
				//ignored
			} else if ("Diffuse".equals(element)) {
				//ignored
			} else if ("Specular".equals(element)) {
				//ignored
			} else if ("AbientColor".equals(element)) {
				setPaint(parseColor(content));
			} else if ("DiffuseColor".equals(element)) {
				//ignored
			} else if ("SpecularColor".equals(element)) {
				//Ignored
			} else if ("UseSingleColor".equals(element) || "SimpleColorModel".equals(element)) {
				//Ignored
			}
		} catch (Exception e) {
			warnings.add("Could not convert " + element + " value of " + content + ": " + e.getMessage());
		}
	}
	
	private void parseTexture(String s) throws FileNotFoundException, MalformedURLException {
		s = s.trim();
		if (s.isEmpty()) {
			return;
		}
		final String[] parts = s.split("\\|");
		
		boolean interpolate = false;
		boolean flipr = false;
		boolean flips = false;
		boolean flipt = false;
		
		
		for (String part : parts) {
			// Sometimes it is name=(value) and sometimes name(value)
			final String name = part.substring(0, part.indexOf("(")).replace("=", "");
			
			final String value = part.substring(part.indexOf("(") + 1, part.length() - 1);
			
			if ("file".equals(name)) {
				if (value.length() > 0) {
					final File f = new File(value);
					if (f.exists()) {
						Attachment a = context.getAttachmentFactory().getAttachment(name);
						setImage(context.getOpenRocketDocument().getDecalImage(a));
					}
					// else {
					// If we can't find the file on the filesystem, we just ignore the decal.
					//}
				}
			} else if ("repeat".equals(name)) {
				repeat = "1".equals(value);
			} else if ("interpolate".equals(name)) {
				interpolate = "1".equals(value);
			} else if ("flipr".equals(name)) {
				flipr = "1".equals(value);
			} else if ("flips".equals(name)) {
				flips = "1".equals(value);
			} else if ("flipt".equals(name)) {
				flipt = "1".equals(value);
			} else if ("preventseam".equals(name)) {
				preventSeam = "1".equals(value);
			} else if ("position".equals(name)) {
				String[] c = value.split(",");
				setOffset(Double.parseDouble(c[0]), Double.parseDouble(c[1]));
			} else if ("origin".equals(name)) {
				String[] c = value.split(",");
				setCenter(Double.parseDouble(c[0]), Double.parseDouble(c[1]));
			} else if ("scale".equals(name)) {
				String[] c = value.split(",");
				setScaleUV(Double.parseDouble(c[0]), Double.parseDouble(c[1]));
			}
		}
		
		if (repeat) {
			setEdgeMode(EdgeMode.REPEAT);
		}
		
		if (preventSeam) {
			setEdgeMode(EdgeMode.MIRROR);
		}
		
		if (!flips) {
			setScaleUV(getScaleU(), getScaleV() * -1);
			setOffset(getOffsetU(), -1 - getOffsetV());
		}
		if (!flipr) {
			setScaleUV(getScaleU() * -1, getScaleV());
			setOffset(-1 - getOffsetU(), getOffsetV());
		}
		
		//TODO Make use of these values
		//System.out.println("Interpolate: " + interpolate);
		//System.out.println("FlipT: " + flipt);
		;
		
	}
	
	static Color weight(Color c, double w) {
		return new Color((int) (c.getRed() * w), (int) (c.getGreen() * w), (int) (c.getBlue() * w), c.getAlpha());
	}
	
	static Color parseColor(String s) {
		// blue and white came from a real file.
		if ("blue".equals(s)) {
			return new Color(0, 0, 255);
		}
		if ("white".equals(s)) {
			return new Color(255, 255, 255);
		}
		// I guessed these are valid color names in Rksim.
		if ("red".equals(s)) {
			return new Color(255, 0, 0);
		}
		if ("green".equals(s)) {
			return new Color(0, 255, 0);
		}
		if ("black".equals(s)) {
			return new Color(0, 0, 0);
		}
		s = s.replace("rgb(", "");
		s = s.replace(")", "");
		String ss[] = s.split(",");
		return new Color(Integer.parseInt(ss[0]), Integer.parseInt(ss[1]), Integer.parseInt(ss[2]));
	}
	
	public boolean isPreventSeam() {
		return preventSeam;
	}
	
	public void setPreventSeam(boolean preventSeam) {
		this.preventSeam = preventSeam;
	}
	
	public boolean isRepeat() {
		return repeat;
	}
	
	public void setRepeat(boolean repeat) {
		this.repeat = repeat;
	}
	
}
