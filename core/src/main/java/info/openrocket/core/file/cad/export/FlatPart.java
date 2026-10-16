package info.openrocket.core.file.cad.export;

import info.openrocket.core.rocketcomponent.FinSet;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;
import info.openrocket.core.util.CoordinateIF;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A flat part that is cut from sheet stock (a fin), described as one or more
 * regions in the XY plane and a thickness. All lengths are in millimeters.
 * <p>
 * Outer loops are counter-clockwise and hole loops clockwise (seen from +Z), so the material is always
 * on the left of the loop direction.
 */
public final class FlatPart {
	private static final double M_TO_MM = 1000;
	/** Points closer than this (mm) are merged. */
	private static final double POINT_TOLERANCE_MM = 1e-6;

	/**
	 * Dimension types reported for the exported parts.
	 */
	public enum Dimension {
		/** Overall length of the fin outline, including any tab (along the rocket axis) */
		OUTLINE_LENGTH,
		/** Overall height of the fin outline, including any tab */
		OUTLINE_HEIGHT,
		ROOT_CHORD,
		TIP_CHORD,
		FIN_HEIGHT,
		SWEEP_LENGTH,
		TAB_LENGTH,
		TAB_HEIGHT,
		/** Thickness of the stock the part is cut from */
		MATERIAL_THICKNESS,
		/** Volume of the solid part, in cubic meters */
		VOLUME
	}

	/**
	 * A single design dimension of a part.
	 *
	 * @param dimension the dimension type
	 * @param index     1-based index for repeated dimensions (e.g. holes), or 0
	 * @param value     the design value, in meters (cubic meters for {@link Dimension#VOLUME})
	 */
	public record DimensionValue(Dimension dimension, int index, double value) { }

	/** A closed loop in the XY plane, in mm. */
	public sealed interface Loop permits Polygon { }

	/**
	 * A closed polygon. The first point is not repeated at the end.
	 */
	public record Polygon(double[] xs, double[] ys) implements Loop {
		/**
		 * @return the number of points
		 */
		public int size() {
			return xs.length;
		}

		/** Signed area, positive when counter-clockwise. */
		public double signedArea() {
			double area = 0;
			for (int i = 0; i < xs.length; i++) {
				int j = (i + 1) % xs.length;
				area += xs[i] * ys[j] - xs[j] * ys[i];
			}
			return area / 2;
		}

		Polygon reversed() {
			double[] rx = new double[xs.length];
			double[] ry = new double[ys.length];
			for (int i = 0; i < xs.length; i++) {
				rx[i] = xs[xs.length - 1 - i];
				ry[i] = ys[ys.length - 1 - i];
			}
			return new Polygon(rx, ry);
		}

		Polygon translated(double dx, double dy) {
			double[] tx = new double[xs.length];
			double[] ty = new double[ys.length];
			for (int i = 0; i < xs.length; i++) {
				tx[i] = xs[i] + dx;
				ty[i] = ys[i] + dy;
			}
			return new Polygon(tx, ty);
		}
	}

	/**
	 * A connected area of material: an outer loop with optional holes.
	 */
	public record Region(Loop outer, List<Loop> holes) { }

	private final String name;
	private final List<Region> regions;
	private final double thicknessMm;
	private final List<DimensionValue> dimensions;

	private FlatPart(String name, List<Region> regions, double thicknessMm,
					 List<DimensionValue> dimensions) {
		this.name = name == null ? "" : name;
		this.regions = List.copyOf(regions);
		this.thicknessMm = thicknessMm;
		this.dimensions = List.copyOf(dimensions);
	}

	/**
	 * @return the name of the part, or an empty string if it has none
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return the connected areas of material that make up the part: one for most parts, two for a fin with a tab
	 * that sticks out beyond the fin
	 */
	public List<Region> getRegions() {
		return regions;
	}

	/**
	 * @return the thickness of the stock the part is cut from, in mm
	 */
	public double getThicknessMm() {
		return thicknessMm;
	}

	/**
	 * @return the dimensions of the part according to the design
	 */
	public List<DimensionValue> getDimensions() {
		return dimensions;
	}

