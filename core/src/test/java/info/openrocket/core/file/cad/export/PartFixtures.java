package info.openrocket.core.file.cad.export;

import info.openrocket.core.rocketcomponent.BodyTube;
import info.openrocket.core.rocketcomponent.CenteringRing;
import info.openrocket.core.rocketcomponent.TrapezoidFinSet;

/**
 * Fins and centering rings with dimensions that are easy to calculate by hand, for the tests.
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
