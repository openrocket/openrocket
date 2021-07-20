package net.sf.openrocket.gui.widgets;

import net.sf.openrocket.gui.components.StageSelector;

import javax.swing.*;
import java.awt.Graphics;
import java.awt.Color;

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
