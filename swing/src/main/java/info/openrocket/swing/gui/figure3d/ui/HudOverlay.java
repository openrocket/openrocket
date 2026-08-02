package info.openrocket.swing.gui.figure3d.ui;

import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import org.lwjgl.system.MemoryUtil;

import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_STREAM_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL21.GL_PIXEL_UNPACK_BUFFER;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

/**
 * Draws the {@link HUDPanel} over the 3D scene.
 *
 * <p>The panel is a Swing component, so it can only be painted on the EDT, while the
 * frame it goes over is drawn on the render thread. The two are decoupled by a
 * BufferedImage: the EDT rasterises the panel into it, the render thread uploads it
 * to a texture and draws it as a full-screen quad. {@code hudLock} guards the image
 * and its buffers across those two threads.</p>
 */
class HudOverlay {

	/** Lower bound on the interval between low-priority repaints, in milliseconds. */
	private static final long MIN_PAINT_INTERVAL_MS = 66;

	private final HUDPanel panel;
	private final GLScenePanel canvas;

	/** Guards the image and its buffers between the EDT and the render thread. */
	private final Object lock = new Object();
	private BufferedImage image;
	private Graphics2D graphics;
	private Texture texture;
	private GLShader shader;
	private int vao;
	private int vbo;
	private int pbo;
	private int pboCapacityBytes;
	private ByteBuffer imageBuffer;
	private IntBuffer intBuffer;

	private final AtomicBoolean paintScheduled = new AtomicBoolean(false);
	private final AtomicBoolean bufferReady = new AtomicBoolean(false);
	private long lastPaintTimeMs;
	private volatile boolean needsUpdate = true;

	HudOverlay(HUDPanel panel, GLScenePanel canvas) {
		this.panel = panel;
		this.canvas = canvas;
	}

	HUDPanel getPanel() {
		return panel;
	}

	void markForUpdate() {
		needsUpdate = true;
	}

	/** Creates the shader, texture and quad the overlay is drawn with. */
	void initResources() {
		shader = new GLShader("/shaders/ui/hud_vertex.glsl", "/shaders/ui/hud_fragment.glsl");
		initTexture();
		initVao();
		requestRepaint(true);
	}

