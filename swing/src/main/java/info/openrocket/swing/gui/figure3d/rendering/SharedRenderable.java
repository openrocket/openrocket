package info.openrocket.swing.gui.figure3d.rendering;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shares one renderable resource through independently releasable leases.
 * The delegate is cleaned when the final lease is released.
 */
public final class SharedRenderable {
	private final Renderable delegate;
	private int leaseCount;
	private boolean cleaned;

	/** @param delegate GPU resource owned by this shared wrapper */
	public SharedRenderable(Renderable delegate) {
		if (delegate == null) {
			throw new IllegalArgumentException("Renderable delegate cannot be null");
		}
		this.delegate = delegate;
	}

	/** @return a new independently releasable view of the shared resource */
	public synchronized Renderable acquire() {
		if (cleaned) {
			throw new IllegalStateException("Renderable resource has already been cleaned");
		}
		leaseCount++;
		return new Lease();
	}

	private synchronized void release() {
		if (leaseCount <= 0) {
			throw new IllegalStateException("Renderable resource has no active leases");
		}
		leaseCount--;
		if (leaseCount == 0) {
			cleaned = true;
			delegate.cleanup();
		}
	}

	private final class Lease implements Renderable {
		private final AtomicBoolean released = new AtomicBoolean(false);

		@Override
		public void render() {
			synchronized (SharedRenderable.this) {
				if (released.get()) {
					throw new IllegalStateException("Renderable lease has already been released");
				}
				delegate.render();
			}
		}

		@Override
		public void cleanup() {
			if (released.compareAndSet(false, true)) {
				release();
			}
		}
	}
}
