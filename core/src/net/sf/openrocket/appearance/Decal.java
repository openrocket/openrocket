package net.sf.openrocket.appearance;

import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;

/**
 * A texture that can be applied by an Appearance. an object of this class is immutable.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class Decal {
	
	/**
	 * enum to flag what happens on edge in a decal
	 * 
	 *
	 */
	public static enum EdgeMode {
		REPEAT("TextureWrap.Repeat"), MIRROR("TextureWrap.Mirror"), CLAMP("TextureWrap.Clamp"), STICKER("TextureWrap.Sticker");
		private final String transName;
		
		EdgeMode(final String name) {
			this.transName = name;
		}
		
		@Override
		public String toString() {
			return Application.getTranslator().get(transName);
		}
	}
	
	private final Coordinate offset, center, scale;
	private final double rotation;
	private final DecalImage image;
	private final EdgeMode mode;
	
	/**
	 * Builds a new decal with the given itens
	 * 
	 * @param offset	The offset of the decal, in coordinate obejct format
	 * @param center	The position of the center of the decal, in coordinate object format
	 * @param scale		The scale of the decal, in coordinate obejct format
	 * @param rotation	Rotation of the decal, in radians
	 * @param image		The image itself
	 * @param mode		The description of Edge behaviour
	 */
	public Decal(final Coordinate offset, final Coordinate center, final Coordinate scale, final double rotation,
			final DecalImage image, final EdgeMode mode) {
		this.offset = offset;
		this.center = center;
		this.scale = scale;
		this.rotation = rotation;
		this.image = image;
		this.mode = mode;
	}
	
	/**
	 * returns the offset, in coordinates object format
	 * 
	 * @return offset coordinates of the decal
	 */
	public Coordinate getOffset() {
		return offset;
	}
	
	/**
	 * return the center, in coordinates object format
	 * 
	 * @return	The center coordinates of the decal
	 */
	public Coordinate getCenter() {
		return center;
	}
	
	/**
	 * return the scaling of the decal, in coordinate format
	 * 
	 * @return	the scale coordinates of the decal
	 */
	public Coordinate getScale() {
		return scale;
	}
	
	/**
	 * returns the rotation of the decal, in radians
	 * 	
	 * @return	the rotation of the decal, in radians
	 */
	public double getRotation() {
		return rotation;
	}
	
	/**
	 * return 	the edge behaviour of the decal
	 * 
	 * @return 	the edge behaviour of the decal
	 */
	public EdgeMode getEdgeMode() {
		return mode;
	}
	
	/**
	 * returns the image of the decal itself
	 * 
	 * @return the image of the decal itself
	 */
	public DecalImage getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		return "Texture [offset=" + offset + ", center=" + center + ", scale=" + scale + ", rotation=" + rotation
				+ ", image=" + image + "]";
	}
	
}
