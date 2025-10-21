package info.openrocket.core.util;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;

public class BoundingBox {
	public CoordinateIF min; // Top-left coordinate of the bounding box
	public CoordinateIF max; // Bottom-right coordinate of the bounding box

	public BoundingBox() {
		clear();
	}

	public BoundingBox(CoordinateIF _min, CoordinateIF _max) {
		this();
		this.min = _min.clone();
		this.max = _max.clone();
	}

	public void clear() {
		min = Coordinate.MAX.setWeight(0.0);
		max = Coordinate.MIN.setWeight(0.0);
	}

	@Override
	public BoundingBox clone() {
		return new BoundingBox(this.min, this.max);
	}

	public boolean isEmpty() {
		return (min.getX() > max.getX()) ||
				(min.getY() > max.getY()) ||
				(min.getZ() > max.getZ());
	}

	/**
	 * Generate a new bounding box, transformed by the given transformation matrix
	 *
	 * Note: This implementation is not _exact_! Do not use this for Aerodynamic,
	 * Mass or Simulation calculations....
	 * But it will be sufficiently close for UI purposes.
	 *
	 * @return a new box, transform by the given transform
	 */
	public BoundingBox transform(Transformation transform) {
		final CoordinateIF p1 = transform.transform(this.min);
		final CoordinateIF p2 = transform.transform(this.max);

		final BoundingBox newBox = new BoundingBox();
		newBox.update(p1);
		newBox.update(p2);

		return newBox;
	}

	private void update_x_min(final double xVal) {
		if (min.getX() > xVal)
			min = min.setX(xVal);
	}

	private void update_y_min(final double yVal) {
		if (min.getY() > yVal)
			min = min.setY(yVal);
	}

	private void update_z_min(final double zVal) {
		if (min.getZ() > zVal)
			min = min.setZ(zVal);
	}

	private void update_x_max(final double xVal) {
		if (max.getX() < xVal)
			max = max.setX(xVal);
	}

	private void update_y_max(final double yVal) {
		if (max.getY() < yVal)
			max = max.setY(yVal);
	}

	private void update_z_max(final double zVal) {
		if (max.getZ() < zVal)
			max = max.setZ(zVal);
	}

	public BoundingBox update(final double val) {
		update_x_min(val);
		update_y_min(val);
		update_z_min(val);

		update_x_max(val);
		update_y_max(val);
		update_z_max(val);
		return this;
	}

	public BoundingBox update(CoordinateIF c) {
		update_x_min(c.getX());
		update_y_min(c.getY());
		update_z_min(c.getZ());

		update_x_max(c.getX());
		update_y_max(c.getY());
		update_z_max(c.getZ());

		return this;
	}

	public BoundingBox update(Rectangle2D rect) {
		update_x_min(rect.getMinX());
		update_y_min(rect.getMinY());
		update_x_max(rect.getMaxX());
		update_y_max(rect.getMaxY());
		return this;
	}

	public BoundingBox update(final CoordinateIF[] list) {
		for (CoordinateIF c : list) {
			update(c);
		}
		return this;
	}

	public BoundingBox update(Collection<CoordinateIF> list) {
		for (CoordinateIF c : list) {
			update(c);
		}
		return this;
	}

	public BoundingBox update(BoundingBox other) {
		if (other.isEmpty()) {
			return this;
		}
		update_x_min(other.min.getX());
		update_y_min(other.min.getY());
		update_z_min(other.min.getZ());

		update_x_max(other.max.getX());
		update_y_max(other.max.getY());
		update_z_max(other.max.getZ());
		return this;
	}

	public CoordinateIF span() {
		return max.sub(min);
	}

	public CoordinateIF[] toArray() {
		return new CoordinateIF[] { this.min, this.max };
	}

	public Collection<CoordinateIF> toCollection() {
		Collection<CoordinateIF> toReturn = new ArrayList<>();
		toReturn.add(this.max);
		toReturn.add(this.min);
		return toReturn;
	}

	public Rectangle2D toRectangle() {
		return new Rectangle2D.Double(min.getX(), min.getY(), (max.getX() - min.getX()), (max.getY() - min.getY()));
	}

	@Override
	public String toString() {
		return String.format("[( %g, %g, %g) < ( %g, %g, %g)]",
				min.getX(), min.getY(), min.getZ(),
				max.getX(), max.getY(), max.getZ());
	}

	@Override
	public boolean equals(Object other) {
		return other instanceof BoundingBox &&
				((BoundingBox) other).min.equals(this.min) &&
				((BoundingBox) other).max.equals(this.max);
	}
}
