package info.openrocket.swing.gui.figure3d.materials;

import info.openrocket.swing.gui.figure3d.rendering.GpuResourceTracker;
import info.openrocket.swing.gui.figure3d.rendering.TextureStateManager;
import org.lwjgl.opengl.EXTTextureFilterAnisotropic;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_LINEAR_MIPMAP_LINEAR;
import static org.lwjgl.opengl.GL11.GL_REPEAT;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_S;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_WRAP_T;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexParameterf;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP;
import static org.lwjgl.opengl.GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X;
import static org.lwjgl.opengl.GL21.GL_SRGB;
import static org.lwjgl.opengl.GL21.GL_SRGB_ALPHA;
import static org.lwjgl.opengl.GL30.GL_RGB32F;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * Manages OpenGL textures with support for 2D textures, cubemaps, HDR images, and texture pooling.
 * Handles automatic sRGB conversion and provides multiple construction methods for different use cases.
 */
public class Texture {

	private static final Logger log = LoggerFactory.getLogger(Texture.class);
	private static final String RESOURCE_PREFIX = "swing/src/main/resources/";

	private int textureId;
	private final int textureType;

	// Cache texture dimensions to avoid GPU queries
	private int width;
	private int height;

	// Add texture format tracking
	private int internalFormat;
	private int format;

	private void trackCreation(String label) {
		GpuResourceTracker.register(GpuResourceTracker.ResourceType.TEXTURE, textureId, label);
	}

	private void trackRelease() {
		GpuResourceTracker.release(GpuResourceTracker.ResourceType.TEXTURE, textureId);
	}

	/**
	 * Releases the GL handle generated at the start of a constructor when loading
	 * fails partway through, so the handle is not leaked and {@link #bind()} /
	 * consumers checking {@link #getId()} see an invalid (0) texture instead of a
	 * half-built one. Only for use before {@code trackCreation} has run.
	 */
	private void abandonTexture() {
		if (textureId != 0) {
			TextureStateManager.evictDeletedTexture(textureId);
			glDeleteTextures(textureId);
			textureId = 0;
		}
	}

	private static String normalizeResourcePath(String path) {
		String normalized = path.replace("\\", "/");
		if (normalized.startsWith("/")) {
			normalized = normalized.substring(1);
		}
		if (normalized.startsWith(RESOURCE_PREFIX)) {
			normalized = normalized.substring(RESOURCE_PREFIX.length());
		}
		return "/" + normalized;
	}

	private static InputStream openResourceStream(String path) {
		String normalizedPath = normalizeResourcePath(path);
		InputStream stream = Texture.class.getResourceAsStream(normalizedPath);
		if (stream == null) {
			// Try without leading slash to support both Class and ClassLoader lookups
			stream = Texture.class.getResourceAsStream(normalizedPath.substring(1));
		}
		return stream;
	}

	private static ByteBuffer readToByteBuffer(InputStream stream) throws IOException {
		byte[] bytes = stream.readAllBytes();
		ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
		buffer.put(bytes).flip();
		return buffer;
	}

	private static ByteBuffer loadImage(String path, IntBuffer w, IntBuffer h, IntBuffer channels, int desiredChannels) {
		ByteBuffer image = STBImage.stbi_load(path, w, h, channels, desiredChannels);
		if (image != null) {
			return image;
		}

		ByteBuffer rawBuffer = null;
		try (InputStream stream = openResourceStream(path)) {
			if (stream != null) {
				rawBuffer = readToByteBuffer(stream);
				image = STBImage.stbi_load_from_memory(rawBuffer, w, h, channels, desiredChannels);
			} else {
				log.error("Texture resource not found: {}", path);
			}
		} catch (IOException e) {
			log.error("Failed to read texture resource {}: {}", path, e.getMessage());
		} finally {
			if (rawBuffer != null) {
				MemoryUtil.memFree(rawBuffer);
			}
		}

		if (image == null) {
			throw new RuntimeException("Failed to load texture file: " + path + "\n" + STBImage.stbi_failure_reason());
		}
		return image;
	}

