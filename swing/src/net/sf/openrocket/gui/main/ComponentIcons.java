package net.sf.openrocket.gui.main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.rocketcomponent.BodyTube;
import net.sf.openrocket.rocketcomponent.Bulkhead;
import net.sf.openrocket.rocketcomponent.CenteringRing;
import net.sf.openrocket.rocketcomponent.EllipticalFinSet;
import net.sf.openrocket.rocketcomponent.EngineBlock;
import net.sf.openrocket.rocketcomponent.FreeformFinSet;
import net.sf.openrocket.rocketcomponent.InnerTube;
import net.sf.openrocket.rocketcomponent.LaunchLug;
import net.sf.openrocket.rocketcomponent.MassComponent;
import net.sf.openrocket.rocketcomponent.MassComponent.MassComponentType;
import net.sf.openrocket.rocketcomponent.NoseCone;
import net.sf.openrocket.rocketcomponent.Parachute;
import net.sf.openrocket.rocketcomponent.ShockCord;
import net.sf.openrocket.rocketcomponent.Streamer;
import net.sf.openrocket.rocketcomponent.Transition;
import net.sf.openrocket.rocketcomponent.TrapezoidFinSet;
import net.sf.openrocket.rocketcomponent.TubeCoupler;
import net.sf.openrocket.rocketcomponent.TubeFinSet;
import net.sf.openrocket.startup.Application;

public class ComponentIcons {
	private static final Translator trans = Application.getTranslator();

	private static final String ICON_DIRECTORY = "pix/componenticons/";
	private static final String SMALL_SUFFIX = "-small.png";
	private static final String LARGE_SUFFIX = "-large.png";

	private static final HashMap<Class<?>, ImageIcon> SMALL_ICONS = new HashMap<Class<?>, ImageIcon>();
	private static final HashMap<Class<?>, ImageIcon> LARGE_ICONS = new HashMap<Class<?>, ImageIcon>();
	private static final HashMap<Class<?>, ImageIcon> DISABLED_ICONS = new HashMap<Class<?>, ImageIcon>();
	private static final HashMap<MassComponentType, ImageIcon> MASS_COMPONENT_SMALL_ICONS = new HashMap<MassComponentType, ImageIcon>();
	static {
		// // Nose cone
		load("nosecone", trans.get("ComponentIcons.Nosecone"), NoseCone.class);
		// // Body tube
		load("bodytube", trans.get("ComponentIcons.Bodytube"), BodyTube.class);
		//// Transition
		load("transition", trans.get("ComponentIcons.Transition"), Transition.class);
		//// Trapezoidal fin set
		load("trapezoidfin", trans.get("ComponentIcons.Trapezoidalfinset"), TrapezoidFinSet.class);
		//// Elliptical fin set
		load("ellipticalfin", trans.get("ComponentIcons.Ellipticalfinset"), EllipticalFinSet.class);
		//// Freeform fin set
		load("freeformfin", trans.get("ComponentIcons.Freeformfinset"), FreeformFinSet.class);
		//// Tube fin set
		load("tubefin", trans.get("ComponentIcons.Tubefinset"), TubeFinSet.class);
		//// Launch lug
		load("launchlug", trans.get("ComponentIcons.Launchlug"), LaunchLug.class);
		//// Inner tube
		load("innertube", trans.get("ComponentIcons.Innertube"), InnerTube.class);
		//// Tube coupler
		load("tubecoupler", trans.get("ComponentIcons.Tubecoupler"), TubeCoupler.class);
		//// Centering ring
		load("centeringring", trans.get("ComponentIcons.Centeringring"), CenteringRing.class);
		//// Bulk head
		load("bulkhead", trans.get("ComponentIcons.Bulkhead"), Bulkhead.class);
		// // Engine block
		load("engineblock", trans.get("ComponentIcons.Engineblock"),
				EngineBlock.class);
		// // Parachute
		load("parachute", trans.get("ComponentIcons.Parachute"),
				Parachute.class);
		// // Streamer
		load("streamer", trans.get("ComponentIcons.Streamer"), Streamer.class);
		// // Shock cord
		load("shockcord", trans.get("ComponentIcons.Shockcord"),
				ShockCord.class);
		load("mass", trans.get("ComponentIcons.Masscomponent"),
				MassComponent.class);
		// // Mass components
		loadMassTypeIcon("mass", trans.get("ComponentIcons.Masscomponent"),
				MassComponentType.MASSCOMPONENT);
		loadMassTypeIcon("altimeter", trans.get("ComponentIcons.Altimeter"),
				MassComponentType.ALTIMETER);
		loadMassTypeIcon("battery", trans.get("ComponentIcons.Battery"),
				MassComponentType.BATTERY);
		loadMassTypeIcon("deployment-charge",
				trans.get("ComponentIcons.Deploymentcharge"),
				MassComponentType.DEPLOYMENTCHARGE);
		loadMassTypeIcon("payload", trans.get("ComponentIcons.Payload"),
				MassComponentType.PAYLOAD);
		loadMassTypeIcon("flight-comp",
				trans.get("ComponentIcons.Flightcomputer"),
				MassComponentType.FLIGHTCOMPUTER);
		loadMassTypeIcon("recovery-hardware",
				trans.get("ComponentIcons.Recoveryhardware"),
				MassComponentType.RECOVERYHARDWARE);
		loadMassTypeIcon("tracker", trans.get("ComponentIcons.Tracker"),
				MassComponentType.TRACKER);
	}

