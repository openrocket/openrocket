package info.openrocket.swing.gui.figure3d.rendering;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GpuResourceTrackerTest {

	@AfterEach
	void resetTracker() {
		GpuResourceTracker.reset();
	}

	@Test
	void tracksEqualHandlesIndependentlyAcrossContexts() {
		Object designContext = new Object();
		Object photoContext = new Object();

		GpuResourceTracker.registerForContext(designContext,
				GpuResourceTracker.ResourceType.TEXTURE, 1, "design texture");
		GpuResourceTracker.registerForContext(photoContext,
				GpuResourceTracker.ResourceType.TEXTURE, 1, "photo texture");

		assertEquals(2, GpuResourceTracker.liveCount(GpuResourceTracker.ResourceType.TEXTURE));

		GpuResourceTracker.releaseForContext(designContext,
				GpuResourceTracker.ResourceType.TEXTURE, 1);
		assertEquals(1, GpuResourceTracker.liveCount(GpuResourceTracker.ResourceType.TEXTURE));

		GpuResourceTracker.releaseForContext(photoContext,
				GpuResourceTracker.ResourceType.TEXTURE, 1);
		assertEquals(0, GpuResourceTracker.liveCount(GpuResourceTracker.ResourceType.TEXTURE));
	}

	@Test
	void duplicateRegistrationDoesNotReplaceTheLiveRecord() {
		Object context = new Object();

		GpuResourceTracker.registerForContext(context,
				GpuResourceTracker.ResourceType.BUFFER, 7, "first");
		GpuResourceTracker.registerForContext(context,
				GpuResourceTracker.ResourceType.BUFFER, 7, "duplicate");

		assertEquals(1, GpuResourceTracker.liveCount(GpuResourceTracker.ResourceType.BUFFER));
	}
}