	private static FloatBuffer loadHdrImage(String path, IntBuffer w, IntBuffer h, IntBuffer channels) {
		FloatBuffer image = STBImage.stbi_loadf(path, w, h, channels, 0);
		if (image != null) {
			return image;
		}

		ByteBuffer rawBuffer = null;
		try (InputStream stream = openResourceStream(path)) {
			if (stream != null) {
				rawBuffer = readToByteBuffer(stream);
				image = STBImage.stbi_loadf_from_memory(rawBuffer, w, h, channels, 0);
			} else {
				log.error("HDR texture resource not found: {}", path);
			}
		} catch (IOException e) {
			log.error("Failed to read HDR texture {}: {}", path, e.getMessage());
		} finally {
			if (rawBuffer != null) {
				MemoryUtil.memFree(rawBuffer);
			}
		}

		if (image == null) {
			throw new RuntimeException("Failed to load HDR image: " + path + "\n" + STBImage.stbi_failure_reason());
		}
		return image;
	}

	private static void bleedTransparentRgb(ByteBuffer image, int width, int height) {
		int pixelCount = width * height;
		byte[] source = new byte[pixelCount * 4];
		for (int i = 0; i < source.length; i++) {
			source[i] = image.get(i);
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int pixelIndex = (y * width + x) * 4;
				int alpha = Byte.toUnsignedInt(source[pixelIndex + 3]);
				if (alpha != 0) {
					continue;
				}

				int red = 0;
				int green = 0;
				int blue = 0;
				int contributors = 0;

				for (int oy = -1; oy <= 1; oy++) {
					for (int ox = -1; ox <= 1; ox++) {
						if (ox == 0 && oy == 0) {
							continue;
						}
						int nx = x + ox;
						int ny = y + oy;
						if (nx < 0 || nx >= width || ny < 0 || ny >= height) {
							continue;
						}

						int neighborIndex = (ny * width + nx) * 4;
						int neighborAlpha = Byte.toUnsignedInt(source[neighborIndex + 3]);
						if (neighborAlpha == 0) {
							continue;
						}

						red += Byte.toUnsignedInt(source[neighborIndex]);
						green += Byte.toUnsignedInt(source[neighborIndex + 1]);
						blue += Byte.toUnsignedInt(source[neighborIndex + 2]);
						contributors++;
					}
				}

				if (contributors > 0) {
					image.put(pixelIndex, (byte) (red / contributors));
					image.put(pixelIndex + 1, (byte) (green / contributors));
					image.put(pixelIndex + 2, (byte) (blue / contributors));
				}
			}
		}
	}

	private static void applyAnisotropicFilteringIfAvailable() {
		if (!GL.getCapabilities().GL_EXT_texture_filter_anisotropic) {
			return;
		}

		float maxAnisotropy = GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT);
		if (maxAnisotropy > 1.0f) {
			glTexParameterf(GL_TEXTURE_2D, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, maxAnisotropy);
		}
	}

	/**
	 * Defines the layout of faces within a single cubemap atlas texture.
	 */
	public enum AtlasLayout {
		/**
		 * A 6x1 horizontal strip. Face order: +X, -X, +Y, -Y, +Z, -Z
		 */
		HORIZONTAL_STRIP,
		/**
		 * A 4x3 horizontal cross. Face order follows common convention.
		 * |    | +Y |    |    |
		 * | -X | +Z | +X | -Z |
		 * |    | -Y |    |    |
		 */
		HORIZONTAL_CROSS
	}

	/**
	 * Creates a texture by loading an image from a file path.
	 * This constructor sets texture wrapping to CLAMP_TO_EDGE, which is ideal for decals.
	 * @param filePath The path to the texture file (e.g., "swing/src/main/resources/textures/decal.png").
	 */
	public Texture(String filePath) {
		this.textureId = glGenTextures();
		this.textureType = GL_TEXTURE_2D;
		glBindTexture(textureType, textureId);

		// Set texture parameters for decals (non-repeating)
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		applyAnisotropicFilteringIfAvailable();

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			// Load image data, forcing 4 channels (RGBA) for consistency
			ByteBuffer image = loadImage(filePath, w, h, channels, 4);

			int width = w.get();
			int height = h.get();
			bleedTransparentRgb(image, width, height);

			this.width = width;
			this.height = height;
			this.internalFormat = GL_SRGB_ALPHA;
			this.format = GL_RGBA;

			// Upload texture data to GPU, specifying SRGB_ALPHA format for automatic gamma correction
			glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
			glGenerateMipmap(GL_TEXTURE_2D);

			// Free the loaded image memory
			STBImage.stbi_image_free(image);
			trackCreation("file:" + filePath);
		} catch (Exception e) {
			abandonTexture();
			log.error("Exception while loading texture: {}", e.getMessage());
		}
	}


	/**
	 * Creates a procedural checkerboard texture for testing and default materials.
	 * @param width texture width in pixels
	 * @param height texture height in pixels
	 */
	public Texture(int width, int height) {
		this.textureId = GL11.glGenTextures();
		this.textureType = GL_TEXTURE_2D;
		GL11.glBindTexture(textureType, textureId);

		ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				boolean isWhite = ((x / 16) % 2 == 0) ^ ((y / 16) % 2 == 0);
				byte c = isWhite ? (byte) 255 : (byte) 80;
				buffer.put(c).put(c).put(c).put((byte) 255);
			}
		}
		buffer.flip();

		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		MemoryUtil.memFree(buffer);

		// Use mipmaps for better quality scaling
		glGenerateMipmap(GL11.GL_TEXTURE_2D);

		// Set texture parameters for repeating
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL_REPEAT);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL_LINEAR);

		this.width = width;
		this.height = height;
		this.internalFormat = GL11.GL_RGBA;
		this.format = GL11.GL_RGBA;
		trackCreation("procedural:" + width + "x" + height);
	}

	/**
	 * Creates a cubemap texture from an array of 6 file paths.
	 * The order of file paths should be: +X, -X, +Y, -Y, +Z, -Z.
	 *
	 * @param cubemapFiles An array of 6 file paths.
	 */
	public Texture(String[] cubemapFiles) {
		if (cubemapFiles.length != 6) {
			throw new IllegalArgumentException("Cubemap requires 6 texture files.");
		}
		this.textureId = glGenTextures();
		this.textureType = GL_TEXTURE_CUBE_MAP;
		glBindTexture(textureType, textureId);

		int faceWidth = 0;
		int faceHeight = 0;
		int detectedFormat = GL_RGB;
		int detectedInternalFormat = GL_SRGB;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			for (int i = 0; i < cubemapFiles.length; i++) {
				ByteBuffer image = loadImage(cubemapFiles[i], w, h, channels, 0);
				int format = (channels.get(0) == 4) ? GL_RGBA : GL_RGB;
				int internalFormat = (channels.get(0) == 4) ? GL_SRGB_ALPHA : GL_SRGB;
				faceWidth = w.get(0);
				faceHeight = h.get(0);
				detectedFormat = format;
				detectedInternalFormat = internalFormat;
				glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, w.get(0), h.get(0), 0, format, GL_UNSIGNED_BYTE, image);
				STBImage.stbi_image_free(image);
			}
		} catch (RuntimeException e) {
			abandonTexture();
			throw e;
		}

		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		this.width = faceWidth;
		this.height = faceHeight;
		this.internalFormat = detectedInternalFormat;
		this.format = detectedFormat;
		trackCreation("cubemap");
	}

	/**
	 * Creates a cubemap texture from a single atlas image file.
	 *
	 * @param atlasFilePath The path to the atlas image file.
	 * @param layout        The layout of the faces in the atlas.
	 */
	public Texture(String atlasFilePath, AtlasLayout layout) {
		this.textureId = glGenTextures();
		this.textureType = GL_TEXTURE_CUBE_MAP;
		glBindTexture(textureType, textureId);

		int faceSize = 0;
		int detectedFormat = GL_RGB;
		int detectedInternalFormat = GL_SRGB;
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			ByteBuffer atlasBuffer = loadImage(atlasFilePath, w, h, channels, 0);

			int atlasWidth = w.get(0);
			int atlasHeight = h.get(0);
			int numChannels = channels.get(0);
			int format = (numChannels == 4) ? GL_RGBA : GL_RGB;
			int internalFormat = (numChannels == 4) ? GL_SRGB_ALPHA : GL_SRGB;
			detectedFormat = format;
			detectedInternalFormat = internalFormat;

			if (layout == AtlasLayout.HORIZONTAL_STRIP) {
				if (atlasWidth != atlasHeight * 6) {
					throw new IllegalArgumentException("For HORIZONTAL_STRIP_6x1 layout, atlas width must be 6 times its height.");
				}
				faceSize = atlasHeight;
				int faceStrideBytes = faceSize * numChannels;
				int atlasStrideBytes = atlasWidth * numChannels;

				for (int i = 0; i < 6; i++) {
					ByteBuffer faceBuffer = MemoryUtil.memAlloc(faceSize * faceSize * numChannels);
					int startX = i * faceSize;

					for (int y = 0; y < faceSize; y++) {
						int srcPosition = y * atlasStrideBytes + startX * numChannels;
						// memSlice is safer as it uses the original buffer's address
						ByteBuffer rowSlice = MemoryUtil.memSlice(atlasBuffer, srcPosition, faceStrideBytes);
						faceBuffer.put(rowSlice);
					}
					faceBuffer.flip();

					glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, faceSize, faceSize, 0, format, GL_UNSIGNED_BYTE, faceBuffer);
					MemoryUtil.memFree(faceBuffer);
				}
			} else if (layout == AtlasLayout.HORIZONTAL_CROSS) {
				faceSize = atlasWidth / 4;
				if (atlasHeight != faceSize * 3) {
					throw new IllegalArgumentException("For HORIZONTAL_CROSS layout, atlas aspect ratio must be 4:3.");
				}
				int atlasStrideBytes = atlasWidth * numChannels;
				int faceStrideBytes = faceSize * numChannels;

				// Mapping from face index (0..5 for +X,-X,+Y,-Y,+Z,-Z) to grid coords (col, row)
				int[][] faceCoords = new int[][] {
						{2, 1}, // +X (Right)
						{0, 1}, // -X (Left)
						{1, 0}, // +Y (Top)
						{1, 2}, // -Y (Bottom)
						{1, 1}, // +Z (Front)
						{3, 1}  // -Z (Back)
				};

				for (int i = 0; i < 6; i++) {
					ByteBuffer faceBuffer = MemoryUtil.memAlloc(faceSize * faceSize * numChannels);

					int col = faceCoords[i][0];
					int row = faceCoords[i][1];
					int startX = col * faceSize;
					int startY = row * faceSize;

					for (int y = 0; y < faceSize; y++) {
						int srcPosition = (startY + y) * atlasStrideBytes + startX * numChannels;
						ByteBuffer rowSlice = MemoryUtil.memSlice(atlasBuffer, srcPosition, faceStrideBytes);
						faceBuffer.put(rowSlice);
					}
					faceBuffer.flip();

					glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, internalFormat, faceSize, faceSize, 0, format, GL_UNSIGNED_BYTE, faceBuffer);
					MemoryUtil.memFree(faceBuffer);
				}
			} else {
				throw new UnsupportedOperationException("Atlas layout not supported: " + layout);
			}

			STBImage.stbi_image_free(atlasBuffer);
		} catch (RuntimeException e) {
			abandonTexture();
			throw e;
		}

		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP, org.lwjgl.opengl.GL12.GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);
		this.width = faceSize;
		this.height = faceSize;
		this.internalFormat = detectedInternalFormat;
		this.format = detectedFormat;
		trackCreation("cubemap-atlas");
	}


	/**
	 * Loads an HDR equirectangular image as a floating-point texture.
	 *
	 * @param filePath Path to the .hdr file.
	 * @param isHDR    Must be true to load as HDR.
	 */
	public Texture(String filePath, boolean isHDR) {
		if (!isHDR) {
			throw new IllegalArgumentException("This constructor is for HDR images only.");
		}
		this.textureType = GL_TEXTURE_2D;
		this.textureId = glGenTextures();
		glBindTexture(this.textureType, textureId);

		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			FloatBuffer image = loadHdrImage(filePath, w, h, channels);

			// Upload the floating-point linear data to the GPU.
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGB32F, w.get(0), h.get(0), 0, GL_RGB, GL_FLOAT, image);
			this.width = w.get(0);
			this.height = h.get(0);
			this.internalFormat = GL_RGB32F;
			this.format = GL_RGB;
			STBImage.stbi_image_free(image);
		} catch (RuntimeException e) {
			abandonTexture();
			throw e;
		}

		// Set these parameters to prevent seams.
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		trackCreation("hdr:" + filePath);
	}

	/**
	 * Creates a texture from a ByteBuffer containing compressed image data (e.g., PNG, JPG).
	 *
	 * @param compressedImageData The ByteBuffer containing the file data of an image.
	 */
	public Texture(ByteBuffer compressedImageData) {
		this.textureType = GL_TEXTURE_2D;
		textureId = glGenTextures();
		glBindTexture(this.textureType, textureId);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR_MIPMAP_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		applyAnisotropicFilteringIfAvailable();

		STBImage.stbi_set_flip_vertically_on_load(true);
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer channels = stack.mallocInt(1);

			ByteBuffer image = STBImage.stbi_load_from_memory(compressedImageData, w, h, channels, 4);
			if (image == null) {
				throw new RuntimeException("Failed to load texture from memory: " + STBImage.stbi_failure_reason());
			}
			bleedTransparentRgb(image, w.get(0), h.get(0));

			glTexImage2D(GL_TEXTURE_2D, 0, GL_SRGB_ALPHA, w.get(), h.get(), 0, GL_RGBA, GL_UNSIGNED_BYTE, image);
			this.width = w.get(0);
			this.height = h.get(0);
			this.internalFormat = GL_SRGB_ALPHA;
			this.format = GL_RGBA;
			glGenerateMipmap(GL_TEXTURE_2D);
			STBImage.stbi_image_free(image);
		} catch (RuntimeException e) {
			abandonTexture();
			throw e;
		} finally {
			STBImage.stbi_set_flip_vertically_on_load(false);
		}
		trackCreation("memory");
	}

	/**
	 * Creates an empty, mutable texture for dynamic updates (e.g., for a UI canvas).
	 * @param width      The width of the texture.
	 * @param height     The height of the texture.
	 * @param isMutable  A flag to distinguish this from the procedural constructor.
	 */
	public Texture(int width, int height, boolean isMutable) {
		if (!isMutable) {
			throw new IllegalArgumentException("This constructor is for creating mutable textures.");
		}
		this.textureId = glGenTextures();
		this.textureType = GL_TEXTURE_2D;
		this.width = width;
		this.height = height;
		this.internalFormat = GL_SRGB_ALPHA;
		this.format = GL_RGBA;

		glBindTexture(textureType, textureId);

		// Use GL_RGBA8 instead of GL_SRGB_ALPHA if you don't need sRGB conversion
		// This can be faster on some GPUs
		glTexImage2D(GL_TEXTURE_2D, 0, internalFormat, width, height, 0, format, GL_UNSIGNED_BYTE, (ByteBuffer) null);

		// Use nearest filtering for UI textures if pixel-perfect rendering is needed
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
		trackCreation("mutable:" + width + "x" + height);
	}

	/**
	 * Gets the OpenGL texture ID.
	 * @return OpenGL texture object ID
	 */
	public int getId() {
		return textureId;
	}

	/**
	 * Binds this texture for rendering.
	 * @throws IllegalArgumentException if texture is invalid
	 */
	public void bind() {
		if (textureId == 0) {
			throw new IllegalArgumentException("Attempting to bind invalid texture (ID = 0)");
		}
		glBindTexture(this.textureType, textureId);
	}

	/**
	 * Updates a region of the texture without recreating it.
	 * More efficient than updating the entire texture.
	 */
	public void updateRegion(int x, int y, int width, int height, ByteBuffer data) {
		if (textureId == 0) {
			throw new IllegalStateException("Cannot update destroyed texture");
		}

		glBindTexture(textureType, textureId);
		glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, width, height, format, GL_UNSIGNED_BYTE, data);
	}

	/**
	 * Gets texture width in pixels.
	 * @return texture width
	 */
	public int getWidth() { return width; }

	/**
	 * Gets texture height in pixels.
	 * @return texture height
	 */
	public int getHeight() { return height; }

	/**
	 * Releases OpenGL resources used by this texture.
	 */
	public void cleanup() {
		if (textureId != 0) {
			trackRelease();
			TextureStateManager.evictDeletedTexture(textureId);
			glDeleteTextures(textureId);
			textureId = 0;
		}
	}

	// Static texture pool for frequently created/destroyed textures
	public static class TexturePool {
		private static final Map<String, Queue<Texture>> pool = new HashMap<>();
		private static final int MAX_POOL_SIZE = 10;

		public static Texture acquire(int width, int height) {
			String key = width + "x" + height;
			synchronized (pool) {
				Queue<Texture> queue = pool.get(key);

				if (queue != null && !queue.isEmpty()) {
					return queue.poll();
				}
			}

			return new Texture(width, height, true);
		}

		public static void release(Texture texture) {
			if (texture.width <= 0 || texture.height <= 0) return;

			String key = texture.width + "x" + texture.height;
			synchronized (pool) {
				Queue<Texture> queue = pool.computeIfAbsent(key, k -> new LinkedList<>());

				if (queue.size() < MAX_POOL_SIZE) {
					queue.offer(texture);
					return;
				}
			}
			texture.cleanup();
		}
	}
}
