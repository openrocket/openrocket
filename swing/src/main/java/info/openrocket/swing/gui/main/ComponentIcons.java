package info.openrocket.swing.gui.main;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.theme.UITheme;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.rocketcomponent.AxialStage;
import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.Bulkhead;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.EllipticalFinSet;
import info.openrocket.core.rocketcomponent.EngineBlock;
import info.openrocket.core.rocketcomponent.FreeformFinSet;
import info.openrocket.core.rocketcomponent.InnerTube;
import info.openrocket.core.rocketcomponent.LaunchLug;
import info.openrocket.core.rocketcomponent.MassComponent;
import info.openrocket.core.rocketcomponent.MassComponent.MassComponentType;
import info.openrocket.core.rocketcomponent.NoseCone;
import info.openrocket.core.rocketcomponent.Parachute;
import info.openrocket.core.rocketcomponent.ParallelStage;
import info.openrocket.core.rocketcomponent.PodSet;
import info.openrocket.core.rocketcomponent.RailButton;
import info.openrocket.core.rocketcomponent.ShockCord;
import info.openrocket.core.rocketcomponent.Streamer;
import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.rocketcomponent.TubeCoupler;
import info.openrocket.core.rocketcomponent.TubeFinSet;
import info.openrocket.core.startup.Application;

public class ComponentIcons {
	private static final Translator trans = Application.getTranslator();

	private static final String ICON_DIRECTORY = "pix/componenticons/";
	private static final String SMALL_SUFFIX = "-small.png";
	private static final String LARGE_SUFFIX = "-large.png";

	// Component image file keys
	private static String noseCone;
	private static String bodyTube;
	private static String transition;
	private static String trapezoidFinSet;
	private static String ellipticalFinSet;
	private static String freeformFinSet;
	private static String tubeFinSet;
	private static String launchLug;
	private static String railButton;
	private static String innerTube;
	private static String tubeCoupler;
	private static String centeringRing;
	private static String bulkhead;
	private static String engineBlock;
	private static String parachute;
	private static String streamer;
	private static String shockCord;
	private static String mass;
	private static String stage;
	private static String boosters;
	private static String pods;

	private static String mass_altimeter;
	private static String mass_battery;
	private static String mass_deployment_charge;
	private static String mass_payload;
	private static String mass_flight_comp;
	private static String mass_recovery_hardware;
	private static String mass_tracker;

	private static final HashMap<Class<?>, ImageIcon> SMALL_ICONS = new HashMap<Class<?>, ImageIcon>();
	private static final HashMap<Class<?>, ImageIcon> LARGE_ICONS = new HashMap<Class<?>, ImageIcon>();
	private static final HashMap<Class<?>, ImageIcon> DISABLED_ICONS = new HashMap<Class<?>, ImageIcon>();
	private static final HashMap<MassComponentType, ImageIcon> MASS_COMPONENT_SMALL_ICONS = new HashMap<MassComponentType, ImageIcon>();
	static {
		initColors();

		// // Nose cone
		load(noseCone, trans.get("ComponentIcons.Nosecone"), NoseCone.class);
		// // Body tube
		load(bodyTube, trans.get("ComponentIcons.Bodytube"), BodyTube.class);
		//// Transition
		load(transition, trans.get("ComponentIcons.Transition"), Transition.class);
		//// Trapezoidal fin set
		load(trapezoidFinSet, trans.get("ComponentIcons.Trapezoidalfinset"), TrapezoidFinSet.class);
		//// Elliptical fin set
		load(ellipticalFinSet, trans.get("ComponentIcons.Ellipticalfinset"), EllipticalFinSet.class);
		//// Freeform fin set
		load(freeformFinSet, trans.get("ComponentIcons.Freeformfinset"), FreeformFinSet.class);
		//// Tube fin set
		load(tubeFinSet, trans.get("ComponentIcons.Tubefinset"), TubeFinSet.class);
		//// Launch lug
		load(launchLug, trans.get("ComponentIcons.Launchlug"), LaunchLug.class);
		//// Rail Button
		load(railButton, trans.get("ComponentIcons.RailButton"), RailButton.class);
		//// Inner tube
		load(innerTube, trans.get("ComponentIcons.Innertube"), InnerTube.class);
		//// Tube coupler
		load(tubeCoupler, trans.get("ComponentIcons.Tubecoupler"), TubeCoupler.class);
		//// Centering ring
		load(centeringRing, trans.get("ComponentIcons.Centeringring"), CenteringRing.class);
		//// Bulkhead
		load(bulkhead, trans.get("ComponentIcons.Bulkhead"), Bulkhead.class);
		// // Engine block
		load(engineBlock, trans.get("ComponentIcons.Engineblock"),
				EngineBlock.class);
		// // Parachute
		load(parachute, trans.get("ComponentIcons.Parachute"),
				Parachute.class);
		// // Streamer
		load(streamer, trans.get("ComponentIcons.Streamer"), Streamer.class);
		// // Shock cord
		load(shockCord, trans.get("ComponentIcons.Shockcord"),
				ShockCord.class);
		load(mass, trans.get("ComponentIcons.Masscomponent"),
				MassComponent.class);
		// // Component Assemblies
		load(stage, trans.get("ComponentIcons.Stage"),
				AxialStage.class);
		load(boosters, trans.get("ComponentIcons.Boosters"),
				ParallelStage.class);
		load(pods, trans.get("ComponentIcons.Pods"),
				PodSet.class);
		// // Mass components
		loadMassTypeIcon(mass, trans.get("ComponentIcons.Masscomponent"), MassComponentType.MASSCOMPONENT);
		loadMassTypeIcon(mass_altimeter, trans.get("ComponentIcons.Altimeter"), MassComponentType.ALTIMETER);
		loadMassTypeIcon(mass_battery, trans.get("ComponentIcons.Battery"), MassComponentType.BATTERY);
		loadMassTypeIcon(mass_deployment_charge, trans.get("ComponentIcons.Deploymentcharge"),
				MassComponentType.DEPLOYMENTCHARGE);
		loadMassTypeIcon(mass_payload, trans.get("ComponentIcons.Payload"), MassComponentType.PAYLOAD);
		loadMassTypeIcon(mass_flight_comp,
				trans.get("ComponentIcons.Flightcomputer"), MassComponentType.FLIGHTCOMPUTER);
		loadMassTypeIcon(mass_recovery_hardware,
				trans.get("ComponentIcons.Recoveryhardware"), MassComponentType.RECOVERYHARDWARE);
		loadMassTypeIcon(mass_tracker, trans.get("ComponentIcons.Tracker"), MassComponentType.TRACKER);
	}

