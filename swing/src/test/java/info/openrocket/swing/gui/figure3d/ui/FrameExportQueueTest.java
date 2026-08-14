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

		queue.requestImageCapture(false, completed::add);
		queue.requestFileExport(true);

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
		queue.requestImageCapture(false, completed::add);
		queue.requestFileExport(false);
		queue.requestImageCapture(true, completed::add);

		queue.failPendingCaptures();

		assertTrue(queue.isEmpty());
		assertEquals(2, completed.size());
		assertNull(completed.get(0));
		assertNull(completed.get(1));
	}
}