	/**
	 * Sizes the image, its buffers and the texture to the current framebuffer,
	 * reusing the existing allocation when it is close enough to the size needed.
	 */
	void initTexture() {
		synchronized (lock) {
			bufferReady.set(false);
			int[] fbSize = canvas.computeFramebufferSize();
			int fbWidth = fbSize[0];
			int fbHeight = fbSize[1];

			if (fbWidth <= 0 || fbHeight <= 0) {
				releaseResources();
				return;
			}

			if (image != null && imageBuffer != null) {
				int currentCapacity = imageBuffer.capacity();
				int requiredCapacity = fbWidth * fbHeight * 4;

				if (currentCapacity >= requiredCapacity && currentCapacity <= requiredCapacity * 1.25) {
					// The buffer still fits; only the texture and the image wrapping it
					// have to be rebuilt at the new size.
					if (texture != null) {
						texture.cleanup();
					}
					texture = new Texture(fbWidth, fbHeight, true);
					recreateImage(fbWidth, fbHeight);
					return;
				}
			}

			releaseResources();

			// Allocate with some padding to reduce future reallocations
			int paddedSize = (int) (fbWidth * fbHeight * 4 * 1.1);    // 10% padding

			recreateImage(fbWidth, fbHeight);
			imageBuffer = MemoryUtil.memAlloc(paddedSize);
			intBuffer = imageBuffer.asIntBuffer();

			// Create texture directly for THIS context - don't use shared pool
			texture = new Texture(fbWidth, fbHeight, true);

			// Allocate the PBO at the same capacity as the host pixel buffer so
			// glBufferData(orphan) → glBufferSubData(host) → glTexSubImage2D(0L)
			// can stream uploads without ever stalling on prior GPU usage.
			pbo = glGenBuffers();
			pboCapacityBytes = paddedSize;
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pbo);
			glBufferData(GL_PIXEL_UNPACK_BUFFER, pboCapacityBytes, GL_STREAM_DRAW);
			glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
		}
	}

	private void recreateImage(int fbWidth, int fbHeight) {
		if (graphics != null) {
			graphics.dispose();
		}
		image = new BufferedImage(fbWidth, fbHeight, BufferedImage.TYPE_INT_ARGB);
		graphics = image.createGraphics();
		graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
	}

	private void releaseResources() {
		if (texture != null) {
			// Cleanup directly - don't return to shared pool
			texture.cleanup();
			texture = null;
		}
		if (pbo != 0) {
			glDeleteBuffers(pbo);
			pbo = 0;
			pboCapacityBytes = 0;
		}
		freeHostBuffers();
	}

	private void freeHostBuffers() {
		if (imageBuffer != null) {
			MemoryUtil.memFree(imageBuffer);
			imageBuffer = null;
			intBuffer = null;
		}
		if (graphics != null) {
			graphics.dispose();
			graphics = null;
		}
		image = null;
	}

	private void initVao() {
		float[] quadVertices = {
				// positions   // texCoords (V coordinates are flipped)
				-1.0f, 1.0f, 0.0f, 0.0f,  // Top-left vertex maps to V=0.0
				-1.0f, -1.0f, 0.0f, 1.0f,  // Bottom-left vertex maps to V=1.0
				1.0f, -1.0f, 1.0f, 1.0f,  // Bottom-right vertex maps to V=1.0

				-1.0f, 1.0f, 0.0f, 0.0f,  // Top-left vertex maps to V=0.0
				1.0f, -1.0f, 1.0f, 1.0f,  // Bottom-right vertex maps to V=1.0
				1.0f, 1.0f, 1.0f, 0.0f   // Top-right vertex maps to V=0.0
		};
		vao = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao, "HUD vao");
		vbo = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vbo, "HUD vbo");
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
		glBindVertexArray(0);
	}

	/**
	 * Repaints the overlay if it has gone stale.
	 *
	 * @param cameraIsMoving whether the camera is mid-gesture, in which case repaints
	 *                       are held to the rate limit so the upload does not block the
	 *                       render thread every frame
	 */
	void refresh(boolean cameraIsMoving) {
		boolean panelNeedsRepaint = panel != null && panel.needsRepaint();
		boolean repaintRequested = false;
		if (needsUpdate || panelNeedsRepaint) {
			// While the camera is being dragged, the orientation gizmo wants
			// to repaint every frame. Force those into the rate-limited path
			// so the HUD upload doesn't block the render thread at 60 fps.
			repaintRequested = requestRepaint(panelNeedsRepaint && !cameraIsMoving);
		}
		if (repaintRequested) {
			needsUpdate = false;
		}
	}

	/** Schedules a repaint on the EDT, subject to the rate limit. */
	boolean requestRepaint(boolean highPriority) {
		if (panel == null || graphics == null || !canvas.isSceneReady()) {
			return false;
		}
		if (!highPriority) {
			long now = System.currentTimeMillis();
			if (now - lastPaintTimeMs < MIN_PAINT_INTERVAL_MS) {
				return false;
			}
		}
		if (!paintScheduled.compareAndSet(false, true)) {
			return true;
		}
		SwingUtilities.invokeLater(() -> {
			try {
				paintOnEdt();
			} finally {
				paintScheduled.set(false);
			}
		});
		return true;
	}

	private void paintOnEdt() {
		synchronized (lock) {
			if (image == null || panel == null || graphics == null || imageBuffer == null || intBuffer == null
					|| !canvas.isSceneReady()) {
				return;
			}

			bufferReady.set(false);

			// The panel works in logical window coordinates
			int windowWidth = Math.max(1, canvas.getWidth());
			int windowHeight = Math.max(1, canvas.getHeight());
			int[] fbSize = canvas.computeFramebufferSize(windowWidth, windowHeight);
			double dpiScale = (double) fbSize[1] / (double) windowHeight;

			graphics.setTransform(graphics.getDeviceConfiguration().getDefaultTransform());
			graphics.scale(dpiScale, dpiScale);

			// Clear with transparent background
			graphics.setBackground(new Color(0, 0, 0, 0));
			graphics.clearRect(0, 0, windowWidth, windowHeight);

			panel.setBounds(0, 0, windowWidth, windowHeight);
			panel.paint(graphics);

			final int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
			intBuffer.clear();
			intBuffer.put(pixels);
			imageBuffer.rewind();
			lastPaintTimeMs = System.currentTimeMillis();
			bufferReady.set(true);
			canvas.markRenderActivity();
		}
	}

	/** Uploads the last rasterised image to the texture, if the EDT has produced one. */
	void uploadIfReady() {
		if (texture == null || shader == null) {
			return;
		}
		if (!bufferReady.get()) {
			return;
		}
		synchronized (lock) {
			if (!bufferReady.get() || texture == null || image == null || imageBuffer == null) {
				return;
			}
			bufferReady.set(false);

			int byteSize = image.getWidth() * image.getHeight() * 4;

			if (pbo != 0) {
				// Async path: orphan the PBO, copy host bytes into pinned memory, then
				// glTexSubImage2D from the bound PBO returns immediately and the GPU
				// performs the upload concurrently with subsequent rendering.
				glBindBuffer(GL_PIXEL_UNPACK_BUFFER, pbo);
				glBufferData(GL_PIXEL_UNPACK_BUFFER, pboCapacityBytes, GL_STREAM_DRAW);
				int oldLimit = imageBuffer.limit();
				imageBuffer.limit(byteSize);
				glBufferSubData(GL_PIXEL_UNPACK_BUFFER, 0, imageBuffer);
				imageBuffer.limit(oldLimit);

				glBindTexture(GL_TEXTURE_2D, texture.getId());
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image.getWidth(), image.getHeight(),
						GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, 0L);
				glBindBuffer(GL_PIXEL_UNPACK_BUFFER, 0);
			} else {
				// Fallback if PBO allocation failed.
				glBindTexture(GL_TEXTURE_2D, texture.getId());
				glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, image.getWidth(), image.getHeight(),
						GL_BGRA, GL_UNSIGNED_INT_8_8_8_8_REV, imageBuffer);
			}
		}
	}

	/** Draws the uploaded texture over the presented scene as a full-screen quad. */
	void draw() {
		if (texture == null || shader == null) {
			return;
		}

		// Set GL state for 2D rendering
		glDisable(GL_DEPTH_TEST);
		glEnable(GL_BLEND);
		glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
		// The HUD is rasterized by Java2D, which composites in sRGB space. Blending
		// it with the sRGB encode still enabled would blend in linear space instead,
		// which lightens translucent panels well beyond their requested alpha and
		// pulls partially covered glyph pixels towards the panel colour, leaving the
		// text thin and washed out. The scene has already been presented at this
		// point, so the encode can be turned off for the overlay only.
		glDisable(GL_FRAMEBUFFER_SRGB);

		shader.use();

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture.getId());

		glBindVertexArray(vao);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindVertexArray(0);

		// Restore GL state
		glEnable(GL_FRAMEBUFFER_SRGB);
		glEnable(GL_DEPTH_TEST);
		glDisable(GL_BLEND);
	}

	/** Clears the scheduling flags, so a torn-down canvas does not resume mid-upload. */
	void resetPendingWork() {
		paintScheduled.set(false);
		bufferReady.set(false);
	}

	/** Releases the GL objects. Must run with the context current. */
	void cleanupGlResources() {
		if (vao != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao);
			glDeleteVertexArrays(vao);
			vao = 0;
		}
		if (vbo != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, vbo);
			glDeleteBuffers(vbo);
			vbo = 0;
		}
		if (shader != null) {
			shader.cleanup();
			shader = null;
		}
		if (texture != null) {
			texture.cleanup();
			texture = null;
		}
	}

	/** Frees the host-side buffers. Safe without a GL context. */
	void cleanupHostResources() {
		synchronized (lock) {
			freeHostBuffers();
			bufferReady.set(false);
			texture = null;
			shader = null;
		}
		if (panel != null) {
			panel.setGLScenePanel(null);
			panel.setSceneViewController(null);
		}
	}
}
