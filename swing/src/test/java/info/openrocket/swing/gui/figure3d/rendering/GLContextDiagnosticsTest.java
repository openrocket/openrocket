package info.openrocket.swing.gui.figure3d.rendering;

import org.junit.jupiter.api.Test;
import org.lwjgl.opengl.awt.GLData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GLContextDiagnosticsTest {

	@Test
	void recordsRequestedAndEffectiveContextAttributesWithoutKeepingGLData() {
		GLData requested = new GLData();
		requested.majorVersion = 3;
		requested.minorVersion = 1;
		requested.versionPolicy = GLData.VersionPolicy.AT_LEAST;
		requested.profile = null;
		requested.samples = 0;
		requested.swapInterval = 0;

		GLData effective = new GLData();
		effective.majorVersion = 4;
		effective.minorVersion = 1;
		effective.versionPolicy = GLData.VersionPolicy.AT_LEAST;
		effective.profile = GLData.Profile.CORE;
		effective.stencilSize = 8;
		effective.sampleBuffers = 1;
		effective.samples = 4;
		effective.swapInterval = 0;

		String recorded = GLContextDiagnostics.record(requested, effective);

		assertTrue(recorded.contains("requested [OpenGL 3.1, version-policy=at_least, profile=unspecified"));
		assertTrue(recorded.contains("effective [OpenGL 4.1, version-policy=at_least, profile=core"));
		assertTrue(recorded.contains("RGBA=8/8/8/8, depth/stencil=24/8"));
		assertTrue(recorded.contains("sample-buffers=1, samples=4"));
		assertTrue(recorded.contains("double-buffer=true"));
		assertTrue(recorded.contains("swap-interval=0"));
		assertEquals(recorded, GLContextDiagnostics.describeForBugReport());

		// Prove the cached diagnostic is a value snapshot rather than the mutable
		// GLData instance owned by AWTGLCanvas.
		effective.samples = 16;
		assertEquals(recorded, GLContextDiagnostics.describeForBugReport());
	}
}
