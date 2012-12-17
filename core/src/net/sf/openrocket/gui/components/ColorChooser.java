/*
 * ColorChooser.java
 */
package net.sf.openrocket.gui.components;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * A panel implementation of a color chooser.  The panel has a label and a textfield.  The label identifies 
 * what the color is to be used for (the purpose), and the textfield is uneditable but has its background set
 * to the currently chosen color as a way of visualizing the color.
 * 
 * The chosen color may be retrieved via a call to getCurrentColor.
 */
public class ColorChooser extends JPanel {

    private static final String COLOR_CHOOSER_BUTTON_LABEL = "Color";

    // The currently selected color 
    private Color curColor;

    final JColorChooser chooser;
    final JLabel label;
    final JTextField field;
    final JPanel p;

    /**
     * Construct a color chooser as a panel.
     * l
     * @param parent     the parent panel to which this component will be added
     * @param theChooser the delegated color chooser; the initial color taken from this chooser
     * @param theLabel   the label used as the 'purpose' of the color; placed next to a textfield
     */
    public ColorChooser (JPanel parent, JColorChooser theChooser, final String theLabel) {
        p = parent;
        chooser = theChooser;
        chooser.setPreviewPanel(this);
        // Initialize the currently selected color 
        curColor = chooser.getColor();
        label = new JLabel(theLabel + ":");

        parent.add(label, "align right");
        field = new JTextField();
        field.setEditable(false);
        field.setBackground(curColor);
        parent.add(field, "width 50:100:100");

        final JButton button = new JButton(COLOR_CHOOSER_BUTTON_LABEL);

        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed (ActionEvent actionEvent) {
                chooser.updateUI();

                final JDialog dialog = JColorChooser.createDialog(null,
                                                                  theLabel, true,
                                                                  chooser,
                                                                  null, null);

                // Wait until current event dispatching completes before showing
                // dialog
                Runnable showDialog = new Runnable() {
                    @Override
                    public void run () {
                        dialog.setVisible(true);
                    }
                };
                SwingUtilities.invokeLater(showDialog);
            }
        };
        button.addActionListener(actionListener);
        parent.add(button, "wrap");

        // Add listener on model to detect changes to selected color 
        ColorSelectionModel model = chooser.getSelectionModel();
        model.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged (ChangeEvent evt) {
                ColorSelectionModel myModel = (ColorSelectionModel) evt.getSource();
                // Get the new color value 
                curColor = myModel.getSelectedColor();
                field.setBackground(curColor);
            }
        });
    }

    /**
     * Get the user-selected color.
     * 
     * @return  the current color
     */
    public Color getCurrentColor () {
        return curColor;
    }
}