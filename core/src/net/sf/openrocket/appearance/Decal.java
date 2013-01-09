package net.sf.openrocket.appearance;

import net.sf.openrocket.startup.Application;
import net.sf.openrocket.util.Coordinate;

/**
 * A texture that can be applied by an Appearance. This class is immutable.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class Decal {
	
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
	
	public Decal(final Coordinate offset, final Coordinate center, final Coordinate scale, final double rotation,
			final DecalImage image, final EdgeMode mode) {
		this.offset = offset;
		this.center = center;
		this.scale = scale;
		this.rotation = rotation;
		this.image = image;
		this.mode = mode;
	}
	
	public Coordinate getOffset() {
		return offset;
	}
	
	public Coordinate getCenter() {
		return center;
	}
	
	public Coordinate getScale() {
		return scale;
	}
	
	public double getRotation() {
		return rotation;
	}
	
	public EdgeMode getEdgeMode() {
		return mode;
	}
	
	public DecalImage getImage() {
		return image;
	}
	
	@Override
	public String toString() {
		return "Texture [offset=" + offset + ", center=" + center + ", scale=" + scale + ", rotation=" + rotation
				+ ", image=" + image + "]";
	}
	
}
