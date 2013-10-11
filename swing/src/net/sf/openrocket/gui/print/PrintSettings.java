package net.sf.openrocket.gui.print;

import java.awt.Color;

import net.sf.openrocket.util.AbstractChangeSource;

/**
 * A class containing all printing settings.
 */
public class PrintSettings extends AbstractChangeSource {
	
	private Color templateFillColor = Color.LIGHT_GRAY;
	private Color templateBorderColor = Color.DARK_GRAY;
	
	private PaperSize paperSize = PaperSize.getDefault();
	private PaperOrientation paperOrientation = PaperOrientation.PORTRAIT;
	
	
	public Color getTemplateFillColor() {
		return templateFillColor;
	}
	
	public void setTemplateFillColor(Color templateFillColor) {
		// Implicitly tests against setting null
		if (templateFillColor.equals(this.templateFillColor)) {
			return;
		}
		this.templateFillColor = templateFillColor;
		fireChangeEvent();
	}
	
	public Color getTemplateBorderColor() {
		return templateBorderColor;
	}
	
	public void setTemplateBorderColor(Color templateBorderColor) {
		// Implicitly tests against setting null
		if (templateBorderColor.equals(this.templateBorderColor)) {
			return;
		}
		this.templateBorderColor = templateBorderColor;
		fireChangeEvent();
	}
	
	public PaperSize getPaperSize() {
		return paperSize;
	}
	
	public void setPaperSize(PaperSize paperSize) {
		if (paperSize.equals(this.paperSize)) {
			return;
		}
		this.paperSize = paperSize;
		fireChangeEvent();
	}
	
	public PaperOrientation getPaperOrientation() {
		return paperOrientation;
	}
	
	public void setPaperOrientation(PaperOrientation orientation) {
		if (orientation.equals(paperOrientation)) {
			return;
		}
		this.paperOrientation = orientation;
		fireChangeEvent();
	}
	
	

	/**
	 * Load settings from the specified print settings.
	 * @param settings	the settings to load
	 */
	public void loadFrom(PrintSettings settings) {
		this.templateFillColor = settings.templateFillColor;
		this.templateBorderColor = settings.templateBorderColor;
		this.paperSize = settings.paperSize;
		this.paperOrientation = settings.paperOrientation;
		fireChangeEvent();
	}
	
	
	@Override
	public String toString() {
		return "PrintSettings [templateFillColor=" + templateFillColor + ", templateBorderColor=" + templateBorderColor + ", paperSize=" + paperSize + ", paperOrientation=" + paperOrientation + "]";
	}
	
}
