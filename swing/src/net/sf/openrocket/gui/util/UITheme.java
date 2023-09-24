package net.sf.openrocket.gui.util;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;
import com.github.weisj.darklaf.theme.OneDarkTheme;
import com.jthemedetecor.OsThemeDetector;
import net.sf.openrocket.l10n.Translator;
import net.sf.openrocket.startup.Application;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class UITheme {
    private static final Translator trans = Application.getTranslator();
    private static final Logger log = LoggerFactory.getLogger(UITheme.class);

    public interface Theme {
        void applyTheme();
        String name(); // Provided by enum, gives the name of the enum constant
        String getDisplayName();
        Color getBackgroundColor();
        Color getBorderColor();
        Color getTextColor();
        Color getDimTextColor();
        Color getTextSelectionForegroundColor();
        Color getTextSelectionBackgroundColor();
        Color getWarningColor();
        Color getDarkWarningColor();
        Color getRowBackgroundLighterColor();
        Color getRowBackgroundDarkerColor();
        Color getFlightDataTextActiveColor();
        Color getFlightDataTextInactiveColor();

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

        void formatScriptTextArea(RSyntaxTextArea textArea);
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
                final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

                GUIUtil.setBestLAF();
                setGlobalFontSize(prefs.getUIFontSize());
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
            public Color getTextSelectionForegroundColor() {
                return UIManager.getColor("Tree.selectionForeground");
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return UIManager.getColor("Tree.selectionBackground");
            }

            @Override
            public Color getWarningColor() {
            	return Color.RED;
            }

            @Override
            public Color getDarkWarningColor() {
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
                return null;
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
        },
        /*
        Dark theme
         */
        DARK {
            private final String displayName = trans.get("UITheme.Dark");

            @Override
            public void applyTheme() {
                final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

                LafManager.install(new DarculaTheme());
                setGlobalFontSize(prefs.getUIFontSize());
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
                return UIManager.getColor("Label.foreground");
            }

            @Override
            public Color getDimTextColor() {
                return new Color(162, 162, 162);
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
            public Color getWarningColor() {
                return new Color(246, 143, 143);
            }

            @Override
            public Color getDarkWarningColor() {
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
                return BorderFactory.createLineBorder(getBorderColor());
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
        },
        /*
        High-contrast dark theme
         */
        DARK_CONTRAST {
            private final String displayName = trans.get("UITheme.DarkContrast");

            @Override
            public void applyTheme() {
                final SwingPreferences prefs = (SwingPreferences) Application.getPreferences();

                LafManager.install(new OneDarkTheme());
                setGlobalFontSize(prefs.getUIFontSize());
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
                return new Color(189, 189, 189);
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
            public Color getWarningColor() {
                return new Color(255, 173, 173);
            }

            @Override
            public Color getDarkWarningColor() {
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
                return new Color(173, 206, 255);
            }

            @Override
            public Color getFlightDataTextInactiveColor() {
                return new Color(149, 186, 255, 127);
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
                return BorderFactory.createLineBorder(getBorderColor());
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
                        return Themes.DARK;
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
            public Color getTextSelectionForegroundColor() {
                return getCurrentTheme().getTextSelectionForegroundColor();
            }

            @Override
            public Color getTextSelectionBackgroundColor() {
                return getCurrentTheme().getTextSelectionBackgroundColor();
            }

            @Override
            public Color getWarningColor() {
                return getCurrentTheme().getWarningColor();
            }

            @Override
            public Color getDarkWarningColor() {
                return getCurrentTheme().getDarkWarningColor();
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
            public void formatScriptTextArea(RSyntaxTextArea textArea) {
                getCurrentTheme().formatScriptTextArea(textArea);
            }
        }
    }

    private static void setGlobalFontSize(int size) {
        // Some fonts have different sizes for different components, so we need to adjust them
        final Map<String, Float> fontOffsets = new HashMap<>();
        fontOffsets.put("MenuBar.font", 1f);
        fontOffsets.put("Tree.font", -1f);
        fontOffsets.put("Slider.font", -2f);
        fontOffsets.put("TableHeader.font", -2f);
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
            if (value instanceof Font) {
                Font newFont = (Font) value;
                float offset = fontOffsets.getOrDefault(key.toString(), 0f);
                newFont = newFont.deriveFont(Integer.valueOf(size).floatValue() + offset);
                UIManager.put(key, newFont);
            }
        }
    }

}
