package info.openrocket.swing.gui.figure3d.thumbnail;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.openrocket.core.appearance.Appearance;
import info.openrocket.core.appearance.Decal;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.core.appearance.defaults.DefaultAppearance;
import info.openrocket.core.rocketcomponent.Rocket;
import info.openrocket.core.util.DecalNotFoundException;
import info.openrocket.core.util.ORColor;
import info.openrocket.swing.gui.figure3d.geometry.IntList;
import info.openrocket.swing.gui.figure3d.geometry.Mesh;
import info.openrocket.swing.gui.figure3d.geometry.RocketMeshBuilder;
import info.openrocket.swing.gui.figure3d.geometry.RocketSceneSnapshot;
import info.openrocket.swing.gui.figure3d.geometry.Vertex;
import info.openrocket.swing.gui.figure3d.materials.TextureTransform;
import info.openrocket.swing.gui.figure3d.scene.properties.RenderingConfiguration;

/**
 * Renders the rocket images that are stored in design files: the thumbnail for file icons (Windows Explorer, macOS
 * Finder), and the 3D side view in the file preview.
 * <p>
 * The images show the same geometry, paint and decals as the 3D view, but are drawn in software so that they do not
 * need an OpenGL context: the 3D view only renders while it is on screen, and it may be disabled or unavailable. The
 * rocket is drawn with an orthographic projection, with soft, view-aligned shading instead of lighting, and with an
 * outline around each part. In the thumbnail, the rocket is drawn at 45 degrees (nose up-left) so that it fills more of
 * the square image, and the outlines are thick enough to keep it recognizable when the operating system scales the
 * image down to icon size.
 */
public final class RocketThumbnailRenderer {
	private static final Logger log = LoggerFactory.getLogger(RocketThumbnailRenderer.class);

	/** Width and height of the thumbnail image, in pixels. */
	public static final int DEFAULT_SIZE = 1024;

	private static final int SUPERSAMPLING = 2;			// Samples per pixel along each axis
	private static final double THUMBNAIL_ANGLE = Math.PI / 4;	// Nose up-left
	private static final double MARGIN = 0.06;			// Fraction of the image left empty on each side
	private static final int THUMBNAIL_BACKGROUND = 0xFFFFFF;
	/** Outline width as a fraction of the image width. */
	private static final double THUMBNAIL_OUTLINE = 1 / 170.0;
	private static final double SIDE_VIEW_OUTLINE = 1 / 400.0;
	/** Brightness of surfaces seen edge-on; surfaces facing the viewer are drawn at full brightness. */
	private static final float EDGE_BRIGHTNESS = 0.72f;
	private static final float OUTLINE_DARKENING = 0.6f;
	/** Alpha above which a fragment is treated as opaque, so that it hides what is behind it. */
	private static final float OPAQUE_ALPHA = 0.999f;
	/** Decals are scaled down to this size; larger images add no detail to the thumbnail. */
	private static final int MAX_TEXTURE_SIZE = 1024;
	private static final Color FALLBACK_PAINT = new Color(0x8F, 0xAA, 0xC4, 1f);
	private static final float[] TEXTURE_COVERAGE = new float[256];

	static {
		// Same alpha bias as the 3D view's fragment shader (adjustTextureCoverage), which keeps thin line art visible.
		for (int a = 0; a < 256; a++) {
			TEXTURE_COVERAGE[a] = (float) Math.pow(a / 255.0, 0.82);
		}
	}

	private RocketThumbnailRenderer() {
	}

	/**
	 * Render the thumbnail of the selected flight configuration of a rocket as PNG data. Must be called on the thread
	 * that owns the rocket model (the EDT in the application).
	 *
	 * @param rocket the rocket to render
	 * @param size   width and height of the image, in pixels
	 * @return the PNG data, or null if the rocket has nothing to draw
	 */
	public static byte[] renderPng(Rocket rocket, int size) {
		RenderTask task = prepareThumbnail(rocket, size);
		return task == null ? null : task.renderPng();
	}

