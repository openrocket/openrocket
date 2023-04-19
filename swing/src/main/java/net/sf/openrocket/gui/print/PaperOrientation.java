package net.sf.openrocket.gui.print;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.RectangleReadOnly;

public enum PaperOrientation {
	
	PORTRAIT("Portrait") {
		@Override
		public Rectangle orient(Rectangle rect) {
			return new RectangleReadOnly(rect);
		}
	},
	LANDSCAPE("Landscape") {
		@Override
		public Rectangle orient(Rectangle rect) {
			return new RectangleReadOnly(new Rectangle(rect).rotate());
		}
	};
	

	private final String name;
	
	private PaperOrientation(String name) {
		this.name = name;
	}
	
	/**
	 * Change the orientation of a portrait paper to the orientation represented by this
	 * orientation.
	 *  
	 * @param rect	the original paper size rectangle
	 * @return		the oriented paper size rectangle
	 */
	public abstract Rectangle orient(Rectangle rect);
	
	
	@Override
	public String toString() {
		return name;
	}
}
