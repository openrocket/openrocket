package info.openrocket.swing.gui.figure3d.materials;

import info.openrocket.core.appearance.DecalImage;
import info.openrocket.swing.gui.figure3d.utils.ColorUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static info.openrocket.swing.gui.figure3d.constants.RenderingConstants.DECAL_SURFACE_ALL;

/**
 * Defines the visual appearance of a SceneObject.
 * Includes settings for bump mapping and selective decals.
 * Implements the Material interface for modular material handling.
 *
 * We use a dedicated class instead of the OpenRocket Appearance3D class, because
 * it is more flexible (e.g. if we want to add metallic materials, emissivity...) and allows for
 * separation of concerns (this appearance if part of the rendering layer and is meant to be
 * sent directly to GPU shaders).
 */
public class Appearance3D implements Material {
	public enum RenderStyle {		// Note: Do NOT change the order of these enums, as they are used in shaders!
		SOLID,      // Ordinal = 0
		TEXTURED,   // Ordinal = 1
		WIREFRAME   // Ordinal = 2
	}

	public enum TextureMode {
		STRETCH, REPEAT_AXIAL, REPEAT_RADIAL, REPEAT_BOTH
	}

	private Vector3f color;			// Base color of the object (stores LINEAR color)
	private Vector3f specularColor = new Vector3f(1.0f, 1.0f, 1.0f);	// Specular color for highlights (RGB)
	private float specularTint = 0.3f;		// How much the specular color is tinted by the base color (0.0 to 1.0; 0.0 = no tint, 1.0 = full tint)
	private boolean isUnlit = false;
	private RenderStyle style;
	private float shine;			// How shiny the surface is (0.0 to 1.0; 0.0 = matte, 1.0 = mirror-like)
	private float roughnessScale;		// Scale for bump mapping (higher values = more detail)
	private float roughnessStrength;		// Strength of the bump effect (0.0 = no bump, higher values = more pronounced bumps)
	private float opacity = 1.0f;
	private boolean opacityAffectsTexture;

	// --- Texture & Decal Properties ---
	private Texture texture;
	private boolean ownsTexture = true;
	private DecalImage textureSourceImage;
	private TextureMode textureMode = TextureMode.STRETCH;
	private final TextureTransform textureTransform = new TextureTransform();
	private Texture decalTexture;
	private boolean ownsDecalTexture = true;
	private final TextureTransform decalTransform = new TextureTransform();
	private int decalSurfaceMask = DECAL_SURFACE_ALL; // Default to all surfaces

	public Appearance3D(Vector3f srgbColor, Texture texture, RenderStyle style) {
		if (style == RenderStyle.SOLID && texture != null) {
			throw new IllegalArgumentException("Use the color constructor for COLOR_ONLY style.");
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
		this.roughnessScale = 50.0f;
		this.roughnessStrength = 0.5f;
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

	public void setSpecularColor(Vector3f specularColor) {
		this.specularColor.set(specularColor);
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
		this.shine = Math.max(0.0f, Math.min(1.0f, shine));
	}
	public void setRoughnessScale(float scale) {
		this.roughnessScale = Math.max(0.0f, scale);
	}
	public void setRoughnessStrength(float strength) {
		this.roughnessStrength = Math.max(0.0f, strength);
	}
	public void setOpacity(float opacity) {
		this.opacity = Math.max(0.0f, Math.min(1.0f, opacity));
	}
	public void setOpacityAffectsTexture(boolean opacityAffectsTexture) {
		this.opacityAffectsTexture = opacityAffectsTexture;
	}
	public void setDecal(Texture decalTexture, Vector2f position, Vector2f scale) {
		setDecal(decalTexture, position, scale, DECAL_SURFACE_ALL);
	}
	public void setDecal(Texture decalTexture, Vector2f position, Vector2f scale, int surfaceMask) {
		setDecal(decalTexture, position, scale, surfaceMask, true);
	}
	public void setDecal(Texture decalTexture, Vector2f position, Vector2f scale, int surfaceMask, boolean ownsDecalTexture) {
		this.decalTexture = decalTexture;
		this.ownsDecalTexture = ownsDecalTexture;
		this.decalTransform.offset = position;
		this.decalTransform.scale = scale;
		this.decalSurfaceMask = surfaceMask;
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
	public Vector3f getSpecularColor() { return specularColor; }
	public float getSpecularTint() { return specularTint; }
	public boolean isUnlit() { return isUnlit; }
	public Texture getTexture() { return texture; }
	public DecalImage getTextureSourceImage() { return textureSourceImage; }
	public TextureMode getTextureMode() { return textureMode; }
	public float getOpacity() { return opacity; }
	public boolean isOpacityAffectsTexture() { return opacityAffectsTexture; }

	@Override
	public RenderStyle getRenderStyle() { return style; }

	public RenderStyle getStyle() { return style; }
	public float getShine() { return shine; }
	public float getRoughnessScale() { return roughnessScale; }
	public float getRoughnessStrength() { return roughnessStrength; }
	public Texture getDecalTexture() { return decalTexture; }
	public int getDecalSurfaceMask() { return decalSurfaceMask; }
	public TextureTransform getTextureTransform() { return textureTransform; }
	public TextureTransform getDecalTransform() { return decalTransform; }

	public void cleanup() {
		if (texture != null && ownsTexture) texture.cleanup();
		if (decalTexture != null && ownsDecalTexture) decalTexture.cleanup();
	}
}
