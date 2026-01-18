package info.openrocket.swing.gui.theme;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.extras.FlatAnimatedLafChange;
import com.formdev.flatlaf.intellijthemes.FlatOneDarkIJTheme;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.formdev.flatlaf.ui.FlatBorder;
import com.formdev.flatlaf.ui.FlatMarginBorder;
import com.jthemedetecor.OsThemeDetector;
import info.openrocket.core.arch.SystemInfo;
import info.openrocket.core.document.Simulation.Status;
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.util.GUIUtil;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
import info.openrocket.core.util.LineStyle;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.MassObject;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.Icon;
import javax.swing.JRootPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.lang.ref.WeakReference;

public class UITheme {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(UITheme.class);

    private static final Map<String, Float> fontOffsets = new HashMap<>();

    public static final class Keys {
        public static final String BACKGROUND = "OR.colors.background";
        public static final String BORDER = "OR.colors.border";
        public static final String TEXT = "OR.colors.text";
        public static final String TEXT_DIM = "OR.colors.text.dim";
        public static final String TEXT_DISABLED = "OR.colors.text.disabled";
        public static final String TEXT_SELECTION_FOREGROUND = "OR.colors.textSelection.foreground";
        public static final String TEXT_SELECTION_BACKGROUND = "OR.colors.textSelection.background";
        public static final String INFO = "OR.colors.info";
        public static final String WARNING = "OR.colors.warning";
        public static final String ERROR = "OR.colors.error";
        public static final String DARK_ERROR = "OR.colors.darkError";
        public static final String ROW_LIGHTER = "OR.colors.row.lighter";
        public static final String ROW_DARKER = "OR.colors.row.darker";
        public static final String TABLE_FLASH = "OR.colors.table.flash";
        public static final String TABLE_SELECTION = "OR.colors.table.selection";
        public static final String FLIGHTDATA_TEXT_ACTIVE = "OR.colors.flightData.textActive";
        public static final String FLIGHTDATA_TEXT_INACTIVE = "OR.colors.flightData.textInactive";
        public static final String MULTI_COMP_EDIT = "OR.colors.multiCompEdit";
        public static final String MOTOR_BORDER = "OR.colors.motor.border";
        public static final String MOTOR_FILL = "OR.colors.motor.fill";
        public static final String CG = "OR.colors.cg";
        public static final String CP = "OR.colors.cp";
        public static final String URL = "OR.colors.url";
        public static final String COMPONENT_TREE_BACKGROUND = "OR.colors.componentTree.background";
        public static final String COMPONENT_TREE_FOREGROUND = "OR.colors.componentTree.foreground";
        public static final String VISIBILITY_HIDDEN_FOREGROUND = "OR.colors.visibility.hidden.foreground";
        public static final String FIN_GRID_MAJOR = "OR.colors.fin.gridMajor";
        public static final String FIN_GRID_MINOR = "OR.colors.fin.gridMinor";
        public static final String FIN_POINT = "OR.colors.fin.point";
        public static final String FIN_SELECTED_POINT = "OR.colors.fin.selectedPoint";
        public static final String FIN_BODY_LINE = "OR.colors.fin.bodyLine";
        public static final String FIN_SNAP_HIGHLIGHT = "OR.colors.fin.snapHighlight";

        public static final String DEFAULT_BODY_COMPONENT_COLOR = "OR.defaults.component.body";
        public static final String DEFAULT_TUBE_FIN_SET_COLOR = "OR.defaults.component.tubeFinSet";
        public static final String DEFAULT_FIN_SET_COLOR = "OR.defaults.component.finSet";
        public static final String DEFAULT_LAUNCH_LUG_COLOR = "OR.defaults.component.launchLug";
        public static final String DEFAULT_RAIL_BUTTON_COLOR = "OR.defaults.component.railButton";
        public static final String DEFAULT_INTERNAL_COMPONENT_COLOR = "OR.defaults.component.internal";
        public static final String DEFAULT_MASS_OBJECT_COLOR = "OR.defaults.component.massObject";
        public static final String DEFAULT_RECOVERY_DEVICE_COLOR = "OR.defaults.component.recoveryDevice";
        public static final String DEFAULT_POD_SET_COLOR = "OR.defaults.component.podSet";
        public static final String DEFAULT_PARALLEL_STAGE_COLOR = "OR.defaults.component.parallelStage";
        public static final String DEFAULT_LINE_STYLE_GENERIC = "OR.defaults.lineStyle.generic";
        public static final String DEFAULT_LINE_STYLE_MASS_OBJECT = "OR.defaults.lineStyle.massObject";
    }

    static {
        fontOffsets.put("MenuBar.font", 1.0f);
        fontOffsets.put("Tree.font", -1.0f);
        fontOffsets.put("Slider.font", -2.0f);
        fontOffsets.put("TableHeader.font", -1.0f);
        fontOffsets.put("ColorChooser.font", -1.0f);
        fontOffsets.put("Menu.acceleratorFont", 1.0f);
        fontOffsets.put("InternalFrame.optionDialogTitleFont", 1.0f);
        fontOffsets.put("InternalFrame.paletteTitleFont", 1.0f);
        fontOffsets.put("MenuItem.font", 1.0f);
        fontOffsets.put("PopupMenu.font", 1.0f);
        fontOffsets.put("MenuItem.acceleratorFont", 1.0f);
        fontOffsets.put("RadioButtonMenuItem.font", 1.0f);
        fontOffsets.put("Table.font", -1.0f);
        fontOffsets.put("InternalFrame.titleFont", 1.0f);
        fontOffsets.put("List.font", -1.0f);
        fontOffsets.put("RadioButtonMenuItem.acceleratorFont", 1.0f);
        fontOffsets.put("CheckBoxMenuItem.acceleratorFont", 1.0f);
        fontOffsets.put("Menu.font", 1.0f);
        fontOffsets.put("TabbedPane.smallFont", -2.0f);
        fontOffsets.put("CheckBoxMenuItem.font", 1.0f);
        fontOffsets.put("ToolTip.font", -2.0f);
    }


    // TODO: replace a bunch of this with the FlatLaf properties files, see https://www.formdev.com/flatlaf/properties-files
    // For FlatLaf theme properties, check out swing/src/main/resources/themes

    public interface Theme {
        void applyTheme();
        void applyThemeToRootPane(JRootPane rootPane);
        String name(); // Provided by enum, gives the name of the enum constant
        String getDisplayName();
        Color getBackgroundColor();
        Color getBorderColor();
        Color getTextColor();
        Color getDimTextColor();
        Color getDisabledTextColor();
        Color getTextSelectionForegroundColor();
        Color getTextSelectionBackgroundColor();
        Color getInformationColor();
        Color getWarningColor();
        Color getErrorColor();
        Color getDarkErrorColor();
        Color getRowBackgroundLighterColor();
        Color getRowBackgroundDarkerColor();
        Color getTableRowFlashColor();
        Color getTableRowSelectionColor();
        Color getFlightDataTextActiveColor();
        Color getFlightDataTextInactiveColor();
        Color getMultiCompEditColor();

        // Component colors
        String getDefaultBodyComponentColor();
        String getDefaultTubeFinSetColor();
        String getDefaultFinSetColor();
        String getDefaultLaunchLugColor();
        String getDefaultRailButtonColor();
        String getDefaultInternalComponentColor();
        String getDefaultMassObjectColor();
        String getDefaultRecoveryDeviceColor();
        String getDefaultPodSetColor();
        String getDefaultParallelStageColor();

        Color getMotorBorderColor();
        Color getMotorFillColor();

