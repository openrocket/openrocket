package net.sf.openrocket.gui.widgets;

import javax.swing.Action;
import javax.swing.Icon;

/**
 * Button specifically for displaying an icon.
 */
public class IconButton extends SelectColorButton {
    private static final int ICON_GAP = 10;
    public static final double ICON_SCALE = 0.8;

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

}
