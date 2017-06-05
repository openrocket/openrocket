package net.sf.openrocket.appearance;

import net.sf.openrocket.util.Color;
import net.sf.openrocket.util.MathUtil;

/**
 * A component appearance.
 * This class is immutable.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class Appearance {
	public static final Appearance MISSING = new Appearance(new Color(0, 0, 0), 1, null);
	
	private final Color paint;
	private final double shine;
	private final Decal texture;
	
	/**
	*	Main constructor
	*	
	*	@param paint	the color to be used
	*	@param shine	shine of the appearance, will be clamped between 0 and 1
	*	@param texture	The appearance texture
	*/
	public Appearance(final Color paint, final double shine, final Decal texture) {
		this.paint = paint;
		this.shine = MathUtil.clamp(shine, 0, 1);
		this.texture = texture;
	}
	
	/**
	*	Main constructor
	*	
	*	@param paint	the color to be used
	*	@param shine	shine of the appearance, will be clamped between 0 and 1
	*/
	public Appearance(final Color paint, final double shine) {
		this(paint,shine,null);
	}
	
	/**
	*	@return colr of the appearance
	*/
	public Color getPaint() {
		return paint;
	}
	
	/**
	*	@return Shine of appearance
	*/
	public double getShine() {
		return shine;
	}
	
	/**
	*	@return Texture used in appearance
	*/
	public Decal getTexture() {
		return texture;
	}
	
	@Override
	public String toString() {
		return "Appearance [paint=" + paint + ", shine="
				+ shine + ", texture=" + texture + "]";
	}
	
}
