package net.sf.openrocket.gui.widgets;

import javax.swing.JToggleButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.UIManager;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

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

    /**
     * This method sets the foreground color of the button. If the button is selected, then the selectForeground is used.
     * If the frame that the button is in goes out of focus or if the button is unselected, then the foreground is used.
     *
     * This is to fix an issue on OSX devices where the foreground color would be black on blue (hardly readable)
     */
    private void addChangeListenerSelectColor() {
        if (UIManager.getColor("ToggleButton.selectForeground") == null
                || UIManager.getColor("ToggleButton.foreground") == null)
            return;

        // Fixes the issue of the background of the button not being blue when selected on macOS
        putClientProperty("JButton.buttonType", "segmented-only");

        // Case: frame goes out of focus
        addPropertyChangeListener("Frame.active", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if (isSelected() && (boolean)evt.getNewValue()) {
                    setForeground(UIManager.getColor("ToggleButton.selectForeground"));
                }
                else {
                    setForeground(UIManager.getColor("ToggleButton.foreground"));
                }
            }
        });

        // Case: button is clicked
        addPropertyChangeListener("ancestor", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
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
