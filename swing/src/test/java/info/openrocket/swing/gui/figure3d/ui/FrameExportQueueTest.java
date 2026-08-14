package info.openrocket.swing.gui.figure3d.ui;

import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrameExportQueueTest {

	@Test
	void requestsKeepTheirTransparencyAndCompletionTogether() {
		FrameExportQueue queue = new FrameExportQueue();
		List<BufferedImage> completed = new ArrayList<>();
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		assertTrue(queue.requestImageCapture(false, completed::add));
		assertTrue(queue.requestFileExport(true));

		FrameExportQueue.Request capture = queue.poll();
		assertFalse(capture.isTransparent());
		assertTrue(capture.isImageCapture());
		capture.complete(image);

		FrameExportQueue.Request fileExport = queue.poll();
		assertTrue(fileExport.isTransparent());
		assertFalse(fileExport.isImageCapture());
		assertEquals(List.of(image), completed);
	}

	@Test
	void failingPendingRequestsCompletesEveryCapture() {
		FrameExportQueue queue = new FrameExportQueue();
		List<BufferedImage> completed = new ArrayList<>();
		assertTrue(queue.requestImageCapture(false, completed::add));
		assertTrue(queue.requestFileExport(false));
		assertTrue(queue.requestImageCapture(true, completed::add));

		queue.closeAndFailPendingCaptures();

		assertTrue(queue.isEmpty());
		assertEquals(2, completed.size());
		assertNull(completed.get(0));
		assertNull(completed.get(1));
	}

	@Test
	void closingRejectsLaterRequestsAndLetsTheCallerFailTheirCompletion() {
		FrameExportQueue queue = new FrameExportQueue();
		List<BufferedImage> completed = new ArrayList<>();

		queue.closeAndFailPendingCaptures();

		assertFalse(queue.requestImageCapture(false, completed::add));
		assertFalse(queue.requestFileExport(false));
		assertTrue(queue.isEmpty());
		assertTrue(completed.isEmpty());
	}
}
