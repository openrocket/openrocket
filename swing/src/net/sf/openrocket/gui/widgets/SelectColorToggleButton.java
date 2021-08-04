package net.sf.openrocket.gui.widgets;

import javax.swing.JToggleButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;
import java.awt.Graphics;

/**
 * This class is a replacement for the standard JToggleButton. Its purpose is to be able
 * to control the foreground color for when the button is (de)selected.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class SelectColorToggleButton extends JToggleButton {
    public SelectColorToggleButton(Action a) {
        super(a);
    }

    public SelectColorToggleButton(String text) {
        super(text);
    }

    public SelectColorToggleButton() {
    }

    public SelectColorToggleButton(Icon icon) {
        super(icon);
    }

    public SelectColorToggleButton(Icon icon, boolean selected) {
        super(icon, selected);
    }

    public SelectColorToggleButton(String text, boolean selected) {
        super(text, selected);
    }

    public SelectColorToggleButton(String text, Icon icon) {
        super(text, icon);
    }

    public SelectColorToggleButton(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
    }

    @Override
    public void paint(Graphics g) {
        if (isSelected()) {
            setForeground(UIManager.getColor("ToggleButton.selectForeground"));
        }
        else {
            setForeground(UIManager.getColor("ToggleButton.foreground"));
        }
        super.paint(g);
    }
}
