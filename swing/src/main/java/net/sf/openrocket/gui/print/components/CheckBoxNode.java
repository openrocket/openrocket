/*
 * CheckBoxNode.java
 */
package net.sf.openrocket.gui.print.components;

/**
 * A class that acts as the textual node of the check box within the JTree.
 */
public class CheckBoxNode {

    /**
     * The text label of the check box.
     */
    String text;

    /**
     * State flag indicating if the check box has been selected.
     */
    boolean selected;

    /**
     * Constructor.
     * 
     * @param theText  the check box label
     * @param isSelected  true if selected
     */
    public CheckBoxNode (String theText, boolean isSelected) {
        text = theText;
        selected = isSelected;
    }

    /**
     * Get the current state of the check box.
     * 
     * @return true if selected
     */
    public boolean isSelected () {
        return selected;
    }

    /**
     * Set the current state of the check box.  Note: this just tracks the state - it 
     * does NOT actually set the state of the check box.
     * 
     * @param isSelected  true if selected
     */
    public void setSelected (boolean isSelected) {
        selected = isSelected;
    }

    /**
     * Get the text of the label.
     * 
     * @return  the text of the label
     */
    public String getText () {
        return text;
    }

    /**
     * Set the text of the label of the check box.
     * 
     * @param theText  the text of the label
     */
    public void setText (String theText) {
        text = theText;
    }

    /**
     * If someone prints this object, the text label will be displayed.
     * 
     * @return  the text label
     */
    @Override
    public String toString () {
        return text;
    }
}