package info.openrocket.swing.gui.figure3d.animation;

import info.openrocket.swing.gui.figure3d.constants.RenderingConstants;
import org.joml.Vector3f;

/** Maps OpenRocket simulation axes to the engine's RHS world (X east, Y up, Z south). */
public final class SimToWorld {
	private SimToWorld() {}
	public static Vector3f toEngine(float east, float north, float altitudeMeters) {
		// Engine: +X right (east), +Y up (altitude), -Z into screen (south)
		return new Vector3f(east, altitudeMeters, -north).mul(RenderingConstants.WORLD_SCALE);
	}
}