	private static void load(String filename, String name,
			Class<?> componentClass) {
		ImageIcon icon = loadSmall(ICON_DIRECTORY + filename + SMALL_SUFFIX,
				name);
		SMALL_ICONS.put(componentClass, icon);

		ImageIcon[] icons = loadLarge(ICON_DIRECTORY + filename + LARGE_SUFFIX,
				name);
		LARGE_ICONS.put(componentClass, icons[0]);
		DISABLED_ICONS.put(componentClass, icons[1]);
	}

	private static void loadMassTypeIcon(String filename, String name,
			MassComponentType t) {
		ImageIcon icon = loadSmall(ICON_DIRECTORY + filename + SMALL_SUFFIX,
				name);
		MASS_COMPONENT_SMALL_ICONS.put(t, icon);
	}

	/**
	 * Return the small icon for a component type.
	 * 
	 * @param c
	 *            the component class.
	 * @return the icon, or <code>null</code> if none available.
	 */
	public static Icon getSmallIcon(Class<?> c) {
		if (c.isAssignableFrom(MassComponent.class)) {
		}
		return SMALL_ICONS.get(c);
	}

	public static Icon getSmallMassTypeIcon(MassComponentType t) {
		return MASS_COMPONENT_SMALL_ICONS.get(t);
	}

	/**
	 * Return the large icon for a component type.
	 * 
	 * @param c
	 *            the component class.
	 * @return the icon, or <code>null</code> if none available.
	 */
	public static Icon getLargeIcon(Class<?> c) {
		return LARGE_ICONS.get(c);
	}

	/**
	 * Return the large disabled icon for a component type.
	 * 
	 * @param c
	 *            the component class.
	 * @return the icon, or <code>null</code> if none available.
	 */
	public static Icon getLargeDisabledIcon(Class<?> c) {
		return DISABLED_ICONS.get(c);
	}

	private static ImageIcon loadSmall(String file, String desc) {
		URL url = ClassLoader.getSystemResource(file);
		if (url == null) {
			Application.getExceptionHandler().handleErrorCondition(
					"ERROR:  Couldn't find file: " + file);
			return null;
		}
		return new ImageIcon(url, desc);
	}

	private static ImageIcon[] loadLarge(String file, String desc) {
		ImageIcon[] icons = new ImageIcon[2];

		URL url = ClassLoader.getSystemResource(file);
		if (url != null) {
			BufferedImage bi, bi2;
			try {
				bi = ImageIO.read(url);
				bi2 = ImageIO.read(url); // How the fsck can one duplicate a
											// BufferedImage???
			} catch (IOException e) {
				Application.getExceptionHandler().handleErrorCondition(
						"ERROR:  Couldn't read file: " + file, e);
				return new ImageIcon[] { null, null };
			}

			icons[0] = new ImageIcon(bi, desc);

			// Create disabled icon
			boolean useAlphaFade = false; // don't use fade to alpha yet
			if (useAlphaFade) { // Fade using alpha

				/*
				 * TODO This code to do fade using alpha had been dead code
				 * inside a "if (false) {" block. Eclipse would give a build
				 * warning about dead code, so this code has been commented out
				 * but left here for future use; am assuming it was dead code
				 * because it wasn't working correctly but that it will be
				 * useful in the future.
				 */
				// int rgb[] = bi2.getRGB(0, 0, bi2.getWidth(), bi2.getHeight(),
				// null, 0, bi2.getWidth());
				// for (int i = 0; i < rgb.length; i++) {
				// final int alpha = (rgb[i] >> 24) & 0xFF;
				// rgb[i] = (rgb[i] & 0xFFFFFF) | (alpha / 3) << 24;
				//
				// //rgb[i] = (rgb[i]&0xFFFFFF) | ((rgb[i]>>1)&0x3F000000);
				// }
				// bi2.setRGB(0, 0, bi2.getWidth(), bi2.getHeight(), rgb, 0,
				// bi2.getWidth());

			} else { // Raster alpha

				for (int x = 0; x < bi.getWidth(); x++) {
					for (int y = 0; y < bi.getHeight(); y++) {
						if ((x + y) % 2 == 0) {
							bi2.setRGB(x, y, 0);
						}
					}
				}

			}

			// // (disabled)
			icons[1] = new ImageIcon(bi2, desc + " "
					+ trans.get("ComponentIcons.disabled"));

			return icons;
		} else {
			Application.getExceptionHandler().handleErrorCondition(
					"ERROR:  Couldn't find file: " + file);
			return new ImageIcon[] { null, null };
		}
	}
}
