package net.sf.openrocket.gui.widgets;

import javax.swing.JToggleButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

/**
 * This class is a replacement for the standard JToggleButton. Its purpose is to be able
 * to control the foreground color for when the button is (de)selected.
 *
 * @author Sibo Van Gool <sibo.vangool@hotmail.com>
 */
public class SelectColorToggleButton extends JToggleButton {
    public SelectColorToggleButton(Action a) {
        super(a);
        addChangeListenerSelectColor();
    }

    public SelectColorToggleButton(String text) {
        super(text);
        addChangeListenerSelectColor();
    }

    public SelectColorToggleButton() {
        addChangeListenerSelectColor();
    }

    public SelectColorToggleButton(Icon icon) {
        super(icon);
        addChangeListenerSelectColor();
    }

    public SelectColorToggleButton(Icon icon, boolean selected) {
        super(icon, selected);
        addChangeListenerSelectColor();
    }

    public SelectColorToggleButton(String text, boolean selected) {
        super(text, selected);
        addChangeListenerSelectColor();
    }

    public SelectColorToggleButton(String text, Icon icon) {
        super(text, icon);
        addChangeListenerSelectColor();
    }

    public SelectColorToggleButton(String text, Icon icon, boolean selected) {
        super(text, icon, selected);
        addChangeListenerSelectColor();
    }

    private void addChangeListenerSelectColor() {
        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (UIManager.getColor("ToggleButton.selectForeground") == null
                        || UIManager.getColor("ToggleButton.foreground") == null)
                    return;
                if (isSelected()) {
                    setForeground(UIManager.getColor("ToggleButton.selectForeground"));
                }
                else {
                    setForeground(UIManager.getColor("ToggleButton.foreground"));
                }
            }
        });
    }
}
