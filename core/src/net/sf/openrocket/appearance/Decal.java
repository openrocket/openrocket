package net.sf.openrocket.appearance;

import java.net.URL;

import net.sf.openrocket.util.Coordinate;

/**
 * A texture that can be applied by an Appearance. This class is immutable.
 * 
 * @author Bill Kuker <bkuker@billkuker.com>
 */
public class Decal {

	public static enum EdgeMode {
		REPEAT, MIRROR, CLAMP;
	}

	private final Coordinate offset, center, scale;
	private final double rotation;
	private final URL image;
	private final EdgeMode mode;

	Decal(final Coordinate offset, final Coordinate center, final Coordinate scale, final double rotation,
			final URL image, final EdgeMode mode) {
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

	public URL getImageURL() {
		return image;
	}

	@Override
	public String toString() {
		return "Texture [offset=" + offset + ", center=" + center + ", scale=" + scale + ", rotation=" + rotation
				+ ", image=" + image + "]";
	}

}
