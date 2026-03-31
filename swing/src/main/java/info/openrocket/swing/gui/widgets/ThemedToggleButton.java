package info.openrocket.swing.gui.widgets;

import javax.swing.Icon;
import javax.swing.JToggleButton;
import javax.swing.UIManager;

/**
 * A toggle button that automatically manages icon appearance based on selection state.
 * When selected, the full-color icon is shown. When not selected, a greyed-out version
 * of the icon is generated and displayed automatically.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class ThemedToggleButton extends JToggleButton {

    public ThemedToggleButton() {
        super();
    }

    public ThemedToggleButton(String text) {
        super(text);
    }

    public ThemedToggleButton(String text, Icon coloredIcon) {
        super(text);
        setSelectedIcon(coloredIcon);
    }

    public ThemedToggleButton(Icon coloredIcon) {
        super();
        setSelectedIcon(coloredIcon);
    }

    /**
     * Sets the icon shown when the button is selected, and automatically generates
     * a greyed-out version for the unselected state.
     *
     * @param coloredIcon the full-color icon to show when selected
     */
    @Override
    public void setSelectedIcon(Icon coloredIcon) {
        super.setSelectedIcon(coloredIcon);
        Icon greyIcon = UIManager.getLookAndFeel().getDisabledIcon(this, coloredIcon);
        super.setIcon(greyIcon);
    }
}
