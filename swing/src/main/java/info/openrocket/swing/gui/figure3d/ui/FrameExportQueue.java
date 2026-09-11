package info.openrocket.swing.gui.figure3d.ui;

import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
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

	private final Queue<Request> requests = new ArrayDeque<>();
	private boolean closed;

	synchronized boolean requestFileExport(boolean transparent) {
		if (closed) {
			return false;
		}
		requests.add(new Request(transparent, null));
		return true;
	}

	synchronized boolean requestImageCapture(boolean transparent, Consumer<BufferedImage> completion) {
		if (closed) {
			return false;
		}
		requests.add(new Request(transparent, Objects.requireNonNull(completion)));
		return true;
	}

	synchronized Request poll() {
		return requests.poll();
	}

	synchronized boolean isEmpty() {
		return requests.isEmpty();
	}

	void closeAndFailPendingCaptures() {
		List<Request> pending;
		synchronized (this) {
			closed = true;
			pending = new ArrayList<>(requests);
			requests.clear();
		}
		for (Request request : pending) {
			request.complete(null);
		}
	}
}
