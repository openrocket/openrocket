package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.rocketcomponent.Transition;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.basic.TubeGenerator;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;

import java.util.ArrayList;
import java.util.List;

/**
 * A component-level generator that understands rocket transitions.
 * It uses the lower-level TubeGenerator to create the actual geometry and includes
 * optimizations for simple conical shapes and adaptive resolution for curved shapes.
 */
public abstract class TransitionGenerator {

	private static final int CURVED_PROFILE_RESOLUTION = 30;
	private static final double RESOLUTION_BIAS_POWER = 2.0;

	public static Mesh create(Transition t, RenderingConfiguration config) {
		List<TubeGenerator.RadiusPoint> profile;
		double length = t.getLength();

		final int segments = switch (config.getQuality().getQuality()) {
			case LOW -> RenderingConstants.LOW_COMPLEX_SEGMENT_COUNT;
			case MEDIUM -> RenderingConstants.MEDIUM_COMPLEX_SEGMENT_COUNT;
			case HIGH -> RenderingConstants.HIGH_COMPLEX_SEGMENT_COUNT;
		};

		if (isConical(t)) {
			profile = List.of(
					new TubeGenerator.RadiusPoint(0.0f, (float) t.getForeRadius()),
					new TubeGenerator.RadiusPoint(1.0f, (float) t.getAftRadius())
			);
		} else {
			profile = new ArrayList<>();
			boolean tipIsFore = t.getForeRadius() < t.getAftRadius();
			for (int i = 0; i <= CURVED_PROFILE_RESOLUTION; i++) {
				float linearPosition = (float) i / CURVED_PROFILE_RESOLUTION;
				float biasedPosition;

				if (tipIsFore) {
					biasedPosition = (float) Math.pow(linearPosition, RESOLUTION_BIAS_POWER);
				} else {
					biasedPosition = 1.0f - (float) Math.pow(1.0f - linearPosition, RESOLUTION_BIAS_POWER);
				}

				double absolutePosition = biasedPosition * length;
				float radius = (float) t.getRadius(absolutePosition);
				profile.add(new TubeGenerator.RadiusPoint(biasedPosition, radius));
			}
		}

		// Create Shoulder data objects from the Transition properties
		TubeGenerator.Shoulder foreShoulder = new TubeGenerator.Shoulder(
				t.getForeShoulderLength(), t.getForeShoulderRadius(), t.getForeShoulderThickness(), t.isForeShoulderCapped());
		TubeGenerator.Shoulder aftShoulder = new TubeGenerator.Shoulder(
				t.getAftShoulderLength(), t.getAftShoulderRadius(), t.getAftShoulderThickness(), t.isAftShoulderCapped());


		// Delegate the entire mesh construction, including shoulders, to the TubeGenerator
		return TubeGenerator.create(profile, (float) t.getThickness(), (float) length, segments,
				t.isFilled(), foreShoulder, aftShoulder, config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY_CUTAWAY);
	}

	private static boolean isConical(Transition t) {
		if (t.getShapeType() == Transition.Shape.CONICAL) {
			return true;
		}
		if (t.getShapeType() == Transition.Shape.OGIVE && t.getShapeParameter() == 0) {
			return true;
		}
		if (t.getShapeType() == Transition.Shape.POWER && t.getShapeParameter() == 1) {
			return true;
		}
		if (t.getShapeType() == Transition.Shape.PARABOLIC && t.getShapeParameter() == 0) {
			return true;
		}
		return false;
	}
}
