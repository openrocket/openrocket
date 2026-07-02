package info.openrocket.swing.gui.figure3d.rendering;

import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;

/**
 * OpenGL shader program wrapper with performance optimizations.
 * 
 * This class handles the complete shader lifecycle including loading shader
 * source from resources, compilation, linking, and uniform management.
 * 
 * Key optimizations include:
 * - Uniform location caching to avoid repeated OpenGL queries
 * - Reusable matrix buffers to reduce memory allocations
 * - Pre-compilation error checking and detailed error reporting
 * - Batch uniform location caching for frequently used uniforms
 * 
 * Supports standard uniform types including matrices, vectors, and scalars
 * with both named access (using caching) and direct location access for
 * maximum performance in render loops.
 */
public class Shader implements ShaderProgram {

	private static final Logger log = LoggerFactory.getLogger(Shader.class);

	private final int programId;
	private final String vertexPath;
	private final String fragmentPath;

	// Cache uniform locations to avoid repeated lookups
	private final Map<String, Integer> uniformLocationCache = new HashMap<>();

	// Reusable buffers to avoid allocations every frame
	private final float[] matrix4Buffer = new float[16];
	private final float[] matrix3Buffer = new float[9];

	/**
	 * Creates a new shader program from vertex and fragment shader source files.
	 *
	 * Loads shader sources from the classpath, compiles them, and links them into
	 * a complete shader program ready for use.
	 *
	 * @param vertexPath Path to vertex shader source file in resources
	 * @param fragmentPath Path to fragment shader source file in resources
	 * @throws Exception If shader loading, compilation, or linking fails
	 */
	public Shader(String vertexPath, String fragmentPath) throws Exception {
		this.vertexPath = vertexPath;
		this.fragmentPath = fragmentPath;

		log.debug("Loading shader: vertex={}, fragment={}", vertexPath, fragmentPath);

		String vertexShaderSource = loadResource(vertexPath);
		String fragmentShaderSource = loadResource(fragmentPath);

		int vertexShader = compileShader(GL_VERTEX_SHADER, vertexShaderSource, vertexPath);
		int fragmentShader = compileShader(GL_FRAGMENT_SHADER, fragmentShaderSource, fragmentPath);

		programId = glCreateProgram();
		if (programId == 0) {
			glDeleteShader(vertexShader);
			glDeleteShader(fragmentShader);
			throw new RuntimeException("Failed to create shader program for: " + vertexPath + ", " + fragmentPath);
		}

		glAttachShader(programId, vertexShader);
		glAttachShader(programId, fragmentShader);
		glLinkProgram(programId);

		if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
			String linkError = glGetProgramInfoLog(programId, 1024);
			glDeleteShader(vertexShader);
			glDeleteShader(fragmentShader);
			glDeleteProgram(programId);
			log.error("Shader link error for {}, {}: {}", vertexPath, fragmentPath, linkError);
			throw new RuntimeException("Error linking shader program (" + vertexPath + ", " + fragmentPath + "): " + linkError);
		}

		glDeleteShader(vertexShader);
		glDeleteShader(fragmentShader);

		GLErrors.check("shader program creation (" + vertexPath + ", " + fragmentPath + ")");