	/**
	 * Render the thumbnail of the selected flight configuration of a rocket. Must be called on the thread that owns
	 * the rocket model (the EDT in the application).
	 *
	 * @param rocket the rocket to render
	 * @param size   width and height of the image, in pixels
	 * @return the image, or null if the rocket has nothing to draw
	 */
	public static BufferedImage render(Rocket rocket, int size) {
		RenderTask task = prepareThumbnail(rocket, size);
		return task == null ? null : task.render();
	}

	/**
	 * Render a 3D side view of the selected flight configuration of a rocket, with the nose to the left, as in the
	 * design view. The image is as high as the rocket needs at the given width, up to {@code maxHeight}. Must be called
	 * on the thread that owns the rocket model (the EDT in the application).
	 *
	 * @param rocket     the rocket to render
	 * @param width      width of the image, in pixels
	 * @param maxHeight  maximum height of the image, in pixels
	 * @param background background color of the image
	 * @return the image, or null if the rocket has nothing to draw
	 */
	public static BufferedImage renderSideView(Rocket rocket, int width, int maxHeight, java.awt.Color background) {
		RenderTask task = prepareSideView(rocket, width, maxHeight, background);
		return task == null ? null : task.render();
	}

	/**
	 * Prepare the thumbnail of the selected flight configuration for rendering. Reads the rocket model, so it must be
	 * called on the thread that owns it (the EDT in the application); the returned task can be rendered on any thread.
	 *
	 * @param rocket the rocket to render
	 * @param size   width and height of the image, in pixels
	 * @return the prepared render, or null if the rocket has nothing to draw
	 */
	public static RenderTask prepareThumbnail(Rocket rocket, int size) {
		return prepare(rocket, THUMBNAIL_ANGLE, size, size, false, THUMBNAIL_BACKGROUND, THUMBNAIL_OUTLINE);
	}

	/**
	 * Prepare a 3D side view of the selected flight configuration for rendering. Reads the rocket model, so it must be
	 * called on the thread that owns it (the EDT in the application); the returned task can be rendered on any thread.
	 *
	 * @param rocket     the rocket to render
	 * @param width      width of the image, in pixels
	 * @param maxHeight  maximum height of the image, in pixels
	 * @param background background color of the image
	 * @return the prepared render, or null if the rocket has nothing to draw
	 */
	public static RenderTask prepareSideView(Rocket rocket, int width, int maxHeight, java.awt.Color background) {
		return prepare(rocket, 0, width, maxHeight, true, background.getRGB() & 0xFFFFFF, SIDE_VIEW_OUTLINE);
	}

	private static RenderTask prepare(Rocket rocket, double angle, int width, int maxHeight, boolean fitHeight,
									  int background, double outline) {
		if (width <= 0 || maxHeight <= 0) {
			throw new IllegalArgumentException("Image dimensions must be positive: " + width + "x" + maxHeight);
		}
		RocketSceneSnapshot snapshot = RocketMeshBuilder.buildSnapshot(rocket, new RenderingConfiguration());
		List<Part> parts = collectParts(snapshot, angle);
		if (parts.isEmpty()) {
			return null;
		}

		float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
		for (Part part : parts) {
			for (int i = 0; i < part.x().length; i++) {
				minX = Math.min(minX, part.x()[i]);
				maxX = Math.max(maxX, part.x()[i]);
				minY = Math.min(minY, part.y()[i]);
				maxY = Math.max(maxY, part.y()[i]);
			}
		}
		double extentX = Math.max(maxX - minX, 1e-9), extentY = Math.max(maxY - minY, 1e-9);
		double margin = MARGIN * Math.min(width, maxHeight);
		double scale = Math.min((width - 2 * margin) / extentX, (maxHeight - 2 * margin) / extentY);
		int height = fitHeight ? Math.min(maxHeight, (int) Math.ceil(extentY * scale + 2 * margin)) : maxHeight;
		double offsetX = width / 2.0 - (minX + maxX) / 2.0 * scale;
		double offsetY = height / 2.0 - (minY + maxY) / 2.0 * scale;
		int outlineWidth = Math.max(2, (int) Math.round(width * SUPERSAMPLING * outline));
		return new RenderTask(parts, width, height, scale, offsetX, offsetY, background, outlineWidth);
	}

