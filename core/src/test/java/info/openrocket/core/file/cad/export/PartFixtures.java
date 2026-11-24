package info.openrocket.core.file.cad.export;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;

/**
 * Fins and centering rings with dimensions that are easy to calculate by hand, for the tests. The values the tests
 * expect, worked out by hand:
 * <ul>
 * <li>Fin of {@link #createFinSet()}: area (100 + 50) / 2 * 60 = 4500 mm^2, volume 4500 * 3 = 13500 mm^3, extents
 * 100 x 60 x 3 mm.</li>
 * <li>Ring of {@code ExportTestSupport.createAllParts()}: OD 50 mm, ID 18 mm, 5 mm thick, with a 6 mm hole centered at
 * (12, 5) mm, so sqrt(12^2 + 5^2) = 13 mm from the axis. Volume pi * (25^2 - 9^2 - 3^2) * 5 = 2675 pi = 8403.76 mm^3.
 * </li>
 * <li>Disc of {@link #createDisc()}: pi * 37.5^2 * 6 = 8437.5 pi = 26507.19 mm^3.</li>
 * <li>All three parts together: 13500 + 11112.5 pi = 48410.95 mm^3.</li>
 * <li>Two 20 mm holes with centers 10 mm apart in a 60 mm disc, 5 mm thick: the holes overlap in a lens of
 * 2 * 10^2 * acos(1/2) - 5 * sqrt(300) = 122.84 mm^2, so they remove 2 * 100 pi - 122.84 = 505.48 mm^2. The disc keeps
 * 900 pi - 505.48 = 2321.95 mm^2, a volume of 11609.76 mm^3.</li>
 * </ul>
 */
final class PartFixtures {
	/** Trapezoid area (100 + 50) / 2 * 60 = 4500 mm^2, 3 mm thick */
	static final double FIN_VOLUME_MM3 = 4500 * 3;
	/** Ring OD 50 mm, ID 18 mm, 5 mm thick */
	static final double RING_VOLUME_MM3 = Math.PI * (25 * 25 - 9 * 9) * 5;

	private PartFixtures() {
	}

	/**
	 * Root 100 mm, tip 50 mm, height 60 mm, sweep 30 mm, 3 mm thick: the outline corners are
	 * (0,0), (30,60), (80,60), (100,0).
	 */
	static TrapezoidFinSet createFinSet() {
		BodyTube tube = new BodyTube();
		tube.setOuterRadius(0.025);
		tube.setLength(0.3);
		TrapezoidFinSet fins = new TrapezoidFinSet();
		tube.addChild(fins);
		fins.setRootChord(0.100);
		fins.setTipChord(0.050);
		fins.setHeight(0.060);
		fins.setSweep(0.030);
		fins.setThickness(0.003);
		fins.setName("Fins");
		return fins;
	}

	static CenteringRing createRing(double outerRadius, double innerRadius) {
		CenteringRing ring = new CenteringRing();
		ring.setOuterRadiusAutomatic(false);
		ring.setInnerRadiusAutomatic(false);
		ring.setOuterRadius(outerRadius);
		ring.setInnerRadius(innerRadius);
		ring.setLength(0.005);
		ring.setName("Ring");
		return ring;
	}

	/** A solid ring of diameter 75 mm, 6 mm thick */
	static CenteringRing createDisc() {
		CenteringRing disc = createRing(0.0375, 0);
		disc.setLength(0.006);
		disc.setName("Disc");
		return disc;
	}
}
