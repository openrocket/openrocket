package info.openrocket.swing.gui.figure3d.flight;

import info.openrocket.core.l10n.Translator;
import info.openrocket.core.simulation.FlightDataType;
import info.openrocket.core.startup.Application;
import info.openrocket.swing.gui.figure3d.materials.Texture;
import info.openrocket.swing.gui.figure3d.rendering.FrameOverlay;
import info.openrocket.swing.gui.figure3d.rendering.GLShader;
import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.nio.IntBuffer;
import java.util.function.IntSupplier;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_CULL_FACE;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SCISSOR_TEST;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGetIntegerv;
import static org.lwjgl.opengl.GL11.glIsEnabled;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL12.GL_BGRA;
import static org.lwjgl.opengl.GL12.GL_UNSIGNED_INT_8_8_8_8_REV;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

/**
 * A small HUD dial in the replay view's bottom left corner showing the wind the tracked body flies
 * through: an arrow pointing where the air blows, turned with the camera so it reads against
 * the view (up is away from the viewer), with the speed and the direction it blows from below. An "N" tick on the ring marks north.
 *
 * <p>The dial is drawn with Java2D into a small image, redrawn only when what it shows changes,
 * and composited as a {@link FrameOverlay}. Everything runs on the render thread.
 */
final class WindIndicator implements FrameOverlay {
	private static final Translator trans = Application.getTranslator();

	// Layout in logical (unscaled) pixels.
	static final int WIDTH = 92;
	static final int HEIGHT = 114;
	private static final int MARGIN = 12;
	static final float DIAL_RADIUS = 26.0f;
	static final float DIAL_CENTER_Y = 46.0f;
	// Don't let the dial cover more than this share of a short view.
	private static final float MAX_HEIGHT_FRACTION = 0.3f;

	private static final float ARROW_LENGTH = 1.5f * DIAL_RADIUS;

	private static final Color BACKGROUND = new Color(18, 20, 24, 90);
	private static final Color RING = new Color(255, 255, 255, 80);
	private static final Color TICK = new Color(255, 255, 255, 110);
	private static final Color NORTH = new Color(225, 95, 95);
	private static final Color ARROW = new Color(140, 205, 230);
	private static final Color TITLE = new Color(200, 205, 210, 190);
	private static final Color VALUE = new Color(232, 234, 237, 220);

	private final IntSupplier logicalHeight;
	private final GLShader shader;
	private final int vao;
	private final int vbo;

	private WindField wind;
	private double time;
	private Texture texture;
	private BufferedImage image;
	private IntBuffer pixelBuffer;
	private Dial shown;

	/**
	 * What the dial shows; the image is redrawn only when this changes.
	 *
	 * @param windAngle   screen angle of the downwind direction, degrees clockwise from up, rounded
	 * @param northAngle  screen angle of north, likewise
	 * @param calm        whether the air is too still to show a direction
	 */
	record Dial(int windAngle, int northAngle, boolean calm, String speed, String direction, int width, int height) {
	}

