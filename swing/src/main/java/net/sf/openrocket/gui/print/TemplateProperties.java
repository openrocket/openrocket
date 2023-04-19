/*
 * TemplateProperties.java
 */
package net.sf.openrocket.gui.print;

import java.awt.Color;

import javax.swing.UIManager;

/**
 * This class is responsible for managing various properties of print templates (fin, nose cone, transitions, etc.).
 * 
 * TODO: HIGH:  Remove this entire class, and instead pass the PrintSettings object to the print methods.
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
	public static Color getFillColor() {
		Color fillColor = UIManager.getColor(TemplateProperties.TEMPLATE_FILL_COLOR_PROPERTY);
		if (fillColor == null) {
			fillColor = Color.lightGray;
		}
		return fillColor;
	}
	
	
	/**
	 * Set the template fill color.
	 */
	public static void setFillColor(Color c) {
		UIManager.put(TemplateProperties.TEMPLATE_FILL_COLOR_PROPERTY, c);
	}
	
	
	/**
	 * Get the current line color.
	 * 
	 * @return  a color to be used as the line in template shapes
	 */
	public static Color getLineColor() {
		Color lineColor = UIManager.getColor(TemplateProperties.TEMPLATE_LINE_COLOR_PROPERTY);
		if (lineColor == null) {
			lineColor = Color.darkGray;
		}
		return lineColor;
	}
	
	/**
	 * Set the template line color.
	 */
	public static void setLineColor(Color c) {
		UIManager.put(TemplateProperties.TEMPLATE_LINE_COLOR_PROPERTY, c);
	}
	
	/**
	 * Set the template colors from the print settings.
	 */
	public static void setColors(PrintSettings settings) {
		setFillColor(settings.getTemplateFillColor());
		setLineColor(settings.getTemplateBorderColor());
	}
}
