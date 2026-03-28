package info.openrocket.swing.gui.util;

import com.formdev.flatlaf.FlatLaf.DisabledIconProvider;
import com.formdev.flatlaf.extras.FlatSVGIcon;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.document.Simulation;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import com.jthemedetecor.OsThemeDetector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.GrayFilter;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public class Icons {
	private static final Logger log = LoggerFactory.getLogger(Icons.class);
	private static final Translator trans = Application.getTranslator();
	private static final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();
	// SVGs can opt into theme coloring by using this placeholder RGB.
	private static final int SVG_THEME_COLOR_RGB = 0x000000;	// All icons should be black by default
	private static final String SVG_DEFAULT_COLOR_KEY = "OR.icons.default";
	// Multiplier to scale icons relative to font height (icons are typically slightly larger than text)
	private static final double ICON_FONT_SIZE_MULTIPLIER = 1.25;

	static {
		log.debug("Starting to load icons");
	}
	
	/**
	 * Icons used for showing the status of a simulation (up to date, out of date, etc).
	 */
	public static final Map<Simulation.Status, Icon> SIMULATION_STATUS_ICON_MAP;
	static {
		final String SIM_UPTODATE = "pix/icons/tick.png";
		final String SIM_CANTRUN = "pix/icons/sim_cantrun.png";
		final String SIM_OUTDATED = "pix/icons/refresh.png";
		final String SIM_ABORTED = "pix/eventicons/event-exception.png";

		HashMap<Simulation.Status, Icon> map = new HashMap<>();
		map.put(Simulation.Status.NOT_SIMULATED, loadImageIcon(SIM_OUTDATED, "Not simulated"));
		map.put(Simulation.Status.CANT_RUN, loadImageIcon(SIM_CANTRUN, "Can't run, no motors assigned."));
		map.put(Simulation.Status.UPTODATE, loadImageIcon(SIM_UPTODATE, "Up to date"));
		map.put(Simulation.Status.LOADED, loadImageIcon(SIM_UPTODATE, "Loaded from File"));
		map.put(Simulation.Status.OUTDATED, loadImageIcon(SIM_OUTDATED, "Out-of-date"));
		map.put(Simulation.Status.EXTERNAL, loadImageIcon(SIM_UPTODATE, "Imported data"));
		map.put(Simulation.Status.ABORTED, loadImageIcon(SIM_ABORTED, "Simulation run aborted"));
		SIMULATION_STATUS_ICON_MAP = Collections.unmodifiableMap(map);
	}
	
	public static final Icon SIMULATION_LISTENER_OK;
	public static final Icon SIMULATION_LISTENER_ERROR;
	static {
		SIMULATION_LISTENER_OK = SIMULATION_STATUS_ICON_MAP.get(Simulation.Status.UPTODATE);
		SIMULATION_LISTENER_ERROR = SIMULATION_STATUS_ICON_MAP.get(Simulation.Status.OUTDATED);
	}

	private static final FlatSVGIcon.ColorFilter MACOS_MENU_COLOR_FILTER;
	static {
		if (SystemInfo.getPlatform() != SystemInfo.Platform.MAC_OS) {
			MACOS_MENU_COLOR_FILTER = null;
		} else {
			// Detect the current OS theme
			boolean isDark = false;
			try {
				OsThemeDetector detector = OsThemeDetector.getDetector();
				isDark = detector.isDark();
			} catch (Exception e) {
				// Fallback: try to detect from system color
				Color menuText = java.awt.SystemColor.menuText;
				if (menuText != null) {
					// If menu text is closer to white, it's dark theme
					isDark = (menuText.getRed() + menuText.getGreen() + menuText.getBlue()) > 384;
				}
			}

			// For macOS menus, convert ALL colors to monochrome (white for dark theme, black for light theme)
			// This ensures multi-colored icons display correctly in native macOS menus
			final Color targetColor = isDark ? Color.WHITE : Color.BLACK;
			MACOS_MENU_COLOR_FILTER = new FlatSVGIcon.ColorFilter(color -> {
				// Preserve alpha channel while converting to target monochrome color
				if (color.getAlpha() != 255) {
					return new Color(targetColor.getRed(), targetColor.getGreen(), targetColor.getBlue(), color.getAlpha());
				}
				return targetColor;
			});
		}
	}

	// Note: most of these icons are from famfamicons Silk set
	// For SVG icons, we use Lucide icons (https://lucide.dev/) where possible.

	public static final Icon FILE_NEW = loadIcon(
			"pix/icons/lucide/file-plus-corner.svg",
			"pix/icons/document-new.png",
			"New document",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x499C54, "OR.icons.plus"
			));
	public static final Icon FILE_OPEN = loadIcon(
			"pix/icons/lucide/file.svg",
			"pix/icons/document-open.png",
			"Open document");
	public static final Icon FILE_OPEN_RECENT = loadSvgIcon(
			"pix/icons/lucide/file-clock.svg",
			"Open recent document",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x2D2DBD, "OR.icons.tintColor"
			));
	public static final Icon FILE_OPEN_EXAMPLE = loadIcon(
			"pix/icons/lucide/file-info.svg",
			"pix/icons/document-open-example.png",
			"Open example document",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x2D2DBD, "OR.icons.tintColor"
			));
	public static final Icon FILE_SAVE = loadIcon(
			"pix/icons/lucide/save.svg",
			"pix/icons/document-save.png",
			"Save document");
	public static final Icon FILE_SAVE_AS = loadIcon(
			"pix/icons/lucide/save-as.svg",
			"pix/icons/document-save-as.png",
			"Save document as",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xFAC132, "OR.icons.pencil"
			));
	public static final Icon SAVE_DECAL = loadIcon(
			"pix/icons/lucide/file-image.svg",
			"pix/icons/Painting-Transparent-PNG_16.png",
			"Save decal image",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xFAC132, "OR.icons.imageSun",
					0x2F6536, "OR.icons.imageMountain"
			));
	public static final Icon FILE_PRINT = loadIcon(
			"pix/icons/lucide/printer.svg",
			"pix/icons/print-design.specs.png",
			"Print specifications");
	public static final Icon FILE_IMPORT = loadIcon(
			"pix/icons/lucide/import.svg",
			"pix/icons/import.png",
			"Import",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xc80000, "OR.icons.import"
			));
	public static final Icon FILE_EXPORT = loadIcon(
			"pix/icons/lucide/export.svg",
			"pix/icons/export.png",
			"Export",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x499C54, "OR.icons.export"
			));
	public static final Icon SIM_TABLE_EXPORT = loadIcon(
			"pix/icons/lucide/sheet.svg",
			"pix/icons/sim_table_export.png",
			"Export simulation table");
	public static final Icon EXPORT_3D = loadImageIcon("pix/icons/model_export3d.png", "Export 3D");
	public static final Icon EXPORT_SVG = loadImageIcon("pix/icons/svg-logo.png", "Export SVG");
	public static final Icon FILE_CLOSE = loadIcon(
			"pix/icons/lucide/circle-x.svg",
			"pix/icons/document-close.png",
			"Close document",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xc80000, "OR.icons.red"
			));
	public static final Icon FILE_QUIT = loadIcon(
			"pix/icons/lucide/power.svg",
			"pix/icons/application-exit.png",
			"Quit OpenRocket",
			Map.of(
					SVG_THEME_COLOR_RGB, "OR.icons.red"
			));
	public static final Icon EDIT_UNDO = loadIcon(
			"pix/icons/lucide/undo.svg",
			"pix/icons/edit-undo.png",
			trans.get("Icons.Undo"),
			Map.of(
					SVG_THEME_COLOR_RGB, "OR.icons.undo"
			));
	public static final Icon EDIT_REDO = loadIcon(
			"pix/icons/lucide/redo.svg",
			"pix/icons/edit-redo.png",
			trans.get("Icons.Redo"),
			Map.of(
					SVG_THEME_COLOR_RGB, "OR.icons.redo"
			));
	public static final Icon EDIT_EDIT = loadIcon(
			"pix/icons/lucide/square-pen.svg",
			"pix/icons/edit-edit.png",
			"Edit",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xFAC132, "OR.icons.pencil"
			));
	public static final Icon EDIT_RENAME = loadIcon(
			"pix/icons/lucide/rename.svg",
			"pix/icons/edit-rename.png",
			"Rename",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x2D2DBD, "OR.icons.tintColor"
			));
	public static final Icon EDIT_CUT = loadIcon(
			"pix/icons/lucide/scissors.svg",
			"pix/icons/edit-cut.png",
			"Cut");
	public static final Icon EDIT_COPY = loadIcon(
			"pix/icons/lucide/copy.svg",
			"pix/icons/edit-copy.png",
			"Copy");
	public static final Icon EDIT_PASTE = loadIcon(
			"pix/icons/lucide/clipboard-paste.svg",
			"pix/icons/edit-paste.png",
			"Paste",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xc80000, "OR.icons.import"
			));
	public static final Icon EDIT_DUPLICATE = loadIcon(
			"pix/icons/lucide/copy-plus.svg",
			"pix/icons/edit-duplicate.png",
			"Duplicate",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x2d2dbd, "OR.icons.duplicate"
			));
	public static final Icon EDIT_DELETE = loadIcon(
			"pix/icons/lucide/trash.svg",
			"pix/icons/edit-delete.png",
			"Delete",
			"OR.icons.delete");
	public static final Icon EDIT_SCALE = loadIcon(
			"pix/icons/lucide/scaling.svg",
			"pix/icons/edit-scale.png",
			"Scale",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x2D2DBD, "OR.icons.tintColor"
			));
	public static final Icon MORE_OPTIONS = loadSvgIcon(
			"pix/icons/lucide/ellipsis-vertical.svg",
			"More options");

	public static final Icon IMAGE_OPEN = loadSvgIcon(
			"pix/icons/lucide/file-image.svg",
			"Open image");
	public static final Icon IMAGE_EDIT = loadIcon(
			"pix/icons/lucide/square-pen.svg",
			"pix/icons/edit-edit.png",
			"Edit image",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xFAC132, "OR.icons.pencil"
			));
	public static final Icon IMAGE_NEW = loadSvgIcon(
			"pix/icons/lucide/image-plus.svg",
			"New image",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xFAC132, "OR.icons.imageSun",
					0x499C54, "OR.icons.plus",
					0x2F6536, "OR.icons.imageMountain"
			));

	public static final Icon SIM_RUN = loadIcon(
			"pix/icons/lucide/play-filled.svg",
			"pix/icons/sim-run.png",
			"Run",
			"OR.icons.play");
	public static final Icon SIM_PLOT = loadIcon(
			"pix/icons/lucide/chart-spline.svg",
			"pix/icons/sim-plot.png",
			"Plot",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x006cb6, "OR.icons.plot"
			));

	public static final Icon HELP_ABOUT = loadIcon(
			"pix/icons/lucide/info.svg",
			"pix/icons/help-about.png",
			"About",
			"OR.icons.help");
	public static final Icon HELP_CHECK_FOR_UPDATES = loadIcon(
			"pix/icons/lucide/refresh-cw.svg",
			"pix/icons/help-check-for-updates.png",
			"Check For Updates");
	public static final Icon HELP_LICENSE = loadIcon(
			"pix/icons/lucide/scale.svg",
			"pix/icons/help-license.png",
			"License");
	public static final Icon HELP_BUG_REPORT = loadIcon(
			"pix/icons/lucide/bug.svg",
			"pix/icons/help-bug.png",
			"Bug report");
	public static final Icon HELP_DEBUG_LOG = loadIcon(
			"pix/icons/lucide/notepad-text.svg",
			"pix/icons/help-log.png",
			"Debug log");
	public static final Icon HELP_TOURS = loadIcon(
			"pix/icons/lucide/bus.svg",
			"pix/icons/help-tours.png",
			"Guided tours");
	public static final Icon DOCUMENTATION = loadIcon(
			"pix/icons/lucide/book-open.svg",
			"pix/icons/documentation.png",
			"Documentation");

	public static final Icon ZOOM_IN = loadIcon(
			"pix/icons/lucide/zoom-in.svg",
			"pix/icons/zoom-in.png",
			"Zoom in",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x499C54, "OR.icons.zoomIn"
			));
	public static final Icon ZOOM_OUT = loadIcon(
			"pix/icons/lucide/zoom-out.svg",
			"pix/icons/zoom-out.png",
			"Zoom out",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xC80000, "OR.icons.zoomOut"
			));
	public static final Icon ZOOM_RESET = loadIcon(
			"pix/icons/lucide/zoom-fit.svg",
			"pix/icons/zoom-reset.png",
			"Reset Zoom & Pan",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x2d2dbd, "OR.icons.zoomFit"
			));

	public static final Icon RULER = loadIcon(
			"pix/icons/lucide/ruler-dimension-line.svg",
			"pix/icons/ruler.png",
			"Ruler",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xFAC132, "OR.colors.ruler"
			));
	public static final Icon SNAP_CLICK = loadIcon(
			"pix/icons/lucide/magnet-click.svg",
			"pix/icons/snap-click.png",
			"Snap",
			Map.of(
					0xE86B55, "OR.icons.magnet.red",
					0xE1E3E2, "OR.icons.magnet.white"
			));
	public static final Icon SNAP = loadSvgIcon(
			"pix/icons/lucide/magnet-filled.svg",
			"Snap",
			Map.of(
					0xE86B55, "OR.icons.magnet.red",
					0xE1E3E2, "OR.icons.magnet.white"
			));

	public static final Icon PREFERENCES = loadIcon(
			"pix/icons/lucide/cog.svg",
			"pix/icons/preferences.png",
			"Preferences");
	
	public static final Icon CONFIGURE = loadIcon(
			"pix/icons/lucide/file-cog.svg",
			"pix/icons/configure.png",
			"Configure",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x2D2DBD, "OR.icons.tintColor"
			));
	public static final Icon HELP = loadIcon(
			"pix/icons/lucide/info.svg",
			"pix/icons/help-about.png",
			"Help");
	public static final Icon UP = loadIcon(
			"pix/icons/lucide/arrow-big-up-dash.svg",
			"pix/icons/up.png",
			"Up",
			Map.of(
					SVG_THEME_COLOR_RGB, "OR.icons.moveUp"
			));
	public static final Icon DOWN = loadIcon(
			"pix/icons/lucide/arrow-big-down-dash.svg",
			"pix/icons/down.png",
			"Down",
			Map.of(
					SVG_THEME_COLOR_RGB, "OR.icons.moveDown"
			));
	public static final Icon REFRESH = loadIcon(
			"pix/icons/lucide/refresh-ccw.svg",
			"pix/icons/refresh.png",
			"Refresh");
	public static final Icon RESET = loadSvgIcon(
			"pix/icons/lucide/rotate-cw.svg",
			"Reset");

	public static final Icon NOT_FAVORITE = loadIcon(
			"pix/icons/lucide/star-off.svg",
			"pix/icons/star_silver.png",
			"Not favorite");
	public static final Icon FAVORITE = loadIcon(
			"pix/icons/lucide/star.svg",
			"pix/icons/star_gold.png",
			"Favorite");

	public static final Icon WARNING_LOW = loadIcon(
			"pix/icons/lucide/info-filled.svg",
			"pix/icons/warning_low.png",
			"Informational",
			"OR.icons.warning.low");
	public static final Icon WARNING_NORMAL = loadIcon(
			"pix/icons/lucide/triangle-alert-filled.svg",
			"pix/icons/warning_normal.png",
			"Warning",
			"OR.icons.warning.normal");
	public static final Icon WARNING_HIGH = loadIcon(
			"pix/icons/lucide/circle-alert-filled.svg",
			"pix/icons/warning_high.png",
			"Critical",
			"OR.icons.warning.high");

	public static final Icon MASS_OVERRIDE = loadIcon(
			"pix/icons/lucide/weight.svg",
			"pix/icons/mass-override_light.png",
			"Mass Override",
			"OR.icons.override");
	public static final Icon MASS_OVERRIDE_SUBCOMPONENT = loadIcon(
			"pix/icons/lucide/weight.svg",
			"pix/icons/mass-override-subcomponent_light.png",
			"Mass Override Subcomponent",
			"OR.icons.override.subcomponent",
			0.75);
	public static final Icon CG_OVERRIDE = loadIcon(
			"pix/icons/lucide/cg-override.svg",
			"pix/icons/cg-override_light.png",
			"CG Override",
			"OR.icons.override");
	public static final Icon CG_OVERRIDE_SUBCOMPONENT = loadIcon(
			"pix/icons/lucide/cg-override.svg",
			"pix/icons/cg-override-subcomponent_light.png",
			"CG Override Subcomponent",
			"OR.icons.override.subcomponent",
			0.75);
	public static final Icon CD_OVERRIDE = loadIcon(
			"pix/icons/lucide/cd-override.svg",
			"pix/icons/cd-override_light.png",
			"CD Override",
			"OR.icons.override");
	public static final Icon CD_OVERRIDE_SUBCOMPONENT = loadIcon(
			"pix/icons/lucide/cd-override.svg",
			"pix/icons/cd-override-subcomponent_light.png",
			"CD Override Subcomponent",
			"OR.icons.override.subcomponent",
			0.75);

	public static final Icon COMPONENT_HIDDEN = loadIcon(
			"pix/icons/lucide/eye-off.svg",
			"pix/icons/component-hidden.png",
			"Component Hidden");
	public static final Icon COMPONENT_SHOWING = loadIcon(
			"pix/icons/lucide/eye.svg",
			"pix/icons/component-showing_light.png",
			"Component Showing");
	public static final Icon COMPONENT_DISABLED = loadIcon(
			"pix/icons/lucide/ban.svg",
			"pix/icons/component-disabled_light.png",
			"Component Disabled");

	public static final Icon LOCKED = loadIcon(
			"pix/icons/lucide/lock.svg",
			"pix/icons/locked.png",
			"Locked",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0xf1c066, "OR.icons.locked"
			));
	public static final Icon UNLOCKED = loadIcon(
			"pix/icons/lucide/lock-open.svg",
			"pix/icons/unlocked.png",
			"Unlocked");
	public static final Icon SCREENSHOT = loadSvgIcon(
			"pix/icons/lucide/camera.svg",
			"Screenshot",
			Map.of(
					SVG_THEME_COLOR_RGB, "OR.icons.camera"
			));
	public static final Icon CONFIGURE_DISPLAY = loadSvgIcon(
			"pix/icons/lucide/monitor-cog.svg",
			"Configure Display",
			Map.of(
					SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY,
					0x2D2DBD, "OR.icons.tintColor"
			));

	// MANUFACTURERS ICONS
	public static final Icon RASAERO = loadImageIcon("pix/icons/RASAero_16.png", "RASAero Icon");
	public static final Icon ROCKSIM = loadImageIcon("pix/icons/Rocksim_16.png", "Rocksim Icon");


	static {
		log.debug("Icons loaded");
	}
	
	/**
	 * Load an ImageIcon from the specified file.  The file is obtained as a system
	 * resource from the normal classpath.  If the file cannot be loaded a bug dialog
	 * is opened and <code>null</code> is returned.
	 * 
	 * @param file	the file to load.
	 * @param name	the description of the icon.
	 * @return		the ImageIcon, or null if the ImageIcon could not be loaded (after the user closes the dialog)
	 */
	public static ImageIcon loadImageIcon(String file, String name) {
		if (System.getProperty("openrocket.unittest") != null) {
			return new ImageIcon();
		}
		
		URL url = ClassLoader.getSystemResource(file);
		if (url == null) {
			Application.getExceptionHandler().handleErrorCondition("Image file " + file + " not found, ignoring.");
			return null;
		}
		ImageIcon icon = new ImageIcon(url, name);
		return (ImageIcon) getScaledIcon(icon, prefs.getUIScale());
	}

	/**
	 * Load an SVG icon without explicit color mapping.
	 *
	 * @param file SVG resource path
	 * @param name description of the icon
	 * @return the SVG icon or null if not found
	 */
	public static Icon loadSvgIcon(String file, String name) {
		return loadSvgIcon(file, name, Collections.singletonMap(SVG_THEME_COLOR_RGB, SVG_DEFAULT_COLOR_KEY));
	}

	/**
	 * Loads an SVG icon from the specified file with a single color theming key.
	 * @param file the SVG file path
	 * @param name the description of the icon
	 * @param colorKey the UIManager color key for theming (can be null)
	 * @return the loaded Icon, or null if the SVG file could not be found
	 */
	public static Icon loadSvgIcon(String file, String name, String colorKey) {
		if (colorKey == null) {
			return loadSvgIcon(file, name);
		}
		return loadSvgIcon(file, name, Collections.singletonMap(SVG_THEME_COLOR_RGB, colorKey));
	}

	/**
	 * Loads an icon, preferring SVG format if available, otherwise falling back to a raster image.
	 * @param svgFile the SVG file path
	 * @param rasterFile the raster image file path
	 * @param name the description of the icon
	 * @param colorKey the UIManager color key for theming (can be null)
	 * @return the loaded Icon, or null if neither file could be found
	 */
	public static Icon loadIcon(String svgFile, String rasterFile, String name, String colorKey) {
		return loadIcon(svgFile, rasterFile, name, colorKey, 1.0);
	}

	/**
	 * Loads an icon, preferring SVG format if available, otherwise falling back to a raster image.
	 * @param svgFile the SVG file path
	 * @param rasterFile the raster image file path
	 * @param name the description of the icon
	 * @param colorKey the UIManager color key for theming (can be null)
	 * @param scaleMultiplier multiplier for icon size (1.0 = normal, < 1.0 = smaller, > 1.0 = larger)
	 * @return the loaded Icon, or null if neither file could be found
	 */
	public static Icon loadIcon(String svgFile, String rasterFile, String name, String colorKey, double scaleMultiplier) {
		if (hasResource(svgFile)) {
			if (colorKey == null) {
				return loadSvgIcon(svgFile, name, Collections.emptyMap(), scaleMultiplier);
			}
			return loadSvgIcon(svgFile, name, Collections.singletonMap(SVG_THEME_COLOR_RGB, colorKey), scaleMultiplier);
		}
		Icon icon = loadImageIcon(rasterFile, name);
		if (icon != null && scaleMultiplier != 1.0) {
			return getScaledIcon(icon, scaleMultiplier);
		}
		return icon;
	}

	public static Icon loadIcon(String svgFile, String rasterFile, String name) {
		return loadIcon(svgFile, rasterFile, name, SVG_DEFAULT_COLOR_KEY);
	}

	/**
	 * Loads an icon, preferring SVG format if available, otherwise falling back to a raster image.
	 * @param svgFile the SVG file path
	 * @param rasterFile the raster image file path
	 * @param name the description of the icon
	 * @param colorKeys map of RGB integer values to UIManager color keys for theming
	 * @return the loaded Icon, or null if neither file could be found
	 */
	public static Icon loadIcon(String svgFile, String rasterFile, String name, Map<Integer, String> colorKeys) {
		return loadIcon(svgFile, rasterFile, name, colorKeys, 1.0);
	}

	/**
	 * Loads an icon, preferring SVG format if available, otherwise falling back to a raster image.
	 * @param svgFile the SVG file path
	 * @param rasterFile the raster image file path
	 * @param name the description of the icon
	 * @param colorKeys map of RGB integer values to UIManager color keys for theming
	 * @param scaleMultiplier multiplier for icon size (1.0 = normal, < 1.0 = smaller, > 1.0 = larger)
	 * @return the loaded Icon, or null if neither file could be found
	 */
	public static Icon loadIcon(String svgFile, String rasterFile, String name, Map<Integer, String> colorKeys, double scaleMultiplier) {
		if (hasResource(svgFile)) {
			return loadSvgIcon(svgFile, name, colorKeys != null ? colorKeys : Collections.emptyMap(), scaleMultiplier);
		}
		Icon icon = loadImageIcon(rasterFile, name);
		if (icon != null && scaleMultiplier != 1.0) {
			return getScaledIcon(icon, scaleMultiplier);
		}
		return icon;
	}

	/**
	 * Loads an SVG icon from the specified file with optional color theming.
	 * @param file the SVG file path
	 * @param name the description of the icon
	 * @param colorKeys map of RGB integer values to UIManager color keys for theming
	 * @return the loaded Icon, or null if the SVG file could not be found
	 */
	private static Icon loadSvgIcon(String file, String name, Map<Integer, String> colorKeys) {
		return loadSvgIcon(file, name, colorKeys, 1.0);
	}

	/**
	 * Loads an SVG icon from the specified file with optional color theming and scale multiplier.
	 * @param file the SVG file path
	 * @param name the description of the icon
	 * @param colorKeys map of RGB integer values to UIManager color keys for theming
	 * @param scaleMultiplier multiplier for icon size (1.0 = normal, < 1.0 = smaller, > 1.0 = larger)
	 * @return the loaded Icon, or null if the SVG file could not be found
	 */
	private static Icon loadSvgIcon(String file, String name, Map<Integer, String> colorKeys, double scaleMultiplier) {
		if (!hasResource(file)) {
			Application.getExceptionHandler().handleErrorCondition("Image file " + file + " not found, ignoring.");
			return null;
		}

		// Get the target height based on font size
		int targetHeight = (int) Math.round(prefs.getUIFontSize() * ICON_FONT_SIZE_MULTIPLIER * scaleMultiplier);

		// First, create a temporary icon to get the original aspect ratio
		FlatSVGIcon icon = new FlatSVGIcon(file, Icons.class.getClassLoader());
		icon.setDescription(name);
		int originalWidth = icon.getIconWidth();
		int originalHeight = icon.getIconHeight();

		FlatSVGIcon.ColorFilter colorFilter = createSvgColorFilter(colorKeys);
		if (colorFilter != null) {
			icon.setColorFilter(colorFilter);
		}

		// If dimensions are invalid, return the icon as is
		if (originalWidth <= 0 || originalHeight <= 0) {
			return icon;
		}

		// Calculate target width maintaining aspect ratio
		double scale = (double) targetHeight / originalHeight;
		int targetWidth = Math.max(1, (int) Math.round(originalWidth * scale));

		// Create the icon with the target dimensions
		icon = icon.derive(targetWidth, targetHeight);

		return icon;
	}

	/**
	 * Checks if a resource file exists in the classpath.
	 * @param file the resource file path
	 * @return true if the resource exists, false otherwise
	 */
	private static boolean hasResource(String file) {
		if (file == null) {
			return false;
		}
		return Icons.class.getClassLoader().getResource(file) != null;
	}

	/**
	 * Creates a color filter for SVG icons that maps specific RGB colors to UIManager color keys.
	 * Uses dynamic color resolution to support theme changes at runtime.
	 * @param colorKeys map of RGB integer values to UIManager color keys
	 * @return a FlatSVGIcon.ColorFilter that applies the color mapping, or null if no mapping is provided
	 */
	private static FlatSVGIcon.ColorFilter createSvgColorFilter(Map<Integer, String> colorKeys) {
		if (colorKeys == null || colorKeys.isEmpty()) {
			return null;
		}

		// Create a copy of the colorKeys map for use in the mapper function
		Map<Integer, String> rgbToKeyMap = new HashMap<>();
		for (Map.Entry<Integer, String> entry : colorKeys.entrySet()) {
			int placeholderRGB = entry.getKey() & 0x00ffffff; // Mask out alpha channel
			rgbToKeyMap.put(placeholderRGB, entry.getValue());
		}

		// Use the mapper constructor for dynamic color resolution at paint time.
		// This allows colors to update when the theme changes without needing to recreate the icons.
		Function<Color, Color> colorMapper = originalColor -> {
			int rgb = originalColor.getRGB() & 0x00ffffff;
			String colorKey = rgbToKeyMap.get(rgb);
			if (colorKey == null) {
				return originalColor;
			}

			Color themedColor = UIManager.getColor(colorKey);
			if (themedColor == null) {
				themedColor = UIManager.getColor(SVG_DEFAULT_COLOR_KEY);
			}
			if (themedColor == null) {
				themedColor = UIManager.getColor("Label.foreground");
			}
			if (themedColor == null) {
				return originalColor;
			}

			// Preserve alpha of original color
			if (themedColor.getAlpha() != originalColor.getAlpha()) {
				return new Color((themedColor.getRGB() & 0x00ffffff) | (originalColor.getRGB() & 0xff000000), true);
			}
			return themedColor;
		};

		return new FlatSVGIcon.ColorFilter(colorMapper);
	}

	/**
	 * Preserves the alpha channel of the source color when applying the target color.
	 * @param target the target color
	 * @param source the source color
	 * @return a new Color with the RGB of target and the alpha of source
	 */
	private static Color preserveAlpha(Color target, Color source) {
		if (target.getAlpha() == source.getAlpha()) {
			return target;
		}
		return new Color((target.getRGB() & 0x00ffffff) | (source.getRGB() & 0xff000000), true);
	}

	/**
	 * Derives a menu icon from an existing icon by applying macOS menu color filtering.
	 * Creates a new icon with the menu color filter applied, leaving the original unchanged.
	 * This method should be used for icons in application menu items (JMenuItem).
	 *
	 * @param icon the source icon to derive from
	 * @return a new icon with macOS menu color filtering applied, or the original icon if not applicable
	 */
	public static Icon deriveMenuIcon(Icon icon) {
		if (!(icon instanceof FlatSVGIcon svgIcon)) {
			return icon;
		}

		svgIcon = new FlatSVGIcon(svgIcon);
		applyMacMenuColorFilter(svgIcon);
		return svgIcon;
	}

	/**
	 * Applies a color filter for macOS menu icons that swaps black/white based on theme.
	 * Detects the OS theme and maps colors appropriately:
	 * - Light theme: icons are black
	 * - Dark theme: icons are white
	 * @param icon the icon to apply the filter to
	 */
	private static void applyMacMenuColorFilter(FlatSVGIcon icon) {
		if (MACOS_MENU_COLOR_FILTER == null) {
			return;
		}
		icon.setColorFilter(MACOS_MENU_COLOR_FILTER);
	}

	/**
	 * Derives a new icon with the specified scale from an existing icon.
	 * For SVG icons, uses FlatSVGIcon.derive(float scale) which preserves color filters.
	 * For other icon types, falls back to getScaledIcon.
	 *
	 * @param icon the source icon to derive from
	 * @param scale the scaling factor (1.0 = no change, < 1.0 = smaller, > 1.0 = larger)
	 * @return a new scaled icon, or the original icon if scale is 1.0 or icon type is not supported
	 */
	public static Icon deriveScaledIcon(Icon icon, float scale) {
		if (icon == null || scale == 1.0f) {
			return icon;
		}

		if (icon instanceof FlatSVGIcon svgIcon) {
			return svgIcon.derive(scale);
		}

		// For ImageIcon and other types, use the existing scaling method
		return getScaledIcon(icon, scale);
	}

	/**
	 * Scales an ImageIcon to the specified scale.
	 * @param icon icon to scale
	 * @param scale the scale to scale to (1 = no scale, < 1 = smaller, > 1 = bigger)
	 * @return scaled down icon. If <icon> is not an ImageIcon, the original icon is returned.
	 */
	public static Icon getScaledIcon(Icon icon, final double scale) {
		if (!(icon instanceof ImageIcon) || scale == 1) {
			return icon;
		}

		Image image = ((ImageIcon) icon).getImage();
		if (image == null) {
			return icon;
		}
		int sourceWidth = image.getWidth(null);
		int sourceHeight = image.getHeight(null);

		if (sourceWidth <= 0 || sourceHeight <= 0) {
			return icon;
		}

		int width = Math.max(1, (int) Math.round(sourceWidth * scale));
		int height = Math.max(1, (int) Math.round(sourceHeight * scale));

		// Create a new scaled image
		Image scaledImage = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);

		// Create and return a new ImageIcon with the scaled image
		return new ImageIcon(scaledImage);
	}

	public static Icon createDisabledIcon(Icon icon) {
		if (icon instanceof DisabledIconProvider) {
			return ((DisabledIconProvider) icon).getDisabledIcon();
		}
		if (!(icon instanceof ImageIcon)) {
			return icon;
		}
		Image image = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = image.getGraphics();
		icon.paintIcon(null, g, 0, 0);
		g.dispose();

		return new ImageIcon(GrayFilter.createDisabledImage(((ImageIcon) icon).getImage()));
	}

}
