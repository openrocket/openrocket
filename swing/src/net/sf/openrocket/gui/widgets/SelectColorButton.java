package net.sf.openrocket.gui.widgets;


import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Graphics;

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
    }
}