	/**
	 * A fully prepared image render: the rocket has been read and its geometry projected, so the remaining work is
	 * pure CPU rasterization that does not touch the rocket model and is safe to run on any thread.
	 */
	public static final class RenderTask {
		private final List<Part> parts;
		private final int width, height;
		private final double scale, offsetX, offsetY;
		private final int background;
		private final int outlineWidth;

		private RenderTask(List<Part> parts, int width, int height, double scale, double offsetX, double offsetY,
						   int background, int outlineWidth) {
			this.parts = parts;
			this.width = width;
			this.height = height;
			this.scale = scale;
			this.offsetX = offsetX;
			this.offsetY = offsetY;
			this.background = background;
			this.outlineWidth = outlineWidth;
		}

		/** Render the prepared image. */
		public BufferedImage render() {
			return new Rasterizer(parts, width, height, scale, offsetX, offsetY, background, outlineWidth).render();
		}

		/** Render the prepared image and encode it as PNG data. */
		public byte[] renderPng() {
			try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
				ImageIO.write(render(), "png", output);
				return output.toByteArray();
			} catch (IOException e) {
				throw new UncheckedIOException("Failed to encode thumbnail image", e);
			}
		}
	}

	// ------------------------------------------------------------------------------------------------------------
	// Scene

	/** RGB in [0, 255] and alpha in [0, 1]. */
	private record Color(float r, float g, float b, float a) {
		Color(int r, int g, int b, float a) {
			this((float) r, (float) g, (float) b, a);
		}
	}

	/** Decal image as packed ARGB pixels, top row first. */
	private record Texture(int[] argb, int width, int height, Decal.EdgeMode edgeMode) {
	}

	/**
	 * Surface material: paint with an optional decal on top. The decal texture coordinates are
	 * {@code s = su * u + sv * v + s0} and {@code t = tu * u + tv * v + t0}.
	 */
	private record Material(Color paint, boolean opacityAffectsTexture, Texture texture,
							float su, float sv, float s0, float tu, float tv, float t0) {
		boolean isTranslucent() {
			return paint.a() < OPAQUE_ALPHA;
		}
	}

	/** One mesh instance, with its vertices projected onto the image plane (before fitting it to the image). */
	private record Part(int id, Material material, float[] x, float[] y, float[] z, float[] u, float[] v, float[] nz,
						IntList indices) {
	}

	private static List<Part> collectParts(RocketSceneSnapshot snapshot, double angle) {
		Map<RocketThumbnailMaterialKey, Material> materials = new HashMap<>();
		Map<DecalImage, Texture> textures = new IdentityHashMap<>();
		List<Part> parts = new ArrayList<>();

		for (RocketSceneSnapshot.ComponentInstance instance : snapshot.getComponentInstances()) {
			Appearance appearance = instance.appearance().appearance();
			if (appearance == null) {
				appearance = DefaultAppearance.getDefaultAppearance(instance.component());
			}
			Material material = materials.computeIfAbsent(
					new RocketThumbnailMaterialKey(appearance, instance.appearance().scaleTextureFromTop()),
					key -> createMaterial(key.appearance(), key.scaleTextureFromTop(), textures));
			addPart(parts, instance.mesh(), instance.modelMatrix(), material, angle);
		}
		for (RocketSceneSnapshot.MotorInstance instance : snapshot.getMotorInstances()) {
			Appearance appearance = DefaultAppearance.getDefaultAppearance(instance.motor());
			Material material = materials.computeIfAbsent(new RocketThumbnailMaterialKey(appearance, true),
					key -> createMaterial(key.appearance(), key.scaleTextureFromTop(), textures));
			addPart(parts, instance.motorMesh(), instance.modelMatrix(), material, angle);
		}
		return parts;
	}

	private record RocketThumbnailMaterialKey(Appearance appearance, boolean scaleTextureFromTop) {
	}

	private static void addPart(List<Part> parts, Mesh mesh, Matrix4f modelMatrix, Material material, double angle) {
		if (mesh == null || mesh.getIndices().size() < 3) {
			return;
		}
		List<Vertex> vertices = mesh.getVertices();
		int count = vertices.size();
		float[] x = new float[count], y = new float[count], z = new float[count];
		float[] u = new float[count], v = new float[count], nz = new float[count];
		float cos = (float) Math.cos(angle), sin = (float) Math.sin(angle);
		Vector3f p = new Vector3f();
		Vector3f n = new Vector3f();
		for (int i = 0; i < count; i++) {
			Vertex vertex = vertices.get(i);
			modelMatrix.transformPosition(vertex.position, p);
			// The 3D scene has the rocket axis along x, y up and z towards the viewer of the side view. Flip y for the
			// image, then rotate the image.
			float ix = p.x, iy = -p.y;
			x[i] = cos * ix - sin * iy;
			y[i] = sin * ix + cos * iy;
			z[i] = p.z;
			u[i] = vertex.texCoords.x;
			v[i] = vertex.texCoords.y;
			modelMatrix.transformDirection(vertex.normal, n);
			float length = n.length();
			nz[i] = length > 0 ? Math.abs(n.z / length) : 1;
		}
		parts.add(new Part(parts.size() + 1, material, x, y, z, u, v, nz, mesh.getIndices()));
	}

	private static Material createMaterial(Appearance appearance, boolean scaleTextureFromTop,
										   Map<DecalImage, Texture> textures) {
		ORColor paint = appearance.getPaint();
		Color color = paint == null ? FALLBACK_PAINT
				: new Color(paint.getRed(), paint.getGreen(), paint.getBlue(), paint.getAlpha() / 255f);

		Decal decal = appearance.getTexture();
		Texture texture = null;
		if (decal != null && decal.getImage() != null) {
			texture = textures.computeIfAbsent(decal.getImage(), image -> loadTexture(image, decal.getEdgeMode()));
		}
		if (texture == null) {
			return new Material(color, appearance.isOpacityAffectsTexture(), null, 0, 0, 0, 0, 0, 0);
		}
		if (texture.edgeMode() != decal.getEdgeMode()) {
			texture = new Texture(texture.argb(), texture.width(), texture.height(), decal.getEdgeMode());
		}

		// Same texture coordinate transformation as the 3D view (AppearanceFactory).
		TextureTransform transform = new TextureTransform();
		transform.scale.set((float) decal.getScale().getX(), (float) decal.getScale().getY());
		transform.offset.set((float) decal.getOffset().getX(), (float) decal.getOffset().getY());
		transform.center.set((float) decal.getCenter().getX(), (float) decal.getCenter().getY());
		transform.rotation = (float) decal.getRotation();
		transform.setScaleFromTop(scaleTextureFromTop);
		Matrix4f m = transform.getTransformMatrix(new Matrix4f());
		return new Material(color, appearance.isOpacityAffectsTexture(), texture,
				m.m00(), m.m10(), m.m30(), m.m01(), m.m11(), m.m31());
	}

	private static Texture loadTexture(DecalImage image, Decal.EdgeMode edgeMode) {
		BufferedImage decal;
		try (InputStream input = image.getBytes()) {
			decal = ImageIO.read(input);
		} catch (IOException | DecalNotFoundException e) {
			log.warn("Could not load decal " + image.getName() + " for the file thumbnail", e);
			return null;
		}
		if (decal == null) {
			log.warn("Could not decode decal " + image.getName() + " for the file thumbnail");
			return null;
		}
		decal = limitSize(decal);
		int width = decal.getWidth(), height = decal.getHeight();
		return new Texture(decal.getRGB(0, 0, width, height, null, 0, width), width, height, edgeMode);
	}

	/** Scale large images down by repeated halving, which keeps fine detail from aliasing. */
	private static BufferedImage limitSize(BufferedImage image) {
		while (Math.max(image.getWidth(), image.getHeight()) > MAX_TEXTURE_SIZE) {
			double factor = Math.max(0.5, MAX_TEXTURE_SIZE / (double) Math.max(image.getWidth(), image.getHeight()));
			int width = Math.max(1, (int) Math.round(image.getWidth() * factor));
			int height = Math.max(1, (int) Math.round(image.getHeight() * factor));
			BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g2 = scaled.createGraphics();
			try {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.drawImage(image, 0, 0, width, height, null);
			} finally {
				g2.dispose();
			}
			image = scaled;
		}
		return image;
	}

	// ------------------------------------------------------------------------------------------------------------
	// Rasterization

	private static final class Rasterizer {
		private final List<Part> parts;
		private final int width, height;		// Size of the image
		private final int nw, nh;				// Size of the supersampled buffers
		private final double scale;				// Supersampled pixels per scene unit
		private final double offsetX, offsetY;
		private final int outlineWidth;			// In supersampled pixels

		private final float[] opaqueDepth;		// Depth of the nearest opaque surface (larger is nearer)
		private final float[] frontDepth;		// Depth of the nearest surface, opaque or translucent
		private final int[] frontId;			// Part of the nearest surface, 0 for background
		private final int[] color;

		// Output of shade(), kept in fields to avoid allocating per pixel
		private float shadedR, shadedG, shadedB, shadedA;

		Rasterizer(List<Part> parts, int width, int height, double scale, double offsetX, double offsetY,
				   int background, int outlineWidth) {
			this.parts = parts;
			this.width = width;
			this.height = height;
			this.nw = width * SUPERSAMPLING;
			this.nh = height * SUPERSAMPLING;
			this.scale = scale * SUPERSAMPLING;
			this.offsetX = offsetX * SUPERSAMPLING;
			this.offsetY = offsetY * SUPERSAMPLING;
			this.outlineWidth = outlineWidth;

			int pixels = nw * nh;
			this.opaqueDepth = new float[pixels];
			this.frontDepth = new float[pixels];
			this.frontId = new int[pixels];
			this.color = new int[pixels];
			Arrays.fill(opaqueDepth, -Float.MAX_VALUE);
			Arrays.fill(frontDepth, -Float.MAX_VALUE);
			Arrays.fill(color, background);
		}

		BufferedImage render() {
			// Opaque surfaces first, then translucent ones from back to front, blended over what is behind them.
			List<int[]> translucent = new ArrayList<>();	// {part index, first vertex index of the triangle}
			for (int p = 0; p < parts.size(); p++) {
				Part part = parts.get(p);
				IntList indices = part.indices();
				for (int i = 0; i + 2 < indices.size(); i += 3) {
					rasterize(part, indices.get(i), indices.get(i + 1), indices.get(i + 2), false);
					if (part.material().isTranslucent()) {
						translucent.add(new int[] { p, i });
					}
				}
			}
			// Sort by triangle centroid depth. This is an approximation of a true depth sort, so very differently
			// sized overlapping translucent triangles can still be ordered incorrectly; acceptable at thumbnail size.
			translucent.sort(Comparator.comparingDouble(t -> centroidDepth(parts.get(t[0]), t[1])));
			for (int[] t : translucent) {
				Part part = parts.get(t[0]);
				IntList indices = part.indices();
				rasterize(part, indices.get(t[1]), indices.get(t[1] + 1), indices.get(t[1] + 2), true);
			}

			drawOutlines();
			return downsample();
		}

		private static double centroidDepth(Part part, int first) {
			IntList indices = part.indices();
			return part.z()[indices.get(first)] + part.z()[indices.get(first + 1)] + part.z()[indices.get(first + 2)];
		}

		/**
		 * Fill the pixels whose centers lie inside a triangle. Pixel centers on a shared edge belong to exactly one of
		 * the two triangles, so translucent surfaces are not blended twice along their seams.
		 */
		private void rasterize(Part part, int i0, int i1, int i2, boolean translucentPass) {
			double x0 = part.x()[i0] * scale + offsetX, y0 = part.y()[i0] * scale + offsetY;
			double x1 = part.x()[i1] * scale + offsetX, y1 = part.y()[i1] * scale + offsetY;
			double x2 = part.x()[i2] * scale + offsetX, y2 = part.y()[i2] * scale + offsetY;
			double det = (x1 - x0) * (y2 - y0) - (x2 - x0) * (y1 - y0);
			if (Math.abs(det) < 1e-12) {
				return;
			}

			// Attributes vary linearly over the image plane with an orthographic projection.
			Gradient z = new Gradient(part.z(), i0, i1, i2, x0, y0, x1, y1, x2, y2, det);
			Gradient u = new Gradient(part.u(), i0, i1, i2, x0, y0, x1, y1, x2, y2, det);
			Gradient v = new Gradient(part.v(), i0, i1, i2, x0, y0, x1, y1, x2, y2, det);
			Gradient nz = new Gradient(part.nz(), i0, i1, i2, x0, y0, x1, y1, x2, y2, det);
			Material material = part.material();

			double[] crossing = new double[1];
			int rowStart = Math.max(0, (int) Math.ceil(Math.min(y0, Math.min(y1, y2)) - 0.5));
			int rowEnd = Math.min(nh - 1, (int) Math.ceil(Math.max(y0, Math.max(y1, y2)) - 0.5) - 1);
			for (int row = rowStart; row <= rowEnd; row++) {
				double yc = row + 0.5;
				double left = Double.MAX_VALUE, right = -Double.MAX_VALUE;
				if (crossEdge(x0, y0, x1, y1, yc, crossing)) {
					left = Math.min(left, crossing[0]);
					right = Math.max(right, crossing[0]);
				}
				if (crossEdge(x1, y1, x2, y2, yc, crossing)) {
					left = Math.min(left, crossing[0]);
					right = Math.max(right, crossing[0]);
				}
				if (crossEdge(x2, y2, x0, y0, yc, crossing)) {
					left = Math.min(left, crossing[0]);
					right = Math.max(right, crossing[0]);
				}
				int colStart = Math.max(0, (int) Math.ceil(left - 0.5));
				int colEnd = Math.min(nw - 1, (int) Math.ceil(right - 0.5) - 1);
				if (colStart > colEnd) {
					continue;
				}

				double xc = colStart + 0.5;
				double zValue = z.at(xc, yc), uValue = u.at(xc, yc), vValue = v.at(xc, yc), nzValue = nz.at(xc, yc);
				int offset = row * nw;
				for (int col = colStart; col <= colEnd; col++, zValue += z.dx, uValue += u.dx, vValue += v.dx,
						nzValue += nz.dx) {
					int o = offset + col;
					float depth = (float) zValue;
					if (depth <= opaqueDepth[o]) {
						continue;
					}
					shade(material, uValue, vValue, nzValue);
					boolean opaque = shadedA >= OPAQUE_ALPHA;
					if (!translucentPass) {
						// Translucent parts can still have fully opaque decal pixels, which hide what is behind them.
						if (!opaque) {
							continue;
						}
						opaqueDepth[o] = depth;
						color[o] = pack(shadedR, shadedG, shadedB);
					} else {
						if (opaque) {
							continue;	// Drawn in the opaque pass
						}
						int c = color[o];
						float a = shadedA;
						color[o] = pack(shadedR * a + ((c >> 16) & 0xFF) * (1 - a),
								shadedG * a + ((c >> 8) & 0xFF) * (1 - a),
								shadedB * a + (c & 0xFF) * (1 - a));
						if (depth <= frontDepth[o]) {
							continue;
						}
					}
					frontDepth[o] = depth;
					frontId[o] = part.id();
				}
			}
		}

		/** Whether the horizontal line at {@code yc} crosses the edge, using a half-open rule; stores x in out[0]. */
		private static boolean crossEdge(double xa, double ya, double xb, double yb, double yc, double[] out) {
			if (ya > yb) {
				// Always interpolate from the same end so that both triangles sharing the edge get the same x.
				double t = xa; xa = xb; xb = t;
				t = ya; ya = yb; yb = t;
			}
			if (yc < ya || yc >= yb) {
				return false;
			}
			out[0] = xa + (yc - ya) * (xb - xa) / (yb - ya);
			return true;
		}

		/**
		 * Color of a surface point: the paint with the decal layered on top, combined the same way as in the 3D
		 * view's fragment shader, and darkened where the surface turns away from the viewer.
		 */
		private void shade(Material material, double u, double v, double nz) {
			Color paint = material.paint();
			float r = paint.r(), g = paint.g(), b = paint.b(), a = paint.a();
			Texture texture = material.texture();
			if (texture != null) {
				double s = material.su() * u + material.sv() * v + material.s0();
				double t = material.tu() * u + material.tv() * v + material.t0();
				int texel = sample(texture, s, t);
				float coverage = TEXTURE_COVERAGE[(texel >>> 24) & 0xFF];
				float tr = (texel >> 16) & 0xFF, tg = (texel >> 8) & 0xFF, tb = texel & 0xFF;
				if (material.opacityAffectsTexture()) {
					r += (tr - r) * coverage;
					g += (tg - g) * coverage;
					b += (tb - b) * coverage;
				} else {
					// The decal is a layer over the translucent paint.
					float base = a * (1 - coverage);
					float alpha = coverage + base;
					if (alpha > 1e-5f) {
						r = (tr * coverage + r * base) / alpha;
						g = (tg * coverage + g * base) / alpha;
						b = (tb * coverage + b * base) / alpha;
					}
					a = alpha;
				}
			}
			float brightness = EDGE_BRIGHTNESS + (1 - EDGE_BRIGHTNESS) * (float) Math.min(1, Math.max(0, nz));
			shadedR = r * brightness;
			shadedG = g * brightness;
			shadedB = b * brightness;
			shadedA = a;
		}

		/**
		 * Bilinear sample of the decal at texture coordinates (s, t), wrapped according to the decal edge mode. The
		 * texture coordinate t runs from the bottom of the image to the top.
		 */
		private static int sample(Texture texture, double s, double t) {
			Decal.EdgeMode mode = texture.edgeMode();
			if (mode == Decal.EdgeMode.STICKER && (s < 0 || s > 1 || t < 0 || t > 1)) {
				return 0;	// Outside a sticker only the paint shows
			}
			int w = texture.width(), h = texture.height();
			double fx = s * w - 0.5, fy = (1 - t) * h - 0.5;
			int x0 = (int) Math.floor(fx), y0 = (int) Math.floor(fy);
			float ax = (float) (fx - x0), ay = (float) (fy - y0);
			int xa = wrap(x0, w, mode), xb = wrap(x0 + 1, w, mode);
			int ya = wrap(y0, h, mode), yb = wrap(y0 + 1, h, mode);
			int[] argb = texture.argb();
			return bilinear(argb[ya * w + xa], argb[ya * w + xb], argb[yb * w + xa], argb[yb * w + xb], ax, ay);
		}

		private static int bilinear(int c00, int c10, int c01, int c11, float ax, float ay) {
			int result = 0;
			for (int shift = 0; shift <= 24; shift += 8) {
				float top = ((c00 >>> shift) & 0xFF) * (1 - ax) + ((c10 >>> shift) & 0xFF) * ax;
				float bottom = ((c01 >>> shift) & 0xFF) * (1 - ax) + ((c11 >>> shift) & 0xFF) * ax;
				result |= (Math.round(top * (1 - ay) + bottom * ay) & 0xFF) << shift;
			}
			return result;
		}

		/**
		 * Darken the pixels of each part near its silhouette and near other parts behind it. Where two parts meet at
		 * about the same depth, only one of them gets the line.
		 */
		private void drawOutlines() {
			int[] distances = { outlineWidth / 2, outlineWidth };
			int[] dx = { 1, -1, 0, 0, 1, 1, -1, -1 };
			int[] dy = { 0, 0, 1, -1, 1, -1, 1, -1 };
			// Depth change that a smooth surface can have over the outline width, in scene units
			float tolerance = (float) (3.0 * outlineWidth / scale);

			boolean[] outline = new boolean[nw * nh];
			for (int y = 0; y < nh; y++) {
				for (int x = 0; x < nw; x++) {
					int o = y * nw + x;
					int id = frontId[o];
					if (id == 0) {
						continue;
					}
					search:
					for (int d = 0; d < dx.length; d++) {
						for (int distance : distances) {
							int qx = x + dx[d] * distance, qy = y + dy[d] * distance;
							if (qx < 0 || qy < 0 || qx >= nw || qy >= nh) {
								outline[o] = true;
								break search;
							}
							int q = qy * nw + qx;
							int other = frontId[q];
							if (other == id) {
								continue;
							}
							float behind = frontDepth[o] - frontDepth[q];
							if (other == 0 || behind > tolerance || (Math.abs(behind) <= tolerance && other > id)) {
								outline[o] = true;
								break search;
							}
						}
					}
				}
			}
			for (int o = 0; o < outline.length; o++) {
				if (outline[o]) {
					int c = color[o];
					color[o] = pack(((c >> 16) & 0xFF) * (1 - OUTLINE_DARKENING), ((c >> 8) & 0xFF) * (1 - OUTLINE_DARKENING),
							(c & 0xFF) * (1 - OUTLINE_DARKENING));
				}
			}
		}

		/** Average each block of supersamples into one image pixel. */
		private BufferedImage downsample() {
			int[] pixels = new int[width * height];
			int samples = SUPERSAMPLING * SUPERSAMPLING;
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int r = 0, g = 0, b = 0;
					for (int sy = 0; sy < SUPERSAMPLING; sy++) {
						int offset = (y * SUPERSAMPLING + sy) * nw + x * SUPERSAMPLING;
						for (int sx = 0; sx < SUPERSAMPLING; sx++) {
							int c = color[offset + sx];
							r += (c >> 16) & 0xFF;
							g += (c >> 8) & 0xFF;
							b += c & 0xFF;
						}
					}
					pixels[y * width + x] = ((r / samples) << 16) | ((g / samples) << 8) | (b / samples);
				}
			}
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			image.setRGB(0, 0, width, height, pixels, 0, width);
			return image;
		}

		private static int pack(float r, float g, float b) {
			return (clamp(r) << 16) | (clamp(g) << 8) | clamp(b);
		}

		private static int clamp(float value) {
			return Math.min(255, Math.max(0, Math.round(value)));
		}
	}

	/**
	 * Map a texel index that may lie outside the image to the texel that is shown there, according to the decal edge
	 * mode: repeated, mirrored at every edge, or clamped to the edge (outside a sticker, nothing is shown at all).
	 */
	static int wrap(int i, int size, Decal.EdgeMode mode) {
		switch (mode) {
			case REPEAT:
				return Math.floorMod(i, size);
			case MIRROR:
				int period = Math.floorMod(i, 2 * size);
				return period < size ? period : 2 * size - 1 - period;
			default:	// CLAMP, and STICKER inside the image
				return Math.min(size - 1, Math.max(0, i));
		}
	}

	/** A vertex attribute as a linear function of the image position: {@code value = at(x, y)}. */
	private static final class Gradient {
		final double dx, dy, x0, y0, value0;

		Gradient(float[] values, int i0, int i1, int i2, double x0, double y0, double x1, double y1,
				 double x2, double y2, double det) {
			double f0 = values[i0], f1 = values[i1], f2 = values[i2];
			this.dx = ((f1 - f0) * (y2 - y0) - (f2 - f0) * (y1 - y0)) / det;
			this.dy = ((f2 - f0) * (x1 - x0) - (f1 - f0) * (x2 - x0)) / det;
			this.x0 = x0;
			this.y0 = y0;
			this.value0 = f0;
		}

		double at(double x, double y) {
			return value0 + dx * (x - x0) + dy * (y - y0);
		}
	}
}
