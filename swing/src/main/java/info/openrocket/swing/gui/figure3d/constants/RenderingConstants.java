package info.openrocket.swing.gui.figure3d.constants;

/**
 * Contains essential rendering constants that are actually used throughout the engine.
 * Only includes constants that are actively used by the codebase.
 */
public abstract class RenderingConstants {

	// --- Particle System Limits ---
	public static final int DEFAULT_MAX_PARTICLES = 10000;
	public static final int FLAME_MAX_QUADS = 3000;
	public static final int SMOKE_MAX_QUADS = 5000;

	// --- Geometry Quality Levels ---
	// Basic shapes (tubes, cones)
	public static final int LOW_SEGMENT_COUNT = 8;
	public static final int MEDIUM_SEGMENT_COUNT = 12;
	public static final int HIGH_SEGMENT_COUNT = 16;

	// Complex components (transitions, nose cones)
	public static final int LOW_COMPLEX_SEGMENT_COUNT = 16;
	public static final int MEDIUM_COMPLEX_SEGMENT_COUNT = 24;
	public static final int HIGH_COMPLEX_SEGMENT_COUNT = 32;

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

	// --- Decal Surface Constants ---
	public static final int DECAL_SURFACE_OUTSIDE = 1; 		// 0b0001
	public static final int DECAL_SURFACE_INSIDE = 2; 		// 0b0010
	public static final int DECAL_SURFACE_FORE = 4; 		// 0b0100
	public static final int DECAL_SURFACE_AFT = 8; 			// 0b1000
	public static final int DECAL_SURFACE_ALL = 0b1111;
}
