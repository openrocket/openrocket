package info.openrocket.swing.gui.widgets;

import info.openrocket.swing.gui.util.Icons;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;

/**
 * Button specifically for displaying an icon.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class IconButton extends JButton {
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
        setPressedIcon(Icons.deriveScaledIcon(defaultIcon, (float) ICON_SCALE));
    }

    @Override
    public Icon getIcon() {
        return Icons.deriveScaledIcon(super.getIcon(), (float) ICON_SCALE);
    }

    @Override
    public Icon getSelectedIcon() {
        return Icons.deriveScaledIcon(super.getSelectedIcon(), (float) ICON_SCALE);
    }

    @Override
    public Icon getDisabledIcon() {
        return Icons.deriveScaledIcon(super.getDisabledIcon(), (float) ICON_SCALE);
    }

    @Override
    public Icon getDisabledSelectedIcon() {
        return Icons.deriveScaledIcon(super.getDisabledSelectedIcon(), (float) ICON_SCALE);
    }

    @Override
    public Icon getRolloverIcon() {
        return Icons.deriveScaledIcon(super.getRolloverIcon(), (float) ICON_SCALE);
    }

    @Override
    public Icon getRolloverSelectedIcon() {
        return Icons.deriveScaledIcon(super.getRolloverSelectedIcon(), (float) ICON_SCALE);
    }
}
