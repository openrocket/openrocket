package info.openrocket.core.appearance;

import info.openrocket.core.util.ORColor;
import info.openrocket.core.util.MathUtil;

/**
 * A component appearance.
 * This class is immutable.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class Appearance {
	public static final Appearance MISSING = new Appearance(new ORColor(0, 0, 0), 1, null);

	private final ORColor paint;
	private final double shine;
	private final Decal texture;
	private final boolean opacityAffectsTexture;

	/**
	 * Main constructor
	 * 
	 * @param paint   the color to be used
	 * @param shine   shine of the appearance, will be clamped between 0 and 1
	 * @param texture The appearance texture
	 */
	public Appearance(final ORColor paint, final double shine, final Decal texture, final boolean opacityAffectsTexture) {
		this.paint = paint;
		this.shine = MathUtil.clamp(shine, 0, 1);
		this.texture = texture;
		this.opacityAffectsTexture = opacityAffectsTexture;
	}

	/**
	 * Main constructor
	 *
	 * @param paint   the color to be used
	 * @param shine   shine of the appearance, will be clamped between 0 and 1
	 * @param texture The appearance texture
	 */
	public Appearance(final ORColor paint, final double shine, final Decal texture) {
		this(paint, shine, texture, false);
	}

	/**
	 * Main constructor
	 * 
	 * @param paint the color to be used
	 * @param shine shine of the appearance, will be clamped between 0 and 1
	 */
	public Appearance(final ORColor paint, final double shine) {
		this(paint, shine, null);
	}

	/**
	 * @return color of the appearance
	 */
	public ORColor getPaint() {
		return paint;
	}

	/**
	 * @return Shine of appearance
	 */
	public double getShine() {
		return shine;
	}

	/**
	 * @return Texture used in appearance
	 */
	public Decal getTexture() {
		return texture;
	}

	/**
	 * @return true if component opacity should also affect textured pixels
	 */
	public boolean isOpacityAffectsTexture() {
		return opacityAffectsTexture;
	}

	@Override
	public String toString() {
		return "Appearance [paint=" + paint + ", shine="
				+ shine + ", texture=" + texture + ", opacityAffectsTexture=" + opacityAffectsTexture + "]";
	}

}