	/**
	 * @return the bounds of the part in mm: minX, minY, maxX, maxY
	 */
	public double[] getBounds() {
		double[] b = {Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
		for (Region region : regions) {
			include(b, region.outer());
		}
		if (b[0] == Double.MAX_VALUE) {
			return new double[]{0, 0, 0, 0};
		}
		return b;
	}

	/**
	 * @return a copy of this part moved by (dx, dy) mm
	 */
	public FlatPart translated(double dx, double dy) {
		List<Region> moved = new ArrayList<>();
		for (Region region : regions) {
			List<Loop> holes = new ArrayList<>();
			for (Loop hole : region.holes()) {
				holes.add(translate(hole, dx, dy));
			}
			moved.add(new Region(translate(region.outer(), dx, dy), holes));
		}
		return new FlatPart(name, moved, thicknessMm, dimensions);
	}

	/**
	 * Creates the flat part of a single fin of a fin set, including its tab.
	 */
	public static FlatPart fromFinSet(FinSet finSet) {
		Objects.requireNonNull(finSet, "finSet");
		List<Region> regions = new ArrayList<>();
		addPolygonRegion(regions, finSet.generateContinuousFinAndTabShape(), 0, 0);
		if (finSet.isTabBeyondFin()) {
			CoordinateIF finFront = finSet.getFinFront();
			addPolygonRegion(regions, finSet.getTabPointsWithRoot(), finFront.getX(), finFront.getY());
		}

		double area = 0;
		for (Region region : regions) {
			area += area(region);
		}
		double thicknessMm = finSet.getThickness() * M_TO_MM;

		FlatPart part = new FlatPart(finSet.getName(), regions, thicknessMm, List.of());
		double[] b = part.getBounds();
		List<DimensionValue> dims = new ArrayList<>();
		dims.add(new DimensionValue(Dimension.OUTLINE_LENGTH, 0, (b[2] - b[0]) / M_TO_MM));
		dims.add(new DimensionValue(Dimension.OUTLINE_HEIGHT, 0, (b[3] - b[1]) / M_TO_MM));
		if (finSet instanceof TrapezoidFinSet trapezoid) {
			dims.add(new DimensionValue(Dimension.ROOT_CHORD, 0, trapezoid.getRootChord()));
			dims.add(new DimensionValue(Dimension.TIP_CHORD, 0, trapezoid.getTipChord()));
			dims.add(new DimensionValue(Dimension.FIN_HEIGHT, 0, trapezoid.getHeight()));
			dims.add(new DimensionValue(Dimension.SWEEP_LENGTH, 0, trapezoid.getSweep()));
		} else {
			dims.add(new DimensionValue(Dimension.ROOT_CHORD, 0, finSet.getLength()));
		}
		if (finSet.getTabHeight() > 0 && finSet.getTabLength() > 0) {
			dims.add(new DimensionValue(Dimension.TAB_LENGTH, 0, finSet.getTabLength()));
			dims.add(new DimensionValue(Dimension.TAB_HEIGHT, 0, finSet.getTabHeight()));
		}
		dims.add(new DimensionValue(Dimension.MATERIAL_THICKNESS, 0, finSet.getThickness()));
		dims.add(new DimensionValue(Dimension.VOLUME, 0, area * thicknessMm * 1e-9));
		return new FlatPart(part.name, regions, thicknessMm, dims);
	}

	/**
	 * Exact area of a region in mm^2.
	 */
	public static double area(Region region) {
		double area = Math.abs(area(region.outer()));
		for (Loop hole : region.holes()) {
			area -= Math.abs(area(hole));
		}
		return area;
	}

	private static double area(Loop loop) {
		return ((Polygon) loop).signedArea();
	}

	private static void addPolygonRegion(List<Region> regions, CoordinateIF[] points, double offsetX, double offsetY) {
		List<double[]> cleaned = new ArrayList<>();
		for (CoordinateIF p : points) {
			double x = (p.getX() + offsetX) * M_TO_MM;
			double y = (p.getY() + offsetY) * M_TO_MM;
			if (!cleaned.isEmpty() && isSame(cleaned.get(cleaned.size() - 1), x, y)) {
				continue;
			}
			cleaned.add(new double[]{x, y});
		}
		while (cleaned.size() > 1 && isSame(cleaned.get(0), cleaned.get(cleaned.size() - 1)[0],
				cleaned.get(cleaned.size() - 1)[1])) {
			cleaned.remove(cleaned.size() - 1);
		}
		if (cleaned.size() < 3) {
			return;
		}

		double[] xs = new double[cleaned.size()];
		double[] ys = new double[cleaned.size()];
		for (int i = 0; i < cleaned.size(); i++) {
			xs[i] = cleaned.get(i)[0];
			ys[i] = cleaned.get(i)[1];
		}
		Polygon polygon = new Polygon(xs, ys);
		if (polygon.signedArea() < 0) {
			polygon = polygon.reversed();
		}
		regions.add(new Region(polygon, List.of()));
	}

	private static boolean isSame(double[] p, double x, double y) {
		return Math.abs(p[0] - x) < POINT_TOLERANCE_MM && Math.abs(p[1] - y) < POINT_TOLERANCE_MM;
	}

	private static Loop translate(Loop loop, double dx, double dy) {
		return ((Polygon) loop).translated(dx, dy);
	}

	private static void include(double[] b, Loop loop) {
		Polygon p = (Polygon) loop;
		for (int i = 0; i < p.size(); i++) {
			b[0] = Math.min(b[0], p.xs()[i]);
			b[1] = Math.min(b[1], p.ys()[i]);
			b[2] = Math.max(b[2], p.xs()[i]);
			b[3] = Math.max(b[3], p.ys()[i]);
		}
	}
}