	/**
	 * Must be created on the GL thread.
	 *
	 * @param logicalHeight the canvas's height in logical pixels, to scale the dial for the display
	 */
	WindIndicator(IntSupplier logicalHeight) {
		this.logicalHeight = logicalHeight;
		shader = new GLShader("/shaders/ui/hud_vertex.glsl", "/shaders/ui/hud_fragment.glsl");
		shader.use();
		shader.setUniformInt(shader.requireUniformLocation("hudTexture"), 0);
		shader.unbind();

		float[] quad = {
				// position, texture coordinates (V flipped: the image's first row is its top)
				-1.0f, 1.0f, 0.0f, 0.0f,
				-1.0f, -1.0f, 0.0f, 1.0f,
				1.0f, -1.0f, 1.0f, 1.0f,
				-1.0f, 1.0f, 0.0f, 0.0f,
				1.0f, -1.0f, 1.0f, 1.0f,
				1.0f, 1.0f, 1.0f, 0.0f
		};
		vao = glGenVertexArrays();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao, "Wind indicator vao");
		vbo = glGenBuffers();
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.BUFFER, vbo, "Wind indicator vbo");
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, quad, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
		glEnableVertexAttribArray(1);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
		glBindVertexArray(0);
	}

	/** Shows the given wind at the given flight time; call on the GL thread. */
	void setWind(WindField wind, double time) {
		this.wind = wind;
		this.time = time;
	}

	@Override
	public void render(Matrix4f cameraViewMatrix, int width, int height) {
		if (wind == null || width <= 0 || height <= 0) {
			return;
		}
		int logical = logicalHeight.getAsInt();
		float scale = logical > 0 ? (float) height / logical : 1.0f;
		scale = Math.min(scale, MAX_HEIGHT_FRACTION * height / HEIGHT);
		int pixelWidth = Math.max(1, Math.round(WIDTH * scale));
		int pixelHeight = Math.max(1, Math.round(HEIGHT * scale));
		int margin = Math.round(MARGIN * scale);
		if (pixelWidth + margin > width || pixelHeight + margin > height) {
			return;
		}

		Dial dial = dialFor(wind, time, cameraViewMatrix, pixelWidth, pixelHeight);
		if (!dial.equals(shown)) {
			upload(dial, scale);
			shown = dial;
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer previousViewport = stack.mallocInt(4);
			glGetIntegerv(GL_VIEWPORT, previousViewport);
			boolean scissorWasEnabled = glIsEnabled(GL_SCISSOR_TEST);
			boolean cullWasEnabled = glIsEnabled(GL_CULL_FACE);
			boolean depthWasEnabled = glIsEnabled(GL_DEPTH_TEST);
			boolean blendWasEnabled = glIsEnabled(GL_BLEND);
			boolean srgbWasEnabled = glIsEnabled(GL_FRAMEBUFFER_SRGB);

			// The framebuffer's origin is its bottom left.
			glViewport(margin, margin, pixelWidth, pixelHeight);
			glDisable(GL_SCISSOR_TEST);
			glDisable(GL_CULL_FACE);
			glDisable(GL_DEPTH_TEST);
			glEnable(GL_BLEND);
			glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
			// Java2D composites in sRGB space; blend its pixels as they are (see HudOverlay).
			glDisable(GL_FRAMEBUFFER_SRGB);

			shader.use();
			glActiveTexture(GL_TEXTURE0);
			glBindTexture(GL_TEXTURE_2D, texture.getId());
			glBindVertexArray(vao);
			glDrawArrays(GL_TRIANGLES, 0, 6);
			glBindVertexArray(0);

			glViewport(previousViewport.get(0), previousViewport.get(1),
					previousViewport.get(2), previousViewport.get(3));
			setEnabled(GL_SCISSOR_TEST, scissorWasEnabled);
			setEnabled(GL_CULL_FACE, cullWasEnabled);
			setEnabled(GL_DEPTH_TEST, depthWasEnabled);
			setEnabled(GL_BLEND, blendWasEnabled);
			setEnabled(GL_FRAMEBUFFER_SRGB, srgbWasEnabled);
		}
	}

	/** What the dial shows for the wind at a flight time, seen through the given camera. */
	static Dial dialFor(WindField wind, double time, Matrix4f cameraViewMatrix, int width, int height) {
		double speed = wind.speedAt(time);
		String speedText = FlightDataType.TYPE_WIND_VELOCITY.getUnitGroup().toStringUnit(speed);
		int northAngle = (int) Math.round(Math.toDegrees(screenAngle(cameraViewMatrix, new Vector3f(0, 0, -1))));
		if (speed < WindField.CALM_SPEED) {
			return new Dial(0, northAngle, true, trans.get("Flight3DFrame.wind.calm"), "", width, height);
		}
		double windAngle = Math.toDegrees(screenAngle(cameraViewMatrix, wind.velocityAt(time)));
		String direction = String.format(trans.get("Flight3DFrame.wind.from"),
				FlightDataType.TYPE_WIND_DIRECTION.getUnitGroup().toStringUnit(wind.directionAt(time)));
		return new Dial((int) Math.round(windAngle), northAngle, false, speedText, direction, width, height);
	}

	/**
	 * Where a horizontal world direction points on a dial turned with the camera, in radians
	 * clockwise from up: up is the camera's horizontal forward direction, right its right.
	 * Its vertical part is ignored.
	 */
	static double screenAngle(Matrix4f cameraViewMatrix, Vector3f direction) {
		Vector3f right = cameraViewMatrix.positiveX(new Vector3f());
		right.y = 0.0f;
		if (right.lengthSquared() < 1.0e-8f) {
			right.set(1.0f, 0.0f, 0.0f);
		}
		right.normalize();
		// Up × right: the horizontal direction the camera faces.
		Vector3f forward = new Vector3f(0.0f, 1.0f, 0.0f).cross(right);
		return Math.atan2(direction.dot(right), direction.dot(forward));
	}

	private void upload(Dial dial, float scale) {
		if (texture == null || image == null || texture.getWidth() != dial.width()
				|| texture.getHeight() != dial.height()) {
			releaseImage();
			texture = new Texture(dial.width(), dial.height(), true);
			image = new BufferedImage(dial.width(), dial.height(), BufferedImage.TYPE_INT_ARGB);
			pixelBuffer = MemoryUtil.memAllocInt(dial.width() * dial.height());
		}
		Graphics2D graphics = image.createGraphics();
		try {
			graphics.setComposite(AlphaComposite.Clear);
			graphics.fillRect(0, 0, dial.width(), dial.height());
			graphics.setComposite(AlphaComposite.SrcOver);
			graphics.scale(scale, scale);
			paint(graphics, dial);
		} finally {
			graphics.dispose();
		}
		int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		pixelBuffer.clear();
		pixelBuffer.put(pixels).flip();
		glBindTexture(GL_TEXTURE_2D, texture.getId());
		glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, dial.width(), dial.height(), GL_BGRA,
				GL_UNSIGNED_INT_8_8_8_8_REV, pixelBuffer);
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	/** Paints the dial in logical pixels. */
	static void paint(Graphics2D g, Dial dial) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

		g.setColor(BACKGROUND);
		g.fill(new RoundRectangle2D.Float(0, 0, WIDTH, HEIGHT, 10, 10));

		Font base = new Font(Font.SANS_SERIF, Font.PLAIN, 10);

		float cx = WIDTH / 2.0f;
		float cy = DIAL_CENTER_Y;
		g.setColor(RING);
		g.setStroke(new BasicStroke(1.2f));
		g.draw(new Ellipse2D.Float(cx - DIAL_RADIUS, cy - DIAL_RADIUS, 2 * DIAL_RADIUS, 2 * DIAL_RADIUS));
		// A tick every 90° from north, north itself in red and labeled.
		for (int i = 0; i < 4; i++) {
			double angle = Math.toRadians(dial.northAngle() + 90.0 * i);
			float sin = (float) Math.sin(angle);
			float cos = (float) -Math.cos(angle);
			g.setColor(i == 0 ? NORTH : TICK);
			g.setStroke(new BasicStroke(i == 0 ? 2.0f : 1.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
			g.draw(new Line2D.Float(cx + sin * (DIAL_RADIUS - 4), cy + cos * (DIAL_RADIUS - 4),
					cx + sin * DIAL_RADIUS, cy + cos * DIAL_RADIUS));
		}
		double north = Math.toRadians(dial.northAngle());
		g.setFont(base.deriveFont(Font.BOLD, 9f));
		g.setColor(NORTH);
		FontMetrics metrics = g.getFontMetrics();
		// Outside the ring, where the arrow never reaches.
		float labelRadius = DIAL_RADIUS + 8;
		float nx = cx + (float) Math.sin(north) * labelRadius;
		float ny = cy - (float) Math.cos(north) * labelRadius;
		g.drawString("N", nx - metrics.stringWidth("N") / 2.0f,
				ny + (metrics.getAscent() - metrics.getDescent()) / 2.0f);

		if (!dial.calm()) {
			paintArrow(g, cx, cy, Math.toRadians(dial.windAngle()), ARROW_LENGTH);
		}

		g.setFont(new Font(Font.MONOSPACED, Font.BOLD, 11));
		g.setColor(VALUE);
		drawCentered(g, dial.speed(), (int) (cy + DIAL_RADIUS + 22));
		g.setFont(base);
		g.setColor(TITLE);
		drawCentered(g, dial.direction(), (int) (cy + DIAL_RADIUS + 35));
	}

	/** An arrow of the given length centered on the dial, pointing at the given screen angle. */
	private static void paintArrow(Graphics2D g, float cx, float cy, double angle, float length) {
		float dx = (float) Math.sin(angle);
		float dy = (float) -Math.cos(angle);
		float head = Math.min(9.0f, length * 0.45f);
		float halfWidth = head * 0.55f;
		float tipX = cx + dx * length / 2.0f;
		float tipY = cy + dy * length / 2.0f;
		float baseX = tipX - dx * head;
		float baseY = tipY - dy * head;

		g.setColor(ARROW);
		g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
		g.draw(new Line2D.Float(cx - dx * length / 2.0f, cy - dy * length / 2.0f, baseX, baseY));
		Path2D.Float arrowHead = new Path2D.Float();
		arrowHead.moveTo(tipX, tipY);
		arrowHead.lineTo(baseX - dy * halfWidth, baseY + dx * halfWidth);
		arrowHead.lineTo(baseX + dy * halfWidth, baseY - dx * halfWidth);
		arrowHead.closePath();
		g.fill(arrowHead);
	}

	private static void drawCentered(Graphics2D g, String text, int baseline) {
		if (text == null || text.isEmpty()) {
			return;
		}
		FontMetrics metrics = g.getFontMetrics();
		g.drawString(text, (WIDTH - metrics.stringWidth(text)) / 2.0f, baseline);
	}

	private static void setEnabled(int capability, boolean enabled) {
		if (enabled) {
			glEnable(capability);
		} else {
			glDisable(capability);
		}
	}

	private void releaseImage() {
		if (texture != null) {
			texture.cleanup();
			texture = null;
		}
		if (pixelBuffer != null) {
			MemoryUtil.memFree(pixelBuffer);
			pixelBuffer = null;
		}
		image = null;
		shown = null;
	}

	@Override
	public void cleanup() {
		releaseImage();
		shader.cleanup();
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.VERTEX_ARRAY, vao);
		glDeleteVertexArrays(vao);
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.BUFFER, vbo);
		glDeleteBuffers(vbo);
	}
}
