package net.sf.openrocket.gui.widgets;

import net.sf.openrocket.gui.util.Icons;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.Insets;

/**
 * Toggle button specifically for displaying an icon.
 * The button does not have any borders and only displays the icon.
 * When setting the selected icon, the button will automatically generate a version of that icon
 * for the unselected state (a greyed out version).
 * You can also set the scale of the icon using the ICON_SCALE variable.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class IconToggleButton extends SelectColorToggleButton {
    private double ICON_SCALE = 1;

    public IconToggleButton(Action a) {
        super(a);
        initSettings();
    }

    public IconToggleButton(String text) {
        super(text);
        initSettings();
    }

    public IconToggleButton() {
        initSettings();
    }

    public IconToggleButton(Icon icon) {
        super(icon);
        initSettings();
    }

    public IconToggleButton(Icon icon, boolean selected) {
        super(icon, selected);
        initSettings();
    }

    public IconToggleButton(String text, boolean selected) {
        super(text, selected);
        initSettings();
    }

    public IconToggleButton(String text, Icon icon) {
        super(text, icon);
        initSettings();
    }

    public IconToggleButton(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
        initSettings();
    }

    public void setIconScale(double iconScale) {
        ICON_SCALE = iconScale;
    }

    private void initSettings() {
        setBorderPainted(false);
        setBorder(null);
        setFocusable(false);
        setMargin(new Insets(0, 0, 0, 0));
        setContentAreaFilled(false);
    }

    @Override
    public void setSelectedIcon(Icon selectedIcon) {
        super.setSelectedIcon(selectedIcon);
        setUnselectedIcon(selectedIcon);
        // There is a bug where the normal override of the pressed icon does not work, so we have to assign it here.
        setPressedIcon(Icons.getScaledIcon(selectedIcon, ICON_SCALE));
    }

    /**
     * Generates and sets an unselected icon based on the current icon.
     * @param icon the icon of the button when it is selected
     */
    private void setUnselectedIcon(Icon icon) {
        Icon unselectedIcon = UIManager.getLookAndFeel().getDisabledIcon(null, icon);
        setIcon(unselectedIcon);
    }

    @Override
    public Icon getIcon() {
        return Icons.getScaledIcon(super.getIcon(), ICON_SCALE);
    }

    @Override
    public Icon getSelectedIcon() {
        return Icons.getScaledIcon(super.getSelectedIcon(), ICON_SCALE);
    }

    @Override
    public Icon getDisabledIcon() {
        return Icons.getScaledIcon(super.getDisabledIcon(), ICON_SCALE);
    }

    @Override
    public Icon getDisabledSelectedIcon() {
        return Icons.getScaledIcon(super.getDisabledSelectedIcon(), ICON_SCALE);
    }

    @Override
    public Icon getRolloverIcon() {
        return Icons.getScaledIcon(super.getRolloverIcon(), ICON_SCALE);
    }

    @Override
    public Icon getRolloverSelectedIcon() {
        return Icons.getScaledIcon(super.getRolloverSelectedIcon(), ICON_SCALE);
    }
}
