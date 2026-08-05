package info.openrocket.swing.gui.figure3d.geometry.components;

import info.openrocket.core.rocketcomponent.BodyComponent;
import info.openrocket.core.rocketcomponent.Coaxial;
import info.openrocket.core.rocketcomponent.RocketComponent;
import info.openrocket.core.rocketcomponent.SymmetricComponent;
import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.basic.TubeGenerator;
import info.openrocket.swing.gui.figure3d.scene.properties.DisplaySettings;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;

public abstract class CoaxialGenerator {
	/**
	 * Creates a Mesh from a Coaxial data object.
	 * @param object The Coaxial object.
	 * @param config The rendering configuration.
	 * @return A Mesh object representing the coaxial component.
	 */
	public static <T extends RocketComponent & Coaxial>  Mesh create(T object, RenderingConfiguration config) {
		boolean isFilled = false;
		if (object instanceof SymmetricComponent) {
			isFilled = ((SymmetricComponent) object).isFilled();
		}

		final int segments = switch (config.getQuality().getQuality()) {
			case LOW -> RenderingConstants.LOW_COMPLEX_SEGMENT_COUNT;
			case MEDIUM -> RenderingConstants.MEDIUM_COMPLEX_SEGMENT_COUNT;
			case HIGH -> RenderingConstants.HIGH_COMPLEX_SEGMENT_COUNT;
		};

		double wallThickness = object.getOuterRadius() - object.getInnerRadius();
		boolean isXRayCutaway = object instanceof BodyComponent && config.getDisplay().getMode() == DisplaySettings.RenderMode.XRAY_CUTAWAY;
		return TubeGenerator.create((float)object.getOuterRadius(), (float)wallThickness, (float)object.getLength(),
				segments, isFilled, isXRayCutaway);
	}
}
