package info.openrocket.swing.gui.figure3d.ui;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/** Thread-safe queue of file exports and in-memory image captures. */
final class FrameExportQueue {
	static final class Request {
		private final boolean transparent;
		private final Consumer<BufferedImage> completion;

		private Request(boolean transparent, Consumer<BufferedImage> completion) {
			this.transparent = transparent;
			this.completion = completion;
		}

		boolean isTransparent() {
			return transparent;
		}

		boolean isImageCapture() {
			return completion != null;
		}

		void complete(BufferedImage image) {
			if (completion != null) {
				completion.accept(image);
			}
		}
	}

	private final ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<>();

	void requestFileExport(boolean transparent) {
		requests.add(new Request(transparent, null));
	}

	void requestImageCapture(boolean transparent, Consumer<BufferedImage> completion) {
		requests.add(new Request(transparent, Objects.requireNonNull(completion)));
	}

	Request poll() {
		return requests.poll();
	}

	boolean isEmpty() {
		return requests.isEmpty();
	}

	void failPendingCaptures() {
		Request request;
		while ((request = requests.poll()) != null) {
			request.complete(null);
		}
	}
}