	private static void initColors() {
		updateColors();
		UITheme.Theme.addUIThemeChangeListener(ComponentIcons::updateColors);
	}

	private static void updateColors() {
		noseCone = GUIUtil.getUITheme().getComponentIconNoseCone();
		bodyTube = GUIUtil.getUITheme().getComponentIconBodyTube();
		transition = GUIUtil.getUITheme().getComponentIconTransition();
		trapezoidFinSet = GUIUtil.getUITheme().getComponentIconTrapezoidFinSet();
		ellipticalFinSet = GUIUtil.getUITheme().getComponentIconEllipticalFinSet();
		freeformFinSet = GUIUtil.getUITheme().getComponentIconFreeformFinSet();
		tubeFinSet = GUIUtil.getUITheme().getComponentIconTubeFinSet();
		launchLug = GUIUtil.getUITheme().getComponentIconLaunchLug();
		railButton = GUIUtil.getUITheme().getComponentIconRailButton();
		innerTube = GUIUtil.getUITheme().getComponentIconInnerTube();
		tubeCoupler = GUIUtil.getUITheme().getComponentIconTubeCoupler();
		centeringRing = GUIUtil.getUITheme().getComponentIconCenteringRing();
		bulkhead = GUIUtil.getUITheme().getComponentIconBulkhead();
		engineBlock = GUIUtil.getUITheme().getComponentIconEngineBlock();
		parachute = GUIUtil.getUITheme().getComponentIconParachute();
		streamer = GUIUtil.getUITheme().getComponentIconStreamer();
		shockCord = GUIUtil.getUITheme().getComponentIconShockCord();
		mass = GUIUtil.getUITheme().getComponentIconMass();
		stage = GUIUtil.getUITheme().getComponentIconStage();
		boosters = GUIUtil.getUITheme().getComponentIconBoosters();
		pods = GUIUtil.getUITheme().getComponentIconPods();
		mass_altimeter = GUIUtil.getUITheme().getComponentIconMassAltimeter();
		mass_battery = GUIUtil.getUITheme().getComponentIconMassBattery();
		mass_deployment_charge = GUIUtil.getUITheme().getComponentIconMassDeploymentCharge();
		mass_payload = GUIUtil.getUITheme().getComponentIconMassPayload();
		mass_flight_comp = GUIUtil.getUITheme().getComponentIconMassFlightComp();
		mass_recovery_hardware = GUIUtil.getUITheme().getComponentIconMassRecoveryHardware();
		mass_tracker = GUIUtil.getUITheme().getComponentIconMassTracker();
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
