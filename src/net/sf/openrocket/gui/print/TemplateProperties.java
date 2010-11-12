/*
 * TemplateProperties.java
 */
package net.sf.openrocket.gui.print;

import javax.swing.UIManager;
import java.awt.Color;

/**
 * This class is responsible for managing various properties of print templates (fin, nose cone, transitions, etc.).
 */
public class TemplateProperties {

    /**
     * The property that defines the fill color.
     */
    public static final String TEMPLATE_FILL_COLOR_PROPERTY = "template.fill.color";

    /**
     * The property that defines the line color.
     */
    public static final String TEMPLATE_LINE_COLOR_PROPERTY = "template.line.color";

    /**
     * Get the current fill color.
     * 
     * @return  a color to be used as the fill in template shapes
     */
    public static Color getFillColor () {
        Color fillColor = UIManager.getColor(TemplateProperties.TEMPLATE_FILL_COLOR_PROPERTY);
        if (fillColor == null) {
            fillColor = Color.lightGray;
        }
        return fillColor;
    }

    /**
     * Get the current line color.
     * 
     * @return  a color to be used as the line in template shapes
     */
    public static Color getLineColor () {
        Color lineColor = UIManager.getColor(TemplateProperties.TEMPLATE_LINE_COLOR_PROPERTY);
        if (lineColor == null) {
            lineColor = Color.darkGray;
        }
        return lineColor;
    }
}
