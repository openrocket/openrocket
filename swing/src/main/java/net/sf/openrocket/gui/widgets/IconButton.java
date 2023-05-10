package net.sf.openrocket.gui.widgets;

import net.sf.openrocket.gui.util.Icons;

import javax.swing.Action;
import javax.swing.Icon;

/**
 * Button specifically for displaying an icon.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class IconButton extends SelectColorButton {
    private static final int ICON_GAP = 10;
    private static final double ICON_SCALE = 0.9;

    public IconButton() {
        setIconTextGap(ICON_GAP);
    }

    public IconButton(Icon icon) {
        super(icon);
        setIconTextGap(ICON_GAP);
    }

    public IconButton(String text) {
        super(text);
        setIconTextGap(ICON_GAP);
    }

    public IconButton(Action a) {
        super(a);
        setIconTextGap(ICON_GAP);
    }

    public IconButton(String text, Icon icon) {
        super(text, icon);
        setIconTextGap(ICON_GAP);
    }

    @Override
    public void setIcon(Icon defaultIcon) {
        super.setIcon(defaultIcon);
        // There is a bug where the normal override of the pressed icon does not work, so we have to assign it here.
        setPressedIcon(Icons.getScaledIcon(defaultIcon, ICON_SCALE));
    }

    @Override
    public Icon getIcon() {
        return Icons.getScaledIcon(super.getIcon(), IconButton.ICON_SCALE);
    }

    @Override
    public Icon getSelectedIcon() {
        return Icons.getScaledIcon(super.getSelectedIcon(), IconButton.ICON_SCALE);
    }

    @Override
    public Icon getDisabledIcon() {
        return Icons.getScaledIcon(super.getDisabledIcon(), IconButton.ICON_SCALE);
    }

    @Override
    public Icon getDisabledSelectedIcon() {
        return Icons.getScaledIcon(super.getDisabledSelectedIcon(), IconButton.ICON_SCALE);
    }

    @Override
    public Icon getRolloverIcon() {
        return Icons.getScaledIcon(super.getRolloverIcon(), IconButton.ICON_SCALE);
    }

    @Override
    public Icon getRolloverSelectedIcon() {
        return Icons.getScaledIcon(super.getRolloverSelectedIcon(), IconButton.ICON_SCALE);
    }
}
