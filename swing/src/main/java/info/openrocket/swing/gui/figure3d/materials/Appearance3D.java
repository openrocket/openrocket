package info.openrocket.swing.gui.figure3d.materials;

import info.openrocket.core.util.MathUtil;
import info.openrocket.core.appearance.DecalImage;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import org.joml.Vector3f;

/**
 * Render-ready appearance derived from an OpenRocket component appearance.
 */
public class Appearance3D {
	public enum RenderStyle {
		SOLID(0),
		TEXTURED(1);

		private final int shaderValue;

		RenderStyle(int shaderValue) {
			this.shaderValue = shaderValue;
		}

		public int getShaderValue() {
			return shaderValue;
		}
	}

	public enum TextureMode {
		STRETCH, REPEAT
	}

	private Vector3f color;			// Base color of the object (stores LINEAR color)
	private boolean isUnlit = false;
	private RenderStyle style;
	private float shine;			// How shiny the surface is (0.0 to 1.0; 0.0 = matte, 1.0 = mirror-like)
	// Surface roughness of the component's finish, normalised to 0 (mirror) .. 1 (roughest).
	// Deliberately the physical property rather than the grain size and bump strength it is
	// drawn with: those depend on the render quality, so the renderer derives them.
	private float roughnessAmount;
	private float opacity = 1.0f;
	private boolean opacityAffectsTexture;

	// --- Texture Properties ---
	private Texture texture;
	private boolean ownsTexture = true;
	private DecalImage textureSourceImage;
	private TextureMode textureMode = TextureMode.STRETCH;
	private final TextureTransform textureTransform = new TextureTransform();

	public Appearance3D(Vector3f srgbColor, Texture texture, RenderStyle style) {
		if (style == RenderStyle.SOLID && texture != null) {
			throw new IllegalArgumentException("Use the color constructor for SOLID style.");
		}
		this.color = new Vector3f();
		if (srgbColor != null) {
			setColor(srgbColor); // Use the new setter to perform conversion
		} else {
			this.color.set(1.0f, 1.0f, 1.0f);
		}
		this.texture = texture;
		this.style = style;
		this.shine = 0.5f;
		this.roughnessAmount = 0.0f;
	}

	public Appearance3D(Vector3f srgbColor) {
		this(srgbColor, null, RenderStyle.SOLID);
	}

	public Appearance3D(Texture texture, RenderStyle style) {
		this(new Vector3f(1.0f, 1.0f, 1.0f), texture, style);
	}

	public Appearance3D() {
		this(null, RenderStyle.SOLID);
	}

	// --- Setters ---
	/**
	 * Sets the base color from an sRGB color vector.
	 * The color is immediately converted to and stored in linear space.
	 * @param srgbColor The color in sRGB space.
	 */
	public void setColor(Vector3f srgbColor) {
		this.color.set(ColorUtils.srgbToLinear(srgbColor));
	}

	public void setTexture(Texture texture) {
		setTexture(texture, true);
	}
	public void setTexture(Texture texture, boolean ownsTexture) {
		this.texture = texture;
		this.ownsTexture = ownsTexture;
	}
	public void setTextureSourceImage(DecalImage textureSourceImage) {
		this.textureSourceImage = textureSourceImage;
	}
	public void clearTexture() {
		if (texture != null && ownsTexture) {
			texture.cleanup();
		}
		texture = null;
		textureSourceImage = null;
		ownsTexture = true;
	}
	public void setUnlit(boolean unlit) {
		isUnlit = unlit;
	}
	public void setShine(float shine) {
		this.shine = MathUtil.clamp(shine, 0.0f, 1.0f);
	}
	public void setRoughnessAmount(float roughnessAmount) {
		this.roughnessAmount = MathUtil.clamp(roughnessAmount, 0.0f, 1.0f);
	}
	public void setOpacity(float opacity) {
		this.opacity = MathUtil.clamp(opacity, 0.0f, 1.0f);
	}
	public void setOpacityAffectsTexture(boolean opacityAffectsTexture) {
		this.opacityAffectsTexture = opacityAffectsTexture;
	}
	public void setTextureMode(TextureMode textureMode) {
		this.textureMode = textureMode;
	}
	public void setRenderStyle(RenderStyle style) {
		this.style = style;
	}

	// --- Getters ---
	/**
	 * Gets the base color in linear space.
	 * @return Linear RGB color vector.
	 */
	public Vector3f getColor() { return color; }
	public boolean isUnlit() { return isUnlit; }
	public Texture getTexture() { return texture; }
	public DecalImage getTextureSourceImage() { return textureSourceImage; }
	public TextureMode getTextureMode() { return textureMode; }
	public float getOpacity() { return opacity; }
	public boolean isOpacityAffectsTexture() { return opacityAffectsTexture; }

	public RenderStyle getStyle() { return style; }
	public float getShine() { return shine; }
	public float getRoughnessAmount() { return roughnessAmount; }
	public TextureTransform getTextureTransform() { return textureTransform; }

	public void cleanup() {
		if (texture != null && ownsTexture) texture.cleanup();
	}
}