        Color getCGColor();
        Color getCPColor();

        Color getURLColor();

        Color getComponentTreeBackgroundColor();
        Color getComponentTreeForegroundColor();
        Color getVisibilityHiddenForegroundColor();

        Color getFinPointGridMajorLineColor();
        Color getFinPointGridMinorLineColor();
        Color getFinPointPointColor();
        Color getFinPointSelectedPointColor();
        Color getFinPointBodyLineColor();
        Color getFinPointSnapHighlightColor();

		Color getStatusColor(Status status);

        Border getBorder();
        Border getMarginBorder();
        Border getUnitSelectorBorder();
        Border getUnitSelectorFocusBorder();

        void formatScriptTextArea(RSyntaxTextArea textArea);

        // Rocket component icons
        String getComponentIconNoseCone();
        String getComponentIconBodyTube();
        String getComponentIconTransition();
        String getComponentIconTrapezoidFinSet();
        String getComponentIconEllipticalFinSet();
        String getComponentIconFreeformFinSet();
        String getComponentIconTubeFinSet();
        String getComponentIconLaunchLug();
        String getComponentIconRailButton();
        String getComponentIconInnerTube();
        String getComponentIconTubeCoupler();
        String getComponentIconCenteringRing();
        String getComponentIconBulkhead();
        String getComponentIconEngineBlock();
        String getComponentIconParachute();
        String getComponentIconStreamer();
        String getComponentIconShockCord();
        String getComponentIconMass();
        String getComponentIconStage();
        String getComponentIconBoosters();
        String getComponentIconPods();
        String getComponentIconMassAltimeter();
        String getComponentIconMassBattery();
        String getComponentIconMassDeploymentCharge();
        String getComponentIconMassPayload();
        String getComponentIconMassFlightComp();
        String getComponentIconMassRecoveryHardware();
        String getComponentIconMassTracker();

        // Static list of listeners (weak to avoid leaks)
        static List<WeakReference<Runnable>> themeChangeListeners = new ArrayList<>();

        // Static method to add a listener
        static void addUIThemeChangeListener(Runnable listener) {
            if (listener == null) {
                return;
            }
            themeChangeListeners.add(new WeakReference<>(listener));
        }

        // Static method to remove a listener
        static void removeUIThemeChangeListener(Runnable listener) {
            if (listener == null) {
                return;
            }
            themeChangeListeners.removeIf(ref -> {
                Runnable r = ref.get();
                return r == null || r == listener;
            });
        }

        // Static method to notify all listeners
        static void notifyUIThemeChangeListeners() {
            ((SwingPreferences) Application.getPreferences()).updateColors();
            Iterator<WeakReference<Runnable>> it = themeChangeListeners.iterator();
            while (it.hasNext()) {
                WeakReference<Runnable> ref = it.next();
                Runnable listener = ref.get();
                if (listener == null) {
                    it.remove();
                    continue;
                }
                listener.run();
            }
        }
    }

    public static boolean isLightTheme(Theme theme) {
        if (theme == Themes.DARK || theme == Themes.DARK_CONTRAST) {
            return false;
        } else if (theme == Themes.LIGHT) {
            return true;
        } else if (theme == Themes.AUTO) {
            try {
                final OsThemeDetector detector = OsThemeDetector.getDetector();
                final boolean isDarkThemeUsed = detector.isDark();
                if (isDarkThemeUsed) {
                    return false;
                } else {
                    return true;
                }
            } catch (Exception ignore) {}
        }

        return false;
    }
	
    public enum Themes implements Theme {
        /*
        Standard light theme
         */
        LIGHT {
            private final String displayName = trans.get("UITheme.Light");

            @Override
            public void applyTheme() {
                preApplyTheme();


                try {
                    if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS) {
                        FlatMacLightLaf.setup();
                    } else {
                        FlatLightLaf.setup();
                    }
                } catch (Exception e) {
                    log.warn("Unable to set system look and feel", e);
                }

                postApplyTheme(this);
            }

            @Override
            public void applyThemeToRootPane(JRootPane rootPane) {
                commonApplyThemeToRootPane(rootPane, getTextColor());
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            @Override
            public Color getBackgroundColor() {
                return themedColor("OR.colors.background", Color.WHITE);
            }

            @Override
            public Color getBorderColor() {
                return themedColor("OR.colors.border", Color.BLACK);
            }

            @Override
            public Color getTextColor() {
                return themedColor("OR.colors.text", Color.BLACK);
            }

            @Override
            public Color getDimTextColor() {
                return themedColor("OR.colors.text.dim", Color.GRAY);
            }

            @Override
            public Color getDisabledTextColor() {
                return themedColor("OR.colors.text.disabled", getDimTextColor());
            }

            @Override
            public Color getTextSelectionForegroundColor() {
                return themedColor("OR.colors.textSelection.foreground", UIManager.getColor("Tree.selectionForeground"));
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return themedColor("OR.colors.textSelection.background", UIManager.getColor("Tree.selectionBackground"));
            }

            @Override
            public Color getInformationColor() {
                return themedColor("OR.colors.info", new Color(45, 45, 189));
            }

            @Override
            public Color getWarningColor() {
                return themedColor("OR.colors.warning", new Color(217, 152, 0));
            }

            @Override
            public Color getErrorColor() {
                return themedColor("OR.colors.error", Color.RED);
            }

            @Override
            public Color getDarkErrorColor() {
            	return themedColor("OR.colors.darkError", new Color(200,0,0));
            }

            @Override
            public Color getRowBackgroundLighterColor() {
                return themedColor("OR.colors.row.lighter", Color.WHITE);
            }

            @Override
            public Color getRowBackgroundDarkerColor() {
                return themedColor("OR.colors.row.darker", new Color(245, 245, 245));
            }

            @Override
            public Color getTableRowFlashColor() {
                return themedColor("OR.colors.table.flash", new Color(255, 255, 204));
            }

            @Override
            public Color getTableRowSelectionColor() {
                return themedColor("OR.colors.table.selection", new Color(217, 233, 255));
            }

            @Override
            public Color getFlightDataTextActiveColor() {
            	return themedColor("OR.colors.flightData.textActive", new Color(0,0,127));
            }

            @Override
            public Color getFlightDataTextInactiveColor() {
            	return themedColor("OR.colors.flightData.textInactive", new Color(0,0,127,127));
            }

            @Override
            public Color getMultiCompEditColor() {
                return themedColor("OR.colors.multiCompEdit", new Color(170, 0, 100));
            }

            @Override
            public String getDefaultBodyComponentColor() {
                return themedString("OR.defaults.component.body", "0,0,240");
            }
            @Override
            public String getDefaultTubeFinSetColor() {
                return themedString("OR.defaults.component.tubeFinSet", "0,0,200");
            }
            @Override
            public String getDefaultFinSetColor() {
                return themedString("OR.defaults.component.finSet", "0,0,200");
            }
            @Override
            public String getDefaultLaunchLugColor() {
                return themedString("OR.defaults.component.launchLug", "0,0,180");
            }
            @Override
            public String getDefaultRailButtonColor() {
                return themedString("OR.defaults.component.railButton", "0,0,180");
            }
            @Override
            public String getDefaultInternalComponentColor() {
                return themedString("OR.defaults.component.internal", "170,0,100");
            }
            @Override
            public String getDefaultMassObjectColor() {
                return themedString("OR.defaults.component.massObject", "0,0,0");
            }
            @Override
            public String getDefaultRecoveryDeviceColor() {
                return themedString("OR.defaults.component.recoveryDevice", "255,0,0");
            }
            @Override
            public String getDefaultPodSetColor() {
                return themedString("OR.defaults.component.podSet", "160,160,215");
            }
            @Override
            public String getDefaultParallelStageColor() {
                return themedString("OR.defaults.component.parallelStage", "198,163,184");
            }

            @Override
            public Color getMotorBorderColor() {
                return themedColor("OR.colors.motor.border", new Color(0, 0, 0, 200));
            }

            @Override
            public Color getMotorFillColor() {
                return themedColor("OR.colors.motor.fill", new Color(0, 0, 0, 100));
            }

            @Override
            public Color getCGColor() {
                return themedColor("OR.colors.cg", Color.BLUE);
            }

            @Override
            public Color getCPColor() {
                return themedColor("OR.colors.cp", Color.RED);
            }

            @Override
            public Color getURLColor() {
                return themedColor("OR.colors.url", Color.BLUE);
            }

            @Override
            public Color getComponentTreeBackgroundColor() {
                return themedColor("OR.colors.componentTree.background", UIManager.getColor("Tree.textBackground"));
            }

            @Override
            public Color getComponentTreeForegroundColor() {
                return themedColor("OR.colors.componentTree.foreground", UIManager.getColor("Tree.textForeground"));
            }

            @Override
            public Color getVisibilityHiddenForegroundColor() {
                return themedColor("OR.colors.visibility.hidden.foreground", UIManager.getColor("Tree.textForeground.hidden.light"));
            }

            @Override
            public Color getFinPointGridMajorLineColor() {
                return themedColor("OR.colors.fin.gridMajor", new Color( 0, 0, 255, 80));
            }

            @Override
            public Color getFinPointGridMinorLineColor() {
                return themedColor("OR.colors.fin.gridMinor", new Color( 0, 0, 255, 30));
            }

            @Override
            public Color getFinPointPointColor() {
                return themedColor("OR.colors.fin.point", new Color(200, 0, 0, 255));
            }

            @Override
            public Color getFinPointSelectedPointColor() {
                return themedColor("OR.colors.fin.selectedPoint", new Color(200, 0, 0, 255));
            }

            @Override
            public Color getFinPointBodyLineColor() {
                return themedColor("OR.colors.fin.bodyLine", Color.BLACK);
            }

            @Override
            public Color getFinPointSnapHighlightColor() {
                return themedColor("OR.colors.fin.snapHighlight", Color.RED);
            }

            @Override
            public Border getBorder() {
                return new FlatBorder();
            }

            @Override
            public Border getMarginBorder() {
                return new FlatMarginBorder();
            }

            @Override
            public Border getUnitSelectorBorder() {
                return new CompoundBorder(
                        new LineBorder(new Color(0.0f, 0.0f, 0.0f, 0.08f), 1),
                        new EmptyBorder(1, 1, 1, 1));
            }

            @Override
            public Border getUnitSelectorFocusBorder() {
                return new CompoundBorder(
                        new LineBorder(new Color(0.0f, 0.0f, 0.0f, 0.6f)),
                        new EmptyBorder(1, 1, 1, 1));
            }

            @Override
            public void formatScriptTextArea(RSyntaxTextArea textArea) {
                try {
                    org.fife.ui.rsyntaxtextarea.Theme theme = org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                            "/org/fife/ui/rsyntaxtextarea/themes/default.xml"));
                    theme.apply(textArea);
                    textArea.setCurrentLineHighlightColor(new Color(255, 255, 230));
                } catch (IOException ioe) {
                   log.warn("Unable to load RSyntaxTextArea theme", ioe);
                }
            }

