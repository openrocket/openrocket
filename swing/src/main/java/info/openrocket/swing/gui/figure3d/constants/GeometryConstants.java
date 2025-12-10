package info.openrocket.swing.gui.figure3d.constants;

/**
 * Constants related to 3D geometry generation and polygon winding order.
 * Defines standards for face orientation and culling behavior.
 */
public abstract class GeometryConstants {
	/**
	 * Defines the vertex winding order to control which face of a polygon is
	 * considered the "front" for lighting and culling.
	 */
	public enum WindingOrder {
		/**
		 * Counter-clockwise order = front-facing polygon.
		 */
		COUNTER_CLOCKWISE,

		/**
		 * Clockwise order = back-facing polygon.
		 */
		CLOCKWISE
	}
}
