package net.sf.openrocket.gui.main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;


public class ComponentIcons {
	
	private static final String ICON_DIRECTORY = "pix/componenticons/";
	private static final String SMALL_SUFFIX = "-small.png";
	private static final String LARGE_SUFFIX = "-large.png";
	
	private static final HashMap<Class<?>,ImageIcon> SMALL_ICONS = 
		new HashMap<Class<?>,ImageIcon>();
	private static final HashMap<Class<?>,ImageIcon> LARGE_ICONS = 
		new HashMap<Class<?>,ImageIcon>();
	private static final HashMap<Class<?>,ImageIcon> DISABLED_ICONS = 
		new HashMap<Class<?>,ImageIcon>();

	static {
		load("nosecone", "Nose cone", NoseCone.class);
		load("bodytube", "Body tube", BodyTube.class);
		load("transition", "Transition", Transition.class);
		load("trapezoidfin", "Trapezoidal fin set", TrapezoidFinSet.class);
		load("ellipticalfin", "Elliptical fin set", EllipticalFinSet.class);
		load("freeformfin", "Freeform fin set", FreeformFinSet.class);
		load("launchlug", "Launch lug", LaunchLug.class);
		load("innertube", "Inner tube", InnerTube.class);
		load("tubecoupler", "Tube coupler", TubeCoupler.class);
		load("centeringring", "Centering ring", CenteringRing.class);
		load("bulkhead", "Bulk head", Bulkhead.class);
		load("engineblock", "Engine block", EngineBlock.class);
		load("parachute", "Parachute", Parachute.class);
		load("streamer", "Streamer", Streamer.class);
		load("shockcord", "Shock cord", ShockCord.class);
		load("mass", "Mass component", MassComponent.class);
	}
	
	private static void load(String filename, String name, Class<?> componentClass) {
		ImageIcon icon = loadSmall(ICON_DIRECTORY + filename + SMALL_SUFFIX, name);
		SMALL_ICONS.put(componentClass, icon);
		
		ImageIcon[] icons = loadLarge(ICON_DIRECTORY + filename + LARGE_SUFFIX, name);
		LARGE_ICONS.put(componentClass, icons[0]);
		DISABLED_ICONS.put(componentClass, icons[1]);
	}
	
	
	
	public static Icon getSmallIcon(Class<?> c) {
		return SMALL_ICONS.get(c);
	}
	public static Icon getLargeIcon(Class<?> c) {
		return LARGE_ICONS.get(c);
	}
	public static Icon getLargeDisabledIcon(Class<?> c) {
		return DISABLED_ICONS.get(c);
	}
	
	
	
	
	private static ImageIcon loadSmall(String file, String desc) {
		URL url = ClassLoader.getSystemResource(file);
		if (url==null) {
	        ExceptionHandler.handleErrorCondition("ERROR:  Couldn't find file: " + file);
			return null;
		}
		return new ImageIcon(url, desc);
	}
	
	
	private static ImageIcon[] loadLarge(String file, String desc) {
		ImageIcon[] icons = new ImageIcon[2];
		
		URL url = ClassLoader.getSystemResource(file);
	    if (url != null) {
	    	BufferedImage bi,bi2;
	    	try {
				bi = ImageIO.read(url);
				bi2 = ImageIO.read(url);   //  How the fsck can one duplicate a BufferedImage???
			} catch (IOException e) {
				ExceptionHandler.handleErrorCondition("ERROR:  Couldn't read file: "+file, e);
		        return new ImageIcon[]{null,null};
			}
			
			icons[0] = new ImageIcon(bi,desc);
			
			// Create disabled icon
			if (false) {   // Fade using alpha 
				
				int rgb[] = bi2.getRGB(0,0,bi2.getWidth(),bi2.getHeight(),null,0,bi2.getWidth());
				for (int i=0; i<rgb.length; i++) {
					final int alpha = (rgb[i]>>24)&0xFF;
					rgb[i] = (rgb[i]&0xFFFFFF) | (alpha/3)<<24;
					
					//rgb[i] = (rgb[i]&0xFFFFFF) | ((rgb[i]>>1)&0x3F000000);
				}
				bi2.setRGB(0, 0, bi2.getWidth(), bi2.getHeight(), rgb, 0, bi2.getWidth());

			} else {   // Raster alpha

				for (int x=0; x < bi.getWidth(); x++) {
					for (int y=0; y < bi.getHeight(); y++) {
						if ((x+y)%2 == 0) {
							bi2.setRGB(x, y, 0);
						}
					}
				}
				
			}
			
			icons[1] = new ImageIcon(bi2,desc + " (disabled)");
	    	
	        return icons;
	    } else {
	    	ExceptionHandler.handleErrorCondition("ERROR:  Couldn't find file: " + file);
	        return new ImageIcon[]{null,null};
	    }
	}
}