            @Override
            public String getComponentIconNoseCone() {
                return "nosecone";
            }
            @Override
            public String getComponentIconBodyTube() {
                return "bodytube";
            }
            @Override
            public String getComponentIconTransition() {
                return "transition";
            }
            @Override
            public String getComponentIconTrapezoidFinSet() {
                return "trapezoidfin";
            }
            @Override
            public String getComponentIconEllipticalFinSet() {
                return "ellipticalfin";
            }
            @Override
            public String getComponentIconFreeformFinSet() {
                return "freeformfin";
            }
            @Override
            public String getComponentIconTubeFinSet() {
                return "tubefin";
            }
            @Override
            public String getComponentIconLaunchLug() {
                return "launchlug";
            }
            @Override
            public String getComponentIconRailButton() {
                return "railbutton";
            }
            @Override
            public String getComponentIconInnerTube() {
                return "innertube";
            }
            @Override
            public String getComponentIconTubeCoupler() {
                return "tubecoupler";
            }
            @Override
            public String getComponentIconCenteringRing() {
                return "centeringring";
            }
            @Override
            public String getComponentIconBulkhead() {
                return "bulkhead";
            }
            @Override
            public String getComponentIconEngineBlock() {
                return "engineblock";
            }
            @Override
            public String getComponentIconParachute() {
                return "parachute";
            }
            @Override
            public String getComponentIconStreamer() {
                return "streamer";
            }
            @Override
            public String getComponentIconShockCord() {
                return "shockcord";
            }
            @Override
            public String getComponentIconMass() {
                return "mass";
            }
            @Override
            public String getComponentIconStage() {
                return "stage";
            }
            @Override
            public String getComponentIconBoosters() {
                return "boosters";
            }
            @Override
            public String getComponentIconPods() {
                return "pods";
            }
            @Override
            public String getComponentIconMassAltimeter() {
                return "altimeter";
            }
            @Override
            public String getComponentIconMassBattery() {
                return "battery";
            }
            @Override
            public String getComponentIconMassDeploymentCharge() {
                return "deployment-charge";
            }
            @Override
            public String getComponentIconMassPayload() {
                return "payload";
            }
            @Override
            public String getComponentIconMassFlightComp() {
                return "flight-comp";
            }
            @Override
            public String getComponentIconMassRecoveryHardware() {
                return "recovery-hardware";
            }
            @Override
            public String getComponentIconMassTracker() {
                return "tracker";
            }
        },
        /*
        Dark theme
         */
        DARK {
            private final String displayName = trans.get("UITheme.Dark");

            @Override
            public void applyTheme() {
                preApplyTheme();

                // Set the actual theme
                FlatDarculaLaf.setup();

                postApplyTheme(this);
            }

            @Override
            public void applyThemeToRootPane(JRootPane rootPane) {
                commonApplyThemeToRootPane(rootPane, getTextColor());
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            @Override
            public Color getBackgroundColor() {
                return themedColor("OR.colors.background", new Color(73, 76, 79));
            }

            @Override
            public Color getBorderColor() {
                return themedColor("OR.colors.border", new Color(97, 99, 101));
            }

            @Override
            public Color getTextColor() {
                return themedColor("OR.colors.text", new Color(173, 173, 173));
            }

            @Override
            public Color getDimTextColor() {
                return themedColor("OR.colors.text.dim", new Color(182, 182, 182));
            }

            @Override
            public Color getDisabledTextColor() {
                return themedColor("OR.colors.text.disabled", new Color(161, 161, 161));
            }

            @Override
            public Color getTextSelectionForegroundColor() {
                return themedColor("OR.colors.textSelection.foreground", Color.WHITE);
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return themedColor("OR.colors.textSelection.background", new Color(75, 110, 175));
            }

            @Override
            public Color getInformationColor() {
                return themedColor("OR.colors.info", new Color(208, 208, 255));
            }

            @Override
            public Color getWarningColor() {
                return themedColor("OR.colors.warning", new Color(255, 224, 166));
            }

            @Override
            public Color getErrorColor() {
                return themedColor("OR.colors.error", new Color(246, 143, 143));
            }

            @Override
            public Color getDarkErrorColor() {
            	return themedColor("OR.colors.darkError", new Color(229, 103, 103));
            }

            @Override
            public Color getRowBackgroundLighterColor() {
                return themedColor("OR.colors.row.lighter", new Color(65, 69, 71));
            }

            @Override
            public Color getRowBackgroundDarkerColor() {
                return themedColor("OR.colors.row.darker", new Color(60, 63, 65));
            }

            @Override
            public Color getTableRowFlashColor() {
                return themedColor("OR.colors.table.flash", new Color(255, 255, 102, 69));
            }

            @Override
            public Color getTableRowSelectionColor() {
                return themedColor("OR.colors.table.selection", new Color(73, 87, 133));
            }

            @Override
            public Color getFlightDataTextActiveColor() {
                return themedColor("OR.colors.flightData.textActive", new Color(145, 183, 231));
            }

            @Override
            public Color getFlightDataTextInactiveColor() {
                return themedColor("OR.colors.flightData.textInactive", new Color(128, 166, 230, 127));
            }

            @Override
            public Color getMultiCompEditColor() {
                return themedColor("OR.colors.multiCompEdit", new Color(222, 146, 176));
            }

            @Override
            public String getDefaultBodyComponentColor() {
                return themedString("OR.defaults.component.body", "150,162,255");
            }
            @Override
            public String getDefaultTubeFinSetColor() {
                return themedString("OR.defaults.component.tubeFinSet", "150,178,255");
            }
            @Override
            public String getDefaultFinSetColor() {
                return themedString("OR.defaults.component.finSet", "150,178,255");
            }
            @Override
            public String getDefaultLaunchLugColor() {
                return themedString("OR.defaults.component.launchLug", "142,153,238");
            }
            @Override
            public String getDefaultRailButtonColor() {
                return themedString("OR.defaults.component.railButton", "142,153,238");
            }
            @Override
            public String getDefaultInternalComponentColor() {
                return themedString("OR.defaults.component.internal", "181,128,151");
            }
            @Override
            public String getDefaultMassObjectColor() {
                return themedString("OR.defaults.component.massObject", "210,210,210");
            }
            @Override
            public String getDefaultRecoveryDeviceColor() {
                return themedString("OR.defaults.component.recoveryDevice", "220,90,90");
            }
            @Override
            public String getDefaultPodSetColor() {
                return themedString("OR.defaults.component.podSet", "190,190,235");
            }
            @Override
            public String getDefaultParallelStageColor() {
                return themedString("OR.defaults.component.parallelStage", "210,180,195");
            }

            @Override
            public Color getMotorBorderColor() {
                return themedColor("OR.colors.motor.border", new Color(0, 0, 0, 100));
            }

            @Override
            public Color getMotorFillColor() {
                return themedColor("OR.colors.motor.fill", new Color(0, 0, 0, 50));
            }

            @Override
            public Color getCGColor() {
                return themedColor("OR.colors.cg", new Color(85, 133, 253));
            }

            @Override
            public Color getCPColor() {
                return themedColor("OR.colors.cp", new Color(255, 72, 106));
            }

            @Override
            public Color getURLColor() {
                return themedColor("OR.colors.url", new Color(150, 167, 255));
            }

            @Override
            public Color getComponentTreeBackgroundColor() {
                return themedColor("OR.colors.componentTree.background", getBackgroundColor());
            }

            @Override
            public Color getComponentTreeForegroundColor() {
                return themedColor("OR.colors.componentTree.foreground", getTextColor());
            }

            @Override
            public Color getVisibilityHiddenForegroundColor() {
                return themedColor("OR.colors.visibility.hidden.foreground", UIManager.getColor("Tree.textForeground.hidden.dark"));
            }

            @Override
            public Color getFinPointGridMajorLineColor() {
                return themedColor("OR.colors.fin.gridMajor", new Color(135, 135, 199, 197));
            }

            @Override
            public Color getFinPointGridMinorLineColor() {
                return themedColor("OR.colors.fin.gridMinor", new Color(121, 121, 189, 69));
            }

            @Override
            public Color getFinPointPointColor() {
                return themedColor("OR.colors.fin.point", new Color(217, 108, 108, 255));
            }

            @Override
            public Color getFinPointSelectedPointColor() {
                return themedColor("OR.colors.fin.selectedPoint", new Color(232, 78, 78, 255));
            }

            @Override
            public Color getFinPointBodyLineColor() {
                return themedColor("OR.colors.fin.bodyLine", Color.WHITE);
            }

            @Override
            public Color getFinPointSnapHighlightColor() {
                return themedColor("OR.colors.fin.snapHighlight", new Color(255, 58, 58, 255));
            }

            @Override
            public Border getBorder() {
                return new FlatBorder();
            }

            @Override
            public Border getMarginBorder() {
                return new FlatMarginBorder();
            }

            @Override
            public Border getUnitSelectorBorder() {
                return new CompoundBorder(
                        new LineBorder(new Color(1.0f, 1.0f, 1.0f, 0.08f), 1),
                        new EmptyBorder(1, 1, 1, 1));
            }

            @Override
            public Border getUnitSelectorFocusBorder() {
                return new CompoundBorder(
                        new LineBorder(new Color(1.0f, 1.0f, 1.0f, 0.6f)),
                        new EmptyBorder(1, 1, 1, 1));
            }

            @Override
            public void formatScriptTextArea(RSyntaxTextArea textArea) {
                try {
                    org.fife.ui.rsyntaxtextarea.Theme theme = org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                            "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
                    theme.apply(textArea);
                } catch (IOException ioe) {
                    log.warn("Unable to load RSyntaxTextArea theme", ioe);
                }
            }

            @Override
            public String getComponentIconNoseCone() {
                return "nosecone";
            }
            @Override
            public String getComponentIconBodyTube() {
                return "bodytube";
            }
            @Override
            public String getComponentIconTransition() {
                return "transition";
            }
            @Override
            public String getComponentIconTrapezoidFinSet() {
                return "trapezoidfin";
            }
            @Override
            public String getComponentIconEllipticalFinSet() {
                return "ellipticalfin_dark";
            }
            @Override
            public String getComponentIconFreeformFinSet() {
                return "freeformfin_dark";
            }
            @Override
            public String getComponentIconTubeFinSet() {
                return "tubefin";
            }
            @Override
            public String getComponentIconLaunchLug() {
                return "launchlug_dark";
            }
            @Override
            public String getComponentIconRailButton() {
                return "railbutton";
            }
            @Override
            public String getComponentIconInnerTube() {
                return "innertube";
            }
            @Override
            public String getComponentIconTubeCoupler() {
                return "tubecoupler";
            }
            @Override
            public String getComponentIconCenteringRing() {
                return "centeringring";
            }
            @Override
            public String getComponentIconBulkhead() {
                return "bulkhead";
            }
            @Override
            public String getComponentIconEngineBlock() {
                return "engineblock";
            }
            @Override
            public String getComponentIconParachute() {
                return "parachute_dark";
            }
            @Override
            public String getComponentIconStreamer() {
                return "streamer_dark";
            }
            @Override
            public String getComponentIconShockCord() {
                return "shockcord_dark";
            }
            @Override
            public String getComponentIconMass() {
                return "mass_dark";
            }
            @Override
            public String getComponentIconStage() {
                return "stage";
            }
            @Override
            public String getComponentIconBoosters() {
                return "boosters";
            }
            @Override
            public String getComponentIconPods() {
                return "pods_dark";
            }
            @Override
            public String getComponentIconMassAltimeter() {
                return "altimeter_dark";
            }
            @Override
            public String getComponentIconMassBattery() {
                return "battery_dark";
            }
            @Override
            public String getComponentIconMassDeploymentCharge() {
                return "deployment-charge_dark";
            }
            @Override
            public String getComponentIconMassPayload() {
                return "payload";
            }
            @Override
            public String getComponentIconMassFlightComp() {
                return "flight-comp_dark";
            }
            @Override
            public String getComponentIconMassRecoveryHardware() {
                return "recovery-hardware_dark";
            }
            @Override
            public String getComponentIconMassTracker() {
                return "tracker_dark";
            }
        },
        /*
        High-contrast dark theme
         */
        DARK_CONTRAST {
            private final String displayName = trans.get("UITheme.DarkContrast");

            @Override
            public void applyTheme() {
                preApplyTheme();

                // Set the actual theme
                FlatOneDarkIJTheme.setup();

                postApplyTheme(this);
            }

            @Override
            public void applyThemeToRootPane(JRootPane rootPane) {
                commonApplyThemeToRootPane(rootPane, getTextColor());
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            @Override
            public Color getBackgroundColor() {
                return themedColor("OR.colors.background", new Color(43, 45, 51));
            }

            @Override
            public Color getBorderColor() {
                return themedColor("OR.colors.border", new Color(163, 163, 163, 204));
            }

            @Override
            public Color getTextColor() {
                return themedColor("OR.colors.text", UIManager.getColor("Label.foreground"));
            }

            @Override
            public Color getDimTextColor() {
                return themedColor("OR.colors.text.dim", new Color(165, 171, 184));
            }

            @Override
            public Color getDisabledTextColor() {
                return themedColor("OR.colors.text.disabled", new Color(128, 128, 128, 223));
            }


            @Override
            public Color getTextSelectionForegroundColor() {
                return themedColor("OR.colors.textSelection.foreground", Color.WHITE);
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return themedColor("OR.colors.textSelection.background", new Color(62, 108, 173));
            }

            @Override
            public Color getInformationColor() {
                return themedColor("OR.colors.info", new Color(197, 197, 252));
            }

            @Override
            public Color getWarningColor() {
                return themedColor("OR.colors.warning", new Color(255, 233, 187));
            }

            @Override
            public Color getErrorColor() {
                return themedColor("OR.colors.error", new Color(255, 173, 173));
            }

            @Override
            public Color getDarkErrorColor() {
                return themedColor("OR.colors.darkError", new Color(255, 178, 178));
            }

            @Override
            public Color getRowBackgroundLighterColor() {
                return themedColor("OR.colors.row.lighter", new Color(43, 49, 58));
            }

            @Override
            public Color getRowBackgroundDarkerColor() {
                return themedColor("OR.colors.row.darker", new Color(34, 37, 44));
            }

            @Override
            public Color getTableRowFlashColor() {
                return themedColor("OR.colors.table.flash", new Color(255, 255, 128, 60));
            }

            @Override
            public Color getTableRowSelectionColor() {
                return themedColor("OR.colors.table.selection", new Color(51, 66, 102));
            }

            @Override
            public Color getFlightDataTextActiveColor() {
                return themedColor("OR.colors.flightData.textActive", new Color(212, 230, 255));
            }

            @Override
            public Color getFlightDataTextInactiveColor() {
                return themedColor("OR.colors.flightData.textInactive", new Color(170, 201, 255, 127));
            }

            @Override
            public Color getMultiCompEditColor() {
                return themedColor("OR.colors.multiCompEdit", new Color(255, 165, 200));
            }

            @Override
            public String getDefaultBodyComponentColor() {
                return themedString("OR.defaults.component.body", "150,175,255");
            }
            @Override
            public String getDefaultTubeFinSetColor() {
                return themedString("OR.defaults.component.tubeFinSet", "150,184,254");
            }
            @Override
            public String getDefaultFinSetColor() {
                return themedString("OR.defaults.component.finSet", "150,184,255");
            }
            @Override
            public String getDefaultLaunchLugColor() {
                return themedString("OR.defaults.component.launchLug", "142,153,238");
            }
            @Override
            public String getDefaultRailButtonColor() {
                return themedString("OR.defaults.component.railButton", "142,153,238");
            }
            @Override
            public String getDefaultInternalComponentColor() {
                return themedString("OR.defaults.component.internal", "181,128,151");
            }
            @Override
            public String getDefaultMassObjectColor() {
                return themedString("OR.defaults.component.massObject", "210,210,210");
            }
            @Override
            public String getDefaultRecoveryDeviceColor() {
                return themedString("OR.defaults.component.recoveryDevice", "220,90,90");
            }
            @Override
            public String getDefaultPodSetColor() {
                return themedString("OR.defaults.component.podSet", "190,190,235");
            }
            @Override
            public String getDefaultParallelStageColor() {
                return themedString("OR.defaults.component.parallelStage", "210,180,195");
            }

            @Override
            public Color getMotorBorderColor() {
                return themedColor("OR.colors.motor.border", new Color(255, 255, 255, 200));
            }

            @Override
            public Color getMotorFillColor() {
                return themedColor("OR.colors.motor.fill", new Color(0, 0, 0, 70));
            }

            @Override
            public Color getCGColor() {
                return themedColor("OR.colors.cg", new Color(85, 133, 253));
            }

            @Override
            public Color getCPColor() {
                return themedColor("OR.colors.cp", new Color(255, 72, 106));
            }

            @Override
            public Color getURLColor() {
                return themedColor("OR.colors.url", new Color(171, 185, 255));
            }

            @Override
            public Color getComponentTreeBackgroundColor() {
                return themedColor("OR.colors.componentTree.background", getBackgroundColor());
            }

            @Override
            public Color getComponentTreeForegroundColor() {
                return themedColor("OR.colors.componentTree.foreground", getTextColor());
            }

            @Override
            public Color getVisibilityHiddenForegroundColor() {
                return themedColor("OR.colors.visibility.hidden.foreground", UIManager.getColor("Tree.textForeground.hidden.dark"));
            }

            @Override
            public Color getFinPointGridMajorLineColor() {
                return themedColor("OR.colors.fin.gridMajor", new Color(164, 164, 224, 197));
            }

            @Override
            public Color getFinPointGridMinorLineColor() {
                return themedColor("OR.colors.fin.gridMinor", new Color(134, 134, 201, 69));
            }

            @Override
            public Color getFinPointPointColor() {
                return themedColor("OR.colors.fin.point", new Color(242, 121, 121, 255));
            }

            @Override
            public Color getFinPointSelectedPointColor() {
                return themedColor("OR.colors.fin.selectedPoint", new Color(232, 78, 78, 255));
            }

            @Override
            public Color getFinPointBodyLineColor() {
                return themedColor("OR.colors.fin.bodyLine", Color.WHITE);
            }

            @Override
            public Color getFinPointSnapHighlightColor() {
                return themedColor("OR.colors.fin.snapHighlight", new Color(241, 77, 77, 255));
            }

            @Override
            public Border getBorder() {
                return new FlatBorder();
            }

            @Override
            public Border getMarginBorder() {
                return new FlatMarginBorder();
            }

            @Override
            public Border getUnitSelectorBorder() {
                return new CompoundBorder(
                        new LineBorder(new Color(0.9f, 0.9f, 0.9f, 0.15f), 1),
                        new EmptyBorder(1, 1, 1, 1));
            }

            @Override
            public Border getUnitSelectorFocusBorder() {
                return new CompoundBorder(
                        new LineBorder(new Color(0.9f, 0.9f, 0.9f, 0.6f)),
                        new EmptyBorder(1, 1, 1, 1));
            }

            @Override
            public void formatScriptTextArea(RSyntaxTextArea textArea) {
                try {
                    org.fife.ui.rsyntaxtextarea.Theme theme = org.fife.ui.rsyntaxtextarea.Theme.load(getClass().getResourceAsStream(
                            "/org/fife/ui/rsyntaxtextarea/themes/monokai.xml"));
                    theme.apply(textArea);
                } catch (IOException ioe) {
                    log.warn("Unable to load RSyntaxTextArea theme", ioe);
                }
            }

            @Override
            public String getComponentIconNoseCone() {
                return DARK.getComponentIconNoseCone();
            }
            @Override
            public String getComponentIconBodyTube() {
                return DARK.getComponentIconBodyTube();
            }
            @Override
            public String getComponentIconTransition() {
                return DARK.getComponentIconTransition();
            }

            @Override
            public String getComponentIconTrapezoidFinSet() {
                return DARK.getComponentIconTrapezoidFinSet();
            }
            @Override
            public String getComponentIconEllipticalFinSet() {
                return DARK.getComponentIconEllipticalFinSet();
            }
            @Override
            public String getComponentIconFreeformFinSet() {
                return DARK.getComponentIconFreeformFinSet();
            }
            @Override
            public String getComponentIconTubeFinSet() {
                return DARK.getComponentIconTubeFinSet();
            }
            @Override
            public String getComponentIconLaunchLug() {
                return DARK.getComponentIconLaunchLug();
            }
            @Override
            public String getComponentIconRailButton() {
                return DARK.getComponentIconRailButton();
            }
            @Override
            public String getComponentIconInnerTube() {
                return DARK.getComponentIconInnerTube();
            }
            @Override
            public String getComponentIconTubeCoupler() {
                return DARK.getComponentIconTubeCoupler();
            }
            @Override
            public String getComponentIconCenteringRing() {
                return DARK.getComponentIconCenteringRing();
            }
            @Override
            public String getComponentIconBulkhead() {
                return DARK.getComponentIconBulkhead();
            }
            @Override
            public String getComponentIconEngineBlock() {
                return DARK.getComponentIconEngineBlock();
            }
            @Override
            public String getComponentIconParachute() {
                return DARK.getComponentIconParachute();
            }
            @Override
            public String getComponentIconStreamer() {
                return DARK.getComponentIconStreamer();
            }
            @Override
            public String getComponentIconShockCord() {
                return DARK.getComponentIconShockCord();
            }
            @Override
            public String getComponentIconMass() {
                return DARK.getComponentIconMass();
            }
            @Override
            public String getComponentIconStage() {
                return DARK.getComponentIconStage();
            }
            @Override
            public String getComponentIconBoosters() {
                return DARK.getComponentIconBoosters();
            }
            @Override
            public String getComponentIconPods() {
                return DARK.getComponentIconPods();
            }
            @Override
            public String getComponentIconMassAltimeter() {
                return DARK.getComponentIconMassAltimeter();
            }

            @Override
            public String getComponentIconMassBattery() {
                return DARK.getComponentIconMassBattery();
            }
            @Override
            public String getComponentIconMassDeploymentCharge() {
                return DARK.getComponentIconMassDeploymentCharge();
            }
            @Override
            public String getComponentIconMassPayload() {
                return DARK.getComponentIconMassPayload();
            }
            @Override
            public String getComponentIconMassFlightComp() {
                return DARK.getComponentIconMassFlightComp();
            }
            @Override
            public String getComponentIconMassRecoveryHardware() {
                return DARK.getComponentIconMassRecoveryHardware();
            }
            @Override
            public String getComponentIconMassTracker() {
                return DARK.getComponentIconMassTracker();
            }
        },
        /*
        Detect best theme based on operating system theme
         */
        AUTO {
            private final String displayName = trans.get("UITheme.Auto");

            private Theme getCurrentTheme() {
                try {
                    final OsThemeDetector detector = OsThemeDetector.getDetector();
                    final boolean isDarkThemeUsed = detector.isDark();
                    if (isDarkThemeUsed) {
                        return Themes.DARK_CONTRAST;
                    } else {
                        return Themes.LIGHT;
                    }
                } catch (Exception ignore) {}

                return Themes.LIGHT;
            }

            @Override
            public void applyTheme() {
                getCurrentTheme().applyTheme();
            }

            @Override
            public void applyThemeToRootPane(JRootPane rootPane) {
                getCurrentTheme().applyThemeToRootPane(rootPane);
            }

            @Override
            public String getDisplayName() {
                return displayName;
            }

            @Override
            public Color getBackgroundColor() {
                return getCurrentTheme().getBackgroundColor();
            }

            @Override
            public Color getBorderColor() {
                return getCurrentTheme().getBorderColor();
            }

            @Override
            public Color getTextColor() {
                return getCurrentTheme().getTextColor();
            }

            @Override
            public Color getDimTextColor() {
                return getCurrentTheme().getDimTextColor();
            }

            @Override
            public Color getDisabledTextColor() {
                return getCurrentTheme().getDisabledTextColor();
            }

            @Override
            public Color getTextSelectionForegroundColor() {
                return getCurrentTheme().getTextSelectionForegroundColor();
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return getCurrentTheme().getTextSelectionBackgroundColor();
            }

            @Override
            public Color getInformationColor() {
                return getCurrentTheme().getInformationColor();
            }

            @Override
            public Color getWarningColor() {
                return getCurrentTheme().getWarningColor();
            }

            @Override
            public Color getErrorColor() {
                return getCurrentTheme().getErrorColor();
            }

            @Override
            public Color getDarkErrorColor() {
                return getCurrentTheme().getDarkErrorColor();
            }

            @Override
            public Color getRowBackgroundLighterColor() {
                return getCurrentTheme().getRowBackgroundLighterColor();
            }

            @Override
            public Color getRowBackgroundDarkerColor() {
                return getCurrentTheme().getRowBackgroundDarkerColor();
            }

            @Override
            public Color getTableRowFlashColor() {
                return getCurrentTheme().getTableRowFlashColor();
            }

            @Override
            public Color getTableRowSelectionColor() {
                return getCurrentTheme().getTableRowSelectionColor();
            }

            @Override
            public Color getFlightDataTextActiveColor() {
                return getCurrentTheme().getFlightDataTextActiveColor();
            }

            @Override
            public Color getFlightDataTextInactiveColor() {
                return getCurrentTheme().getFlightDataTextInactiveColor();
            }

            @Override
            public Color getMultiCompEditColor() {
                return getCurrentTheme().getMultiCompEditColor();
            }

            @Override
            public String getDefaultBodyComponentColor() {
                return getCurrentTheme().getDefaultBodyComponentColor();
            }

            @Override
            public String getDefaultTubeFinSetColor() {
                return getCurrentTheme().getDefaultTubeFinSetColor();
            }

            @Override
            public String getDefaultFinSetColor() {
                return getCurrentTheme().getDefaultFinSetColor();
            }

            @Override
            public String getDefaultLaunchLugColor() {
                return getCurrentTheme().getDefaultLaunchLugColor();
            }

            @Override
            public String getDefaultRailButtonColor() {
                return getCurrentTheme().getDefaultRailButtonColor();
            }

            @Override
            public String getDefaultInternalComponentColor() {
                return getCurrentTheme().getDefaultInternalComponentColor();
            }

            @Override
            public String getDefaultMassObjectColor() {
                return getCurrentTheme().getDefaultMassObjectColor();
            }

            @Override
            public String getDefaultRecoveryDeviceColor() {
                return getCurrentTheme().getDefaultRecoveryDeviceColor();
            }

            @Override
            public String getDefaultPodSetColor() {
                return getCurrentTheme().getDefaultPodSetColor();
            }

            @Override
            public String getDefaultParallelStageColor() {
                return getCurrentTheme().getDefaultParallelStageColor();
            }

            @Override
            public Color getMotorBorderColor() {
                return getCurrentTheme().getMotorBorderColor();
            }

            @Override
            public Color getMotorFillColor() {
                return getCurrentTheme().getMotorFillColor();
            }

            @Override
            public Color getCGColor() {
                return getCurrentTheme().getCGColor();
            }

            @Override
            public Color getCPColor() {
                return getCurrentTheme().getCPColor();
            }

            @Override
            public Color getURLColor() {
                return getCurrentTheme().getURLColor();
            }

            @Override
            public Color getComponentTreeBackgroundColor() {
                return getCurrentTheme().getComponentTreeBackgroundColor();
            }

            @Override
            public Color getComponentTreeForegroundColor() {
                return getCurrentTheme().getComponentTreeForegroundColor();
            }

            @Override
            public Color getVisibilityHiddenForegroundColor() {
                return getCurrentTheme().getVisibilityHiddenForegroundColor();
            }

            @Override
            public Color getFinPointGridMajorLineColor() {
                return getCurrentTheme().getFinPointGridMajorLineColor();
            }

            @Override
            public Color getFinPointGridMinorLineColor() {
                return getCurrentTheme().getFinPointGridMinorLineColor();
            }

            @Override
            public Color getFinPointPointColor() {
                return getCurrentTheme().getFinPointPointColor();
            }

            @Override
            public Color getFinPointSelectedPointColor() {
                return getCurrentTheme().getFinPointSelectedPointColor();
            }

            @Override
            public Color getFinPointBodyLineColor() {
                return getCurrentTheme().getFinPointBodyLineColor();
            }

            @Override
            public Color getFinPointSnapHighlightColor() {
                return getCurrentTheme().getFinPointBodyLineColor();
            }

            @Override
            public Border getBorder() {
                return getCurrentTheme().getBorder();
            }

            @Override
            public Border getMarginBorder() {
                return getCurrentTheme().getMarginBorder();
            }

            @Override
            public Border getUnitSelectorBorder() {
                return getCurrentTheme().getUnitSelectorBorder();
            }

            @Override
            public Border getUnitSelectorFocusBorder() {
                return getCurrentTheme().getUnitSelectorFocusBorder();
            }

            @Override
            public void formatScriptTextArea(RSyntaxTextArea textArea) {
                getCurrentTheme().formatScriptTextArea(textArea);
            }

            @Override
            public String getComponentIconNoseCone() {
                return getCurrentTheme().getComponentIconNoseCone();
            }
            @Override
            public String getComponentIconBodyTube() {
                return getCurrentTheme().getComponentIconBodyTube();
            }
            @Override
            public String getComponentIconTransition() {
                return getCurrentTheme().getComponentIconTransition();
            }
            @Override
            public String getComponentIconTrapezoidFinSet() {
                return getCurrentTheme().getComponentIconTrapezoidFinSet();
            }
            @Override
            public String getComponentIconEllipticalFinSet() {
                return getCurrentTheme().getComponentIconEllipticalFinSet();
            }
            @Override
            public String getComponentIconFreeformFinSet() {
                return getCurrentTheme().getComponentIconFreeformFinSet();
            }
            @Override
            public String getComponentIconTubeFinSet() {
                return getCurrentTheme().getComponentIconTubeFinSet();
            }
            @Override
            public String getComponentIconLaunchLug() {
                return getCurrentTheme().getComponentIconLaunchLug();
            }
            @Override
            public String getComponentIconRailButton() {
                return getCurrentTheme().getComponentIconRailButton();
            }
            @Override
            public String getComponentIconInnerTube() {
                return getCurrentTheme().getComponentIconInnerTube();
            }
            @Override
            public String getComponentIconTubeCoupler() {
                return getCurrentTheme().getComponentIconTubeCoupler();
            }
            @Override
            public String getComponentIconCenteringRing() {
                return getCurrentTheme().getComponentIconCenteringRing();
            }
            @Override
            public String getComponentIconBulkhead() {
                return getCurrentTheme().getComponentIconBulkhead();
            }
            @Override
            public String getComponentIconEngineBlock() {
                return getCurrentTheme().getComponentIconEngineBlock();
            }
            @Override
            public String getComponentIconParachute() {
                return getCurrentTheme().getComponentIconParachute();
            }
            @Override
            public String getComponentIconStreamer() {
                return getCurrentTheme().getComponentIconStreamer();
            }
            @Override
            public String getComponentIconShockCord() {
                return getCurrentTheme().getComponentIconShockCord();
            }
            @Override
            public String getComponentIconMass() {
                return getCurrentTheme().getComponentIconMass();
            }
            @Override
            public String getComponentIconStage() {
                return getCurrentTheme().getComponentIconStage();
            }
            @Override
            public String getComponentIconBoosters() {
                return getCurrentTheme().getComponentIconBoosters();
            }
            @Override
            public String getComponentIconPods() {
                return getCurrentTheme().getComponentIconPods();
            }
            @Override
            public String getComponentIconMassAltimeter() {
                return getCurrentTheme().getComponentIconMassAltimeter();
            }
            @Override
            public String getComponentIconMassBattery() {
                return getCurrentTheme().getComponentIconMassBattery();
            }
            @Override
            public String getComponentIconMassDeploymentCharge() {
                return getCurrentTheme().getComponentIconMassDeploymentCharge();
            }
            @Override
            public String getComponentIconMassPayload() {
                return getCurrentTheme().getComponentIconMassPayload();
            }
            @Override
            public String getComponentIconMassFlightComp() {
                return getCurrentTheme().getComponentIconMassFlightComp();
            }
            @Override
            public String getComponentIconMassRecoveryHardware() {
                return getCurrentTheme().getComponentIconMassRecoveryHardware();
            }
            @Override
            public String getComponentIconMassTracker() {
                return getCurrentTheme().getComponentIconMassTracker();
            }
        };

		@Override
		public Color getStatusColor(Status status) {
			switch (status) {
			case ABORTED:
            case CANT_RUN:
				return getErrorColor();
				
			case OUTDATED:
				return getWarningColor();
				
			default:
				return getTextColor();
			}
		}
    }

    public static Color getColor(String key) {
        return themedColor(key, defaultColorForKey(key));
    }

    public static Color getColor(String key, Color fallback) {
        return themedColor(key, fallback != null ? fallback : defaultColorForKey(key));
    }

    public static String getString(String key) {
        return themedString(key, defaultStringForKey(key));
    }

    public static String getString(String key, String fallback) {
        return themedString(key, fallback != null ? fallback : defaultStringForKey(key));
    }

    public static LineStyle getDefaultLineStyle(Class<? extends RocketComponent> c) {
        String key = Keys.DEFAULT_LINE_STYLE_GENERIC;
        if (MassObject.class.isAssignableFrom(c)) {
            key = Keys.DEFAULT_LINE_STYLE_MASS_OBJECT;
        }

        String uiValue = UIManager.getString(key);
        if (uiValue != null) {
            try {
                return LineStyle.valueOf(uiValue);
            } catch (IllegalArgumentException ignore) {
                // fall back below
            }
        }
        return Application.getPreferences().getDefaultLineStyle(c);
    }

    public static Color getStatusColor(Status status) {
        switch (status) {
            case ABORTED:
            case CANT_RUN:
                return getColor(Keys.ERROR);
            case OUTDATED:
                return getColor(Keys.WARNING);
            default:
                return getColor(Keys.TEXT);
        }
    }

    private static Color themedColor(String key, Color fallback) {
        Color color = UIManager.getColor(key);
        return color != null ? color : fallback;
    }

    private static String themedString(String key, String fallback) {
        String value = UIManager.getString(key);
        return value != null ? value : fallback;
    }

    private static Color defaultColorForKey(String key) {
        switch (key) {
            case Keys.BACKGROUND: return Color.WHITE;
            case Keys.BORDER: return Color.BLACK;
            case Keys.TEXT: return Color.BLACK;
            case Keys.TEXT_DIM: return Color.GRAY;
            case Keys.TEXT_DISABLED: return Color.GRAY;
            case Keys.TEXT_SELECTION_FOREGROUND: return UIManager.getColor("Tree.selectionForeground");
            case Keys.TEXT_SELECTION_BACKGROUND: return UIManager.getColor("Tree.selectionBackground");
            case Keys.INFO: return new Color(45, 45, 189);
            case Keys.WARNING: return new Color(217, 152, 0);
            case Keys.ERROR: return Color.RED;
            case Keys.DARK_ERROR: return new Color(200, 0, 0);
            case Keys.ROW_LIGHTER: return Color.WHITE;
            case Keys.ROW_DARKER: return new Color(245, 245, 245);
            case Keys.TABLE_FLASH: return new Color(255, 255, 204);
            case Keys.TABLE_SELECTION: return new Color(217, 233, 255);
            case Keys.FLIGHTDATA_TEXT_ACTIVE: return new Color(0, 0, 127);
            case Keys.FLIGHTDATA_TEXT_INACTIVE: return new Color(0, 0, 127, 127);
            case Keys.MULTI_COMP_EDIT: return new Color(170, 0, 100);
            case Keys.MOTOR_BORDER: return new Color(0, 0, 0, 200);
            case Keys.MOTOR_FILL: return new Color(0, 0, 0, 100);
            case Keys.CG: return Color.BLUE;
            case Keys.CP: return Color.RED;
            case Keys.URL: return Color.BLUE;
            case Keys.COMPONENT_TREE_BACKGROUND: return UIManager.getColor("Tree.textBackground");
            case Keys.COMPONENT_TREE_FOREGROUND: return UIManager.getColor("Tree.textForeground");
            case Keys.VISIBILITY_HIDDEN_FOREGROUND: return UIManager.getColor("Tree.textForeground.hidden.light");
            case Keys.FIN_GRID_MAJOR: return new Color(0, 0, 255, 80);
            case Keys.FIN_GRID_MINOR: return new Color(0, 0, 255, 30);
            case Keys.FIN_POINT: return new Color(200, 0, 0, 255);
            case Keys.FIN_SELECTED_POINT: return new Color(200, 0, 0, 255);
            case Keys.FIN_BODY_LINE: return Color.BLACK;
            case Keys.FIN_SNAP_HIGHLIGHT: return Color.RED;
            default: return null;
        }
    }

    private static String defaultStringForKey(String key) {
        switch (key) {
            case Keys.DEFAULT_BODY_COMPONENT_COLOR: return "0,0,240";
            case Keys.DEFAULT_TUBE_FIN_SET_COLOR: return "0,0,200";
            case Keys.DEFAULT_FIN_SET_COLOR: return "0,0,200";
            case Keys.DEFAULT_LAUNCH_LUG_COLOR: return "0,0,180";
            case Keys.DEFAULT_RAIL_BUTTON_COLOR: return "0,0,180";
            case Keys.DEFAULT_INTERNAL_COMPONENT_COLOR: return "170,0,100";
            case Keys.DEFAULT_MASS_OBJECT_COLOR: return "0,0,0";
            case Keys.DEFAULT_RECOVERY_DEVICE_COLOR: return "255,0,0";
            case Keys.DEFAULT_POD_SET_COLOR: return "160,160,215";
            case Keys.DEFAULT_PARALLEL_STAGE_COLOR: return "198,163,184";
            case Keys.DEFAULT_LINE_STYLE_GENERIC: return "SOLID";
            case Keys.DEFAULT_LINE_STYLE_MASS_OBJECT: return "DASHED";
            default: return null;
        }
    }

    
    private static void preApplyTheme() {
        FlatAnimatedLafChange.showSnapshot();
        FlatLaf.registerCustomDefaultsSource("themes");
    }

    private static void postApplyTheme(Theme theme) {
        final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

        // Clear custom default font when switching to non-FlatLaf LaF
        //if (!(UIManager.getLookAndFeel() instanceof FlatLaf)) {
        //    UIManager.put("defaultFont", null);
        //}

        // Set the UI scale factor
        String uiScale = String.valueOf(((SwingPreferences) Application.getPreferences()).getUIScale());
        log.info("Setting UI scale factor to {}", uiScale);
        System.setProperty("flatlaf.uiScale", uiScale);

        // Load custom fonts
        log.info("Loading custom fonts");
        GUIUtil.loadCustomFonts();

        // Set the global font to
        int fontSize = prefs.getUIFontSize();
        String fontStyle = prefs.getUIFontStyle();
        double fontTracking = prefs.getUIFontTracking();
        log.info("Setting global font to {} {} {}", fontSize, fontStyle, fontTracking);
        setGlobalFont(fontStyle, fontSize, (float) fontTracking);

        // After applying the theme settings, notify listeners
        Theme.notifyUIThemeChangeListeners();

        // Update all components
        FlatLaf.updateUI();
        FlatAnimatedLafChange.hideSnapshotWithAnimation();
    }

    private static void commonApplyThemeToRootPane(JRootPane rootPane, Color TextColor) {
        // Check out https://www.formdev.com/flatlaf/client-properties
        rootPane.putClientProperty("JRootPane.titleBarBackground", rootPane.getBackground());
        rootPane.putClientProperty("JRootPane.titleBarForeground", TextColor);
        if (SystemInfo.getPlatform() == SystemInfo.Platform.MAC_OS) {
            if (com.formdev.flatlaf.util.SystemInfo.isMacFullWindowContentSupported) {
                // Remove the separator line from the title bar
                rootPane.putClientProperty("apple.awt.transparentTitleBar", true);
                rootPane.putClientProperty("apple.awt.fullscreenable", true);
            }
        }
    }

    private static void setGlobalFont(String fontStyle, int size, float letterSpacing) {
        // Iterate over all keys in the UIManager defaults and set the font
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font) {
                float offset = 0.0f;
                // Check if this key has a size offset
                if (key.toString().endsWith(".font")) {
                    String fontKey = key.toString();
                    // Reuse the existing fontOffsets map logic here
                    offset = fontOffsets.getOrDefault(fontKey, 0.0f);
                }
                // Create a font with the letter spacing attribute
                Map<TextAttribute, Object> attributes = new HashMap<>();
                attributes.put(TextAttribute.FAMILY, fontStyle);
                attributes.put(TextAttribute.SIZE, size + offset);
                attributes.put(TextAttribute.TRACKING, letterSpacing);

                Font newFont = Font.getFont(attributes);
                UIManager.put(key, newFont);
            }
        }

        // Set the default font
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.FAMILY, fontStyle);
        attributes.put(TextAttribute.SIZE, size);
        attributes.put(TextAttribute.TRACKING, letterSpacing);
        UIManager.put("defaultFont", Font.getFont(attributes));
    }
}
