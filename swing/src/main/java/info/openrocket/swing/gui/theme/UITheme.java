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
import info.openrocket.core.l10n.Translator;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.util.Icons;
import info.openrocket.swing.gui.util.SwingPreferences;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UITheme {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(UITheme.class);


    // TODO: replace a bunch of this with the FlatLaf properties files, see https://www.formdev.com/flatlaf/properties-files

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

        Color getFinPointGridMajorLineColor();
        Color getFinPointGridMinorLineColor();
        Color getFinPointPointColor();
        Color getFinPointSelectedPointColor();
        Color getFinPointBodyLineColor();

        Icon getMassOverrideIcon();
        Icon getMassOverrideSubcomponentIcon();
        Icon getCGOverrideIcon();
        Icon getCGOverrideSubcomponentIcon();
        Icon getCDOverrideIcon();
        Icon getCDOverrideSubcomponentIcon();

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

        // Static list of listeners
        static List<Runnable> themeChangeListeners = new ArrayList<>();

        // Static method to add a listener
        static void addUIThemeChangeListener(Runnable listener) {
            // TODO: implement this once you have implemented invalidation for each listener so that we don't get memory leaks
            //themeChangeListeners.add(listener);
        }

        // Static method to remove a listener
        static void removeUIThemeChangeListener(Runnable listener) {
            themeChangeListeners.remove(listener);
        }

        // Static method to notify all listeners
        static void notifyUIThemeChangeListeners() {
            for (Runnable listener : themeChangeListeners) {
                listener.run();
            }
        }
    }

    public static boolean isLightTheme(Theme theme) {
        if (theme == Themes.DARK) {
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
                return Color.WHITE;
            }

            @Override
            public Color getBorderColor() {
                return Color.BLACK;
            }

            @Override
            public Color getTextColor() {
                return Color.BLACK;
            }

            @Override
            public Color getDimTextColor() {
                return Color.GRAY;
            }

            @Override
            public Color getDisabledTextColor() {
                return getDimTextColor();
            }

            @Override
            public Color getTextSelectionForegroundColor() {
                return UIManager.getColor("Tree.selectionForeground");
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return UIManager.getColor("Tree.selectionBackground");
            }

            @Override
            public Color getInformationColor() {
                return new Color(45, 45, 189);
            }

            @Override
            public Color getWarningColor() {
                return new Color(217, 152, 0);
            }

            @Override
            public Color getErrorColor() {
                return Color.RED;
            }

            @Override
            public Color getDarkErrorColor() {
            	return new Color(200,0,0);
            }

            @Override
            public Color getRowBackgroundLighterColor() {
                return Color.WHITE;
            }

            @Override
            public Color getRowBackgroundDarkerColor() {
                return new Color(245, 245, 245);
            }

            @Override
            public Color getFlightDataTextActiveColor() {
            	return new Color(0,0,127);
            }

            @Override
            public Color getFlightDataTextInactiveColor() {
            	return new Color(0,0,127,127);
            }

            @Override
            public Color getMultiCompEditColor() {
                return new Color(170, 0, 100);
            }

            @Override
            public String getDefaultBodyComponentColor() {
                return "0,0,240";
            }
            @Override
            public String getDefaultTubeFinSetColor() {
                return "0,0,200";
            }
            @Override
            public String getDefaultFinSetColor() {
                return "0,0,200";
            }
            @Override
            public String getDefaultLaunchLugColor() {
                return "0,0,180";
            }
            @Override
            public String getDefaultRailButtonColor() {
                return "0,0,180";
            }
            @Override
            public String getDefaultInternalComponentColor() {
                return "170,0,100";
            }
            @Override
            public String getDefaultMassObjectColor() {
                return "0,0,0";
            }
            @Override
            public String getDefaultRecoveryDeviceColor() {
                return "255,0,0";
            }
            @Override
            public String getDefaultPodSetColor() {
                return "160,160,215";
            }
            @Override
            public String getDefaultParallelStageColor() {
                return "198,163,184";
            }

            @Override
            public Color getMotorBorderColor() {
                return new Color(0, 0, 0, 200);
            }

            @Override
            public Color getMotorFillColor() {
                return new Color(0, 0, 0, 100);
            }

            @Override
            public Color getCGColor() {
                return Color.BLUE;
            }

            @Override
            public Color getCPColor() {
                return Color.RED;
            }

            @Override
            public Color getURLColor() {
                return Color.BLUE;
            }

            @Override
            public Color getComponentTreeBackgroundColor() {
                return UIManager.getColor("Tree.textBackground");
            }

            @Override
            public Color getComponentTreeForegroundColor() {
                return UIManager.getColor("Tree.textForeground");
            }

            @Override
            public Color getFinPointGridMajorLineColor() {
                return new Color( 0, 0, 255, 80);
            }

            @Override
            public Color getFinPointGridMinorLineColor() {
                return new Color( 0, 0, 255, 30);
            }

            @Override
            public Color getFinPointPointColor() {
                return new Color(200, 0, 0, 255);
            }

            @Override
            public Color getFinPointSelectedPointColor() {
                return new Color(200, 0, 0, 255);
            }

            @Override
            public Color getFinPointBodyLineColor() {
                return Color.BLACK;
            }

            @Override
            public Icon getMassOverrideIcon() {
                return Icons.MASS_OVERRIDE_LIGHT;
            }

            @Override
            public Icon getMassOverrideSubcomponentIcon() {
                return Icons.MASS_OVERRIDE_SUBCOMPONENT_LIGHT;
            }

            @Override
            public Icon getCGOverrideIcon() {
                return Icons.CG_OVERRIDE_LIGHT;
            }

            @Override
            public Icon getCGOverrideSubcomponentIcon() {
                return Icons.CG_OVERRIDE_SUBCOMPONENT_LIGHT;
            }

            @Override
            public Icon getCDOverrideIcon() {
                return Icons.CD_OVERRIDE_LIGHT;
            }

            @Override
            public Icon getCDOverrideSubcomponentIcon() {
                return Icons.CD_OVERRIDE_SUBCOMPONENT_LIGHT;
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
                        new LineBorder(new Color(0f, 0f, 0f, 0.08f), 1),
                        new EmptyBorder(1, 1, 1, 1));
            }

            @Override
            public Border getUnitSelectorFocusBorder() {
                return new CompoundBorder(
                        new LineBorder(new Color(0f, 0f, 0f, 0.6f)),
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
                return new Color(73, 76, 79);
            }

            @Override
            public Color getBorderColor() {
                return new Color(97, 99, 101);
            }

            @Override
            public Color getTextColor() {
                return new Color(173, 173, 173);
            }

            @Override
            public Color getDimTextColor() {
                return new Color(182, 182, 182);
            }

            @Override
            public Color getDisabledTextColor() {
                return new Color(161, 161, 161);
            }

            @Override
            public Color getTextSelectionForegroundColor() {
                return Color.WHITE;
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return new Color(75, 110, 175);
            }

            @Override
            public Color getInformationColor() {
                return new Color(208, 208, 255);
            }

            @Override
            public Color getWarningColor() {
                return new Color(255, 224, 166);
            }

            @Override
            public Color getErrorColor() {
                return new Color(246, 143, 143);
            }

            @Override
            public Color getDarkErrorColor() {
            	return new Color(229, 103, 103);
            }

            @Override
            public Color getRowBackgroundLighterColor() {
                return new Color(65, 69, 71);
            }

            @Override
            public Color getRowBackgroundDarkerColor() {
                return new Color(60, 63, 65);
            }

            @Override
            public Color getFlightDataTextActiveColor() {
                return new Color(145, 183, 231);
            }

            @Override
            public Color getFlightDataTextInactiveColor() {
                return new Color(128, 166, 230, 127);
            }

            @Override
            public Color getMultiCompEditColor() {
                return new Color(222, 146, 176);
            }

            @Override
            public String getDefaultBodyComponentColor() {
                return "150,162,255";
            }
            @Override
            public String getDefaultTubeFinSetColor() {
                return "150,178,255";
            }
            @Override
            public String getDefaultFinSetColor() {
                return "150,178,255";
            }
            @Override
            public String getDefaultLaunchLugColor() {
                return "142,153,238";
            }
            @Override
            public String getDefaultRailButtonColor() {
                return "142,153,238";
            }
            @Override
            public String getDefaultInternalComponentColor() {
                return "181,128,151";
            }
            @Override
            public String getDefaultMassObjectColor() {
                return "210,210,210";
            }
            @Override
            public String getDefaultRecoveryDeviceColor() {
                return "220,90,90";
            }
            @Override
            public String getDefaultPodSetColor() {
                return "190,190,235";
            }
            @Override
            public String getDefaultParallelStageColor() {
                return "210,180,195";
            }

            @Override
            public Color getMotorBorderColor() {
                return new Color(0, 0, 0, 100);
            }

            @Override
            public Color getMotorFillColor() {
                return new Color(0, 0, 0, 50);
            }

            @Override
            public Color getCGColor() {
                return new Color(85, 133, 253);
            }

            @Override
            public Color getCPColor() {
                return new Color(255, 72, 106);
            }

            @Override
            public Color getURLColor() {
                return new Color(150, 167, 255);
            }

            @Override
            public Color getComponentTreeBackgroundColor() {
                return getBackgroundColor();
            }

            @Override
            public Color getComponentTreeForegroundColor() {
                return getTextColor();
            }

            @Override
            public Color getFinPointGridMajorLineColor() {
                return new Color(135, 135, 199, 197);
            }

            @Override
            public Color getFinPointGridMinorLineColor() {
                return new Color(121, 121, 189, 69);
            }

            @Override
            public Color getFinPointPointColor() {
                return new Color(217, 108, 108, 255);
            }

            @Override
            public Color getFinPointSelectedPointColor() {
                return new Color(232, 78, 78, 255);
            }

            @Override
            public Color getFinPointBodyLineColor() {
                return Color.WHITE;
            }

            @Override
            public Icon getMassOverrideIcon() {
                return Icons.MASS_OVERRIDE_DARK;
            }

            @Override
            public Icon getMassOverrideSubcomponentIcon() {
                return Icons.MASS_OVERRIDE_SUBCOMPONENT_DARK;
            }

            @Override
            public Icon getCGOverrideIcon() {
                return Icons.CG_OVERRIDE_DARK;
            }

            @Override
            public Icon getCGOverrideSubcomponentIcon() {
                return Icons.CG_OVERRIDE_SUBCOMPONENT_DARK;
            }

            @Override
            public Icon getCDOverrideIcon() {
                return Icons.CD_OVERRIDE_DARK;
            }

            @Override
            public Icon getCDOverrideSubcomponentIcon() {
                return Icons.CD_OVERRIDE_SUBCOMPONENT_DARK;
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
                        new LineBorder(new Color(1f, 1f, 1f, 0.08f), 1),
                        new EmptyBorder(1, 1, 1, 1));
            }

            @Override
            public Border getUnitSelectorFocusBorder() {
                return new CompoundBorder(
                        new LineBorder(new Color(1f, 1f, 1f, 0.6f)),
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
                return new Color(43, 45, 51);
            }

            @Override
            public Color getBorderColor() {
                return new Color(163, 163, 163, 204);
            }

            @Override
            public Color getTextColor() {
                return UIManager.getColor("Label.foreground");
            }

            @Override
            public Color getDimTextColor() {
                return new Color(165, 171, 184);
            }

            @Override
            public Color getDisabledTextColor() {
                return new Color(128, 128, 128);
            }


            @Override
            public Color getTextSelectionForegroundColor() {
                return Color.WHITE;
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return new Color(62, 108, 173);
            }

            @Override
            public Color getInformationColor() {
                return new Color(197, 197, 252);
            }

            @Override
            public Color getWarningColor() {
                return new Color(255, 233, 187);
            }

            @Override
            public Color getErrorColor() {
                return new Color(255, 173, 173);
            }

            @Override
            public Color getDarkErrorColor() {
                return new Color(255, 178, 178);
            }

            @Override
            public Color getRowBackgroundLighterColor() {
                return new Color(43, 49, 58);
            }

            @Override
            public Color getRowBackgroundDarkerColor() {
                return new Color(34, 37, 44);
            }

            @Override
            public Color getFlightDataTextActiveColor() {
                return new Color(212, 230, 255);
            }

            @Override
            public Color getFlightDataTextInactiveColor() {
                return new Color(170, 201, 255, 127);
            }

            @Override
            public Color getMultiCompEditColor() {
                return new Color(255, 165, 200);
            }

            @Override
            public String getDefaultBodyComponentColor() {
                return "150,175,255";
            }
            @Override
            public String getDefaultTubeFinSetColor() {
                return "150,184,254";
            }
            @Override
            public String getDefaultFinSetColor() {
                return "150,184,255";
            }
            @Override
            public String getDefaultLaunchLugColor() {
                return "142,153,238";
            }
            @Override
            public String getDefaultRailButtonColor() {
                return "142,153,238";
            }
            @Override
            public String getDefaultInternalComponentColor() {
                return "181,128,151";
            }
            @Override
            public String getDefaultMassObjectColor() {
                return "210,210,210";
            }
            @Override
            public String getDefaultRecoveryDeviceColor() {
                return "220,90,90";
            }
            @Override
            public String getDefaultPodSetColor() {
                return "190,190,235";
            }
            @Override
            public String getDefaultParallelStageColor() {
                return "210,180,195";
            }

            @Override
            public Color getMotorBorderColor() {
                return new Color(255, 255, 255, 200);
            }

            @Override
            public Color getMotorFillColor() {
                return new Color(0, 0, 0, 70);
            }

            @Override
            public Color getCGColor() {
                return new Color(85, 133, 253);
            }

            @Override
            public Color getCPColor() {
                return new Color(255, 72, 106);
            }

            @Override
            public Color getURLColor() {
                return new Color(171, 185, 255);
            }

            @Override
            public Color getComponentTreeBackgroundColor() {
                return getBackgroundColor();
            }

            @Override
            public Color getComponentTreeForegroundColor() {
                return getTextColor();
            }

            @Override
            public Color getFinPointGridMajorLineColor() {
                return new Color(164, 164, 224, 197);
            }

            @Override
            public Color getFinPointGridMinorLineColor() {
                return new Color(134, 134, 201, 69);
            }

            @Override
            public Color getFinPointPointColor() {
                return new Color(242, 121, 121, 255);
            }

            @Override
            public Color getFinPointSelectedPointColor() {
                return new Color(232, 78, 78, 255);
            }

            @Override
            public Color getFinPointBodyLineColor() {
                return Color.WHITE;
            }

            @Override
            public Icon getMassOverrideIcon() {
                return Icons.MASS_OVERRIDE_DARK;
            }

            @Override
            public Icon getMassOverrideSubcomponentIcon() {
                return Icons.MASS_OVERRIDE_SUBCOMPONENT_DARK;
            }

            @Override
            public Icon getCGOverrideIcon() {
                return Icons.CG_OVERRIDE_DARK;
            }

            @Override
            public Icon getCGOverrideSubcomponentIcon() {
                return Icons.CG_OVERRIDE_SUBCOMPONENT_DARK;
            }

            @Override
            public Icon getCDOverrideIcon() {
                return Icons.CD_OVERRIDE_DARK;
            }

            @Override
            public Icon getCDOverrideSubcomponentIcon() {
                return Icons.CD_OVERRIDE_SUBCOMPONENT_DARK;
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
                        new LineBorder(new Color(.9f, 0.9f, 0.9f, 0.15f), 1),
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
            public Icon getMassOverrideIcon() {
                return getCurrentTheme().getMassOverrideIcon();
            }

            @Override
            public Icon getMassOverrideSubcomponentIcon() {
                return getCurrentTheme().getMassOverrideSubcomponentIcon();
            }

            @Override
            public Icon getCGOverrideIcon() {
                return getCurrentTheme().getCGOverrideIcon();
            }

            @Override
            public Icon getCGOverrideSubcomponentIcon() {
                return getCurrentTheme().getCGOverrideSubcomponentIcon();
            }

            @Override
            public Icon getCDOverrideIcon() {
                return getCurrentTheme().getCDOverrideIcon();
            }

            @Override
            public Icon getCDOverrideSubcomponentIcon() {
                return getCurrentTheme().getCDOverrideSubcomponentIcon();
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
        }
    }

    private static void preApplyTheme() {
        FlatAnimatedLafChange.showSnapshot();

        FlatLaf.registerCustomDefaultsSource("themes");
    }

    private static void postApplyTheme(Theme theme) {
        final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

        // TODO: For some reason, FlatLaf does not take the correct values from the properties file
        UIManager.put("OR.ScrollPane.borderColor", theme.getBorderColor());

        // Clear custom default font when switching to non-FlatLaf LaF
        if (!(UIManager.getLookAndFeel() instanceof FlatLaf)) {
            UIManager.put("defaultFont", null);
        }

        setGlobalFontSize(prefs.getUIFontSize());

        System.setProperty("flatlaf.uiScale.enabled", "true");
        System.setProperty("flatlaf.uiScale", String.valueOf(prefs.getUIFontSize() / 12));

        // After applying the theme settings, notify listeners
        Theme.notifyUIThemeChangeListeners();

        // Update all components
        FlatLaf.updateUI();     // TODO: has no effect (UI doesn't change)
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

    private static void setGlobalFontSize(int size) {
        // Some fonts have different sizes for different components, so we need to adjust them
        final Map<String, Float> fontOffsets = new HashMap<>();
        fontOffsets.put("MenuBar.font", 1f);
        fontOffsets.put("Tree.font", -1f);
        fontOffsets.put("Slider.font", -2f);
        fontOffsets.put("TableHeader.font", -1f);
        fontOffsets.put("ColorChooser.font", -1f);
        fontOffsets.put("Menu.acceleratorFont", 1f);
        fontOffsets.put("InternalFrame.optionDialogTitleFont", 1f);
        fontOffsets.put("InternalFrame.paletteTitleFont", 1f);
        fontOffsets.put("MenuItem.font", 1f);
        fontOffsets.put("PopupMenu.font", 1f);
        fontOffsets.put("MenuItem.acceleratorFont", 1f);
        fontOffsets.put("RadioButtonMenuItem.font", 1f);
        fontOffsets.put("Table.font", -1f);
        //fontOffsets.put("IconButton.font", -2f);      // The default doesn't really look nice, we want the normal font size instead
        fontOffsets.put("InternalFrame.titleFont", 1f);
        fontOffsets.put("List.font", -1f);
        fontOffsets.put("RadioButtonMenuItem.acceleratorFont", 1f);
        fontOffsets.put("CheckBoxMenuItem.acceleratorFont", 1f);
        fontOffsets.put("Menu.font", 1f);
        fontOffsets.put("TabbedPane.smallFont", -2f);
        fontOffsets.put("CheckBoxMenuItem.font", 1f);
        fontOffsets.put("ToolTip.font", -2f);

        // Iterate over all keys in the UIManager defaults and set the font
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements();) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof Font newFont) {
				float offset = fontOffsets.getOrDefault(key.toString(), 0f);
                newFont = newFont.deriveFont(Integer.valueOf(size).floatValue() + offset);
                UIManager.put(key, newFont);
            }
        }
    }

}
