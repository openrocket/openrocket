package info.openrocket.swing.gui.figure3d.rendering;

import info.openrocket.core.document.OpenRocketDocument;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport;
import info.openrocket.swing.gui.figure3d.GoldenImageTestSupport.RenderHarness;
import info.openrocket.swing.util.BaseTestCase;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Verifies that disposing a 3D canvas releases every GPU resource its renderer created. */
@Tag("requires-live-opengl")
class RealisticRendererCleanupTest extends BaseTestCase {

	@Test
	@Timeout(value = 60, unit = TimeUnit.SECONDS, threadMode = Timeout.ThreadMode.SEPARATE_THREAD)
	void disposingCanvasReleasesAllRendererResources() throws Exception {
		Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
				"Renderer cleanup test requires a live graphical environment");
		OpenRocketDocument document = GoldenImageTestSupport.createStyledRocketDocument();
		// Earlier tests in the same JVM may have left resources registered, so compare against
		// the count before this canvas existed rather than expecting zero.
		int programsBefore = GpuResourceTracker.liveCount(GpuResourceTracker.ResourceType.PROGRAM);
		int resourcesBefore = GpuResourceTracker.liveCount();

		try (RenderHarness harness = GoldenImageTestSupport.createRenderHarness(
				document.getRocket(), "Renderer cleanup", null)) {
			GoldenImageTestSupport.awaitInitialized(harness);
		}

		assertEquals(programsBefore, GpuResourceTracker.liveCount(GpuResourceTracker.ResourceType.PROGRAM),
				"Shader programs leaked by canvas disposal");
		assertEquals(resourcesBefore, GpuResourceTracker.liveCount(),
				"GPU resources leaked by canvas disposal");
	}
}
