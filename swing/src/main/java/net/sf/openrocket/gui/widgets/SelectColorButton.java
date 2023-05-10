package net.sf.openrocket.gui.widgets;


import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Graphics;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SelectColorButton extends JButton {
    public SelectColorButton() {
        addChangeListenerSelectColor();
    }

    public SelectColorButton(Icon icon) {
        super(icon);
        addChangeListenerSelectColor();
    }

    public SelectColorButton(String text) {
        super(text);
        addChangeListenerSelectColor();
    }

    public SelectColorButton(Action a) {
        super(a);
        addChangeListenerSelectColor();
    }

    public SelectColorButton(String text, Icon icon) {
        super(text, icon);
        addChangeListenerSelectColor();
    }

    private void addChangeListenerSelectColor() {
        if (UIManager.getColor("Button.selectForeground") == null
                || UIManager.getColor("Button.foreground") == null)
            return;

        // Fixes the issue of the background of the button not being blue when selected on macOS
        putClientProperty("JButton.buttonType", "segmented-only");

        addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                if (getModel().isArmed()) {
                    setForeground(UIManager.getColor("Button.selectForeground"));
                }
                else {
                    setForeground(UIManager.getColor("Button.foreground"));
                }
            }
        });

        // Need to add this, otherwise the foreground can remain in the selectForeground state when the button is clicked
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                setForeground(UIManager.getColor("Button.foreground"));
            }
        });
    }
}
