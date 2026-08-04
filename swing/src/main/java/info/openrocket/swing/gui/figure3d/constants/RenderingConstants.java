package info.openrocket.swing.gui.figure3d.constants;

/** Shared geometry and rendering constants. */
public abstract class RenderingConstants {

	// --- Particle System Limits ---
	public static final int DEFAULT_MAX_PARTICLES = 10000;
	public static final int FLAME_MAX_QUADS = 3000;
	public static final int SMOKE_MAX_QUADS = 5000;

	// --- Geometry Quality Levels ---
	// Basic shapes (tubes, cones)
	public static final int LOW_SEGMENT_COUNT = 18;
	public static final int MEDIUM_SEGMENT_COUNT = 24;
	public static final int HIGH_SEGMENT_COUNT = 32;

	// Complex components (transitions, nose cones)
	public static final int LOW_COMPLEX_SEGMENT_COUNT = 24;
	public static final int MEDIUM_COMPLEX_SEGMENT_COUNT = 32;
	public static final int HIGH_COMPLEX_SEGMENT_COUNT = 48;

	// --- Fin Set Geometry Segments ---
	// Root segments (for curved root surface)
	public static final int LOW_FIN_ROOT_SEGMENTS = 3;
	public static final int MEDIUM_FIN_ROOT_SEGMENTS = 5;
	public static final int HIGH_FIN_ROOT_SEGMENTS = 7;
	
	// Root X segments (along root length)
	public static final int LOW_FIN_ROOT_X_SEGMENTS = 4;
	public static final int MEDIUM_FIN_ROOT_X_SEGMENTS = 6;
	public static final int HIGH_FIN_ROOT_X_SEGMENTS = 8;
	
	// Fillet segments (for fillet arc)
	public static final int LOW_FIN_FILLET_SEGMENTS = 6;
	public static final int MEDIUM_FIN_FILLET_SEGMENTS = 8;
	public static final int HIGH_FIN_FILLET_SEGMENTS = 10;
	
	// Fillet X segments (along fillet length)
	public static final int LOW_FIN_FILLET_X_SEGMENTS = 4;
	public static final int MEDIUM_FIN_FILLET_X_SEGMENTS = 6;
	public static final int HIGH_FIN_FILLET_X_SEGMENTS = 8;

	// --- World Scaling ---
	public static final float WORLD_SCALE = 20.0f;

	// --- Surface IDs and Decal Surface Masks ---
	// Each vertex carries a surface ID (a small bit index). Decal masks select
	// surfaces by bit: the fragment shader samples the decal only when
	// (decalSurfaceMask & (1 << surfaceID)) != 0, so DECAL_SURFACE_x == 1 << SURFACE_ID_x.
	// The IDs are also used directly, e.g. the hide-inner-surfaces feature discards
	// fragments whose surface ID is SURFACE_ID_INSIDE (see fragment.glsl).
	public static final int SURFACE_ID_OUTSIDE = 0;
	public static final int SURFACE_ID_INSIDE = 1;
	public static final int SURFACE_ID_FORE = 2;
	public static final int SURFACE_ID_AFT = 3;

	public static final int DECAL_SURFACE_OUTSIDE = 1 << SURFACE_ID_OUTSIDE;	// 0b0001
	public static final int DECAL_SURFACE_INSIDE = 1 << SURFACE_ID_INSIDE;		// 0b0010
	public static final int DECAL_SURFACE_FORE = 1 << SURFACE_ID_FORE;			// 0b0100
	public static final int DECAL_SURFACE_AFT = 1 << SURFACE_ID_AFT;			// 0b1000
	public static final int DECAL_SURFACE_ALL = 0b1111;

	/**
	 * Vertex surface ID for faces that should never receive decals, such as the
	 * edge band around a fin. This ID's bit (1 << 5 = 32) falls outside
	 * {@link #DECAL_SURFACE_ALL} and is therefore excluded from every decal mask.
	 */
	public static final int SURFACE_ID_EDGE = 5;
}
