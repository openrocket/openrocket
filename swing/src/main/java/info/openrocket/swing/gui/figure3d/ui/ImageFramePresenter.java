package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.swing.gui.figure3d.rendering.GLRenderer;
import org.lwjgl.system.MemoryUtil;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL11.glReadBuffer;
import static org.lwjgl.opengl.GL11.glReadPixels;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL21.GL_PIXEL_PACK_BUFFER;
import static org.lwjgl.opengl.GL30.GL_COLOR_ATTACHMENT0;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_BINDING;
import static org.lwjgl.opengl.GL30.GL_READ_BUFFER;
import static org.lwjgl.opengl.GL30.glBindFramebuffer;

/**
 * Presents finished frames by reading them back into an image and drawing that to
 * the Canvas peer, instead of swapping the GL drawable.
 *
 * <p>This is the macOS path. There, presenting through the NSOpenGLView layer runs
 * -[NSOpenGLContext setView:] on whichever thread swaps, which aborts in AppKit's
 * main-thread check; see PRESENT_VIA_IMAGE_ON_MACOS.</p>
 *
 * <p>The render thread reads the frame back and publishes it; the EDT draws it in
 * paint(). Two staging images are alternated so the reader and the writer never
 * touch the same one, and {@code lock} covers the hand-off.</p>
 */
class ImageFramePresenter {

	private final GLScenePanel canvas;

	private final Object lock = new Object();
	private volatile BufferedImage presentedImage;
	private BufferedImage presentedImageA;
	private BufferedImage presentedImageB;
	private ByteBuffer readbackBuffer;
	private volatile boolean hasContent = false;

	ImageFramePresenter(GLScenePanel canvas) {
		this.canvas = canvas;
	}

	/** Whether the last published frame had anything in the middle of it. */
	boolean hasContent() {
		return hasContent;
	}

	boolean hasPublishedFrame() {
		return presentedImage != null;
	}

	/** The lock the EDT must hold to paint a consistent frame. */
	Object presentLock() {
		return lock;
	}

	/**
	 * Reads the finished frame back from the renderer's resolved FBO, publishes it
	 * and draws it. Runs on the render thread.
	 */
	void presentFrame(GLRenderer renderer) {
		int width = renderer.getRenderWidth();
		int height = renderer.getRenderHeight();
		if (width <= 0 || height <= 0) {
			return;
		}

		int requiredBytes = width * height * 4;
		if (readbackBuffer == null || readbackBuffer.capacity() < requiredBytes) {
			if (readbackBuffer != null) {
				MemoryUtil.memFree(readbackBuffer);
			}
			readbackBuffer = MemoryUtil.memAlloc(requiredBytes);
		}
		readbackBuffer.clear();

		int previousFramebuffer = glGetInteger(GL_FRAMEBUFFER_BINDING);
		int previousReadBuffer = glGetInteger(GL_READ_BUFFER);
		glBindBuffer(GL_PIXEL_PACK_BUFFER, 0);
		glBindFramebuffer(GL_FRAMEBUFFER, renderer.getResolvedFramebufferId());
		glReadBuffer(GL_COLOR_ATTACHMENT0);
		// BGRA + INT_8_8_8_8_REV packs each pixel as a native-order int that matches
		// the TYPE_INT_* layout, so rows can be bulk-copied without per-pixel swizzling.
		glReadPixels(0, 0, width, height, GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, readbackBuffer);
		glBindFramebuffer(GL_FRAMEBUFFER, previousFramebuffer);
		glReadBuffer(previousReadBuffer);

		IntBuffer pixels = readbackBuffer.asIntBuffer();
		hasContent = pixels.get((height / 2) * width + width / 2) != 0;

		synchronized (lock) {
			BufferedImage staging = nextStagingImage(width, height);
			int[] target = ((DataBufferInt) staging.getRaster().getDataBuffer()).getData();
			for (int y = 0; y < height; y++) {
				// GL rows are bottom-up; images are top-down
				pixels.get(target, (height - 1 - y) * width, width);
			}
			presentedImage = staging;
		}
		blitToPeer();
	}

	/**
	 * Returns the ping-pong image the render thread may write to (the one the EDT
	 * is not currently painting), recreating it if the frame size changed.
	 */
	private BufferedImage nextStagingImage(int width, int height) {
		BufferedImage current = presentedImage;
		BufferedImage staging = (current == presentedImageA) ? presentedImageB : presentedImageA;
		if (staging == null || staging.getWidth() != width || staging.getHeight() != height) {
			// INT_RGB so the frame is drawn opaque, ignoring the FBO's alpha channel
			staging = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			if (current == presentedImageA) {
				presentedImageB = staging;
			} else {
				presentedImageA = staging;
			}
		}
		return staging;
	}

	/**
	 * Draws the published frame directly to the Canvas peer. A BufferStrategy is
	 * deliberately not used here: AppKit discards its native buffers during a
	 * live resize, exposing their white initialization before Java can refill
	 * them. A fresh peer Graphics always targets the current surface, while
	 * the canvas's paint() handles normal expose and damage events.
	 */
	void blitToPeer() {
		if (!canvas.isShowing()) {
			// A frame finishing right as the user switches to the 2D view must not
			// blit over the newly shown card.
			return;
		}
		synchronized (lock) {
			Graphics g = canvas.getGraphics();
			if (g == null) {
				canvas.repaint();
				return;
			}
			try {
				paintInto(g);
			} finally {
				g.dispose();
			}
			Toolkit.getDefaultToolkit().sync();
		}
	}

	/** Composites the latest presented frame and the HUD into the given graphics. */
	void paintInto(Graphics g) {
		int width = canvas.getWidth();
		int height = canvas.getHeight();
		BufferedImage image = presentedImage;
		if (image == null) {
			g.setColor(canvas.getBackground());
			g.fillRect(0, 0, width, height);
			return;
		}
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		// The image is at framebuffer (HiDPI) resolution; drawing at the logical
		// size maps it 1:1 onto the physical pixels of the scaled Graphics.
		g2.drawImage(image, 0, 0, width, height, null);
		canvas.compositeHud(g2, width, height);
	}

	/**
	 * Frees the readback buffer. Only safe while no render is in flight, since the
	 * render thread reads into it during {@link #presentFrame}.
	 */
	void cleanup() {
		if (readbackBuffer != null) {
			MemoryUtil.memFree(readbackBuffer);
			readbackBuffer = null;
		}
	}
}
