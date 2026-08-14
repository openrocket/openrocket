package info.openrocket.swing.gui.figure3d.constants;

/** Shared geometry and rendering constants. */
public abstract class RenderingConstants {

	// --- Particle System Limits ---
	public static final int DEFAULT_MAX_PARTICLES = 10000;
	public static final int FLAME_MAX_QUADS = 3000;
	public static final int SMOKE_MAX_QUADS = 5000;
	public static final int MAX_LIGHTS = 10;

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

	// --- Surface IDs ---
	// The fragment shader uses these to distinguish inner walls from visible surfaces.
	public static final int SURFACE_ID_OUTSIDE = 0;
	public static final int SURFACE_ID_INSIDE = 1;
	public static final int SURFACE_ID_FORE = 2;
	public static final int SURFACE_ID_AFT = 3;

	/** Vertex surface ID for a fin's edge band and fillet caps. */
	public static final int SURFACE_ID_EDGE = 5;
}
