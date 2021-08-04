package net.sf.openrocket.gui.widgets;


import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.UIManager;
import java.awt.Graphics;

public class SelectColorButton extends JButton {
    public SelectColorButton() {
    }

    public SelectColorButton(Icon icon) {
        super(icon);
    }

    public SelectColorButton(String text) {
        super(text);
    }

    public SelectColorButton(Action a) {
        super(a);
    }

    public SelectColorButton(String text, Icon icon) {
        super(text, icon);
    }



    @Override
    public void paint(Graphics g) {
        if (getModel().isArmed()) {
            setForeground(UIManager.getColor("Button.selectForeground"));
        }
        else {
            setForeground(UIManager.getColor("Button.foreground"));
        }
        super.paint(g);
    }
}