		GpuResourceTracker.register(GpuResourceTracker.ResourceType.PROGRAM, programId, vertexPath + " | " + fragmentPath);
		log.debug("Shader program created successfully: id={}", programId);
	}

	/**
	 * Activates this shader program for subsequent rendering operations.
	 */
	@Override
	public void use() {
		glUseProgram(programId);
	}

	/**
	 * Deactivates the current shader program.
	 */
	@Override
	public void unbind() {
		glUseProgram(0);
	}

	/**
	 * Gets the OpenGL program ID for this shader.
	 * 
	 * @return The OpenGL program ID
	 */
	@Override
	public int getProgramId() {
		return programId;
	}

	/**
	 * Gets a uniform location with caching to avoid repeated GL calls.
	 * @param name The uniform name
	 * @return The uniform location, or -1 if not found
	 */
	@Override
	public int getUniformLocation(String name) {
		return uniformLocationCache.computeIfAbsent(name, n -> glGetUniformLocation(programId, n));
	}

	/**
	 * Sets a Matrix4f uniform by name (uses caching).
	 * @param name   The name of the uniform in the shader.
	 * @param matrix The Matrix4f to set.
	 */
	@Override
	public void setUniform(String name, Matrix4f matrix) {
		int location = getUniformLocation(name);
		if (location >= 0) {
			matrix.get(matrix4Buffer);
			glUniformMatrix4fv(location, false, matrix4Buffer);
		}
	}

	/**
	 * Sets a Matrix4f uniform using a pre-cached location (fastest).
	 * @param location The cached uniform location
	 * @param matrix The matrix to set
	 */
	@Override
	public void setUniform(int location, Matrix4f matrix) {
		if (location >= 0) {
			matrix.get(matrix4Buffer);
			glUniformMatrix4fv(location, false, matrix4Buffer);
		}
	}

	/**
	 * Sets a Matrix3f uniform by name (uses caching).
	 * @param name   The name of the uniform in the shader.
	 * @param matrix The Matrix3f to set.
	 */
	@Override
	public void setUniform(String name, Matrix3f matrix) {
		int location = getUniformLocation(name);
		if (location >= 0) {
			matrix.get(matrix3Buffer);
			glUniformMatrix3fv(location, false, matrix3Buffer);
		}
	}

	/**
	 * Sets a Matrix3f uniform using a pre-cached location (fastest).
	 * @param location The cached uniform location
	 * @param matrix The matrix to set
	 */
	@Override
	public void setUniform(int location, Matrix3f matrix) {
		if (location >= 0) {
			matrix.get(matrix3Buffer);
			glUniformMatrix3fv(location, false, matrix3Buffer);
		}
	}

	/**
	 * Sets a Vector4f uniform by name.
	 * @param name The name of the uniform.
	 * @param vector The vector to set.
	 */
	@Override
	public void setUniform(String name, Vector4f vector) {
		int location = getUniformLocation(name);
		if (location >= 0) {
			GL20.glUniform4f(location, vector.x, vector.y, vector.z, vector.w);
		}
	}

	/**
	 * Sets a Vector3f uniform by name.
	 * @param name The name of the uniform.
	 * @param vector The vector to set.
	 */
	@Override
	public void setUniform(String name, Vector3f vector) {
		int location = getUniformLocation(name);
		if (location >= 0) {
			GL20.glUniform3f(location, vector.x, vector.y, vector.z);
		}
	}

	/**
	 * Sets a float uniform by name.
	 * @param name The name of the uniform.
	 * @param value The float value to set.
	 */
	@Override
	public void setUniform(String name, float value) {
		int location = getUniformLocation(name);
		if (location >= 0) {
			GL20.glUniform1f(location, value);
		}
	}

	/**
	 * Pre-caches all uniform locations from a list of names.
	 * Call this after shader creation for frequently used uniforms.
	 * @param uniformNames Array of uniform names to cache
	 */
	@Override
	public void cacheUniformLocations(String... uniformNames) {
		for (String name : uniformNames) {
			getUniformLocation(name); // This will cache the location
		}
	}

	/**
	 * Releases all OpenGL resources associated with this shader.
	 * 
	 * This method should be called when the shader is no longer needed
	 * to prevent memory leaks.
	 */
	@Override
	public void cleanup() {
		if (programId != 0) {
			GpuResourceTracker.release(GpuResourceTracker.ResourceType.PROGRAM, programId);
			glDeleteProgram(programId);
		}
		uniformLocationCache.clear();
	}

	/**
	 * Compiles a shader from source code.
	 *
	 * @param type The shader type (GL_VERTEX_SHADER or GL_FRAGMENT_SHADER)
	 * @param source The shader source code
	 * @param path The path to the shader file (for error reporting)
	 * @return The compiled shader ID
	 * @throws RuntimeException If compilation fails
	 */
	private static int compileShader(int type, String source, String path) {
		String shaderTypeName = (type == GL_VERTEX_SHADER) ? "vertex" : "fragment";
		int shaderId = glCreateShader(type);
		if (shaderId == 0) {
			log.error("Failed to create {} shader for: {}", shaderTypeName, path);
			throw new RuntimeException("Error creating " + shaderTypeName + " shader for: " + path);
		}

		glShaderSource(shaderId, source);
		glCompileShader(shaderId);

		if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
			String error = glGetShaderInfoLog(shaderId, 2048);
			glDeleteShader(shaderId); // Clean up on error
			log.error("Compilation error in {} shader '{}': {}", shaderTypeName, path, error);
			throw new RuntimeException("Error compiling " + shaderTypeName + " shader '" + path + "':\n" + error);
		}

		log.debug("Compiled {} shader: {}", shaderTypeName, path);
		return shaderId;
	}

	/**
	 * Loads a text resource from the classpath.
	 *
	 * @param fileName Path to the resource file
	 * @return The complete file contents as a string
	 * @throws Exception If the file cannot be read
	 */
	private static String loadResource(String fileName) throws Exception {
		InputStream in = Shader.class.getResourceAsStream(fileName);
		if (in == null) {
			log.error("Shader resource not found: {}", fileName);
			throw new RuntimeException("Shader resource not found: " + fileName);
		}
		try (Scanner scanner = new Scanner(in, StandardCharsets.UTF_8)) {
			return scanner.useDelimiter("\\A").next();
		} catch (Exception e) {
			log.error("Failed to read shader resource '{}': {}", fileName, e.getMessage());
			throw new RuntimeException("Failed to read shader resource '" + fileName + "'", e);
		}
	}
}
