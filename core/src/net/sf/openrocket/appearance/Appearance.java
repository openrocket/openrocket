package net.sf.openrocket.appearance;

import net.sf.openrocket.util.Color;

/**
 * A component appearance.
 * This class is immutable.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class Appearance {
	public static final Appearance MISSING = new Appearance(new Color(0,0,0), 100, null);

	private final Color paint;
	private final int shine;
	private final Decal texture;
	
	Appearance(final Color paint, final int shine, final Decal texture){
		this.paint = paint;
		this.shine = shine;
		this.texture = texture;
	}
	
	Appearance(final Color paint, final int shine){
		this.paint = paint;
		this.shine = shine;
		this.texture = null;
	}
	
	public Color getPaint() {
		return paint;
	}

	public int getShine() {
		return shine;
	}

	public Decal getTexture() {
		return texture;
	}

	@Override
	public String toString() {
		return "Appearance [paint=" + paint + ", shine="
				+ shine + ", texture=" + texture + "]";
	}

}
